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

            public int getId() {
                return id;
            }
        }
    }

    public static class PersonCreditsResponse {

        @SerializedName("crew")
        private List<Crew> crew;
        @SerializedName("cast")
        private List<PersonCreditsResponse.Cast> cast;

        public List<PersonCreditsResponse.Cast> getCast() {
            return cast;
        }

        public List<Crew> getCrew() {
            return crew;
        }

        public static class Crew {

            private String job;
            private String title;

            @SerializedName("poster_path")
            private String posterPath;
            private String release_date;
            private int id;

            public int getId() {
                return id;
            }

            public String getJob() {
                return job;
            }

            public String getTitle() {
                return title;
            }

            private String name;

            public String getName() {
                return name;
            }

            public String getPosterPath() {
                return posterPath;
            }
            public String getReleaseDate() {return release_date;}
        }

        public static class Cast {

            private int id;
            private String name;
            private String title;

            @SerializedName("profile_path")
            private String profilePath;

            @SerializedName("poster_path")
            private String posterPath;

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public String getProfilePath() {
                return profilePath;
            }

            public String getTitle() {
                return title;

            }
            public String getPosterPath() {
                return posterPath;
            }
        }
    }
}