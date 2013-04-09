package ca.langleys.spike.android.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

/**
 * BundleProvider implementation that retrieves Bundle content from the
 * Android application's assets repository.
 */
public class AndroidBundleProvider implements BundleProvider {

	private static final Logger log = LoggerFactory.getLogger(AndroidBundleProvider.class);
	
	/**
	 * Location of the bundles within the Android Asset directory.
	 */
	private static final String ASSET_DIRECTORY = "core-bundles";
	
	/**
	 * Android application context, providing access to the application assets
	 * and file system.
	 */
	private Context context;
	
	/**
	 * Bundles found within the application assets; safely cacheable as 
	 * they never change once the application is deployed.
	 */
	private ArrayList<String> bundles;
	
	public AndroidBundleProvider(Context context) {
		this.context = context;
		bundles = new ArrayList<String>();
		try {
			for (String name : context.getAssets().list(ASSET_DIRECTORY)) {
				log.debug("Found asset bundle: " + name);
				bundles.add(name);
			}
		}
		catch (IOException ioe) {
			log.error("Could not read asset directory " + ASSET_DIRECTORY, ioe);
		}
	}
	
	@Override
	public List<String> getNames() {
		return bundles;
	}

	@Override
	public InputStream getBundleStream(String name) throws IOException {
		File inFile = new File(ASSET_DIRECTORY, name);
		InputStream assetStream = context.getAssets().open(inFile.getPath());
		File outFile = getBundlePath(name);
		log.debug(String.format(
				"Extracting %s from assets to %s", inFile.getAbsolutePath(),
				outFile.getAbsolutePath()));
		FileOperations.streamToFile(assetStream, outFile);
		return new FileInputStream(outFile);
		
	}

	private File getBundlePath(String name) {
		return new File(context.getDir("dex", Context.MODE_PRIVATE), name);
	}
}
