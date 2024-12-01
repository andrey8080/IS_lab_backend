package itmo.andrey.lab1_backend.service;

import itmo.andrey.lab1_backend.domain.entitie.User;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserCacheService {

    private final ConcurrentHashMap<String, User> userCache = new ConcurrentHashMap<>();

    public void cacheUser(User user) {
        userCache.put(user.getName(), user);
    }

    public User getUserFromCache(String username) {
        return userCache.get(username);
    }

    public boolean isUserInCache(String username) {
        return userCache.containsKey(username);
    }

    public void clearCache() {
        userCache.clear();
    }
}
