package com.meng.SeeMeMove;

/**
 * Android Accelerometer Sensor Manager Archetype
 * @author Richard Flanagan
 */

public interface AccelerometerListener {
	 
	public void onAccelerationChanged(float x, float y, float z);

	public void getSomeData(String intensity);
	
	public void onStopAccelerometerListening();
	
}