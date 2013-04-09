package ca.langleys.spike.android.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleBundleProvider implements BundleProvider {

	private static final Logger log = LoggerFactory.getLogger(SimpleBundleProvider.class);
	private static final File SOURCE_DIR = new File("c:/temp/osgi/shipped");
	private ArrayList<String> bundles;
	
	public SimpleBundleProvider() {
		bundles = new ArrayList<String>();
			for (File f : SOURCE_DIR.listFiles()) {
				if (f.isFile() && f.getName().endsWith(".jar")) {
					log.debug(f.getName() + " recognized for auto-deployment");
					bundles.add(f.getName());
				}
			}
	}

	
	@Override
	public List<String> getNames() {
		return bundles;
	}

	@Override
	public InputStream getBundleStream(String name) throws IOException {
		File f = new File(SOURCE_DIR, name);
		FileInputStream stream = new FileInputStream(f);
		return stream;
	}

}
