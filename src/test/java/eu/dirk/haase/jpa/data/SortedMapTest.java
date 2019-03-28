package eu.dirk.haase.jpa.data;

import eu.dirk.haase.hibernate.Flushable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.SortedMap;
import java.util.TreeMap;

@RunWith(BlockJUnit4ClassRunner.class)
public class SortedMapTest {


    @Test
    public void test() {
        final SortedMap<Integer, Object> flushableMap = new TreeMap<>();
        for(int i=0; 10>i;++i) {
            flushableMap.put(i, "");
        }

        System.out.println(flushableMap.headMap(5));
    }

}
