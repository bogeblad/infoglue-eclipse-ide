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
 * + Created on 2004-nov-18
 *  
 */
package org.infoglue.igide.cms.connection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.infoglue.igide.cms.exceptions.InvalidLoginException;
import org.infoglue.igide.helper.HTTPHelper;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.NotificationListener;
import org.infoglue.igide.helper.http.HTTPTextDocumentListenerEngine;
import org.infoglue.igide.helper.http.TextDocumentListener;

/**
 * @author Stefan Sik
 *  
 */

/*
 * TODO: TODO: Refactor to use commons HttpClient, and improve authentication
 */

public class InfoglueConnection implements TextDocumentListener
{
    public static final String TREESERVICEACTION           		= "SimpleContentXml.action";
    //public static final String CONTENTVERSIONSERVICEACTION 		= "SimpleContentXml!ContentVersion.action?enableCompression=true";
    public static final String CONTENTVERSIONSERVICEACTION 		= "SimpleContentXml!ContentVersion.action";
    public static final String CONTENTSERVICEACTION 			= "SimpleContentXml!Content.action";
    public static final String CONTENTVERSIONHEADSERVICEACTION 	= "SimpleContentXml!ContentVersionHead.action";
    public static final String CONTENTTYPEDEFSERVICEACTION 		= "SimpleContentXml!ContentTypeDefinitions.action";
    public static final String APPLICATIONSETTINGS 				= "SimpleContentXml!applicationSettings.action";
    public static final String UPDATECONTENTVERSION 			= "UpdateContentVersion!xml.action";
    public static final String UPDATECONTENTVERSIONSTANDALONE	= "UpdateContentVersion!standalone.action";
    public static final String TEMPLATELOGICMETHODS 			= "ViewApplicationSettings!getClassMethods.action?";
    public static final String NOTIFICATIONSERVICE 				= "SimpleContentXml!getChangeNotificationsStream.action";
    public static final String VIEWCONTENTVERSIONSTANDALONE 	= "ViewContentVersion!standalone.action";

    public static final String ROOTCONTENTSERVICEACTION = "SimpleContentXml!RootContent.action";
    public static final String MASTERLANGUAGESERVICEACTION = "SimpleContentXml!masterLanguage.action";
    public static final String CREATEREPOSITORY = "CreateRepository!XML.action";
    public static final String UPDATEACCESSRIGHTS = "UpdateAccessRights.action";

    // TODO: In Infoglue, create an xml view for CreateContent.action
    public static final String CREATECONTENT 					= "CreateContent.action";

    // TODO: Use HttpClient!!!
    // private HttpClient 			client = null;

    private URL                baseUrl;
    private URL 			   deliveryBaseUrl;
    private String             username;
    private String             password;

    private InfoglueProxy     infoglueProxy              = null;
    private boolean connected = false;
    private TreeViewer viewer;
    private HttpClient client;

    private List<NotificationListener> notificationListeners = new ArrayList<NotificationListener>();
    private boolean listeningForNotifications = false;
    private boolean enableListenForNotifications = true;

    public InfoglueConnection(String baseUrl, String username, String password, TreeViewer viewer) throws IllegalStateException
    {
        connected = false;
        notificationListeners = new ArrayList();
        listeningForNotifications = false;
        enableListenForNotifications = true;
        Logger.logConsole("Creating new connection: ");
        Logger.logConsole((new StringBuilder("BaseUrl: ")).append(baseUrl).toString());
        Logger.logConsole((new StringBuilder("UserName: ")).append(username).toString());
        setBaseUrl(baseUrl);
        this.username = username;
        this.password = password;
        this.viewer = viewer;

        try 
        {
			getInfoglueProxy().getContentTypeDefinitions();
		}
        catch (Exception e) 
        {
           	Logger.logConsole("Error getting content types on connection: " + e.getMessage());
            throw new IllegalStateException("Could not initialize content types");
   		}
    }
    
    private void setBaseUrl(String baseUrl)
    {
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        try
        {
            this.baseUrl = new URL(baseUrl);
        }
        catch (MalformedURLException e)
        {
            this.baseUrl = null;
        }
    }

    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof InfoglueConnection)
    	{
    		InfoglueConnection c = (InfoglueConnection) obj;
    		try {
    			return c.baseUrl.equals(baseUrl) && c.username.equals(username) && c.password.equals(password);
    		}
    		catch(Exception e) {}
    	}
    	return super.equals(obj);
    }
    
    
    @Override
    public int hashCode() {
    	try {
        	return baseUrl.hashCode();
    	}
    	catch(Exception e)
    	{
    		
    	}
    	return 0;
    }
    
    public void addNotificationListener(NotificationListener listener) throws MalformedURLException
    {
        if(enableListenForNotifications)
        {
        	checkTextDocumentListener();
            synchronized (notificationListeners) {
                notificationListeners.add(listener);
			}
            
        }
    }
    
    private void checkTextDocumentListener() throws MalformedURLException
    {
        if(!listeningForNotifications)
        {
    		listeningForNotifications = true;
            (new HTTPTextDocumentListenerEngine(this, getUrl(NOTIFICATIONSERVICE, ""), getClient(), viewer)).start();
        }
    }
    
    public void removeNotificationListener(NotificationListener listener)
    {
    	synchronized (notificationListeners) {
            notificationListeners.remove(listener);
        }
    }

    public String getKey()
    {
        return baseUrl.toExternalForm() + "_" + username + password;
    }

    public synchronized InfoglueProxy getInfoglueProxy()
    {
        if (infoglueProxy == null)
    	{
			try 
			{
				infoglueProxy = new InfoglueProxy(this);
			} 
			catch (InvalidLoginException e) 
			{
		        for(NotificationListener listener: notificationListeners)
		        {
		            listener.connectionException(this, e);
		        }
			} 
			catch (IOException e) 
			{
		        for(NotificationListener listener: notificationListeners)
		        {
		            listener.connectionException(this, e);
		        }
			} 
			catch (InvocationTargetException e) 
			{
		        for(NotificationListener listener: notificationListeners)
		        {
		            listener.connectionException(this, e);
		        }
			}
    	}
        return infoglueProxy;
    }


    public URL getUrl(String service, String params) throws MalformedURLException
    {
    	/*
    	 * TODO: MAybe this is the place to implement session handling
    	 */
    	
    	
        /*
         * Delivery services
         */
        if(service.indexOf(TEMPLATELOGICMETHODS)>-1)
        {
            return new URL(getDeliveryBaseUrl(), service + params);
        }
        
    	if(service.indexOf("?")>-1 && params.startsWith("?"))
    	{
    		params = "&" + params.substring(1);
    	}
        
        /*
         * CMS services
         */
        params = service + params;
        if (params.indexOf("?") > -1)
            params += "&j_username=" + getUsername() + "&j_password=" + password;
        else
            params += "?j_username=" + username + "&j_password=" + password;

        URL ret = new URL(getBaseUrl(), params);
        return ret;
    }

    /*
     * TODO: Do a safer check!!
     */
    private String checkData(String data) throws MalformedURLException, InvalidLoginException
    {
        if (data.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC")) { throw new InvalidLoginException(); }
        return data;
    }

    public String postData(String src, String service, Map data) throws MalformedURLException
    {
    	System.out.println("InfoglueConnection: postData");
        URL url = getUrl(service, src);
        return HTTPHelper.postData(url, data);
    }

    public String getXML(String src, String service) throws InvalidLoginException, IOException
    {
		return checkData(HTTPHelper.getUrlContent(getUrl(service, src)));
    }


    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public URL getBaseUrl()
    {
        return baseUrl;
    }

    public URL getDeliveryBaseUrl()
    {
        return deliveryBaseUrl;
    }

    public void setDeliveryBaseUrl(String deliveryBaseUrl)
    {
    	if (deliveryBaseUrl.endsWith("ViewPage.action"))
    		deliveryBaseUrl = deliveryBaseUrl.substring(0, deliveryBaseUrl.indexOf("ViewPage.action"));
        if (!deliveryBaseUrl.endsWith("/")) deliveryBaseUrl += "/";
        
        try
        {
            this.deliveryBaseUrl = new URL(baseUrl, deliveryBaseUrl);
        }
        catch (MalformedURLException e)
        {
            this.deliveryBaseUrl = null;
        }
    }


    public boolean isListenForNotifications()
    {
        return listeningForNotifications;
    }
    public void setListenForNotifications(boolean listenForNotifications)
    {
        this.listeningForNotifications = listenForNotifications;
    }

    /*
     * @see org.infoglue.igide.helper.http.TextDocumentListener#recieveDocument(java.lang.String)
     */
    public void recieveDocument(final String document)
    {
    	final InfoglueConnection connection = this;
        Display.getDefault().syncExec(new Runnable() { public void run () 
        {  
            NotificationMessage mess = null;
            try
            {
                mess = InfoglueProxy.createNotificationMessage(connection, document);
                if(mess!=null)
                {
    	            for(NotificationListener listener: notificationListeners)
    	            {
    	                listener.recieveCMSNotification(mess);
    	            }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
        } });
    }

	public void onException(Exception e) {
		// restart
		listeningForNotifications = false;
        for(NotificationListener listener: notificationListeners)
        {
            listener.connectionException(this, e);
        }
	}

	public void onConnection(URL url) {
		setConnected(true);
        for(NotificationListener listener: notificationListeners)
        {
            listener.connectedToRemoteSystem(this);
        }
	}

	public void onEndConnection(URL url) {
		Logger.logConsole("onEndConnection");
		listeningForNotifications = false;
		setConnected(false);
        for(NotificationListener listener: notificationListeners)
        {
            listener.disconnectedFromRemoteSystem(this);
        }
	}

	private void setConnected(boolean connected) {
		this.connected = connected;
	}
	public boolean isConnected() {
		return connected;
	}
	
	public HttpClient getClient()
    {
        if(client == null)
        {
            client = new HttpClient();
            org.apache.commons.httpclient.Credentials cred = new UsernamePasswordCredentials(username, password);
            org.apache.commons.httpclient.Credentials ntcred = new NTCredentials(username, password, getBaseUrl().getHost(), "corp");
            client.getParams().setAuthenticationPreemptive(true);
            client.getState().setCredentials(new AuthScope(getBaseUrl().getHost(), getBaseUrl().getPort(), AuthScope.ANY_REALM), ntcred);
            client.getHostConfiguration().setHost(getBaseUrl().getHost(), getBaseUrl().getPort());
        }
        return client;
    }
}