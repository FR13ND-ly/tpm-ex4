package tpm;

import java.util.concurrent.CountDownLatch;

public class Main {

    private static final int LIMIT = 120000;
    private static final int RUNS = 5;
    private static final int WARMUP_RUNS = 3;
    private static final int[] THREAD_COUNTS = {4, 8, 12};

    public static void main(String[] args)
            throws InterruptedException {

        for (String algorithm : new String[]{"Anderson", "CLH"}) {
            for (int threads : THREAD_COUNTS) {
                runBenchmark(algorithm, threads);
            }
        }
    }

    private static void runBenchmark(String algorithm, int threadCount)
            throws InterruptedException {

        for (int i = 0; i < WARMUP_RUNS; i++) {
            runOnce(algorithm, threadCount);
        }

        double total = 0;

        System.out.println("------------------------------");
        System.out.println("Lock: " + algorithm);
        System.out.println("Threaduri: " + threadCount);

        for (int run = 1; run <= RUNS; run++) {
            double timeMs = runOnce(algorithm, threadCount);
            total += timeMs;

            System.out.printf(
                    "Rulare %d: %.4f ms%n", run, timeMs
            );
        }

        System.out.printf(
                "Timp mediu: %.4f ms%n", total / RUNS
        );

        System.out.println("------------------------------");
    }

    private static double runOnce(String algorithm, int threadCount)
            throws InterruptedException {

        SimpleLock lock;

        if (algorithm.equals("Anderson")) {
            lock = new AndersonLock(threadCount);
        } else {
            lock = new CLHLock();
        }

        SharedCounter counter = new SharedCounter(LIMIT, lock);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread worker = new Thread(() -> {
                try {
                    ready.countDown();
                    start.await();

                    counter.incrementUntilLimit();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });

            worker.start();
        }

        ready.await();

        long startTime = System.nanoTime();

        start.countDown();

        done.await();

        long endTime = System.nanoTime();

        int result = counter.getValue();

        if (result != LIMIT) {
            throw new IllegalStateException(
                    "Contor incorect: " + result
            );
        }

        return (endTime - startTime) / 1_000_000.0;
    }
}