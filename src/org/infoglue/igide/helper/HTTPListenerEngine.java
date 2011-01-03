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
 * Created on 2005-apr-25
 *
 */
package org.infoglue.igide.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Stefan Sik
 * 
 */
public class HTTPListenerEngine implements Runnable
{
    private StringListener listener = null;
    private URL url;
    private Thread wThread = null;
    
    public static void main(String[] args)
    {
        try
        {
            URL url = new URL("http://localhost:8080/infoglueCMS/SimpleContentXml!getChangeNotificationsStream.action?j_username=root&j_password=");
            new HTTPListenerEngine(null, url).start();
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public HTTPListenerEngine(StringListener listener, URL url)
    {
        this.url = url;
        this.listener = listener;
        wThread = new Thread(this);
    }
    
    public void start()
    {
        wThread.start();
    }

    public void run()
    {
        getData();
    } 
    
    private void getData()
    {
        String str = null;
        try
        {
            URLConnection urlConn;
            DataOutputStream printout;
            DataInputStream input;
            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);

            // Get response data.
            input = new DataInputStream(urlConn.getInputStream());
            while (null != ((str = input.readLine())))
            {
                if(listener != null)
                    listener.recieveData(str);
                else
                    System.out.println(str);
            }
            input.close();
        }
        catch (MalformedURLException me)
        {
            System.err.println("MalformedURLException: " + me);
        }
        catch (IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

}
