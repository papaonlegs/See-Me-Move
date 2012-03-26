package com.meng.SeeMeMove;

import java.io.FileWriter;
import java.util.ArrayList;

public class DataWrite {
    
	private FileWriter fWriter;
	
    public DataWrite(ArrayList<Float> outputData, String fileName) {
    		
		try {
			fWriter = new FileWriter("\\sdcard\\" + fileName + ".csv");
		    
			for(int i = 0 ; i < outputData.size() ; i++) {
				//fWriter.append(Long.toString(timestamps.get(i)));
				//fWriter.append(",");
				fWriter.append(Float.toString(outputData.get(i)));
				fWriter.append('\n');
			}			
		    fWriter.flush();
		    fWriter.close();
		}catch(Exception e){e.printStackTrace();}  
    }
}
