package elit.express.exchangerates;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class NetworkService {
    private static final String BASE_URL = "https://api.exchangeratesapi.io/";
    private static NetworkService mInstance;
    private Retrofit mRetrofit;

    private NetworkService() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    static NetworkService getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkService();
        }
        return mInstance;
    }

    JSONPlaceHolderApi getJSONApi() {
        return mRetrofit.create(JSONPlaceHolderApi.class);
    }
}
