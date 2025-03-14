package com.example.hitchhikingservice.cache;

import com.example.hitchhikingservice.model.entity.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideCache extends LfuCache<Ride> {

    public RideCache() {
        super(100);
    }
}
