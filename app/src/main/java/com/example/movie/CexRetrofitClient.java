package com.example.movie;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CexRetrofitClient {

    private static CexRetrofitClient instance;
    private CexApi api;

    private CexRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://uk.webuy.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(CexApi.class);
    }

    // Singleton instance
    public static CexRetrofitClient getInstance() {
        if (instance == null) {
            instance = new CexRetrofitClient();
        }
        return instance;
    }

    // **Expose the API so MovieDetailActivity can call it**
    public CexApi getApi() {
        return api;
    }
}
