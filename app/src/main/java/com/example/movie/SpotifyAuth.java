package com.example.movie;

import android.util.Base64;

import androidx.annotation.NonNull;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import java.io.IOException;

public class SpotifyAuth {

    // ⚠️ In a production app you'd hide these server-side.
    private static final String CLIENT_ID = "4a12592861bf4384a0ab616126d84be3";
    private static final String CLIENT_SECRET = "2ee0fb89df4b42dab6e5a21f234a52b0";

    private interface AuthService {
        @FormUrlEncoded
        @POST("api/token")
        Call<SpotifyTokenResponse> getAccessToken(@Field("grant_type") String grantType);
    }

    public interface TokenCallback {
        void onTokenReceived(String token);
        void onError(Throwable t);
    }

    public static void fetchAccessToken(TokenCallback callback) {
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        final String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @NonNull
                    @Override
                    public Response intercept(@NonNull Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .header("Authorization", basic)
                                .header("Content-Type", "application/x-www-form-urlencoded");
                        return chain.proceed(builder.build());
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://accounts.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        AuthService service = retrofit.create(AuthService.class);
        Call<SpotifyTokenResponse> call = service.getAccessToken("client_credentials");
        call.enqueue(new retrofit2.Callback<SpotifyTokenResponse>() {
            @Override
            public void onResponse(Call<SpotifyTokenResponse> call, retrofit2.Response<SpotifyTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onTokenReceived(response.body().getAccess_token());
                } else {
                    callback.onError(new Exception("Spotify token error: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<SpotifyTokenResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
