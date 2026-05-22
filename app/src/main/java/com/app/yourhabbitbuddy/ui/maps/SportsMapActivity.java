package com.app.yourhabbitbuddy.ui.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.yourhabbitbuddy.utils.SportsVenueFinder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.app.yourhabbitbuddy.R;
import com.app.yourhabbitbuddy.data.SportsVenue;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.ArrayList;
import java.util.List;

public class SportsMapActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final double DEFAULT_LAT = 50.4501;
    private static final double DEFAULT_LON = 30.5234;

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private List<SportsVenue> venues = new ArrayList<>();
    private LocationManager locationManager;
    private boolean isFirstLocation = true;
    private boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Налаштування OSMDroid ПЕРЕД setContentView
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("YourHabitBuddy/1.0");

        setContentView(R.layout.activity_sports_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.sports_map));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Ініціалізація карти
        initMap();

        // Налаштування кнопки оновлення
        setupRefreshButton();

        // Перевірка дозволу
        checkLocationPermission();
    }

    private void initMap() {
        mapView = findViewById(R.id.mapView);
        if (mapView == null) {
            return;
        }

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));

        // Шар з моєю локацією
        myLocationOverlay = new MyLocationNewOverlay(mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(() -> {
            if (mapView == null) return;
            GeoPoint myLocation = myLocationOverlay.getMyLocation();
            if (myLocation != null) {
                runOnUiThread(() -> {
                    if (mapView != null) {
                        mapView.getController().animateTo(myLocation);
                        if (isFirstLocation) {
                            loadNearbyVenues(myLocation.getLatitude(), myLocation.getLongitude());
                            isFirstLocation = false;
                        }
                    }
                });
            }
        });
        mapView.getOverlays().add(myLocationOverlay);
        isMapReady = true;
    }

    private void setupRefreshButton() {
        FloatingActionButton fabRefresh = findViewById(R.id.fab_refresh);
        if (fabRefresh != null) {
            fabRefresh.setOnClickListener(v -> {
                if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                    loadNearbyVenues(
                            myLocationOverlay.getMyLocation().getLatitude(),
                            myLocationOverlay.getMyLocation().getLongitude()
                    );
                } else {
                    Toast.makeText(this, getString(R.string.searching_venues), Toast.LENGTH_SHORT).show();
                    loadNearbyVenues(DEFAULT_LAT, DEFAULT_LON);
                }
            });
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void startLocationUpdates() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                onLocationChanged(lastLocation);
            }
        }
    }

    private void loadNearbyVenues(double lat, double lon) {
        if (!isMapReady) return;

        Snackbar.make(findViewById(android.R.id.content), getString(R.string.searching_venues), Snackbar.LENGTH_INDEFINITE).show();

        SportsVenueFinder.findNearbyVenues(lat, lon, 5000, new SportsVenueFinder.VenueCallback() {
            @Override
            public void onSuccess(List<SportsVenue> foundVenues) {
                venues = foundVenues;
                runOnUiThread(() -> {
                    addMarkersToMap();
                    String message = venues.isEmpty() ?
                            getString(R.string.no_venues_found) :
                            "✅ " + venues.size() + " " + getString(R.string.venues_found);
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error) + ": " + error, Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addMarkersToMap() {
        if (mapView == null) return;

        // Очищаємо старі маркери
        List<Object> toRemove = new ArrayList<>();
        for (Object overlay : mapView.getOverlays()) {
            if (overlay instanceof Marker && overlay != myLocationOverlay) {
                toRemove.add(overlay);
            }
        }
        mapView.getOverlays().removeAll(toRemove);

        for (SportsVenue venue : venues) {
            GeoPoint point = new GeoPoint(venue.getLatitude(), venue.getLongitude());
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setTitle(venue.getName());
            marker.setSubDescription(getMarkerDescription(venue));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(getResources().getDrawable(getIconForSportType(venue.getSportType())));
            marker.setOnMarkerClickListener((m, mapView) -> {
                showVenueDetails(venue);
                return true;
            });
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private int getIconForSportType(String sportType) {
        if (sportType == null) return R.drawable.ic_sports;
        switch (sportType) {
            case "soccer": return R.drawable.ic_soccer;
            case "basketball": return R.drawable.ic_basketball;
            case "tennis": return R.drawable.ic_tennis;
            case "swimming": return R.drawable.ic_swimming;
            case "gym": return R.drawable.ic_gym;
            default: return R.drawable.ic_sports;
        }
    }

    private String getMarkerDescription(SportsVenue venue) {
        String desc = "";
        if (venue.getDistance() > 0) {
            desc += String.format("📏 %.0f м", venue.getDistance());
        }
        if (venue.isFree()) {
            if (!desc.isEmpty()) desc += " • ";
            desc += "🆓 " + getString(R.string.free);
        }
        return desc;
    }

    private void showVenueDetails(SportsVenue venue) {
        StringBuilder message = new StringBuilder();
        message.append("📍 ").append(venue.getName()).append("\n\n");
        if (venue.getDistance() > 0) {
            message.append("📏 ").append(getString(R.string.distance)).append(": ").append(String.format("%.0f м", venue.getDistance())).append("\n");
        }
        if (venue.getSportType() != null && !venue.getSportType().equals("general")) {
            message.append("🏷️ ").append(getString(R.string.habit_type)).append(": ").append(formatSportType(venue.getSportType())).append("\n");
        }
        if (venue.getAddress() != null && !venue.getAddress().isEmpty()) {
            message.append("📭 ").append(getString(R.string.address)).append(": ").append(venue.getAddress()).append("\n");
        }
        if (venue.getOpeningHours() != null && !venue.getOpeningHours().isEmpty()) {
            message.append("⏰ ").append(getString(R.string.opening_hours)).append(": ").append(venue.getOpeningHours()).append("\n");
        }
        if (venue.isFree()) {
            message.append("💰 ").append(getString(R.string.free)).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(venue.getName())
                .setMessage(message.toString())
                .setPositiveButton(getString(R.string.build_route), (d, w) -> openNavigation(venue))
                .setNegativeButton(getString(R.string.close), null)
                .show();
    }

    private String formatSportType(String sport) {
        switch (sport.toLowerCase()) {
            case "soccer": return "⚽ " + getString(R.string.football_pitch);
            case "basketball": return "🏀 " + getString(R.string.basketball_court);
            case "tennis": return "🎾 " + getString(R.string.tennis_court);
            case "swimming": return "🏊 " + getString(R.string.swimming_pool);
            case "gym": return "💪 " + getString(R.string.gym);
            default: return sport;
        }
    }

    private void openNavigation(SportsVenue venue) {
        String uri = "https://www.google.com/maps/dir/?api=1&destination=" +
                venue.getLatitude() + "," + venue.getLongitude() + "&travelmode=walking";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (mapView == null) return;
        GeoPoint myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().animateTo(myLocation);
        if (isFirstLocation) {
            loadNearbyVenues(location.getLatitude(), location.getLongitude());
            isFirstLocation = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_needed), Toast.LENGTH_LONG).show();
                loadNearbyVenues(DEFAULT_LAT, DEFAULT_LON);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDetach();
        if (locationManager != null) locationManager.removeUpdates(this);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}
    @Override public void onProviderDisabled(@NonNull String provider) {}
}