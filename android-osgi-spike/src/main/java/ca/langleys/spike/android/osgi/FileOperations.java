package ca.langleys.spike.android.osgi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.Environment;


/**
 * Utility methods for use on Android.
 */
public class FileOperations {

	private static final Logger log = LoggerFactory.getLogger(FileOperations.class);
	
	/**
	 * Copy contents of an InputStream to a filesystem location.
	 * 
	 * @TODO This is used in moving bundle content from the Android application's
	 *       assets to the internal storage space. It may not be required - perhaps
	 *       bundles can be deployed directly from assets without the intermediate
	 *       step?
	 *       
	 * @param input
	 * @param f
	 */
	public static void streamToFile(InputStream input, File f) {
		final int BUF_SIZE = 8 * 1024;
		byte buffer[] = new byte[BUF_SIZE];
		OutputStream out = null;
		try {
			f.getParentFile().mkdirs();
			BufferedInputStream bis = new BufferedInputStream(input);
			out = new BufferedOutputStream(new FileOutputStream(f), BUF_SIZE);
			while (true) {
				int bytesRead = bis.read(buffer);
				if (bytesRead == -1) break;
				out.write(buffer,  0,  bytesRead);
			} 
			out.close();
			bis.close();
		}
		catch (FileNotFoundException fnf) {
			log.error("Could not open output stream", fnf);
		}
		catch (IOException ioe) {
			log.error("Error writing to file", ioe);
		}
	}
	

	/**
	 * Useful in determining where stuff is stored for debugging purposes.
	 * Paths vary between Android 2.x and 4.x, and maybe by 
	 * manufacturer/model as well.
	 * 
	 * @param context Android application context
	 */
	public static void reportDirectoryPaths(Context context) {
        log.debug("SD Card: " + Environment.getExternalStorageDirectory());
        log.debug("SD Card state: " + Environment.getExternalStorageState());
        log.debug("getFilesDir(): " + context.getFilesDir().getAbsolutePath());
        log.debug("getCacheDir(): " + context.getCacheDir().getAbsolutePath());
        log.debug("getExternalFilesDir(): " + context.getExternalFilesDir(null));
	}
	
}
