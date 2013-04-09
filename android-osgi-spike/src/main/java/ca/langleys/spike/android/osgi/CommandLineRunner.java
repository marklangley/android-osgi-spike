package ca.langleys.spike.android.osgi;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console app for running the OSGi framework demo on a traditional 
 * Java virtual machine.
 * 
 * @author mlangley
 *
 */
public class CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(CommandLineRunner.class);
	private OSGiFramework osgi;

	public static void main(String args[]) {
		log.debug("Starting up...");
		CommandLineRunner runner = new CommandLineRunner();
		runner.run();
	}
	
	private void run() {
        initOSGi();
        startOSGi();
        while(true) {
        	log.debug("Still sleeping");
        	try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}

    private void initOSGi() {
    	File baseDir = new File("c:/temp/osgi");
        osgi = new OSGiFramework(baseDir, new SimpleBundleProvider());
    }

    private void startOSGi() {
    	try {
    		osgi.start(new OSGiProgressCallback(null, null));
    		String status = osgi.reportStatus();
    		log.info("OSGi should be started now!\n" + status);
    	}
    	catch(Exception e) {
    		log.error("Exception", e);
    	}
    }
    
}
