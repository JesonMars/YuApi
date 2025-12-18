package com.pingo.yuapi.dto;

/**
 * Request DTO for saving user commute setup
 * Contains home and work addresses with their coordinates
 */
public class CommuteSetupRequest {
    private String userId;
    private String homeAddress;
    private String homeCity;
    private String workAddress;
    private String workCity;
    private LocationDTO homeLocation;
    private LocationDTO workLocation;

    public CommuteSetupRequest() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getHomeCity() {
        return homeCity;
    }

    public void setHomeCity(String homeCity) {
        this.homeCity = homeCity;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public String getWorkCity() {
        return workCity;
    }

    public void setWorkCity(String workCity) {
        this.workCity = workCity;
    }

    public LocationDTO getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(LocationDTO homeLocation) {
        this.homeLocation = homeLocation;
    }

    public LocationDTO getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(LocationDTO workLocation) {
        this.workLocation = workLocation;
    }
}
