package elit.express.exchangerates;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_LONG;

public class RatesActivity extends AppCompatActivity {

    private ListView ratesListView;

    private long timestamp=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        ratesListView = findViewById(R.id.ratesListView);
        ratesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showLatestRates();
            }
        });

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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.rates, ratesArray);

        ratesListView.setAdapter(adapter);
    }

    private void showLatestRates() {
        dbReadTime(new MyCallback() {
            @Override
            public void onCallback(long timestamp) {

                RatesActivity.this.timestamp=timestamp;

                if(System.currentTimeMillis()-timestamp>=600000)
                    getNewRates();
                else
                    dbRead();
            }
        });
    }

    void dbWrite(LatestRates latestRates){
        FirebaseDatabase.getInstance().getReference("mydb").setValue(latestRates);
    }

    void dbReadTime(final MyCallback callback){
        FirebaseDatabase.getInstance().getReference("mydb").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatestRates latestRates=dataSnapshot.getValue(LatestRates.class);
                callback.onCallback(latestRates.getTimestamp());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), String.valueOf(timestamp), LENGTH_LONG).show();
            }
        });
    }

    void dbRead(){
        FirebaseDatabase.getInstance().getReference("mydb").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatestRates latestRates=dataSnapshot.getValue(LatestRates.class);
                fillRatesList(latestRates);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), String.valueOf(timestamp), LENGTH_LONG).show();
            }
        });
    }

    void getNewRates(){
        NetworkService.getInstance()
                .getJSONApi()
                .getPostWithID("USD")
                .enqueue(new Callback<LatestRates>() {
                    @Override
                    public void onResponse(@NonNull Call<LatestRates> call, @NonNull Response<LatestRates> response) {
                        fillRatesList(response.body());

                        response.body().setTimestamp(System.currentTimeMillis());
                        dbWrite(response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<LatestRates> call, @NonNull Throwable t) {

                        Toast.makeText(getApplicationContext(), "error", LENGTH_LONG).show();
                        t.printStackTrace();
                    }
                });
    }

}
