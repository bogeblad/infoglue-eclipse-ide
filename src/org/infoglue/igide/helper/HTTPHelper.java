/* ===============================================================================
 *
 * Part of the InfoglueIDE Project 
 *
 * ===============================================================================
 *
 * Copyright (C) Stefan Sik 2007
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
/* 
 * Created on 2004-nov-16
 *
 */
package org.infoglue.igide.helper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Stefan Sik
 * 
 */
public class HTTPHelper
{
	public static String getUrlContent(URL url) throws IOException
	{
	    try
	    {
			//Logger.logConsole("HTTPHelper: Getting content from: " + url.toExternalForm());
		    
		    URLConnection connection = url.openConnection();
			connection.setConnectTimeout(3000);
		    connection.setReadTimeout(20000);
		    connection.setUseCaches(false);
			InputStream inStream = null;
		    inStream = connection.getInputStream();
		    InputStreamReader inStreamReader = new InputStreamReader(inStream, "UTF-8");
		    BufferedReader buffer = new BufferedReader(inStreamReader);            
		    StringBuffer strbuf = new StringBuffer();   
		    String line; 
		    while((line = buffer.readLine()) != null) 
		    {
		        strbuf.append(line);
		    }                                              
		    String readData = strbuf.toString();  
		    
		    buffer.close();
		    inStream.close();
		    
		    Logger.logConsole("readData:" + readData.length());
		    return readData;	    	
	    }
	    catch (IOException ioe) 
	    {
	    	Logger.logConsole("Error:" + ioe.getMessage());
	    	throw ioe;
		}
	    catch (Exception e) 
	    {
	    	Logger.logConsole("Error:" + e.getMessage());
	    	throw new IOException("Unexpected error:" + e.getMessage());
		}
	}
	public static String getUrlContent(URL base, String urlAddress) throws Exception
	{
	    URL url = new URL(base, urlAddress);
	    return getUrlContent(url);
	}
	
	
    public static String postData(URL base, String urlAddress, Map data) throws MalformedURLException
    {
	    URL url = new URL(base, urlAddress);
	    return postData(url, data);
    }
    
    public static String postData(URL url, Map data)
    {
    	StringBuilder returnData = new StringBuilder();
        try
        {
            URLConnection urlConn;
            DataOutputStream printout;
            DataInputStream input;
            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            printout = new DataOutputStream(urlConn.getOutputStream());

            String content = "";
            String separator = "";
            for(Iterator i=data.keySet().iterator();i.hasNext();)
            {
                String key = (String) i.next();
                String value = (String) data.get(key);
                content += separator + key + "=" + URLEncoder.encode(value, "UTF-8");
                separator = "&";
            }
            
            printout.writeBytes(content);
            printout.flush();
            printout.close();

            // Get response data.
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String l = null;
            while((l = reader.readLine()) != null)
            {
            	returnData.append(l);
            	returnData.append("\n");
            }
            /*
            input = new DataInputStream(urlConn.getInputStream());
            while (null != ((str = input.readLine())))
            {
                // System.out.println("RESPONSEDATA:" + str);
            }
			*/
            
            reader.close();

        }
        catch (MalformedURLException me)
        {
            System.err.println("MalformedURLException: " + me);
        }
        catch (IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
        return returnData.toString();
        
    } 
	
}
