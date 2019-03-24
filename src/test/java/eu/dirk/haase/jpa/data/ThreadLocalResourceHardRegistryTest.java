package eu.dirk.haase.jpa.data;

import eu.dirk.haase.hibernate.jdbc.ThreadLocalResourceHardRegistry;
import eu.dirk.haase.hibernate.jdbc.ThreadLocalResourceRegistry;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class ThreadLocalResourceHardRegistryTest extends ThreadLocalResourceRegistryTest {

    @Before
    public void setUp() {
        this.registry = ThreadLocalResourceRegistry.newInstance(ThreadLocalResourceRegistry.RefType.HARD);
        this.count = 0;
    }

}
