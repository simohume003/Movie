package com.example.movie;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PersonApiResponse {
    public static class PersonSearchResponse {

        @SerializedName("results")
        private List<Person> results;

        public List<Person> getResults() {
            return results;
        }

        public static class Person {
            private int id;
            public int getId() { return id; }
        }
    }

    public static class PersonCreditsResponse {

        @SerializedName("crew")
        private List<Crew> crew;

        public List<Crew> getCrew() {
            return crew;
        }

        public static class Crew {

            private String job;
            private String title;

            @SerializedName("poster_path")
            private String posterPath;

            private int id;

            public int getId() {
                return id;
            }

            public String getJob() { return job; }
            public String getTitle() { return title; }
            public String getPosterPath() { return posterPath; }
        }
    }
}