package eu.dirk.haase.jpa.data;

import eu.dirk.haase.hibernate.Flushable;
import eu.dirk.haase.hibernate.FlushableRegistry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(BlockJUnit4ClassRunner.class)
public class FlushableRegistryTest {

    private int flushIndex;
    private List<MyFlushable> flushableList;
    private Function<Integer, MyFlushable> newInstance;
    private FlushableRegistry registry;

    @Before
    public void setUp() {
        flushIndex = 0;
        registry = FlushableRegistry.newInstance();
        newInstance = (i) -> new MyFlushableUntil(i);
        flushableList = new ArrayList<>();
        for (int i = 0; 10 > i; ++i) {
            flushableList.add(newInstance.apply(i));
        }
    }

    @Test
    public void test_that_all_flushables_are_flushed() {
        // Given
        // When
        for (int i = 0; 10 > i; ++i) {
            registry.register(flushableList.get(i));
        }
        int size1 = registry.sizeCurrent();
        int size2 = registry.flushAllCurrent();
        int size3 = registry.sizeCurrent();
        // Then
        assertThat(size1).isEqualTo(10);
        assertThat(size2).isEqualTo(10);
        assertThat(size3).isEqualTo(0);
        for (int i = 0; 10 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.createIndex()).isEqualTo(actual.flushIndex());
        }
    }

    @Test
    public void test_that_head_flushables_are_flushed() {
        // Given
        // When
        for (int i = 0; 10 > i; ++i) {
            registry.register(flushableList.get(i));
        }
        int size1 = registry.sizeCurrent();
        MyFlushable myFlushable = newInstance.apply(5);
        int size2 = registry.register(myFlushable);
        int size3 = registry.sizeCurrent();
        // Then
        assertThat(size1).isEqualTo(10);
        assertThat(size2).isEqualTo(6);
        assertThat(size3).isEqualTo(5);
        assertThat(myFlushable.flushIndex()).isNull();
        for (int i = 0; 6 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.createIndex()).isEqualTo(actual.flushIndex());
        }
        for (int i = 6; 10 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.flushIndex()).isNull();
        }
    }

    @Test
    public void test_that_head_flushables_are_flushed_when_flush_the_fifth_flushable() {
        // Given
        final List<MyFlushable> flushableList = new ArrayList<>();
        for (int i = 0; 10 > i; ++i) {
            flushableList.add(new MyFlushableUntil(i));
        }
        // When
        for (int i = 0; 10 > i; ++i) {
            registry.register(flushableList.get(i));
        }
        int size1 = registry.sizeCurrent();
        flushableList.get(4).flush();
        int size2 = registry.sizeCurrent();
        // Then
        assertThat(size1).isEqualTo(10);
        assertThat(size2).isEqualTo(5);
        for (int i = 0; 5 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.createIndex()).isEqualTo(actual.flushIndex());
        }
        for (int i = 6; 10 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.flushIndex()).isNull();
        }
    }

    @Test
    public void test_that_not_registered_flushable_is_several_times_flushable() {
        // Given
        final MyFlushable myFlushable = new MyFlushableUntil(99);
        // When
        myFlushable.flush();
        myFlushable.flush();
        myFlushable.flush();
        myFlushable.flush();
        // Then
        assertThat(myFlushable.flushIndex()).isEqualTo(3);
    }

    @Test
    public void test_that_registered_flushable_is_several_times_flushable() {
        // Given
        final MyFlushable myFlushable = new MyFlushableUntil(99);
        registry.register(myFlushable);
        // When
        myFlushable.flush();
        myFlushable.flush();
        myFlushable.flush();
        myFlushable.flush();
        // Then
        assertThat(myFlushable.flushIndex()).isEqualTo(3);
    }

    @Test
    @Ignore("Unterstuetzung fuer Entity-Manager gekoppelte Persistence-Contexte ist abgeschaltet.")
    public void test_that_registering_last_equal_but_not_identical_flushable_again_is_flushing() {
        // Given
        for (int i = 0; 10 > i; ++i) {
            registry.register(flushableList.get(i));
        }
        // When
        int size1 = registry.register(flushableList.get(9));
        int size2 = registry.register(newInstance.apply(9));
        int size3 = registry.register(newInstance.apply(9));
        int size4 = registry.register(flushableList.get(9));
        int size5 = registry.sizeCurrent();
        // Then
        assertThat(size1).isEqualTo(0);
        assertThat(size2).isEqualTo(10);
        assertThat(size3).isEqualTo(1);
        assertThat(size4).isEqualTo(1);
        assertThat(size5).isEqualTo(1);
        for (int i = 0; 10 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.createIndex()).isEqualTo(actual.flushIndex());
        }
    }

    @Test
    public void test_that_registering_last_flushable_again_is_not_flushing() {
        // Given
        for (int i = 0; 10 > i; ++i) {
            registry.register(flushableList.get(i));
        }
        // When
        int size1 = registry.register(flushableList.get(9));
        int size2 = registry.register(flushableList.get(9));
        int size3 = registry.register(flushableList.get(9));
        int size4 = registry.register(flushableList.get(9));
        int size5 = registry.sizeCurrent();
        // Then
        assertThat(size1).isEqualTo(0);
        assertThat(size2).isEqualTo(0);
        assertThat(size3).isEqualTo(0);
        assertThat(size4).isEqualTo(0);
        assertThat(size5).isEqualTo(10);
        for (int i = 0; 10 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.flushIndex()).isNull();
        }
    }

    interface MyFlushable extends Flushable {
        int createIndex();

        Integer flushIndex();
    }

    class MyFlushableNoFun implements Flushable, MyFlushable {

        final int createIndex;
        Integer flushIndex;

        MyFlushableNoFun(int index) {
            this.createIndex = index;
        }

        public int createIndex() {
            return createIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyFlushableNoFun that = (MyFlushableNoFun) o;

            return createIndex == that.createIndex;
        }

        @Override
        public void flush() {
            this.flushIndex = FlushableRegistryTest.this.flushIndex++;
        }

        public Integer flushIndex() {
            return flushIndex;
        }

        @Override
        public int hashCode() {
            return createIndex;
        }

    }

    class MyFlushableUntil implements Flushable, MyFlushable, FlushableRegistry.UntilNowFlushable {

        final int createIndex;
        Integer flushIndex;
        Runnable flushUntilFunction;

        MyFlushableUntil(int index) {
            this.createIndex = index;
        }

        public int createIndex() {
            return createIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyFlushableUntil that = (MyFlushableUntil) o;

            return createIndex == that.createIndex;
        }

        @Override
        public void flush() {
            if (!FlushableRegistry.UntilNowFlushable.flush(this.flushUntilFunction)) {
                realFlush();
            }
        }

        public Integer flushIndex() {
            return flushIndex;
        }

        @Override
        public int hashCode() {
            return createIndex;
        }

        public void realFlush() {
            this.flushIndex = FlushableRegistryTest.this.flushIndex++;
        }

        @Override
        public void setUntilNowFlushableFunction(Runnable flushUntilFunction) {
            this.flushUntilFunction = flushUntilFunction;
        }
    }

}
