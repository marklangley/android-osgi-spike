package ca.langleys.spike.android.osgi;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Callback object to receive progress updates from the OSGi
 * start process and update the on-screen UI.
 * 
 * @TODO The progress bar steps across nicely on Samsung Inspire,
 *       but not at all on Nexus 7.
 */
public class OSGiProgressCallback {

	private Logger log = LoggerFactory.getLogger(OSGiProgressCallback.class);
	
	private ProgressBar progressBar;
	int position;
	int maxSteps;
	
	public OSGiProgressCallback(ProgressBar progressBar, TextView status) {
		this.progressBar = progressBar;
	}
	
	public void reset(int startPosition, int maxSteps) {
		this.position = startPosition;
		this.maxSteps = maxSteps;
		ViewUpdateTask task = new ViewUpdateTask(progressBar, null, 
				startPosition, maxSteps, true, "");
		task.execute();
	}
	
	public void progress(String msg) {
		++position;
		ViewUpdateTask task = new ViewUpdateTask(progressBar, null,
				position, maxSteps, true, msg);
		task.execute();
	}
	
	public void complete() {
		ViewUpdateTask task = new ViewUpdateTask(progressBar, null,
				maxSteps, maxSteps, false, "");
		task.execute();
	}
	
	private class ViewUpdateTask extends AsyncTask<Void, Void, Void> {

		private int position;
		private int max;
		private boolean enabled;
		private String msg;
		private ProgressBar progressBar;
		private TextView status;
		
		public ViewUpdateTask(ProgressBar progressBar, TextView status, int position, int max, boolean enabled, String msg) {
			this.progressBar = progressBar;
			this.status = status;
			this.position = position;
			this.max = max;
			this.enabled = enabled;
			this.msg = msg;
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			return null;
		}
		
		@Override
		protected void onPostExecute(Void arg0) {
			String newStatus = String.format(Locale.US, "%s (%d of %d)", msg, position, maxSteps);
			log.debug("Update progress: " + newStatus);
			if (progressBar != null) {
				if (enabled) {
					progressBar.setMax(max);
					progressBar.setProgress(position);
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setEnabled(true);
				}
			}
			if (status != null) {
				if (enabled) {
					status.setText(newStatus);
				}
				else {
					status.setText("");
				}
			}
		}
	}
}
