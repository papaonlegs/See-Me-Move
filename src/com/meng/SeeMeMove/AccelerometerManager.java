package com.meng.SeeMeMove;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
 
/**
 * Android Accelerometer Sensor Manager Archetype
 * @author Richard Flanagan
 */

public class AccelerometerManager {

    private static Sensor sensor;
    private static SensorManager sensorManager;
    private static AccelerometerListener listener;
    
	private static SeeMeMoveTools magnitude;
    private static ArrayList<SeeMeMoveTools> magnitudes = new ArrayList<SeeMeMoveTools>();
   
    private static double TENSECONDSINNANO = 10000000000d;
    private static int sampleRate = 140;

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
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {}
        listener.onStopListening();
    }
 
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public static boolean isSupported() {
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
        magnitude = new SeeMeMoveTools(sampleRate);
        magnitudes.clear();
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

        float startTime = 0;
        float timeDiff = 0;
        float averg;
    	
    	@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
        @Override
		public void onSensorChanged(SensorEvent event) {                         	        	
        	if(startTime == 0)
        		startTime = event.timestamp;       	
        	timeDiff = event.timestamp - startTime;
        	      	
        	if(timeDiff >= TENSECONDSINNANO) {	     
    			try {
    				magnitudes.add(magnitude);	
    				magnitude.interpolateData(true);           				
    			} catch (DataFormatException e) {}            			
       		
        		if(magnitudes.size() >= 1) {
        			try {
        				averg = magnitudes.get(magnitudes.size()-1).getAverage();
        				callPost(Float.toString(averg));
        			}catch (IllegalAccessException e) {e.printStackTrace();}
        		}
        		magnitude = new SeeMeMoveTools(sampleRate);
        		startTime = event.timestamp;
        	}
        	// Add raw accelerometer value to SMM magnitude object 
        	magnitude.addValue((float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2)), event.timestamp);   	                              
         
        	listener.onAccelerationChanged(event.values[0], event.values[1], event.values[2], sampleRate, averg);
        }
    }; 
    
    public static void callPost(String postMessage) {
        listener.postResult(postMessage);
    }
}