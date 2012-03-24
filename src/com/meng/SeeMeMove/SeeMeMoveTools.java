package com.meng.SeeMeMove;

import java.util.ArrayList;
import java.util.zip.DataFormatException;
import android.util.Log;

/**
 * @author Richard Flanagan
 */

public class SeeMeMoveTools 
{
	// Number of nanoseconds in a second
	private static final long SECOND_TO_NANO = 1000000000;	
	// Contains raw sensor values 
	private ArrayList<Float> values;
	// Contains corresponding time stamp
	private ArrayList<Long> timevalues;
	
	private int sampleRate;
	private float avgValue;
		
    /**
     * Constructor
     */
	public SeeMeMoveTools()
	{
		this.values = new ArrayList<Float>();
		this.timevalues = new ArrayList<Long>();
	}
	
	public SeeMeMoveTools(int sampleRate)
	{
		this.values = new ArrayList<Float>();
		this.timevalues = new ArrayList<Long>();
		this.sampleRate = sampleRate;
	}
	
	public void clear() {
		this.values.clear();
		this.timevalues.clear();
	}
	
    /**
     * Add value with corresponding timestamp
     * @param value
     *             uninterpolated data value
     * @param timevalue
     *             corresponding time value for data
     */
	public void addValue(float value, long timevalue) {
		values.add(new Float(value));
		timevalues.add(new Long(timevalue));
	}
	
	public void setTimeArray(ArrayList<Long> timevalues) {
		this.timevalues = timevalues;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	public int getValueArraySize() {
		int size = values.size();
		return size;
	}
	
	public float getAverage() throws IllegalAccessException {
		if(values.isEmpty() == true)
			throw new IllegalAccessException("No Values have been added to this object");
		return this.avgValue;
	}
	
	public float getValue(long time) {
		if(time < timevalues.get(0))
			throw new IndexOutOfBoundsException("Requested value is before start time");
		if(time > timevalues.get(timevalues.size()-1))
			throw new IndexOutOfBoundsException("Requested value is out of bounds");

		float retval=0;
		for(int i=0;i<timevalues.size()-1;i++) {
			Long firsttimeval=timevalues.get(i);
			Long secondtimeval=timevalues.get(i+1);
			if(time>=firsttimeval&&time<=secondtimeval) {
				float firstval= values.get(i);
				float secondval= values.get(i+1);
				if(secondval>=firstval) {
					retval = ((time-firsttimeval)*(secondval-firstval)/(secondtimeval-firsttimeval))+firstval;
				}
				else {
					retval = ((secondtimeval-time)*((firstval-secondval)/(secondtimeval-firsttimeval)))+secondval;
				}
                return retval;
			}
		}
		return retval;
	}
	
	public ArrayList<Float> interpolateData(int sampleRate, boolean smooth) throws DataFormatException {
		// Error Checking
		if(sampleRate == 0)
			throw new NullPointerException("No Sample rate set: Set in constructor or use method");
		if(values.size() == 0)
			throw new NullPointerException("No data in array to interpolate");
		if((SECOND_TO_NANO/sampleRate) > (timevalues.get(timevalues.size()-1)-timevalues.get(0)))
			throw new DataFormatException("Sampling rate is greater then data range");
		
		ArrayList<Float> interpolatedData = new ArrayList<Float>() ;		
		long time = timevalues.get(0);		
		
		while(time < timevalues.get(timevalues.size()-1)) {
			interpolatedData.add(getValue(time));
			time += (SECOND_TO_NANO/sampleRate);
		}
		if(smooth == true)
			interpolatedData = lowPassFilter(interpolatedData);
		return interpolatedData;
	}
	
	public void interpolateData(boolean smooth) throws DataFormatException {
		// Error Checking
		if(this.sampleRate == 0)
			throw new NullPointerException("No Sample rate set: Set in constructor or use method");
		if(values.size() == 0)
			throw new NullPointerException("No data in array to interpolate");
		if((SECOND_TO_NANO/sampleRate) > (timevalues.get(timevalues.size()-1)-timevalues.get(0)))
			throw new DataFormatException("Sampling rate is greater then data range");
		
		ArrayList<Float> interpolatedData = new ArrayList<Float>() ;		
		long time = timevalues.get(0);		
	
		while(time < timevalues.get(timevalues.size()-1)) {
			interpolatedData.add(getValue(time));
			time += (SECOND_TO_NANO/this.sampleRate);
		}	
		// Smooth if flag is true
		if(smooth == true)
			interpolatedData = lowPassFilter(interpolatedData);

		//Calculate average value in array
		calculateAverage(interpolatedData);
	}
	
	private ArrayList<Float> lowPassFilter(ArrayList<Float> notSmooth) {
		ArrayList<Float> smooth = new ArrayList<Float>() ;
		float value = notSmooth.get(0);
		for(int i = 1 ; i < notSmooth.size() ; i++) {
		    float currentValue = notSmooth.get(i);
		    value += (currentValue - value) / 20;
		    smooth.add(value);
		}
		return smooth;
	}
	
	private void calculateAverage(ArrayList<Float> values) {
		float average = 0;
		for(int i = 0 ; i < values.size() ; i++) {
			average += values.get(i);
		}
		average = average/values.size();
		this.avgValue = average;
	}
}	