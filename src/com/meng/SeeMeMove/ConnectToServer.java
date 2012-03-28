package com.meng.SeeMeMove;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class ConnectToServer {
	
    private HttpURLConnection connection;
    //private OutputStreamWriter wr;
    private URL url; 
    private String param, response;
    
	public ConnectToServer(final String id, final String rms, final String latt, final String longg) {
    	try {
			url = new URL("http://webprojects.eecs.qmul.ac.uk/ec09240/seememove/addentry.php");
		} catch (MalformedURLException e) {e.printStackTrace();}

		try {
			param = "id=" + URLEncoder.encode(id, "UTF-8") + "&rms=" + URLEncoder.encode(rms, "UTF-8") + "&lat=" + URLEncoder.encode(latt, "UTF-8") + "&longg=" + URLEncoder.encode(longg, "UTF-8");
		} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
    	
    	try {
			connection = (HttpURLConnection)url.openConnection();
		} catch (IOException e) {e.printStackTrace();}
		try {
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
			connection.setRequestMethod("POST");
			connection.setFixedLengthStreamingMode(param.getBytes().length);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		} catch (ProtocolException e) {e.printStackTrace();}
		try {
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			out.print(param);
			out.close();
		} catch (IOException e) {e.printStackTrace();}
		response=""; // clear response string
		try{
			Scanner inStream;
			inStream = new Scanner(connection.getInputStream());
			while(inStream.hasNextLine())
				response+=(inStream.nextLine());
		}catch (IOException e) {e.printStackTrace();}
		
		finally {
		//close the connection, set all objects to null
			connection.disconnect();
		}
	}
}