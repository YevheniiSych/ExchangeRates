package elit.express.exchangerates;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Rates7DaysI {
    @GET("/history")
    Call<Rates7Days> getPost(
            @Query("start_at") String start_at,
            @Query("end_at") String end_at,
            @Query("base") String base,
            @Query("symbols") String symbols
    );
}

