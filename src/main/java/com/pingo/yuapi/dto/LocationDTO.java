package com.pingo.yuapi.dto;

/**
 * Location DTO for commute setup
 * Contains only longitude and latitude coordinates
 */
public class LocationDTO {
    private Double longitude;
    private Double latitude;

    public LocationDTO() {}

    public LocationDTO(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
