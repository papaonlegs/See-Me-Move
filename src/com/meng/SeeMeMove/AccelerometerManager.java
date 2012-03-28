package com.meng.SeeMeMove;

import java.util.List;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
 
/**
 * Android Accelerometer Sensor Manager Archetype
 * @author Richard Flanagan
 */

public class AccelerometerManager {

    private static Sensor sensor;
    private static SensorManager sensorManager;
    private static AccelerometerListener listener;
    
    // GPS data store
    private static double lattitude;
    private static double longitude;
    private static boolean gpsFix = false;
    
	// Create SeeMeMoveTools object
    private static SeeMeMoveTools SMMT;

    /** indicates whether or not Accelerometer Sensor is supported */
    private static Boolean supported;
    /** indicates whether or not Accelerometer Sensor is running */
    private static boolean running = false;
    
    /**
     * Returns true if the manager is listening to orientation changes
     */
    public static boolean isListening() {
        return running;
    }
 
    /**
     * Unregisters listeners
     */
    public static void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null)
                sensorManager.unregisterListener(sensorEventListener);
        } catch (Exception e) {}
        listener.onStopAccelerometerListening();
    }
 
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public static boolean accIsSupported() {
        if (supported == null) {
            if (SeeMeMoveActivity.getContext() != null) {
                sensorManager = (SensorManager) SeeMeMoveActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                supported = new Boolean(sensors.size() > 0);
            } 
            else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }
 
    /**
     * Configure the listener for shaking
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void configure(int threshold, int interval) {
    }
 
    /**
     * Registers listener and starts listening
     * @param accelerometerListener
     *             callback for accelerometer events
     */
    public static void startToListen(AccelerometerListener accelerometerListener) {
        sensorManager = (SensorManager) SeeMeMoveActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sensor = sensors.get(0);
            running = sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
            listener = accelerometerListener;
        }
        // Instantiate a new SeeMeMoveTools object
        SMMT = new SeeMeMoveTools();
        // Set parameters of the SeeMeMoveTools object
        SMMT.setSmooth(true);
        SMMT.setSampleRate(100);
        SMMT.setWindow(10000);
    }
 
    /**
     * Configures threshold and interval
     * And registers a listener and starts listening
     * @param accelerometerListener
     *             callback for accelerometer events
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between two shake events
     */
    public static void startListening(AccelerometerListener accelerometerListener, int threshold, int interval) {
        configure(threshold, interval);
        startToListen(accelerometerListener);
    }
 
    /**
     * The listener that listen to events from the accelerometer listener
     */
    private static SensorEventListener sensorEventListener = new SensorEventListener() {
    	@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        @Override
		public void onSensorChanged(SensorEvent event) {                         	        	  	
        	// Add raw accelerometer value to SeeMeMoveTools object
        	SMMT.addValue(event.values[0], event.values[1], event.values[2], event.timestamp);      	
        }
    }; 
    
    public static void updateLocation(Location location) {
		lattitude = location.getLatitude();
		longitude = location.getLongitude();
		SMMT.setGPS(location.getLatitude(), location.getLongitude());
    }
    
    public static LocationListener locationListener = new LocationListener() {
    	@Override
    	public void onProviderDisabled(String provider) {		
    	}  	
    	@Override
    	public void onProviderEnabled(String provider) {		
    	}   	
    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras) {	 
    	}   	
    	@Override
    	public void onLocationChanged(Location location) {
    		gpsFix = true;
    		updateLocation(location);    	
    	}
    };
      
}