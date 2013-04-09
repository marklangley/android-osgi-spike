package ca.langleys.spike.android.osgi;

import static org.junit.Assert.*;

import org.junit.Test;
import org.osgi.framework.launch.FrameworkFactory;

public class OSGiFrameworkTest {

	@Test
	public void testGetFrameworkFactory() throws Exception {
		FrameworkFactory factory = OSGiFramework.getFrameworkFactory();
		assertNotNull(factory);
	}

}
