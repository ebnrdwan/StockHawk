package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;
    public static  String StringofBuilder;


    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

             ArrayList<ContentValues> quoteCVs = new ArrayList<>();


            while (iterator.hasNext()) {
                String symbol = iterator.next();


                try {

                    Stock stock = quotes.get(symbol);
                    StockQuote  quote=null;
                    if (stock.equals(null))
                     stock.getQuote();

                    Float price = null;
                    Float change = null;
                    Float percentChange = null;
                    try {
                        price = quote.getPrice().floatValue();
                        change = quote.getChange().floatValue();
                        percentChange = quote.getChangeInPercent().floatValue();
                    } catch (Exception e) {
                        Toast.makeText(context,"you entered invalid Stock",Toast.LENGTH_SHORT).show();
                    }


                       // WARNING! Don't request historical data for a stock that doesn't exist!
                        // The request will hang forever X_x
                        List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);
                    Log.d("LOLOLOLO",history.toString());

                        StringBuilder historyBuilder = new StringBuilder();

                        for (HistoricalQuote it : history) {
                            historyBuilder.append(it.getDate().getTimeInMillis());
                            historyBuilder.append(", ");
                            historyBuilder.append(it.getClose());
                            historyBuilder.append("\n");
                        }

                        if (price != null && !symbol.equals(null) && percentChange != null && change != null && !historyBuilder.toString().equals(null)) {
                            ContentValues quoteCV = new ContentValues();
                            quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                            quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                            quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                            quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                            quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                            quoteCVs.add(quoteCV);
                        } else {
                            Toast.makeText(context, "notnotnot", Toast.LENGTH_SHORT).show();
                        }



                } catch (Exception e) {
                    throw new EmptyStackException();
                }


//                boolean isItAvailable = false;
//                for (int i = 0 ; i <stockArray.length;i++){
//                    String stockobject = stockArray[i];
//
////                    float price =0;
////                    float change =0;
////                    float percentChange =0;
//                    if (symbol.equals(stockobject)){
//                        isItAvailable=true;
//                        break;
//                    }
//
//
//                }


            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
