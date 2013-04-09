package ca.langleys.spike.android.osgi;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Android application screen, providing widgets to start/stop the OSGI container
 * and launch the deployed UIs.
 */
public class HelloAndroidActivity extends Activity {

	private static final Logger log = LoggerFactory.getLogger(HelloAndroidActivity.class);
	
    private static final String TAG = "android-osgi-spike";
    
    private OSGiFramework osgi;
    
    private TextView statusView;
    private ProgressBar progress;
    private CheckBox osgiSwitch;
    
    
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// standard Android initialization
        super.onCreate(savedInstanceState);
        log.info(TAG, "onCreate");
        setContentView(R.layout.main);
        
        // Report file system characteristics to the log. Not strictly needed,
        // but occasionally useful to have.
        FileOperations.reportDirectoryPaths(this);
    	
        initOSGi();
    	statusView = (TextView) findViewById(R.id.status);
    	progress = (ProgressBar) findViewById(R.id.progress);
    	osgiSwitch = (CheckBox) findViewById(R.id.osgiSwitch);
    }
    
    private void initOSGi() {
        // saving bundles on external storage might be useful for debugging, and
        // works on Android 2.x. It crashes on Android 4+ as it refuses to 
        // write/load dex files from an unsecured filesystem. A nice helpful
        // exception is reported by Android and promptly silently swallowed by
        // Felix.
        //
        //File baseDir = new File(getExternalFilesDir(null), "felix");
    	
    	// The application cache directory is the correct storage location,
    	// but is difficult to examine.
        File baseDir = new File(getCacheDir(), "felix");
        osgi = new OSGiFramework(baseDir, new AndroidBundleProvider(this));
    }
    
    /**
     * Launch the browser to the designated app home page.
     * 
     * @TODO Always launches a new browser tab. Reloading an existing tab
     *       might be preferable.
     */
    public void startUI(View view) {
    	String page = (view.getId() == R.id.uiConsole) ?
    			"/system/console" : "/hello/";
    	Uri uri = Uri.parse("http://localhost:8080" + page);
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);    	
    }
    
    /**
     * Start or stop the OSGi framework depending on the checkbox state.
     * @param view
     */
    public void toggleFramework(View view) {
    	if (osgiSwitch.isChecked()) {
    		startOSGi();
    	}
    	else {
    		stopOSGi();
    	}
    }

	public void startOSGi() {
		(new StartOSGiTask()).execute();
	}
    
    public void stopOSGi() {
    	try {
    		osgi.stop();
    		showResults("OSGi should be stopped now.");
    	}
    	catch (Exception e) {
    		showResults(e.getMessage());
    	}
    }
    
    private void showResults(String text) {
    	statusView.setText(text);
    }
    
    private class StartOSGiTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			try {
				osgi.start(new OSGiProgressCallback(progress, statusView));
				return osgi.reportStatus();
			} catch (Exception e) {
				return e.getMessage();
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			showResults(result);
		}
    }
}

