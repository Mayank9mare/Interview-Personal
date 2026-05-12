package com.uber.hitcountermultithreaded;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class WebpageVisitCounterTest {
    @Test
    void singlePage_incrementAndGet() {
        WebpageVisitCounter c = new WebpageVisitCounter(2);
        c.incrementVisitCount(0);
        c.incrementVisitCount(1);
        c.incrementVisitCount(1);
        c.incrementVisitCount(1);
        c.incrementVisitCount(0);
        assertEquals(2, c.getVisitCount(0));
        assertEquals(3, c.getVisitCount(1));
    }

    @Test
    void initialCountIsZero() {
        WebpageVisitCounter c = new WebpageVisitCounter(5);
        for (int i = 0; i < 5; i++) assertEquals(0, c.getVisitCount(i));
    }

    @Test
    void concurrentIncrements_correctTotal() throws InterruptedException {
        int pages = 3, threads = 10, incrementsPerThread = 100;
        WebpageVisitCounter c = new WebpageVisitCounter(pages);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                for (int i = 0; i < incrementsPerThread; i++) c.incrementVisitCount(0);
                latch.countDown();
            });
        }
        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();
        assertEquals(threads * incrementsPerThread, c.getVisitCount(0));
    }
}
