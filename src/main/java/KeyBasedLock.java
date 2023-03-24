import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KeyBasedLock {

    private final Map<String, Lock> lockMap;

    public KeyBasedLock(Set<String> keys) {
        this.lockMap = new HashMap<>(keys.size());
        for (String key: keys) {
            this.lockMap.put(key, new ReentrantLock());
        }
    }

    public boolean tryLock(String key) {
        Lock lock = lockMap.get(key);
        if (lock == null) {
            return false;
        }
        return lock.tryLock();
    }

    public void unlock(String key) {
        Lock lock = lockMap.get(key);
        if (lock != null) {
            lock.unlock();
        }
    }
}
