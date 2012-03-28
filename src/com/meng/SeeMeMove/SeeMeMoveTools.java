package com.meng.SeeMeMove;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author Richard Flanagan
 */

public class SeeMeMoveTools 
{	
	// Parameter Variables THESE MUST BE SET!!
	private int sampleRate = 100; // desired sample rate which is set when instantiating the object or defined after
	private int window = 10000; // in milliseconds default set to 10 seconds
	private boolean smooth = false; 
	private int smoothOrder = 20; // Order of lowpass filter
	
	// Time Variables 
	private static final long NANO_IN_SECOND = 1000000000l; // Number of nanoseconds in a second
	private static final long NANO_IN_MILISECOND = 1000000l; // number of nanoseconds in 1 millisecond
    private long windowInNano; // length of window in nanoseconds calculated by constructor or set by user
    private float startTime = 0;
    private float timeDiff = 0;	
	
	// Contains corresponding time stamp
	private ArrayList<Long> timevalues;
	// Array index values of raw samples per window 
	private ArrayList<Integer> rawWindowIndexs;
	// Interpolated times
	private ArrayList<Long> timestampInter;
	
	// Raw sensor values 
	private ArrayList<Float> xRawValues;
	private ArrayList<Float> yRawValues;
	private ArrayList<Float> zRawValues;
	
	// Interpolated sensor values 
	private ArrayList<Float> xInterValues;
	private ArrayList<Float> yInterValues;
	private ArrayList<Float> zInterValues;
	
	// Magnitude
	private ArrayList<Float> magnitude;
	private int previousMagIndex = 0;
	
	// RMS 
	private ArrayList<Float> RMS;
	private int previousRMSIndex = 0;
	
	// Average
	private ArrayList<Float> average;
	private int previousAverageIndex = 0;
	
	// FFT
	private ArrayList<ArrayList<Double>> fftBins;
	private static int fftStartIndex = 0;
	
	// Write data to file
	private FileWriter fWriter;
	private BufferedWriter out;
	private File root = Environment.getExternalStorageDirectory();
	private File magFile;
	
	// Unique phone ID
	private String phoneID;
	private static TelephonyManager telephonyManager = (TelephonyManager) SeeMeMoveActivity.getContext().getSystemService(Context.TELEPHONY_SERVICE);
	
	// GPS 
	private double longitude;
	private double lattitude; 
		
    /**
     * Constructor
     */
	public SeeMeMoveTools() {
		// Instantiate global variables  
		this.xRawValues = new ArrayList<Float>();
		this.yRawValues = new ArrayList<Float>();
		this.zRawValues = new ArrayList<Float>();
		this.xInterValues = new ArrayList<Float>();
		this.yInterValues = new ArrayList<Float>();
		this.zInterValues = new ArrayList<Float>();
		this.timevalues = new ArrayList<Long>();
		this.timestampInter = new ArrayList<Long>();
		this.RMS = new ArrayList<Float>();
		this.magnitude = new ArrayList<Float>();
		this.average = new ArrayList<Float>();	
		this.fftBins = new ArrayList<ArrayList<Double>>();	
		this.rawWindowIndexs = new ArrayList<Integer>();
		this.rawWindowIndexs.add(0); // Add starting index of zero to array containing index of each new window
		
		// Create output buffer to write file
		try {
			magFile = new File(root, "SeeMeMoveMagnitude.csv");
			fWriter = new FileWriter(magFile);
			out = new BufferedWriter(fWriter);
		} catch (IOException e) {e.printStackTrace();}
		
		// Set window to default size
		windowInNano = NANO_IN_MILISECOND * window;
		
		// Get unique phone ID
		this.phoneID = getDeviceID(telephonyManager);
	}
	
	public SeeMeMoveTools(int sampleRate) {
		// Set parameter 
		this.sampleRate = sampleRate;
		
		// Instantiate global variables 
		this.xRawValues = new ArrayList<Float>();
		this.yRawValues = new ArrayList<Float>();
		this.zRawValues = new ArrayList<Float>();
		this.zRawValues = new ArrayList<Float>();
		this.xInterValues = new ArrayList<Float>();
		this.yInterValues = new ArrayList<Float>();
		this.zInterValues = new ArrayList<Float>();
		this.timevalues = new ArrayList<Long>();
		this.timestampInter = new ArrayList<Long>();
		this.RMS = new ArrayList<Float>();
		this.magnitude = new ArrayList<Float>();
		this.average = new ArrayList<Float>();
		this.fftBins = new ArrayList<ArrayList<Double>>();	
		this.rawWindowIndexs = new ArrayList<Integer>();
		this.rawWindowIndexs.add(0); // Add starting index of zero to array containing index of each new window
		
		// Create output buffer to write file
		try {
			magFile = new File(root, "SeeMeMoveMagnitude.csv");
			fWriter = new FileWriter(magFile);
			out = new BufferedWriter(fWriter);
		} catch (IOException e) {e.printStackTrace();}
		
		// Set window to default size
		windowInNano = NANO_IN_MILISECOND * window;
		
		// Get unique phone ID
		this.phoneID = getDeviceID(telephonyManager);
	}
	
	public void clear() {
		this.xRawValues.clear();
		this.yRawValues.clear();
		this.zRawValues.clear();
		this.timevalues.clear();
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
		Log.i("Parameter Set", "Sample Rate: " + Integer.toString(sampleRate));
	}
	
	public void setWindow(int window) {
		this.window = window;
		Log.i("Parameter Set", "Window Size: " + Integer.toString(window) + " milliseconds");
		this.windowInNano = window * NANO_IN_MILISECOND;
		Log.i("Parameter Set", "Window Size: " + Long.toString(windowInNano) + " nanoseconds");
	}
	
	public void setSmooth(boolean smooth) {
		this.smooth = smooth;
		Log.i("Data", "Smooth: " + Boolean.toString(smooth));
	}
	
	public void setSmooth(boolean smooth, int order) {
		this.smooth = smooth;
		Log.i("Data", "Smooth: " + Boolean.toString(smooth));
		this.smoothOrder = order;
		Log.i("Data", "Filter Order: " + Integer.toString(order));
	}
	
	public void setGPS(double latt, double longg) {
		this.lattitude = latt;
		Log.i("Data", "Latitude: " + Double.toString(lattitude));
		this.longitude = longg;
		Log.i("Data", "Longitude: " + Double.toString(longitude));
	}
	
	public float getAverage(int index) throws IllegalAccessException {
		if(magnitude.isEmpty() == true)
			throw new IllegalAccessException("No Values have been added to this object");
		return this.average.get(index);
	}
	
    /**
     * Add raw accelerometer values with corresponding timestamp to SeeMeMoveTools object
     * @param xValue
     *             raw x-axis accelerometer data
     * @param yValue
     *             raw y-axis accelerometer data
     * @param zValue
     *             raw z-axis accelerometer data
     * @param timevalue
     *             accelerometer event timestamp in milliseconds       
     */
	public void addValue(float xValue, float yValue, float zValue, long timevalue) {		
		// Adds input data to corresponding global variables 
		xRawValues.add(xValue);
		yRawValues.add(yValue);
		zRawValues.add(zValue);
		timevalues.add(timevalue);
		
		// Checks to see if 
    	if(this.startTime == 0)
    		this.startTime = timevalue;       	
    	// Calculate time difference between current input and last input 
    	this.timeDiff = timevalue - this.startTime;
    	
    	if(this.timeDiff >= windowInNano) {	 
    		rawWindowIndexs.add(xRawValues.size());
    		Log.i("Data", "Raw Window Size: " + Integer.toString((rawWindowIndexs.get(rawWindowIndexs.size()-1))-(rawWindowIndexs.get(rawWindowIndexs.size()-2))));
            
    		// Create a new worker thread so as not to block the UI thread
    		new Thread(new Runnable() {
                public void run() {	     	
                	// Local variables to store window data to be passed  
                	ArrayList<Long> timevaluesWindow = new ArrayList<Long>();
                	ArrayList<Float> rawXWindowValues = new ArrayList<Float>();
                	ArrayList<Float> rawYWindowValues = new ArrayList<Float>();
                	ArrayList<Float> rawZWindowValues = new ArrayList<Float>();
                	
                	// Add correct window data to above 
                	for(int i = rawWindowIndexs.get(rawWindowIndexs.size()-2); i < rawWindowIndexs.get(rawWindowIndexs.size()-1) ; i ++) {
                		timevaluesWindow.add(timevalues.get(i));
                		rawXWindowValues.add(xRawValues.get(i));
                		rawYWindowValues.add(yRawValues.get(i));
                		rawZWindowValues.add(zRawValues.get(i));
                	}
                	
                	// Interpolate raw data to compensate for fluctuating sample rate  
            		try {               		                   
            			xInterValues.addAll(interpolateData(rawXWindowValues, timevaluesWindow));
                    	yInterValues.addAll(interpolateData(rawYWindowValues, timevaluesWindow));
						zInterValues.addAll(interpolateData(rawZWindowValues, timevaluesWindow));
					} catch (DataFormatException e) {e.printStackTrace();}
            		
            		Log.i("Array Size", "Interpolated X: " + Integer.toString(xInterValues.size()));
                	Log.i("Array Size", "Interpolated Y: " + Integer.toString(yInterValues.size()));
                	Log.i("Array Size", "Interpolated Z: " + Integer.toString(zInterValues.size()));
                	
                	// More processing 
            		calculateMagnitude();            		
            		calculateRMS();
            		calculateAverage();
            		calculateFFT();
            		
            		// Pre-posting error checking
            		try {
	            		if(phoneID == null)
	            			throw new DataFormatException("Phone ID not found");
	            		if(RMS.get(RMS.size()-1) == 0f)
	            			throw new DataFormatException("Phone ID not found");
            		} catch (DataFormatException e){e.printStackTrace();} 
            		
            		// Post data to server             
            		String rms = Float.toString(RMS.get(RMS.size()-1));
                	String latt = Double.toString(lattitude);
                	String longg = Double.toString(longitude);
                	new ConnectToServer(phoneID, rms, latt, longg);
                	
                	//Write Data to file
            		try {
            			for(int i = 0 ; i < fftBins.get(fftBins.size()-1).size() ; i++) {
            				out.write(i +",");
            				out.write(Double.toString(fftBins.get(fftBins.size()-1).get(i)));
            				out.write("\n");
            			}			
            			out.flush();
            			//out.close();
            		}catch(IOException e){e.printStackTrace();}  
                }
            }).start(); 
            this.startTime = timevalue;
    	}
	}	
	
	public ArrayList<Float> interpolateData(List<Float> rawData, ArrayList<Long> rawTime) throws DataFormatException {
		// Error Checking
		if(sampleRate == 0)
			throw new NullPointerException("No Sample rate set: Set in constructor or use method");
		if(xRawValues.size() == 0)
			throw new NullPointerException("No data in array to interpolate");
		if((NANO_IN_SECOND/sampleRate) > ((timevalues.get(rawWindowIndexs.get(rawWindowIndexs.size()-2)+1))-(timevalues.get(rawWindowIndexs.get(rawWindowIndexs.size()-2)))))
			throw new DataFormatException("Sampling rate is greater then data range");
		
		ArrayList<Float> interpolatedData = new ArrayList<Float>(); // ArrayList to store new interpolated data				
		long time = rawTime.get(0);		
		
		// Interpolate rawData
		while(time < rawTime.get(rawTime.size()-1)) {			
			float interValue = 0;
			// Loops through to find corresponding time in rawTime
			find:
				for(int i = 0 ; i < rawTime.size()-1 ; i++) {
					long firstTimeValue = rawTime.get(i);
					long secondTimeValue = rawTime.get(i+1);
					if(time >= firstTimeValue && time <= secondTimeValue) {
						float firstValue = rawData.get(i);
						float secondValue = rawData.get(i+1);
						if(secondValue >= firstValue) {
							interValue = ((time-firstTimeValue)*(secondValue-firstValue)/(secondTimeValue-firstTimeValue))+firstValue;
						}
						else {
							interValue = ((secondTimeValue-time)*((firstValue-secondValue)/(secondTimeValue-firstTimeValue)))+secondValue;
						}
						interpolatedData.add(interValue);
						break find;
					}
				}		
			time += (NANO_IN_SECOND/sampleRate);
		}
		// Smooth data
		if(smooth == true)
			interpolatedData = lowPassFilter(interpolatedData);
		// Return interpolated data
		return interpolatedData;
	}
	
	private ArrayList<Float> lowPassFilter(ArrayList<Float> notSmooth) {
		ArrayList<Float> smooth = new ArrayList<Float>() ;
		float value = notSmooth.get(0);
		for(int i = 1 ; i < notSmooth.size() ; i++) {
		    float currentValue = notSmooth.get(i);
		    value += (currentValue - value) / this.smoothOrder;
		    smooth.add(value);
		}
		return smooth;
	}
	
	private void calculateMagnitude() {
    	for(int i = this.previousMagIndex ; i < this.xInterValues.size() ; i++) {
    		this.magnitude.add((float) Math.sqrt(Math.pow(this.xInterValues.get(i), 2) + Math.pow(this.yInterValues.get(i), 2) + Math.pow(this.zInterValues.get(i), 2)));   	                              
    	}
    	this.previousMagIndex = this.magnitude.size();
    	Log.i("Array Size", "Magnitude: " + Integer.toString(this.magnitude.size()));
	}
		
	private void calculateRMS() {
		float newRMS = 0f;
    	for(int i = this.previousRMSIndex ; i < this.magnitude.size() ; i++) {
    		newRMS += (float) Math.pow(this.magnitude.get(i), 2);
    	}
    	newRMS = (float) Math.sqrt((newRMS/(this.magnitude.size()-this.previousRMSIndex)));
		  	                              
		this.RMS.add(newRMS);
		Log.i("Data", "RMS: " + Float.toString(newRMS));
		this.previousRMSIndex = this.magnitude.size();
	}
	
	private void calculateFFT() {
		Complex[] x = new Complex[512];

        for (int i = 0 ; i < 512 ; i++) {
        	x[i] = new Complex((double)magnitude.get(i+fftStartIndex), 0);
        }
       
        Complex[] y = FFT.fft(x);
        ArrayList<Double> bins = new ArrayList<Double>();
        
        for (int i = 0 ; i < y.length ; i++) {
        	bins.add(y[i].abs());
        }       
        Log.i("Array Size", "Bin Number: " + bins.size());
        fftBins.add(bins);
        
        fftStartIndex = (fftStartIndex + 512);
	}
	
	private void calculateAverage() {
		float average = 0;
		for(int i = this.previousMagIndex ; i < this.magnitude.size() ; i++) {
			average += this.magnitude.get(i);
		}
		average = average/(this.magnitude.size()-this.previousAverageIndex);
		this.average.add(average);
		Log.i("Data", "Average Mag: " + Float.toString(average));
		this.previousAverageIndex = this.magnitude.size();
	}
	
	private String getDeviceID(TelephonyManager phonyManager){		 
		String id = phonyManager.getDeviceId();
		if (id == null){
			id = "not available";
		}
		Log.i("Data", "Phone ID: " + id);
		return id;	
	}
}	