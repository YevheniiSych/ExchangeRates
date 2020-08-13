package elit.express.exchangerates;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JSONPlaceHolderApi {
    @GET("/latest")
    public Call<LatestRates> getPostWithID(@Query("base") String base);
}
