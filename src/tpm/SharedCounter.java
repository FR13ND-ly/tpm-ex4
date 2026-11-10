
package tmp.locks;

public class SharedCounter {
    private int value = 0;
    private final int limit;
    private final SimpleLock lock;

    public SharedCounter(int limit, SimpleLock lock) {
        this.limit = limit;
        this.lock = lock;
    }

    public void incrementUntilLimit() {
        while (true) {
            lock.lock();

            try {
                if (value >= limit) {
                    return;
                }

                value++;
            } finally {
                lock.unlock();
            }
        }
    }

    public int getValue() {
        return value;
    }
}
