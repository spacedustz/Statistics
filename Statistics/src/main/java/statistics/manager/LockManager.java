package statistics.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class LockManager {
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();

    public Lock getLock(String instanceName) {
        return locks.computeIfAbsent(instanceName, id -> new ReentrantLock());
    }
}

