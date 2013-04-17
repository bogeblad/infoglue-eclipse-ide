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
package org.infoglue.igide.model.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.cms.ContentTypeDefinition;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.InfoglueProxy;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.MasterNode;
import org.infoglue.igide.preferences.PreferenceHelper;
/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class ContentNodeFactory {
	
	public static List<String> primaryAttributeKeys = new ArrayList<String>();
	static {
		primaryAttributeKeys.add("HTMLTemplate.Template");
		primaryAttributeKeys.add("SimpleText.Text");
	}

	public static ContentNode createContentRootNode(IResource localrecource, MasterNode master, InfoglueConnection connection) 
	{
		return new ContentNode(localrecource, master, connection);
	}
	
	public static ContentNode createContentNode(ContentNode parentNode, String id, String parent, String text,	String type, String src, Integer activeVersion, Integer repositoryId, Integer activeVersionStateId, Integer contentTypeId, String activeVersionModifier, boolean bHasChildren) 
	{
		Logger.logConsole("createContentNode, type: "  + type);
		IResource 	parentResource = parentNode.getLocalrecource();
		IFolder 	parentFolder = null;
		IFile 		thisFile = null;
		IFolder 	thisFolder = null;
		IResource 	thisResource = null;

		if(parentResource instanceof IProject)
		{
			IProject parentProject = (IProject) parentResource;
			parentFolder = parentProject.getFolder("WebContent");
		}
		else if(parentResource instanceof IFolder)
		{
			parentFolder = (IFolder) parentResource;
		}
		else
		{
			// TODO: throw instead
			return null;
		}
		
		
		/*
		 * If this is a item, then localresource should be the main attribute for the contenttype
		 * for example, if the content type is HTMLTemplate, then localresource for this node shuld 
		 * be the file that represents the "Template" attribute. Thats because we want to be able 
		 * to provide default team functionality on that attribute, such as compare with and so on.
		 */
		if(type.equalsIgnoreCase(ContentNode.ITEM))
		{
			Logger.logConsole("============================================================");
			Logger.logConsole("Node is an item:");
			InfoglueProxy proxy = parentNode.getConnection().getInfoglueProxy();
			try 
			{
				ContentTypeDefinition def = proxy.getContentTypeDefinition(contentTypeId);
				Logger.logConsole("ContentType: " + def.getName());
				Map<String, ContentTypeAttribute> attributes = InfoglueCMS.getContentTypeAttributes(def.getSchemaValue());
				Logger.logConsole("Attributes:");
				
				/*
				 * Check if we have a primary attribute.
				 */
				for(String key : attributes.keySet())
				{
					String thisKey = def.getName() + "." + attributes.get(key).getName();
					if(primaryAttributeKeys.contains(thisKey))
					{
						Logger.logConsole(thisKey + " is a primary attribute key");
						String fileName = Utils.cleanFileName(text.trim());
						thisFile = parentFolder.getFile(fileName + PreferenceHelper.getFileExtensionForAttributeKey(thisKey));
						if(!thisFile.exists())
						{
						}
						else
						{
							Logger.logConsole(thisFile.toString() + " exists");
						}
						// Use this file as the resource for this node.
						thisResource = thisFile;
					}
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			Logger.logConsole("============================================================");
		}

		/*
		 * If we dont have assigned this resource. Create a folder.
		 */
		if(thisResource == null)
		{
			/*
			 * Make thisResource a folder. 
			 */
			thisFolder = parentFolder.getFolder(text.trim());
			if(!thisFolder.exists())
			{
				try 
				{
					thisFolder.create(true, true, Utils.getMonitor(null));
				} 
				catch (CoreException e) 
				{
					e.printStackTrace();
					return null;
				}
			}
			
			thisResource = thisFolder;
		}
		
		ContentNode node = new ContentNode(thisResource, parentNode, id, parent, text, type, src);
        node.setActiveVersion(activeVersion);
        node.setRepositoryId(repositoryId);
    	node.setActiveVersionStateId(activeVersionStateId);
        node.setContentTypeId(contentTypeId);
    	node.setActiveVersionModifier(activeVersionModifier);
        node.setChildren(bHasChildren);
		
		return node;
	}


}
