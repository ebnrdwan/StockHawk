package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailStock extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private int position = StockAdapter.theadapterPosition;
    LineChart lineChart;
    TextView t;
    List<Float> objects = Collections.EMPTY_LIST;
    Uri mUri;
    final int ID_DETAIL_LOADER=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_stock);


        lineChart = (LineChart) findViewById(R.id.mylinechart);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra("SYMBOL_CODE");
        mUri = intent.getData();
//        Calendar from = Calendar.getInstance();
//        Calendar to = Calendar.getInstance();
//        from.add(Calendar.YEAR, -5); // from 5 years ago
//        try {
//
//
//            Set<String> stockPref = PrefUtils.getStocks(this);
//            Set<String> stockCopy = new HashSet<>();
//            stockCopy.addAll(stockPref);
//            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);
//            Map<String, Stock> quotes = YahooFinance.get(stockArray);
//            Stock stockObject = quotes.get(symbol);
//            List<HistoricalQuote> history = stockObject.getHistory(from, to, Interval.WEEKLY);
//
//            objects = new ArrayList<>();
//
//            for (int i = 0; i < history.size(); i++) {
//                objects.add(Float.valueOf(String.valueOf(history.get(i))));
//            }
//
//
//             t = (TextView) findViewById(R.id.textView);
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
        List<Float> mylistobjects = new ArrayList<>();
        mylistobjects.add(3.3f);
        mylistobjects.add(4.3f);
        mylistobjects.add(5.3f);
        mylistobjects.add(3.3f);
        mylistobjects.add(7.3f);
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < mylistobjects.size(); i++) {
            Float object = mylistobjects.get(i);
            entries.add(new Entry(i, object));

        }
        LineDataSet dataset = new LineDataSet(entries, this.getString(R.string.mydataSetLabe));
        LineData lineData = new LineData(dataset);
        lineChart.setData(lineData);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                mUri,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }


//        String history = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
//        t.setText(history);

//        objects = new ArrayList<>();
//
//        for (int i = 0; i < history.size(); i++) {
//            objects.add(Float.valueOf(String.valueOf(history.get(i))));
//        }






    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
