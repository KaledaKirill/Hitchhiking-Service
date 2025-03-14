package com.example.hitchhikingservice.cache;

import com.example.hitchhikingservice.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserCache extends LfuCache<User> {

    public UserCache() {
        super(3);
    }
}
