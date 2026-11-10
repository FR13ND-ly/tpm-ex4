package tpm;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class AndersonLock implements SimpleLock {
    private final AtomicInteger tail = new AtomicInteger(0);
    private final AtomicIntegerArray flags;
    private final int size;

    private final ThreadLocal<Integer> mySlot =
            new ThreadLocal<>();

    public AndersonLock(int size) {
        if (size < 1) {
            throw new IllegalArgumentException();
        }

        this.size = size;
        flags = new AtomicIntegerArray(size);
        flags.set(0, 1);
    }

    @Override
    public void lock() {
        int slot = Math.floorMod(
                tail.getAndIncrement(), size
        );

        mySlot.set(slot);

        while (flags.get(slot) == 0) {
            Thread.onSpinWait();
        }
    }

    @Override
    public void unlock() {
        int slot = mySlot.get();

        flags.set(slot, 0);
        flags.set((slot + 1) % size, 1);

        mySlot.remove();
    }
}
