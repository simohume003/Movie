package com.example.movie;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class WatchProviderResponse {

    @SerializedName("results")
    private Map<String, CountryProviders> results;

    public Map<String, CountryProviders> getResults() {
        return results;
    }

    public static class CountryProviders {
        @SerializedName("flatrate")
        private Provider[] flatrate;

        public Provider[] getFlatrate() {
            return flatrate;
        }
    }

    public static class Provider {
        @SerializedName("provider_name")
        private String providerName;

        public String getProviderName() {
            return providerName;
        }
    }
}
