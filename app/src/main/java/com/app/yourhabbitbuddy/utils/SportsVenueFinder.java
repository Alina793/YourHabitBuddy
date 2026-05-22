package com.app.yourhabbitbuddy.utils;

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

public class SportsVenueFinder {

    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search";

    public interface VenueCallback {
        void onSuccess(List<SportsVenue> venues);
        void onError(String error);
    }

    public static void findNearbyVenues(double lat, double lon, double radius, VenueCallback callback) {
        new FetchVenuesTask(callback).execute(lat, lon, radius);
    }

    private static class FetchVenuesTask extends AsyncTask<Double, Void, List<SportsVenue>> {
        private VenueCallback callback;
        private String error;

        FetchVenuesTask(VenueCallback callback) {
            this.callback = callback;
        }

        @Override
        protected List<SportsVenue> doInBackground(Double... params) {
            double lat = params[0];
            double lon = params[1];
            int radius = params[2].intValue();

            List<SportsVenue> venues = new ArrayList<>();

            // Пошук спортивних об'єктів через Nominatim
            String[] queries = {
                    "sports centre",
                    "stadium",
                    "sports field",
                    "fitness centre",
                    "swimming pool",
                    "gym",
                    "basketball court",
                    "football pitch",
                    "tennis court"
            };

            try {
                for (String query : queries) {
                    String encodedQuery = URLEncoder.encode(query, "UTF-8");
                    String urlString = NOMINATIM_API +
                            "?q=" + encodedQuery +
                            "&format=json" +
                            "&limit=10" +
                            "&viewbox=" + (lon - 0.05) + "," + (lat + 0.05) + "," + (lon + 0.05) + "," + (lat - 0.05) +
                            "&bounded=1";

                    Log.d("Nominatim", "URL: " + urlString);

                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "YourHabitBuddy/1.0");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONArray results = new JSONArray(response.toString());

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject result = results.getJSONObject(i);
                            SportsVenue venue = parseVenue(result, query);
                            if (venue != null) {
                                calculateDistance(venue, lat, lon);
                                if (venue.getDistance() <= radius && !isDuplicate(venues, venue)) {
                                    venues.add(venue);
                                }
                            }
                        }
                    }
                    conn.disconnect();

                    // Затримка щоб не перевантажувати API
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                error = e.getMessage();
                Log.e("Nominatim", "Error: " + e.getMessage(), e);
            }

            return venues;
        }

        private SportsVenue parseVenue(JSONObject result, String queryType) throws Exception {
            SportsVenue venue = new SportsVenue();

            String lat = result.optString("lat");
            String lon = result.optString("lon");
            String displayName = result.optString("display_name");
            String type = result.optString("type");
            String category = result.optString("category");

            if (lat.isEmpty() || lon.isEmpty()) {
                return null;
            }

            venue.setLatitude(Double.parseDouble(lat));
            venue.setLongitude(Double.parseDouble(lon));
            venue.setName(getShortName(displayName));
            venue.setSportType(getSportTypeFromQuery(queryType));
            venue.setAddress(displayName);

            return venue;
        }

        private String getShortName(String displayName) {
            if (displayName == null) return "Спортивний майданчик";
            String[] parts = displayName.split(",");
            return parts[0].trim();
        }

        private String getSportTypeFromQuery(String query) {
            switch (query.toLowerCase()) {
                case "sports centre": return "sports_centre";
                case "stadium": return "stadium";
                case "sports field": return "field";
                case "fitness centre": return "fitness";
                case "swimming pool": return "swimming";
                case "gym": return "gym";
                case "basketball court": return "basketball";
                case "football pitch": return "soccer";
                case "tennis court": return "tennis";
                default: return "sport";
            }
        }

        private boolean isDuplicate(List<SportsVenue> venues, SportsVenue newVenue) {
            for (SportsVenue venue : venues) {
                double distance = Math.hypot(
                        venue.getLatitude() - newVenue.getLatitude(),
                        venue.getLongitude() - newVenue.getLongitude()
                );
                if (distance < 0.0001) { // близько 10 метрів
                    return true;
                }
            }
            return false;
        }

        private void calculateDistance(SportsVenue venue, double lat, double lon) {
            float[] results = new float[1];
            Location.distanceBetween(lat, lon, venue.getLatitude(), venue.getLongitude(), results);
            venue.setDistance(results[0]);
        }

        @Override
        protected void onPostExecute(List<SportsVenue> venues) {
            if (callback != null) {
                if (error != null && venues.isEmpty()) {
                    callback.onError(error);
                } else {
                    callback.onSuccess(venues);
                }
            }
        }
    }
}