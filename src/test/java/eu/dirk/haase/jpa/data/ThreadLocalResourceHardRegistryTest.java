package eu.dirk.haase.jpa.data;

import eu.dirk.haase.hibernate.jdbc.ThreadLocalResourceHardRegistry;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class ThreadLocalResourceHardRegistryTest extends ThreadLocalResourceRegistryTest {

    @Before
    public void setUp() {
        this.registry = new ThreadLocalResourceHardRegistry();
        this.count = 0;
    }

}
