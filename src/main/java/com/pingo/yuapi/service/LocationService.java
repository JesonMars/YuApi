package com.pingo.yuapi.service;

import com.pingo.yuapi.entity.Location;
import com.pingo.yuapi.mapper.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LocationService {

    @Autowired
    private LocationMapper locationMapper;

    public List<Location> getAllLocations() {
        return locationMapper.selectAll();
    }

    public List<Location> getLocationsByType(String type) {
        return locationMapper.selectByType(type);
    }

    public Location getLocationById(Long id) {
        return locationMapper.selectById(id);
    }

    public boolean addLocation(Location location) {
        return locationMapper.insert(location) > 0;
    }

    public boolean updateLocation(Location location) {
        return locationMapper.updateById(location) > 0;
    }

    public boolean deleteLocation(Long id) {
        return locationMapper.deleteById(id) > 0;
    }
}