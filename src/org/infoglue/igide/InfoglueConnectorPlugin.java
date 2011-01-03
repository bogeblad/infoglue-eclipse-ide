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

package org.infoglue.igide;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Stefan Sik
 */
public class InfoglueConnectorPlugin extends AbstractUIPlugin {

	private static InfoglueConnectorPlugin plugin;
	private ResourceBundle resourceBundle;
	final public static String id = "org.infoglue.igide"; 
	
	protected void initializeDefaultPluginPreferences() {
		
		/*
		 * TODO:
		 * Work on the "project" concept instead of just defining one connection in the
		 * Infoglue Connector settings. For now, in the development phase connect to one
		 * infoglue server. 
		 * 
		 * My idea is that the user creates an Infoglue Project in eclipse and in that projects
		 * settings specify connection details. Or perhaps look at how cvs repositories are handled
		 * in eclipse and present a simular interface for the infoglue connections. 
		 * 
		 * Important!
		 * Also work on the authentication. We should not send username and password
		 * as parameters to each query.  
		 */
		
	}
	
	public InfoglueConnectorPlugin() 
	{
		super();
		plugin = this;

		try 
		{ 
			resourceBundle = ResourceBundle.getBundle("org.infoglue.igide.InfoglueConnectorPluginResources");
		} 
		catch (MissingResourceException e) 
		{
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception 
	{
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception 
	{
		super.stop(context);
		InfoglueConnectorPlugin.getDefault().savePluginPreferences();
	}

	public static InfoglueConnectorPlugin getDefault() 
	{
		return plugin;
	}

	public static String getResourceString(String key) 
	{
		ResourceBundle bundle = InfoglueConnectorPlugin.getDefault().getResourceBundle();
		try 
		{
			return (bundle != null) ? bundle.getString(key) : key;
		} 
		catch (MissingResourceException e) 
		{
			return key;
		}
	}

	public ResourceBundle getResourceBundle() 
	{
		return resourceBundle;
	}
}
