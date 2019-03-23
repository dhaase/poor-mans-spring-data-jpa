package eu.dirk.haase.jpa.data;

import eu.dirk.haase.hibernate.jdbc.ThreadLocalResourceRegistry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ThreadLocalResourceRegistryTest {

    protected int count;
    protected ThreadLocalResourceRegistry<String, Integer> registry;

    @Test
    public void test_that_clearAll_removes_all_values() throws InterruptedException, ExecutionException {
        // Given
        final String key1 = "key1";
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Object> list1 = new ArrayList<>();
        CountDownLatch latch1 = new CountDownLatch(1);
        // When
        Future<?> future1 = executorService.submit(() -> {
            // Thread two
            list1.add(registry.newIfAbsent(key1, (k) -> ++this.count));
            latch1.await();
            list1.add(registry.getCurrent(key1));
            return null;
        });
        Thread.sleep(100);
        Integer value1a = registry.newIfAbsent(key1, (k) -> ++this.count + 23);
        registry.clearAll();
        latch1.countDown();
        future1.get();
        Integer value1b = registry.getCurrent(key1);
        // Then
        assertThat(list1.get(0)).isEqualTo(1);
        assertThat(list1.get(1)).isNull();
        assertThat(value1a).isEqualTo(25);
        assertThat(value1b).isNull();
        // clean up
        executorService.shutdownNow();
    }

    @Test
    public void test_that_clearFunction_is_bound_to_the_internal_map_object() throws InterruptedException, ExecutionException {
        // Given
        final String key1 = "key1";
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch latch1 = new CountDownLatch(2);
        List<Object> list1 = new ArrayList<>();
        // When
        Future<?> future1 = executorService.submit(() -> {
            // Thread two
            list1.add(registry.newIfAbsent(key1, (k) -> ++this.count));
            latch1.await();
            list1.add(registry.getCurrent(key1));
            return null;
        });
        Thread.sleep(100);
        Integer value1a = registry.newIfAbsent(key1, (k) -> ++this.count);
        latch1.countDown();
        registry.clearFunction(key1).run();
        Integer value1b = registry.getCurrent(key1);
        latch1.countDown();
        future1.get();
        // Then
        assertThat(list1.get(0)).isEqualTo(1);
        assertThat(list1.get(1)).isEqualTo(1);
        assertThat(value1a).isEqualTo(2);
        assertThat(value1b).isNull();
        // clean up
        executorService.shutdownNow();
    }

    @Test
    public void test_that_clearFunction_clears_the_value_independent_of_threads() throws InterruptedException, ExecutionException {
        // Given
        final String key1 = "key1";
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch latch1 = new CountDownLatch(2);
        List<Object> list1 = new ArrayList<>();
        // When
        Future<?> future1 = executorService.submit(() -> {
            // Thread two
            list1.add(registry.newIfAbsent(key1, (k) -> ++this.count));
            list1.add(registry.clearFunction(key1));
            latch1.await();
            list1.add(registry.getCurrent(key1));
            return null;
        });
        Thread.sleep(100);
        latch1.countDown();
        ((Runnable) list1.get(1)).run();
        latch1.countDown();
        future1.get();
        // Then
        assertThat(list1.get(0)).isEqualTo(1);
        assertThat(list1.get(2)).isNull();
        // clean up
        executorService.shutdownNow();
    }

    @Test
    public void test_that_the_values_are_bound_on_threads() throws InterruptedException, ExecutionException {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        // When
        Future<List<Integer>> future1 = executorService.submit(() -> {
            // Thread two
            List<Integer> list = new ArrayList<>();
            list.add(registry.newIfAbsent(key1, (k) -> ++this.count));
            list.add(registry.getCurrent(key1));
            latch1.await();
            return list;
        });
        Thread.sleep(100);
        latch1.countDown();
        List<Integer> list1 = future1.get();
        // Thread one
        Integer value1 = registry.getCurrent(key1);
        //
        Future<List<Integer>> future2 = executorService.submit(() -> {
            // Thread three
            List<Integer> list = new ArrayList<>();
            list.add(registry.newIfAbsent(key2, (k) -> ++this.count + 100));
            list.add(registry.getCurrent(key2));
            latch2.await();
            return list;
        });
        Thread.sleep(100);
        latch2.countDown();
        List<Integer> list2 = future2.get();
        // Thread one
        Integer value2 = registry.getCurrent(key1);
        // Then
        assertThat(value1).isNull();
        assertThat(value2).isNull();
        assertThat(list1.get(0)).isEqualTo(1);
        assertThat(list1.get(1)).isEqualTo(1);
        assertThat(list2.get(0)).isEqualTo(102);
        assertThat(list2.get(1)).isEqualTo(102);
        // clean up
        executorService.shutdownNow();
    }

    @Test
    public void test_that_getCurrent_returns_first_value() {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        // When
        int value1a = registry.newIfAbsent(key1, (k) -> ++this.count);
        int value2a = registry.getCurrent(key1);
        int value3a = registry.getCurrent(key1);
        //
        int value1b = registry.newIfAbsent(key2, (k) -> ++this.count + 100);
        int value2b = registry.getCurrent(key2);
        int value3b = registry.getCurrent(key2);
        // Then
        assertThat(value1a).isEqualTo(1);
        assertThat(value2a).isEqualTo(1);
        assertThat(value3a).isEqualTo(1);
        assertThat(value1b).isEqualTo(102);
        assertThat(value2b).isEqualTo(102);
        assertThat(value3b).isEqualTo(102);
    }

    @Test
    public void test_that_isCurrentExisting_returns_true() {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        int value1a = registry.newIfAbsent(key1, (k) -> ++this.count);
        // When
        boolean isExiting1b = registry.isCurrentExisting(key1);
        boolean isExiting2b = registry.isCurrentExisting(key2);
        // Then
        assertThat(value1a).isEqualTo(1);
        assertThat(isExiting1b).isTrue();
        assertThat(isExiting2b).isFalse();
    }

    @Test
    public void test_that_newIfAbsent_creates_only_once() {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        // When
        int value1a = registry.newIfAbsent(key1, (k) -> ++this.count);
        int value2a = registry.newIfAbsent(key1, (k) -> ++this.count);
        int value3a = registry.newIfAbsent(key1, (k) -> ++this.count);
        //
        int value1b = registry.newIfAbsent(key2, (k) -> ++this.count + 100);
        int value2b = registry.newIfAbsent(key2, (k) -> ++this.count + 100);
        int value3b = registry.newIfAbsent(key2, (k) -> ++this.count + 100);
        // Then
        assertThat(value1a).isEqualTo(1);
        assertThat(value2a).isEqualTo(1);
        assertThat(value3a).isEqualTo(1);
        //
        assertThat(value1b).isEqualTo(102);
        assertThat(value2b).isEqualTo(102);
        assertThat(value3b).isEqualTo(102);
    }

    @Test
    public void test_that_removeCurrent_removes_the_value() {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        Integer value1a = registry.newIfAbsent(key1, (k) -> ++this.count + 100);
        Integer value2a = registry.newIfAbsent(key2, (k) -> ++this.count + 10);
        // When
        Integer value1b1 = registry.removeCurrent(key1);
        Integer value1b2 = registry.getCurrent(key1);
        Integer value2b = registry.getCurrent(key2);
        // Then
        assertThat(value1a).isEqualTo(101);
        assertThat(value2a).isEqualTo(12);
        assertThat(value1b1).isEqualTo(101);
        assertThat(value1b2).isNull();
        assertThat(value2b).isEqualTo(12);
    }

    @Test
    public void test_that_remove_function_removes_the_value() {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        Integer value1a = registry.newIfAbsent(key1, (k) -> ++this.count + 100);
        Integer value2a = registry.newIfAbsent(key2, (k) -> ++this.count + 10);
        Runnable clearFunction1 = registry.clearFunction(key1);
        // When
        clearFunction1.run();
        Integer value1b = registry.getCurrent(key1);
        Integer value2b = registry.getCurrent(key2);
        // Then
        assertThat(value1a).isEqualTo(101);
        assertThat(value2a).isEqualTo(12);
        assertThat(value1b).isNull();
        assertThat(value2b).isEqualTo(12);
    }

    @Test
    public void test_that_clearCurrent_removes_all_values_of_the_current_thread() {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        Integer value1a = registry.newIfAbsent(key1, (k) -> ++this.count + 100);
        Integer value2a = registry.newIfAbsent(key2, (k) -> ++this.count + 10);
        // When
        registry.clearCurrent();
        Integer value1b = registry.getCurrent(key1);
        Integer value2b = registry.getCurrent(key2);
        // Then
        assertThat(value1a).isEqualTo(101);
        assertThat(value2a).isEqualTo(12);
        assertThat(value1b).isNull();
        assertThat(value2b).isNull();
    }

    @Test
    public void test_that_clearCurrent_never_removes_the_values_of_the_other_thread() throws ExecutionException, InterruptedException {
        // Given
        final String key1 = "key1";
        final String key2 = "key2";
        Integer value1a = registry.newIfAbsent(key1, (k) -> ++this.count + 100);
        Integer value2a = registry.newIfAbsent(key2, (k) -> ++this.count + 10);
        ExecutorService executorService = Executors.newCachedThreadPool();
        // When
        Future<?> future = executorService.submit(() -> registry.clearCurrent());
        future.get();
        Integer value1b = registry.getCurrent(key1);
        Integer value2b = registry.getCurrent(key2);
        // Then
        assertThat(value1a).isEqualTo(101);
        assertThat(value2a).isEqualTo(12);
        assertThat(value1b).isEqualTo(101);
        assertThat(value2b).isEqualTo(12);
        // cleanup
        executorService.shutdownNow();
    }

}
