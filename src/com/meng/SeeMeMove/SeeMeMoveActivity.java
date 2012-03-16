package com.meng.SeeMeMove;

import com.meng.AccelerometerExample.R;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
 
/**
 * @author Richard Flanagan
 */

public class SeeMeMoveActivity extends Activity implements AccelerometerListener {
 
    private static Context CONTEXT;
	private static TextView statusText;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        CONTEXT = this;
        statusText = (TextView) findViewById(R.id.statusText);
        
		//if(!haveNetworkConnection()) {
			statusText.setText("No Internet Connection");
		//}
		//else {
			statusText.setText("Started Capturing");
			captureState(0);
		//}
    }
 
    protected void onResume() {
        super.onResume();
    }
 
    protected void onDestroy() {
        super.onDestroy();
        if(AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
    }
 
    public static Context getContext() {
        return CONTEXT;
    }
    
    /**
     * onStopListening callback
     */
    public void onStopListening() {
    	statusText.setText("Stopped Capturing");
    }
 
    /**
     * postData callback
     */
    public void postResult(final String intensity) {        
    	// create new worker thread to perform server post request
        new Thread(new Runnable() {
            public void run() {	     	
            	new ConnectToServer(intensity);	
            }
        }).start();       
        Toast.makeText(SeeMeMoveActivity.CONTEXT, "DATA SENT | Average: " + intensity, 4000).show();
    }
      
	/**
     * onAccelerationChanged callback
     */
    public void onAccelerationChanged(float x, float y, float z, int samplingRate, float currentAverage) {
        ((TextView) findViewById(R.id.statusText)).setText("X: " + String.valueOf(x) + 
        													"\nY: " + String.valueOf(y) + 
        													"\nZ: " + String.valueOf(z) +         												
        													"\nSAMPLE RATE: " + String.valueOf(samplingRate) +
        													"\nRUNNING AVERAGE: " + String.valueOf(currentAverage));
    }
	
	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}
	
	private void captureState(int flag) {
		switch (flag) {
		case 0:
			if(AccelerometerManager.isSupported()) {
	            AccelerometerManager.startToListen(this);
	        }
			break;
		case 1:
			if(AccelerometerManager.isListening()) {
	            AccelerometerManager.stopListening();
	        }
	        break;
		} 
	}
}
