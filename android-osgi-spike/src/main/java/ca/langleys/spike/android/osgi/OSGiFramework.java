package ca.langleys.spike.android.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates instantiation and launch of the OSGi framework.
 * 
 * As of OSGi 4.2 the framework itself can be swapped in and out at launch time.
 * Multiple framework providers (framework factories) can be present, 
 * * with META-INF/services manifest
 * entries used to announce their presence at class loading time.
 * 
 * This implementation uses the java.util.ServiceLoader to obtain the available
 * FrameworkFactory implementations, then chooses the first one on the list.
 *
 * This code is adapted from examples provided at 
 * http://felix.apache.org/site/apache-felix-framework-launching-and-embedding.html
 * 
 * It has not been tested with FrameworkFactory implementations other than Felix.
 */
public class OSGiFramework {
	
	private static final Logger log = LoggerFactory.getLogger(OSGiFramework.class);
	
	private Framework m_framework;
	private BundleProvider autodeploys;
	
	private File cacheDirectory;
	private File bundleDirectory;

	/**
	 * Instantiate the framework.
	 * 
	 * @param storageDirectory Base directory for file storage. Deployed bundles
	 *                         and the OSGi cache will be placed in subdirectories.
	 * @param autodeploys      Provider of bundles to be auto-deployed/auto-updated.
	 */
	public OSGiFramework(File storageDirectory, BundleProvider autodeploys) {
		cacheDirectory = new File(storageDirectory, "cache");
		bundleDirectory = new File(storageDirectory, "bundles");
		if (! (cacheDirectory.mkdirs() && bundleDirectory.mkdirs())) {
			log.error("Could not create cache or bundle directory");
		}
		this.autodeploys = autodeploys;
	}
	
	public synchronized boolean isRunning() {
		return (m_framework != null);
	}
	
	/**
	 * Get the system BundleContext.
	 * 
	 * The system BundleContext provides the host application access to the OSGI
	 * environment.
	 * 
	 * @return the system bundle context of the running OSGi framework,
	 * or null if the framework is not running.
	 */
	public synchronized BundleContext getSystemContext() {
		return isRunning() ? m_framework.getBundleContext() : null;
	}
	
	public synchronized Framework start(OSGiProgressCallback callback) throws Exception {
		callback.reset(0, 3 + 2 * autodeploys.getNames().size());
		if (m_framework == null) {
			callback.progress("Instantiating framework");
			m_framework = instantiate();
			log.debug("Initializing OSGi framework");
			callback.progress("Initializing framework");
			m_framework.init();
			autoDeployDefaultBundles(callback);
			log.debug("Starting OSGi framework");
			callback.progress("Starting framework");
			m_framework.start();
			callback.complete();
		}
		return m_framework;
	}
	
	public synchronized void stop() throws BundleException {
		if (m_framework == null) {
			return;
		}
		try {
			log.debug("Stopping OSGi framework");
			m_framework.stop();
		}
		finally {
		    m_framework = null;
		}
	}
	
	
	/**
	 * Instantiate the configured framework.
	 */
	private synchronized Framework instantiate() throws Exception {
		log.debug("Instantiating OSGi Framework");
		FrameworkFactory factory = getFrameworkFactory();
		Map<String,String> config = getFrameworkConfig(factory);
		Framework framework = factory.newFramework(config);
		return framework;
	}
	
	/**
	 * Populate configuration settings suitable for use with the specified
	 * FrameworkFactory implementation.
	 * 
	 * Assumes that Apache Felix is in use, and uses 
	 * http://felix.apache.org/site/apache-felix-framework-configuration-properties.html
	 * as the reference for appropriate properties.
	 * 
	 * @param factory
	 *            the FrameworkFactory in use
	 * @return Map of suitable configuration strings.
	 * @throws IllegalArgumentException
	 *             if the framework factory is not recognized.
	 */
	private Map<String, String> getFrameworkConfig(FrameworkFactory factory)
			throws IllegalArgumentException {
		String factoryName = factory.getClass().getName();
		if (! "org.apache.felix.framework.FrameworkFactory".equals(factoryName)) {
			throw new IllegalArgumentException("Don't know how to configure " + factoryName);
		}
		
		
		HashMap<String,String> map = new HashMap<String,String>();
		
		map.put("org.osgi.framework.storage", cacheDirectory.getAbsolutePath());
		map.put("org.osgi.framework.storage.clean", "onFirstInit");
		map.put("felix.cache.locking",  "false");

		// Implicit boot delegation must be turned off to work around 
		// SecurityManagerEx.getClassContext() returning null on Android 4.x,
		// which then triggers a NullPointerException when Felix attempts to 
		// load the jetty bundle.
		//
		// See http://www.mail-archive.com/users@felix.apache.org/msg11397.html
		// for more discussion.
		map.put("felix.bootdelegation.implicit", "false");
		map.put("felix.log.level",  "4");

		// 
		// Specify packages that will be provided to the installed bundles 
		// by the host.
		//
		// SLF4J is included since we have already linked it into the framework;
		// it may as well be available to the bundles as well.
		//
		String extraPackages = "org.slf4j; version=1.7.2";
		map.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, extraPackages);
		return map;
	}
	
	/**
	 * Automatically deploy pre-determined, always-present bundles.
	 * This is a simplified version of the reference code in
	 * org.apache.felix.main.AutoProcessor.processAutoDeploy().
	 */
	private void autoDeployDefaultBundles(OSGiProgressCallback callback) {
		
		log.debug("Auto-deploying default bundles");
		BundleContext context = m_framework.getBundleContext();

		int startLevel = getStartLevel(context);
		
		// Get list of already installed bundles as a map.
		Map<String, Bundle> installedBundleMap = getInstalledBundles(context);

		// get the bundles to be deployed
		List<String> names = autodeploys.getNames();

		// Install bundle JAR files and remember the bundle objects.
		final List<Bundle> startBundleList = new ArrayList<Bundle>();
		for (String name : names) {
			try {
				log.debug("Starting deployment of " + name);
				callback.progress("Deploying " + name);
				// Look up the bundle by location, removing it from
				// the map of installed bundles so the remaining bundles
				// indicate which bundles may need to be uninstalled.
				InputStream input = autodeploys.getBundleStream(name);
				Bundle b = installedBundleMap.remove(name);

				// If the bundle is not already installed, then install it
				// if the 'install' action is present.
				if (b == null) {
					log.debug("Installing bundle " + name + " into context");
					b = context.installBundle(name, input);
				}
				else {
					log.debug("Bundle " + name + " was previously installed, updating it");
					b.update();
				}
				
				// If we have found and/or successfully installed a bundle,
				// then add it to the list of bundles to potentially start
				// and also set its start level accordingly.
				if ((b != null) && !isFragment(b)) {
					log.debug("Setting start level for bundle " + name);
					startBundleList.add(b);
					BundleStartLevel bs = (BundleStartLevel) b.adapt(BundleStartLevel.class);
					bs.setStartLevel(startLevel);
				}
			}
			catch (IOException ioe) {
				log.error("Could not access asset directory", ioe);
			}
			catch (BundleException ex) {
				log.error("Auto-deploy install failed", ex);
			}
		}

		// Start all installed and/or updated bundles
		for (Bundle b : startBundleList) {
			try {
				callback.progress("Starting " + b.getSymbolicName());
				log.debug("Starting bundle " + b.getLocation());
				b.start();
			} 
			catch (BundleException ex) {
				log.error("Auto-deploy start: ", ex);
			}
		}
	}	
	
	private Map<String, Bundle> getInstalledBundles(BundleContext context) {
		log.debug("Looking for previously deployed bundles");
		Map<String, Bundle> map = new HashMap<String, Bundle>();
		Bundle[] bundles = context.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			String location = bundles[i].getLocation();
			log.debug("Found previously deployed bundle: " + location);
			map.put(location, bundles[i]);
		}
		return map;
	}
	
	/**
	 * Ask the framework for the default run level for newly installed bundles.
	 * @param context the system context
	 * @return the start level
	 */
	private int getStartLevel(BundleContext context) {
		FrameworkStartLevel sl = (FrameworkStartLevel) m_framework.adapt(FrameworkStartLevel.class);
		int level = sl.getInitialBundleStartLevel();
		log.debug("Framework's initial bundle start level is " + level);
		return level;
	}
	
	private static boolean isFragment(Bundle bundle) {
		return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
	}	
	
	/**
	 * Use the ServiceLoader approach to obtaining an OSGi FrameworkFactory.
	 * 
	 * This is the "correct" way to obtain a FrameworkFactory, but it doesn't
	 * work on Android because the APK assembler refuses to copy resources
	 * in a META-INF folder. So there is no way to create the META-INF/services
	 * entry specifying the class name. 
	 * 
	 * It has been preserved as it is still the "correct" approach for 
	 * traditional systems. Perhaps the real SpotLight v5 implementation can
	 * detect the platform it is running on and switch between lookup methods.
	 * 
	 * Or maybe we'll be perfectly happy with Apache Felix, and there will be
	 * no need to select a provider at run time. Time will tell...
	 */
	static FrameworkFactory getFrameworkFactoryViaServiceLoader() {
		ServiceLoader<FrameworkFactory> loader  = ServiceLoader.load(FrameworkFactory.class);
		Iterator<FrameworkFactory> factories = loader.iterator();
		return factories.next();
	}

	
	static FrameworkFactory getFrameworkFactory() throws Exception
    {
		return new org.apache.felix.framework.FrameworkFactory();
    }
	
	public  String reportStatus() {
		if (! isRunning()) {
			return "Not running";
		}
		String result = "";
		Bundle[] bundles = m_framework.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			result = result + String.format("%s: %s\n", 
					bundle.getSymbolicName(), 
					describeState(bundle.getState()));
		}
		return result;
	}
	
	private String describeState(int bundleState) {
		switch (bundleState) {
		case Bundle.ACTIVE: return "ACTIVE";
		case Bundle.INSTALLED: return "INSTALLED";
		case Bundle.RESOLVED: return "RESOLVED";
		case Bundle.STARTING: return "STARTING";
		case Bundle.STOPPING: return "STOPPING";
		case Bundle.UNINSTALLED: return "UNINSTALLED";
		default: return "Unknown";
		}
	}
}
