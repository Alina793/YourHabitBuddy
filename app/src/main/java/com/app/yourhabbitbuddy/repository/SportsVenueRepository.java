package com.app.yourhabbitbuddy.repository;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.app.yourhabbitbuddy.data.SportsVenue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SportsVenueRepository {

    private static final String OVERPASS_API = "https://overpass-api.de/api/interpreter";
    private static final String USER_AGENT = "YourHabitBuddy/1.0";

    public interface OnVenuesLoadedListener {
        void onSuccess(List<SportsVenue> venues);

        void onError(String error);
    }

    public static void findNearbyVenues(double lat, double lon, double radius, OnVenuesLoadedListener listener) {
        new FetchVenuesTask(listener).execute(lat, lon, radius);
    }

    private static class FetchVenuesTask extends AsyncTask<Double, Void, List<SportsVenue>> {
        private OnVenuesLoadedListener listener;
        private String errorMessage;

        FetchVenuesTask(OnVenuesLoadedListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<SportsVenue> doInBackground(Double... params) {
            double lat = params[0];
            double lon = params[1];
            double radius = params[2];

            List<SportsVenue> venues = new ArrayList<>();

            String query = String.format(
                    "[out:json];(" +
                            "  node[\"leisure\"=\"sports_centre\"](around:%d,%f,%f);" +
                            "  node[\"leisure\"=\"pitch\"](around:%d,%f,%f);" +
                            "  node[\"sport\"](around:%d,%f,%f);" +
                            "  node[\"leisure\"=\"fitness_centre\"](around:%d,%f,%f);" +
                            "  node[\"leisure\"=\"swimming_pool\"](around:%d,%f,%f);" +
                            "  node[\"leisure\"=\"stadium\"](around:%d,%f,%f);" +
                            "  way[\"leisure\"=\"sports_centre\"](around:%d,%f,%f);" +
                            "  way[\"leisure\"=\"pitch\"](around:%d,%f,%f);" +
                            "  relation[\"leisure\"=\"sports_centre\"](around:%d,%f,%f);" +
                            ");out center;",
                    radius, lat, lon, radius, lat, lon, radius, lat, lon,
                    radius, lat, lon, radius, lat, lon, radius, lat, lon,
                    radius, lat, lon, radius, lat, lon
            );

            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                URL url = new URL(OVERPASS_API + "?data=" + encoded);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray elements = json.getJSONArray("elements");

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject el = elements.getJSONObject(i);
                    SportsVenue venue = parseVenue(el);
                    if (venue != null) {
                        calculateDistance(venue, lat, lon);
                        venues.add(venue);
                    }
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e("SportsVenue", "Error: " + e.getMessage());
            }

            return venues;
        }

        private SportsVenue parseVenue(JSONObject el) throws Exception {
            SportsVenue venue = new SportsVenue();
            venue.setId(el.optLong("id"));
            venue.setType(el.optString("type"));

            if (el.has("lat") && el.has("lon")) {
                venue.setLatitude(el.getDouble("lat"));
                venue.setLongitude(el.getDouble("lon"));
            } else if (el.has("center")) {
                JSONObject center = el.getJSONObject("center");
                venue.setLatitude(center.getDouble("lat"));
                venue.setLongitude(center.getDouble("lon"));
            } else {
                return null;
            }

            JSONObject tags = el.optJSONObject("tags");
            if (tags != null) {
                String name = tags.optString("name", "");
                if (name.isEmpty()) {
                    String sport = tags.optString("sport", "");
                    String leisure = tags.optString("leisure", "");
                    name = getDefaultName(sport, leisure);
                }
                venue.setName(name);
                venue.setSportType(tags.optString("sport", "general"));
                venue.setAddress(tags.optString("addr:street", ""));
                venue.setOpeningHours(tags.optString("opening_hours", ""));
                venue.setFree(tags.has("fee") && tags.optString("fee").equals("no"));
                venue.setPhone(tags.optString("phone", ""));
                venue.setWebsite(tags.optString("website", ""));
            }

            return venue;
        }

        private String getDefaultName(String sport, String leisure) {
            if (!sport.isEmpty()) return translateSport(sport);
            if (!leisure.isEmpty()) return translateLeisure(leisure);
            return "Спортивний майданчик";
        }

        private void calculateDistance(SportsVenue venue, double lat, double lon) {
            float[] results = new float[1];
            Location.distanceBetween(lat, lon, venue.getLatitude(), venue.getLongitude(), results);
            venue.setDistance(results[0]);
        }

        @Override
        protected void onPostExecute(List<SportsVenue> venues) {
            if (listener != null) {
                if (errorMessage != null) {
                    listener.onError(errorMessage);
                } else {
                    listener.onSuccess(venues);
                }
            }
        }
    }

    private static String translateSport(String sport) {
        switch (sport.toLowerCase()) {
            case "soccer":
                return "⚽ Футбол";
            case "basketball":
                return "🏀 Баскетбол";
            case "tennis":
                return "🎾 Теніс";
            case "swimming":
                return "🏊 Плавання";
            case "fitness":
                return "💪 Фітнес";
            case "yoga":
                return "🧘 Йога";
            case "running":
                return "🏃 Біг";
            case "volleyball":
                return "🏐 Волейбол";
            default:
                return sport;
        }
    }

    private static String translateLeisure(String leisure) {
        switch (leisure.toLowerCase()) {
            case "sports_centre":
                return "Спортивний центр";
            case "pitch":
                return "Спортивний майданчик";
            case "swimming_pool":
                return "Басейн";
            case "stadium":
                return "Стадіон";
            case "fitness_centre":
                return "Фітнес центр";
            default:
                return leisure;
        }
    }
}