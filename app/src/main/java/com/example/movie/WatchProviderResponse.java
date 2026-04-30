package com.example.movie;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class WatchProviderResponse {

    private Map<String, CountryProviders> results;

    public Map<String, CountryProviders> getResults() { return results; }

    public static class CountryProviders {
        private List<Provider> flatrate;

        public List<Provider> getFlatrate() { return flatrate; }
        private List<Provider> rent;
        public List<Provider> getRent() {
            return rent;
        }
    }

    public static class Provider {
        private String provider_name;

        public String getProviderName() { return provider_name; }
        @SerializedName("logo_path")
        private String logoPath;

        public String getLogoPath() {
            return logoPath;
        }
    }


}
