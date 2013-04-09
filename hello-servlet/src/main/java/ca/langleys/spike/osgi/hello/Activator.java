package ca.langleys.spike.osgi.hello;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors the HttpService status and registers content availability as either the 
 * HttpService or this bundle stops and starts.
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer<HttpService, HttpService> {

	private static final Logger log = LoggerFactory.getLogger(Activator.class);
	
	private BundleContext bundleContext;
	private ServiceTracker<HttpService, HttpService> tracker;
	
	
	/**
	 * Start tracking the HttpService when this bundle starts.
	 */
	public void start(BundleContext context) throws Exception {
		log.debug("hello-servlet bundle is starting");
		bundleContext = context;
		tracker = new ServiceTracker<HttpService,HttpService>(context, HttpService.class.getName(), this);
		tracker.open();
	}

	/**
	 * Cancel HttpService tracking when the bundle is stopped.
	 */
	public void stop(BundleContext context) throws Exception {
		log.debug("hello-servlet bundle is stopping");
	    tracker.close();
	}

	
	/**
	 * Process ServiceTracker callbacks to register this module's servlets 
	 * with each HttpService that comes available.
	 */
	public HttpService addingService(ServiceReference<HttpService> reference) {
		HttpService httpService = bundleContext.getService(reference);
		mountContent(httpService);
		return httpService;
	}

	/**
	 * No action is required when the HttpService is modified.
	 */
	public void modifiedService(ServiceReference<HttpService> reference, HttpService service) {
	}

	/**
	 * Process ServiceTracker callbacks to unregister this module's servlets
	 * from HttpServices that are going away.
	 */
	public void removedService(ServiceReference<HttpService> reference, HttpService service) {
		HttpService httpService = bundleContext.getService(reference);
		dismountContent(httpService);
	}

	/**
	 * Register all servlets provided by this bundle for access
	 * via a HttpService.
	 * 
	 * @TODO For a production system it is important to handle
	 *       NamespaceExceptions otherwise we will silently
	 *       trash another bundle's registrations when we
	 *       unregister.
	 *        
	 * @param httpService Non-null HttpService to register with.
	 */
	private void mountContent(HttpService httpService) {
		try {
			log.debug("Registering /hello servlet with service " + httpService);
			httpService.registerServlet("/hello", new HelloServlet(), null, null);
			httpService.registerResources("/hello/assets", "/assets", null);
		}
		catch (Exception ex) {
			log.warn("Could not register servlets", ex);
		}
	}

	/**
	 * Remove servlet registrations from the stated HttpService.
	 * 
	 * @TODO For a production system, don't unregister an alias unless
	 *       we know it was registered by this bundle previously.
	 *       
	 * @param httpService HttpService to unregister. Must not be null.
	 */
	private void dismountContent(HttpService httpService) {
		log.debug("Unregistering /hello servlet");
		httpService.unregister("/hello");
		httpService.unregister("/hello/assets");
	}

}
