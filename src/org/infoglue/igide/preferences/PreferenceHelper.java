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
package org.infoglue.igide.preferences;

import java.util.HashMap;
import java.util.Map;

import org.infoglue.igide.InfoglueConnectorPlugin;
/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class PreferenceHelper {

	public static final Map DEFAULT_EXTENSIONS = new HashMap();
	static
	{
		DEFAULT_EXTENSIONS.put("HTMLTemplate.ComponentProperties", ".xml");
		DEFAULT_EXTENSIONS.put("HTMLTemplate.Template", ".jsp");
	}
	
	public static String getFileExtensionForAttributeKey(String key)
	{
		String ext = (String) PreferenceHelper.DEFAULT_EXTENSIONS.get(key);
		if(ext == null) ext = ".jsp";
		
	    String value = InfoglueConnectorPlugin.getDefault().getPreferenceStore().getString(InfogluePreferencePage.P_ACC_TYPES);
	    String[] list = value.split(";");
	    for(int i = 0; i<list.length; i++)
	    {
	    	String currentKey = list[i].split(",")[0];
	    	if(currentKey.equals(key))
	    	{
	    		ext = list[i].split(",")[1];
	    		break;
	    	}
	    }
        if(ext.startsWith("*"))
        {
        	ext = ext.substring(1);
        }
        if(!ext.startsWith("."))
        {
        	ext = "." + ext;
        }
		
		return ext;
	}


}
