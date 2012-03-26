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
        SMMT = new SeeMeMoveTools();
        // Set parameters of the SeeMeMoveTools object
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
        	// Add raw accelerometer value to SMM magnitude object
        	SMMT.addValue(event.values[0], event.values[1], event.values[2], event.timestamp);
        	 
        	//magnitude.addValue((float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2)), event.timestamp);   	                              
         
        	listener.onAccelerationChanged(event.values[0], event.values[1], event.values[2]);
        }
    }; 
    
}