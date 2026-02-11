package com.example.movie;

public class TmdbMapper {

    public static String genreToId(String genre) {
        if (genre == null) return null;

        switch (genre.toLowerCase()) {
            case "horror": return "27";
            case "action": return "28";
            case "comedy": return "35";
            case "drama": return "18";
            case "thriller": return "53";
            case "romance": return "10749";
            case "animation": return "16";
            case "sci-fi":
            case "science fiction": return "878";
            default: return null;
        }
    }

    public static String serviceToProvider(String service) {
        if (service == null) return null;

        switch (service.toLowerCase()) {
            case "netflix": return "8";
            case "prime video":
            case "amazon prime":
            case "amazon prime video": return "119";
            case "disney":
            case "disney+": return "337";
            case "apple":
            case "apple tv": return "350";
            case "now tv": return "39";
            default: return null;
        }
    }
}

