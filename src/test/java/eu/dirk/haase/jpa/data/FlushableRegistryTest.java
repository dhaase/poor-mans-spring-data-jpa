package eu.dirk.haase.jpa.data;

import eu.dirk.haase.hibernate.Flushable;
import eu.dirk.haase.hibernate.FlushableRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(BlockJUnit4ClassRunner.class)
public class FlushableRegistryTest {

    private int flushIndex;
    private List<MyFlushable> flushableList;
    private FlushableRegistry registry;

    @Before
    public void setUp() {
        flushIndex = 0;
        registry = FlushableRegistry.newInstance();
        flushableList = new ArrayList<>();
        for (int i = 0; 10 > i; ++i) {
            flushableList.add(new MyFlushable(i));
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
            assertThat(actual.createIndex).isEqualTo(actual.flushIndex);
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
        MyFlushable myFlushable = new MyFlushable(5);
        int size2 = registry.register(myFlushable);
        int size3 = registry.sizeCurrent();
        // Then
        assertThat(size1).isEqualTo(10);
        assertThat(size2).isEqualTo(6);
        assertThat(size3).isEqualTo(5);
        assertThat(myFlushable.flushIndex).isNull();
        for (int i = 0; 6 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.createIndex).isEqualTo(actual.flushIndex);
        }
        for (int i = 6; 10 > i; ++i) {
            final MyFlushable actual = flushableList.get(i);
            assertThat(actual.flushIndex).isNull();
        }
    }

    @Test
    public void test_that_register_last_flushable_again_do_not_change_the_state() {
        // Given
        for (int i = 0; 10 > i; ++i) {
            registry.register(flushableList.get(i));
        }
        // When
        int size1 = registry.register(flushableList.get(9));
        int size2 = registry.register(new MyFlushable(9));
        int size3 = registry.register(new MyFlushable(9));
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
            assertThat(actual.createIndex).isEqualTo(actual.flushIndex);
        }
    }

    class MyFlushable implements Flushable {

        final int createIndex;
        Integer flushIndex;

        MyFlushable(int index) {
            this.createIndex = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyFlushable that = (MyFlushable) o;

            return createIndex == that.createIndex;
        }

        @Override
        public void flush() {
            this.flushIndex = FlushableRegistryTest.this.flushIndex++;
        }

        @Override
        public int hashCode() {
            return createIndex;
        }
    }

}
