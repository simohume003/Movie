package com.example.movie;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CexResponse {

    @SerializedName("Products")
    private List<CexProduct> products;

    public List<CexProduct> getProducts() {
        return products;
    }

    public static class CexProduct {
        @SerializedName("Title")
        private String title;

        @SerializedName("Price")
        private String price;

        public String getTitle() { return title; }
        public String getPrice() { return price; }
    }
}
