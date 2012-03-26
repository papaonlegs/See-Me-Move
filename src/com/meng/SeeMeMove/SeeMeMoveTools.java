package com.meng.SeeMeMove;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Richard Flanagan
 */

public class SeeMeMoveTools 
{
    // FOR TESTING
	private static AccelerometerListener listener;
	
	// Parameter Variables THESE MUST BE SET!!
	private int sampleRate = 60; // desired sample rate which is set when instantiating the object or defined after
	private int window = 10000; // in milliseconds default set to 10 seconds
	private boolean smooth = false; 
	private int smoothOrder = 20; // Order of lowpass filter
	
	// Time Variables 
	private static final long NANO_IN_SECOND = 1000000000; // Number of nanoseconds in a second
	private static final double NANO_IN_MILISECOND = 1000000d; // number of nanoseconds in 1 millisecond
    private double windowInNano; // length of window in nanoseconds calculated by constructor or set by user
    private float startTime = 0;
    private float timeDiff = 0;	
	
	// Contains corresponding time stamp
	private ArrayList<Long> timevalues;
	// Array index values of raw samples per window 
	private ArrayList<Integer> rawWindowIndexs; 
	
	// Raw sensor values 
	private ArrayList<Float> xRawValues;
	private ArrayList<Float> yRawValues;
	private ArrayList<Float> zRawValues;
	
	// Window of raw sensor values pre-interpolation 
	private ArrayList<Float> rawWindowValues;
	private ArrayList<Long> timevaluesWindow;
	
	// Interpolated sensor values 
	private ArrayList<Float> xInterValues;
	private ArrayList<Float> yInterValues;
	private ArrayList<Float> zInterValues;
	
	private float avgValue = 0;
		
    /**
     * Constructor
     */
	public SeeMeMoveTools() {
		this.xRawValues = new ArrayList<Float>();
		this.yRawValues = new ArrayList<Float>();
		this.zRawValues = new ArrayList<Float>();
		this.xInterValues = new ArrayList<Float>();
		this.yInterValues = new ArrayList<Float>();
		this.zInterValues = new ArrayList<Float>();
		this.timevalues = new ArrayList<Long>();
		
		this.rawWindowIndexs = new ArrayList<Integer>();
		this.rawWindowIndexs.add(0);
		
		windowInNano = NANO_IN_MILISECOND * window;
	}
	
	public SeeMeMoveTools(int sampleRate) {
		this.sampleRate = sampleRate;
		
		this.xRawValues = new ArrayList<Float>();
		this.yRawValues = new ArrayList<Float>();
		this.zRawValues = new ArrayList<Float>();
		this.zRawValues = new ArrayList<Float>();
		this.xInterValues = new ArrayList<Float>();
		this.yInterValues = new ArrayList<Float>();
		this.zInterValues = new ArrayList<Float>();
		this.timevalues = new ArrayList<Long>();
		
		this.rawWindowIndexs = new ArrayList<Integer>();
		this.rawWindowIndexs.add(0);
		
		windowInNano = NANO_IN_MILISECOND * window;
	}
	
	public void clear() {
		this.xRawValues.clear();
		this.yRawValues.clear();
		this.zRawValues.clear();
		this.timevalues.clear();
	}
	
    /**
     * Add value with corresponding timestamp
     * @param value
     *             uninterpolated data value
     * @param timevalue
     *             corresponding time value for data
     */
	public void addValue(float xValue, float yValue, float zValue, long timevalue) {
		//TODO Add checks to see if all parameters have been set
				
		xRawValues.add(xValue);
		yRawValues.add(yValue);
		zRawValues.add(zValue);
		timevalues.add(timevalue);
		
    	if(this.startTime == 0)
    		this.startTime = timevalue;       	
    	this.timeDiff = timevalue - this.startTime;
    	
    	if(this.timeDiff >= windowInNano) {	 
    		rawWindowIndexs.add(xRawValues.size());
            new Thread(new Runnable() {
                public void run() {	     	
                	// TODO call signal processing methods
            		timevaluesWindow = new ArrayList<Long>();
            		timevaluesWindow = (ArrayList<Long>) timevalues.subList(rawWindowIndexs.get(rawWindowIndexs.size()-2), rawWindowIndexs.get(rawWindowIndexs.size()-1));
                	try {               		
                    	xInterValues.addAll(interpolateData((ArrayList<Float>)xRawValues.subList(rawWindowIndexs.get(rawWindowIndexs.size()-2), rawWindowIndexs.get(rawWindowIndexs.size()-1)), timevaluesWindow));
                    	yInterValues.addAll(interpolateData((ArrayList<Float>)yRawValues.subList(rawWindowIndexs.get(rawWindowIndexs.size()-2), rawWindowIndexs.get(rawWindowIndexs.size()-1)), timevaluesWindow));
						zInterValues.addAll(interpolateData((ArrayList<Float>)zRawValues.subList(rawWindowIndexs.get(rawWindowIndexs.size()-2), rawWindowIndexs.get(rawWindowIndexs.size()-1)), timevaluesWindow));
					} catch (DataFormatException e) {e.printStackTrace();}
                	
                	// TODO server stuff
                	String postMessage = Float.toString(avgValue);
                	new ConnectToServer(postMessage);
                	// FOR TESTING 
                	listener.postResult(postMessage);
                }
            }).start(); 
            this.startTime = timevalue;
    	}
	}	
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	public void setWindow(int window) {
		this.window = window;
		this.windowInNano = window * NANO_IN_MILISECOND;
	}
	
	public float getAverage() throws IllegalAccessException {
		//if(values.isEmpty() == true)
		//	throw new IllegalAccessException("No Values have been added to this object");
		return this.avgValue;
	}
	
	public float getValue(long time) {
		// Error Checking
		if(time < timevalues.get(0))
			throw new IndexOutOfBoundsException("Requested value is before start time");
		if(time > timevalues.get(timevalues.size()-1))
			throw new IndexOutOfBoundsException("Requested value is out of bounds");
				
		float retval = 0;
		for(int i = rawWindowIndexs.get(rawWindowIndexs.size()-2) ; i < rawWindowIndexs.get(rawWindowIndexs.size()-1) ; i++) {
			Long firsttimeval = timevalues.get(i);
			Long secondtimeval = timevalues.get(i+1);
			if(time >= firsttimeval && time <= secondtimeval) {
				float firstval = this.rawWindowValues.get(i);
				float secondval = this.rawWindowValues.get(i+1);
				if(secondval >= firstval) {
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
	
	public ArrayList<Float> interpolateData(ArrayList<Float> rawData, ArrayList<Long> rawTime) throws DataFormatException {
		// Error Checking
		if(sampleRate == 0)
			throw new NullPointerException("No Sample rate set: Set in constructor or use method");
		if(xRawValues.size() == 0)
			throw new NullPointerException("No data in array to interpolate");
		if((NANO_IN_SECOND/sampleRate) > (timevalues.get(rawWindowIndexs.get(rawWindowIndexs.size()-2))-timevalues.get(rawWindowIndexs.get(rawWindowIndexs.size()-2)+1)))
			throw new DataFormatException("Sampling rate is greater then data range");
		
		this.rawWindowValues = new ArrayList<Float>(rawData);
		ArrayList<Float> interpolatedData = new ArrayList<Float>() ;		
		long time = rawTime.get(0);		
		
		while(time < rawTime.get(rawTime.size()-1)) {
			interpolatedData.add(getValue(time));
			time += (NANO_IN_SECOND/sampleRate);
		}
		// Smooth data if required 
		if(smooth == true)
			interpolatedData = lowPassFilter(interpolatedData);
		// Return Data
		return interpolatedData;
	}
	
	private ArrayList<Float> lowPassFilter(ArrayList<Float> notSmooth) {
		ArrayList<Float> smooth = new ArrayList<Float>() ;
		float value = notSmooth.get(0);
		for(int i = 1 ; i < notSmooth.size() ; i++) {
		    float currentValue = notSmooth.get(i);
		    value += (currentValue - value) / smoothOrder;
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