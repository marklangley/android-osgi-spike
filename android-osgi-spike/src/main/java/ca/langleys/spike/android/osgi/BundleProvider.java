package ca.langleys.spike.android.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Provides a list of OSGi bundles to be deployed and InputStreams
 * to read the bundle contents from.
 * 
 * Introduced as an experimental abstraction to keep Android specific
 * lookup of auto-deployed bundles out of the otherwise host-agnostic
 * OSGiFramework class.
 */
public interface BundleProvider {
	/**
	 * List the available bundles
	 * @return List of bundle names.
	 */
	List<String> getNames();
	
	/**
	 * Retrieve contents of the named bundle.
	 * @param name The bundle to retrieve, as previously returned by getNames()
	 * @return InputStream for reading the bundle contents.
	 * @throws IOException if the bundle cannot be opened.
	 */
	InputStream getBundleStream(String name) throws IOException;
}
