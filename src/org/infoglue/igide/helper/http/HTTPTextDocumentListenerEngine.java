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
package org.infoglue.igide.helper.http;

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
public class HTTPTextDocumentListenerEngine implements Runnable
{
    private TextDocumentListener listener = null;
    private URL url;
    private Thread wThread = null;
    
    public static void main(String[] args)
    {
        try
        {
            URL url = new URL("http://localhost:8080/infoglueCMS/SimpleContentXml!getChangeNotificationsStream.action?j_username=root&j_password=");
            new HTTPTextDocumentListenerEngine(null, url).start();
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public HTTPTextDocumentListenerEngine(TextDocumentListener listener, URL url)
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
        listen();
    } 
    
    private void listen()
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
            String boundary = urlConn.getHeaderField("boundary");
            System.out.println("In HTTPTextDocumentListenerEngine: boundary=" + boundary);
            // urlConn.setRequestProperty("boundary", "~infoglue/multipart-boundary-164398655093-" + System.currentTimeMillis());

            // Get response data.
            input = new DataInputStream(urlConn.getInputStream());
            StringBuffer buf = new StringBuffer();
            if(listener != null)
            	listener.onConnection(url);
            
            while (null != ((str = input.readLine())))
            {
                if(str.equals(boundary))
                {
                    if(listener != null)
                        listener.recieveDocument(buf.toString());
                    else
                        System.out.println("NEW DOCUMENT!!\r\n" + buf.toString() + "\r\n");
                    
                    buf = new StringBuffer();
                }
                else
                {
                    buf.append(str);
                }
            }
            input.close();
        }
        catch (MalformedURLException me)
        {
        	if(listener != null) 
        		listener.onException(me);
        	else
        		System.err.println("MalformedURLException: " + me);
        }
        catch (IOException ioe)
        {
        	if(listener != null)
        		listener.onException(ioe);
        	else
        		System.out.println("TextDocumentListener cannot connect to: " + url.toExternalForm());
        }
        finally
        {
        	if(listener != null)
        		listener.onEndConnection(url);
        }
    }

}
