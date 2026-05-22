package com.app.yourhabbitbuddy.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.app.yourhabbitbuddy.R;

@Entity(tableName = "sports_venues")
public class SportsVenue {
    @PrimaryKey
    private long id;
    private String name;
    private String type;
    private String sportType;
    private double latitude;
    private double longitude;
    private String address;
    private String openingHours;
    private boolean isFree;
    private float distance;
    private String phone;
    private String website;
    private int rating;

    public SportsVenue() {}

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getSportType() { return sportType; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public String getOpeningHours() { return openingHours; }
    public boolean isFree() { return isFree; }
    public float getDistance() { return distance; }
    public String getPhone() { return phone; }
    public String getWebsite() { return website; }
    public int getRating() { return rating; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setSportType(String sportType) { this.sportType = sportType; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setAddress(String address) { this.address = address; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public void setFree(boolean free) { isFree = free; }
    public void setDistance(float distance) { this.distance = distance; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setWebsite(String website) { this.website = website; }
    public void setRating(int rating) { this.rating = rating; }

    public int getIconResource() {
        if (sportType != null) {
            switch (sportType.toLowerCase()) {
                case "soccer":
//                case "football": return R.drawable.ic_soccer;
//                case "basketball": return R.drawable.ic_basketball;
//                case "tennis": return R.drawable.ic_tennis;
//                case "swimming": return R.drawable.ic_swimming;
                case "fitness":
                case "gym": return R.drawable.ic_gym;
//                case "yoga": return R.drawable.ic_yoga;
                case "running":
//                case "athletics": return R.drawable.ic_running;
                default: return R.drawable.ic_sports;
            }
        }
        return R.drawable.ic_sports;
    }
}