package elit.express.exchangerates;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_LONG;

public class RatesActivity extends AppCompatActivity {

    private ListView ratesListView;

    private long timestamp = 0;

    private Map<String, String> dateRate; //map with dates and rates for last 7 days

    private ArrayList<String> sortedDates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        ratesListView = findViewById(R.id.ratesListView);
        ratesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                get7DaysRates(view);
            }
        });

        showLatestRates();
    }


    @Override
    public void onBackPressed() {
        View graph = findViewById(R.id.graph);
        if (graph.getVisibility() == View.VISIBLE)
            graph.setVisibility(View.GONE);
        else
            finish();
    }

    @SuppressLint("DefaultLocale")
    private void fillRatesList(LatestRates latestRates) {
        ArrayList<String> ratesArray = new ArrayList<>();

        for (Map.Entry<String, String> pair : latestRates.getRates().entrySet()) {
            ratesArray.add(pair.getKey() + " \n " +
                    String.format("%.2f", Double.valueOf(pair.getValue())));//make exchange rates with two decimal precision
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.rates, ratesArray);

        ratesListView.setAdapter(adapter);
    }

    private void showLatestRates() {
        final long tenMinutesInMillis=600000;
        dbReadTime(new MyCallback() {
            @Override
            public void onCallback(long timestamp) {

                RatesActivity.this.timestamp = timestamp;

                if (System.currentTimeMillis() - timestamp >= tenMinutesInMillis)
                    getNewRates();// load new data from web service
                else
                    dbRead();// load saved data from Firebase
            }
        });
    }

    private void dbWrite(LatestRates latestRates) {
        FirebaseDatabase.getInstance().getReference("mydb").setValue(latestRates);
    }

    private void dbReadTime(final MyCallback callback) {
        FirebaseDatabase.getInstance().getReference("mydb").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LatestRates latestRates = dataSnapshot.getValue(LatestRates.class);
                assert latestRates != null;
                callback.onCallback(latestRates.getTimestamp());//callback needs to recive timestamp
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), R.string.dbError, LENGTH_LONG).show();
            }
        });
    }

    private void dbRead() {
        FirebaseDatabase.getInstance().getReference("mydb").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LatestRates latestRates = dataSnapshot.getValue(LatestRates.class);
                assert latestRates != null;
                fillRatesList(latestRates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), R.string.dbError, LENGTH_LONG).show();
            }
        });
    }

    private void getNewRates() {
        NetworkService.getInstance()
                .getJSONApi()
                .getPostWithID("USD")
                .enqueue(new Callback<LatestRates>() {
                    @Override
                    public void onResponse(@NonNull Call<LatestRates> call, @NonNull Response<LatestRates> response) {
                        assert response.body() != null;
                        fillRatesList(response.body());

                        response.body().setTimestamp(System.currentTimeMillis());// add timestamp to save in db
                        dbWrite(response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<LatestRates> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), R.string.webError, LENGTH_LONG).show();
                        t.printStackTrace();
                    }
                });
    }

    private void get7DaysRates(View view) {

        String[] params = params7DaysRates((TextView) view);//formatted parameters
        String start_at = params[0];
        String end_at = params[1];
        String base = "USD";
        String symbols = params[2];

        NetworkService.getInstance()
                .getJSONApi()
                .getPost(start_at, end_at, base, symbols)
                .enqueue(new Callback<Rates7Days>() {
                    @Override
                    public void onResponse(@NonNull Call<Rates7Days> call, @NonNull Response<Rates7Days> response) {
                        if (response.body() != null)
                            selectDatesAndRates(response.body());
                        else
                            Toast.makeText(getApplicationContext(), R.string.noRates, LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Rates7Days> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    @SuppressLint("SimpleDateFormat")
    String[] params7DaysRates(TextView view) {
        String start_at, end_at, symbols;

        start_at = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(System.currentTimeMillis() - 604800000);//calculate and format date of week ago

        end_at = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(System.currentTimeMillis());//get and format current date

        symbols = view.getText().toString().split(" ")[0];//parse choosed rate

        return new String[]{start_at, end_at, symbols};
    }

    private void selectDatesAndRates(Rates7Days rates7Days) {
        dateRate = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> pair : rates7Days.getRates().entrySet()) {
            for (Map.Entry<String, String> pair2 : pair.getValue().entrySet()) {
                dateRate.put(pair.getKey(), pair2.getValue());
            }
        }

        sortedDates = sortDate();

        graph();

    }

    void graph() {

        DataPoint[] dates = makeDataPoints();

        GraphView graph = findViewById(R.id.graph);

        graph.removeAllSeries();

        // you can directly pass Date objects to DataPoint-Constructor
        // this will convert the Date to double via Date#getTime()
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dates);

        graph.addSeries(series);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplication()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);


        graphSetVisible();
    }

    DataPoint[] makeDataPoints() {
        DataPoint[] dates = new DataPoint[sortedDates.size()];
        for (int i = 0; i < sortedDates.size(); i++) {

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            Date date = null;
            try {
                date = formatter.parse(sortedDates.get(i));//parse Strings to Dates
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String rate = String.format(Locale.UK, "%.2f",
                    Double.valueOf(Objects.requireNonNull(dateRate.get(sortedDates.get(i)))));

            assert date != null;
            dates[i] = new DataPoint(
                    date,
                    Float.parseFloat(rate)
            );
        }
        return dates;
    }

    void graphSetVisible() {
        View graph = findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);
    }

    ArrayList<String> sortDate() {
        ArrayList<String> sortByDate = new ArrayList<>(dateRate.keySet());
        Collections.sort(sortByDate);
        return sortByDate;
    }

}
