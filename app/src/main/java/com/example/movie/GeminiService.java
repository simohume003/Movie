package com.example.movie;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiService {

    private static final String TAG = "GEMINI";
    private static final String API_KEY = "AIzaSyCN9S_QntZHhbUphi17MTxBc5S7xXdPofw";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";

    public interface GeminiCallback {
        void onSuccess(String result);
        void onError(Exception e);
    }

    public static void getMovieFromSong(String song, GeminiCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-goog-api-key", API_KEY);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();

                JSONObject textPart = new JSONObject();
                textPart.put(
                        "text",
                        "Recommend ONE movie based on genre,year or tempo for the song: \"" + song +
                                "\". Return only:\nMovie: <title>\nReason: <short reason>"
                );

                parts.put(textPart);
                content.put("parts", parts);
                contents.put(content);
                body.put("contents", contents);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(response.toString());
                String text =
                        json.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                callback.onSuccess(text);

            } catch (Exception e) {
                Log.e(TAG, "Failed", e);
                callback.onError(e);
            }
        }).start();
    }
}
