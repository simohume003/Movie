package com.example.movie;

import java.util.List;
import java.util.Map;

public class WatchProviderResponse {

    private Map<String, CountryProviders> results;

    public Map<String, CountryProviders> getResults() { return results; }

    public static class CountryProviders {
        private List<Provider> flatrate;

        public List<Provider> getFlatrate() { return flatrate; }
    }

    public static class Provider {
        private String provider_name;

        public String getProviderName() { return provider_name; }
    }
}
