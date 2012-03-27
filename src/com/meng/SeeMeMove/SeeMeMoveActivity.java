package com.meng.SeeMeMove;

import com.meng.AccelerometerExample.R;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;
import android.widget.Toast;
 
/**
 * @author Richard Flanagan
 */

public class SeeMeMoveActivity extends Activity implements AccelerometerListener {
 
    private static Context CONTEXT;
	private static TextView statusText;
	
    private LocationManager locationManager;
	private String provider;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        CONTEXT = this;
        statusText = (TextView) findViewById(R.id.statusText);
        
		if(!haveNetworkConnection()) {
			statusText.setText("No Internet Connection");
		}
		else {
			statusText.setText("Started Capturing");
			if(AccelerometerManager.accIsSupported()) {
	            AccelerometerManager.startToListen(this);
	        }
		}
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	Criteria criteria = new Criteria();
    	criteria.setAccuracy(Criteria.ACCURACY_FINE);
    	criteria.setBearingRequired(true);
    	criteria.setCostAllowed(true);
    	criteria.setPowerRequirement(Criteria.POWER_LOW);
    	criteria.setAltitudeRequired(false);
    	
    	locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	locationManager.addGpsStatusListener(gpsStatusListener);
    	
    	provider = locationManager.getBestProvider(criteria, true);
    	locationManager.requestLocationUpdates(provider, 2, 10000, AccelerometerManager.locationListener);    	
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
    	Toast.makeText(SeeMeMoveActivity.CONTEXT, "DATA SENT | Average: " + intensity, 4000).show();    
    }
      
	/**
     * onAccelerationChanged callback
     */
    public void onAccelerationChanged(float x, float y, float z) {
        ((TextView) findViewById(R.id.statusText)).setText("X: " + String.valueOf(x) + 
        													"\nY: " + String.valueOf(y) + 
        													"\nZ: " + String.valueOf(z));
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
	
    GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {		
		@Override
		public void onGpsStatusChanged(int event) {	
		}
	}; 
}