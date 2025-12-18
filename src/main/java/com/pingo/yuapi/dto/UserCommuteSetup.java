package com.pingo.yuapi.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for user commute setup
 * Contains home and work addresses with coordinates and setup time
 */
public class UserCommuteSetup {
    private String homeAddress;
    private String homeCity;
    private String workAddress;
    private String workCity;
    private LocationDTO homeLocation;
    private LocationDTO workLocation;
    private LocalDateTime setupTime;

    public UserCommuteSetup() {}

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

    public LocalDateTime getSetupTime() {
        return setupTime;
    }

    public void setSetupTime(LocalDateTime setupTime) {
        this.setupTime = setupTime;
    }
}
