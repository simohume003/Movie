package com.example.movie;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CexApi {
    // Search products by title
    @GET("json/products/search")
    Call<CexResponse> searchProduct(@Query("q") String query);
}
