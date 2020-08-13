package elit.express.exchangerates;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_LONG;

public class RatesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        showLatestRates();

    }

    @Override
    public void onBackPressed() {
        finish();
        //Toast.makeText(this, "fersdc", LENGTH_LONG).show();
    }

    void fillRatesList(LatestRates latestRates) {
        ArrayList<String> ratesArray = new ArrayList<>();
        for (Map.Entry<String, String> pair : latestRates.getRates().entrySet()) {
            ratesArray.add(pair.getKey() + " \n " + pair.getValue());
        }

//        Toast.makeText(getApplicationContext(), latestRates.getRates().keySet().toString(), LENGTH_LONG).show();
//        for (int i = 0; i < ratesArray.size(); i++) {
//            ratesArray.add(i+"\n"+ i+"."+i);
//        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.rates, ratesArray);

        ListView ratesListView = findViewById(R.id.ratesListView);
        ratesListView.setAdapter(adapter);
        ratesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showLatestRates();
            }
        });
    }


    private void showLatestRates() {
        NetworkService.getInstance()
                .getJSONApi()
                .getPostWithID("USD")
                .enqueue(new Callback<LatestRates>() {
                    @Override
                    public void onResponse(@NonNull Call<LatestRates> call, @NonNull Response<LatestRates> response) {
                        fillRatesList(response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<LatestRates> call, @NonNull Throwable t) {

                        Toast.makeText(getApplicationContext(), "error", LENGTH_LONG).show();
                        //textView.append("Error occurred while getting request!");
                        t.printStackTrace();
                    }
                });
    }
}
