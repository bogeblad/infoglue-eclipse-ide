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
import java.security.AccessControlException;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.infoglue.igide.helper.Logger;

/**
 * @author Stefan Sik
 * 
 */
public class HTTPTextDocumentListenerEngine implements Runnable
{
    private TextDocumentListener listener;
    private URL url;
    private Thread wThread;
    private HttpClient client;
    private long lastRetry;
    private TreeViewer viewer;
    
    public static void main(String[] args)
    {
        try
        {
            URL url = new URL("http://localhost:8080/infoglueCMS/SimpleContentXml!getChangeNotificationsStream.action?j_username=root&j_password=");
            (new HTTPTextDocumentListenerEngine(null, url, null, null)).start();
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public HTTPTextDocumentListenerEngine(TextDocumentListener listener, URL url, HttpClient client, TreeViewer viewer)
    {
        this.listener = null;
        wThread = null;
        this.client = null;
        lastRetry = System.currentTimeMillis();
        this.url = url;
        this.listener = listener;
        this.viewer = viewer;
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
    	String errorMessage;
        boolean error;
        errorMessage = "";
        error = false;
    	try
    	{
	        Logger.logConsole("Starting listen thread");
	        URLConnection urlConn = url.openConnection();
	        urlConn.setConnectTimeout(3000);
	        urlConn.setRequestProperty("Connection", "Keep-Alive");
	        urlConn.setReadTimeout(0);
	        urlConn.setDoInput(true);
	        urlConn.setDoOutput(true);
	        urlConn.setUseCaches(false);
	        urlConn.setAllowUserInteraction(false);
	        if(urlConn.getHeaderFields().toString().indexOf("401 Unauthorized") > -1)
	        {
	            Logger.logConsole("User has no access to the CMS - closing connection");
	            throw new AccessControlException("User has no access to the CMS - closing connection");
	        }
	        String boundary = urlConn.getHeaderField("boundary");
	        DataInputStream input = new DataInputStream(urlConn.getInputStream());
	        StringBuffer buf = new StringBuffer();
	        if(listener != null)
	            listener.onConnection(url);
	        String str = null;
	        while((str = input.readLine()) != null) 
	        {
	            if(str.indexOf("XMLNotificationWriter.ping") == -1)
	            {
	                if(str.equals(boundary))
	                {
	                	String message = buf.toString();

	                	// By checking there is more in the String than the XML declaration we assume the message is valid
	                	if (message != null && !message.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").equals(""))
	                	{
		                    if(listener != null)
		                        listener.recieveDocument(buf.toString());
		                    else
		                        Logger.logConsole((new StringBuilder("NEW DOCUMENT!!\r\n")).append(buf.toString()).append("\r\n").toString());
	                	}
	                    buf = new StringBuffer();
	                } 
	                else
	                {
	                    buf.append(str);
	                }
	            }
	        }
            input.close();
    	}
    	catch(MalformedURLException me)
    	{
	        error = true;
	        errorMessage = (new StringBuilder("Faulty CMS-url:")).append(url).toString();
	        if(listener != null)
	            listener.onException(me);
	        else
	            System.err.println((new StringBuilder("MalformedURLException: ")).append(me).toString());
	        final String errorMessageFinal = errorMessage;
	        Logger.logConsole((new StringBuilder("The connection was shut. Was it an error:")).append(error).toString());
	        if(!error)
	        {
	            if(System.currentTimeMillis() - lastRetry > 20000L)
	            {
	                Logger.logConsole("Trying to restart the listener as it was a while since last...");
	                lastRetry = System.currentTimeMillis();
	                listen();
	            }
	        } else
	        {
	            try
	            {
	                if(listener != null)
	                    listener.onEndConnection(url);
	            }
	            catch(Exception e)
	            {
	                Logger.logConsole((new StringBuilder("Error ending connection:")).append(e.getMessage()).toString());
	            }
	            Display.getDefault().asyncExec(new Runnable() {
	
	                public void run()
	                {
	                    MessageDialog.openError(viewer.getControl().getShell(), "Error", (new StringBuilder()).append(errorMessageFinal).toString());
	                }
	            });
	        }
    	}
    	catch(IOException ioe)
    	{
            error = true;
            errorMessage = "Got an I/O-Exception talking to the CMS. Check that it is started and in valid state.";
            Logger.logConsole((new StringBuilder("ioe: ")).append(ioe.getMessage()).toString());
            if(listener != null)
                listener.onException(ioe);
            else
                Logger.logConsole((new StringBuilder("TextDocumentListener cannot connect to: ")).append(url.toExternalForm()).toString());
            final String errorMessageFinal = errorMessage;
            Logger.logConsole((new StringBuilder("The connection was shut. Was it an error:")).append(error).toString());
            if(!error)
            {
                if(System.currentTimeMillis() - lastRetry > 20000L)
                {
                    Logger.logConsole("Trying to restart the listener as it was a while since last...");
                    lastRetry = System.currentTimeMillis();
                    listen();
                }
            } else
            {
                try
                {
                    if(listener != null)
                        listener.onEndConnection(url);
                }
                catch(Exception e)
                {
                    Logger.logConsole((new StringBuilder("Error ending connection:")).append(e.getMessage()).toString());
                }
                Display.getDefault().asyncExec(new Runnable() {
	
	                public void run()
	                {
	                    MessageDialog.openError(viewer.getControl().getShell(), "Error", (new StringBuilder()).append(errorMessageFinal).toString());
	                }
	            });
            }
    	}
        catch(AccessControlException ace)
        {
	        error = true;
	        errorMessage = "The user you tried to connect with did not have the correct access rights. Check that he/she has roles etc enough to access the CMS";
	        Logger.logConsole((new StringBuilder("ioe: ")).append(ace.getMessage()).toString());
	        if(listener != null)
	            listener.onException(ace);
	        else
	            Logger.logConsole((new StringBuilder()).append(ace.getMessage()).toString());
	        final String errorMessageFinal = errorMessage;
	        Logger.logConsole((new StringBuilder("The connection was shut. Was it an error:")).append(error).toString());
	        if(!error)
	        {
	            if(System.currentTimeMillis() - lastRetry > 20000L)
	            {
	                Logger.logConsole("Trying to restart the listener as it was a while since last...");
	                lastRetry = System.currentTimeMillis();
	                listen();
	            }
	        } else
	        {
	            try
	            {
	                if(listener != null)
	                    listener.onEndConnection(url);
	            }
	            catch(Exception e)
	            {
	                Logger.logConsole((new StringBuilder("Error ending connection:")).append(e.getMessage()).toString());
	            }
	            Display.getDefault().asyncExec(new Runnable() {
	
	                public void run()
	                {
	                    MessageDialog.openError(viewer.getControl().getShell(), "Error", (new StringBuilder()).append(errorMessageFinal).toString());
	                }
	            });
	        }
        }
        catch(Exception exception)
        {
            final String errorMessageFinal = errorMessage;
            Logger.logConsole((new StringBuilder("The connection was shut. Was it an error:")).append(error).toString());
            if(!error)
            {
                if(System.currentTimeMillis() - lastRetry > 20000L)
                {
                    Logger.logConsole("Trying to restart the listener as it was a while since last...");
                    lastRetry = System.currentTimeMillis();
                    listen();
                }
            } else
            {
                try
                {
                    if(listener != null)
                        listener.onEndConnection(url);
                }
                catch(Exception e)
                {
                    Logger.logConsole((new StringBuilder("Error ending connection:")).append(e.getMessage()).toString());
                }
                Display.getDefault().asyncExec(new Runnable() {
	
	                public void run()
	                {
	                    MessageDialog.openError(viewer.getControl().getShell(), "Error", (new StringBuilder()).append(errorMessageFinal).toString());
	                }
	            });
            }
        }
        catch (Throwable e) {
            final String errorMessageFinal = errorMessage;
            Logger.logConsole((new StringBuilder("The connection was shut. Was it an error:")).append(error).toString());
            if(!error)
            {
                if(System.currentTimeMillis() - lastRetry > 20000L)
                {
                    Logger.logConsole("Trying to restart the listener as it was a while since last...");
                    lastRetry = System.currentTimeMillis();
                    listen();
                }
            } else
            {
                try
                {
                    if(listener != null)
                        listener.onEndConnection(url);
                }
                catch(Exception e2)
                {
                    Logger.logConsole((new StringBuilder("Error ending connection:")).append(e2.getMessage()).toString());
                }
                Display.getDefault().asyncExec(new Runnable() {
	
	                public void run()
	                {
	                    MessageDialog.openError(viewer.getControl().getShell(), "Error", (new StringBuilder()).append(errorMessageFinal).toString());
	                }
	            });
            }
		}
    }

}
