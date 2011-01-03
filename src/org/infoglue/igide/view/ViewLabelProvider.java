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
package org.infoglue.igide.view;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.igide.InfoglueConnectorPlugin;
import org.infoglue.igide.model.content.ContentNode;

/**
 * 
 * @author Stefan Sik
 *
 */


/*
 * DecoratingLabelProvider
 */

public class ViewLabelProvider extends LabelProvider implements IColorProvider
{
	private Image contentRoot = null;
	
	private String getStateLabel(Integer stateId)
	{
		
		if(stateId == null)
		{
			return null;
		}
		else if(stateId.equals(ContentVersionVO.WORKING_STATE))
		{
			return "Working";
		}
		else if (stateId.equals(ContentVersionVO.PUBLISH_STATE))
		{
			return "Preview";
		}
		else if(stateId.equals(ContentVersionVO.PUBLISHED_STATE))
		{
			return "Published";
		}
		return "";
		
	}
	
    public String getText(Object element)
    {
    	String text = "";
    	if(element == null)
    	{
    		text = "PROBLEM NODE: do not use / in content names";
    		return text;
    	}
    		
    	if(element instanceof ContentNode)
    	{
    		ContentNode node = (ContentNode) element;
    		String stateLabel = getStateLabel(node.getActiveVersionStateId());
    		String activeVersion = "" + node.getActiveVersion();
    		String extra = "";
    		if(stateLabel != null && activeVersion != null)
    			extra += " - " + stateLabel;
    		
    		if(stateLabel == null && activeVersion == null && node.getNodeType().equals(ContentNode.ITEM))
    		{
    			extra = " (not created)";
    		}
    		
    		// extra = " - " + node.getLocalrecource();
    		
    		text = node.getText() + extra; 
    	}
    	else
    	{
    		text = element.toString();
    	}
    	
    	return text;
    }
    
    public Image getImage(Object element)
    {
    	if(element == null)
    	{
            String imageKey = ISharedImages.IMG_OBJ_FOLDER;
    		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
    	}

        String imageKey = ISharedImages.IMG_OBJ_FOLDER;
        
        ContentNode node = (ContentNode) element;
        if(node.isRoot())
        {
        	ImageDescriptor d = null;
        	if(node.getConnection().isConnected())
        	{
            	d = InfoglueConnectorPlugin.imageDescriptorFromPlugin(InfoglueConnectorPlugin.id, "icons/nfs_mount.png");
        	}
        	else
        	{
            	d = InfoglueConnectorPlugin.imageDescriptorFromPlugin(InfoglueConnectorPlugin.id, "icons/file_alert.png");
        	}
 		   	return d.createImage();
        }
        
        if(node.getNodeType().equalsIgnoreCase("item"))
        {
		   // imageKey = ISharedImages.IMG_OBJ_FILE;
 		   ImageDescriptor d = InfoglueConnectorPlugin.imageDescriptorFromPlugin(InfoglueConnectorPlugin.id, "icons/file_locked.png");
 		   if(node.getActiveVersionStateId()==null)
 		   {
       		d = InfoglueConnectorPlugin.imageDescriptorFromPlugin(InfoglueConnectorPlugin.id, "icons/file_alert.png");        	
 		   }
 		   else if(node.getActiveVersionStateId().equals(ContentVersionVO.WORKING_STATE))
        	{
        		d = InfoglueConnectorPlugin.imageDescriptorFromPlugin(InfoglueConnectorPlugin.id, "icons/file_doc.png");        	
        	}
		   return d.createImage();
        }
        if(node.getNodeType().equalsIgnoreCase("repository"))
        {
 		   //imageKey = ISharedImages.IMG_OBJ_FOLDER;
        	ImageDescriptor d = InfoglueConnectorPlugin.imageDescriptorFromPlugin(InfoglueConnectorPlugin.id, "icons/contentRoot.gif");
		   contentRoot = d.createImage();
		   return contentRoot;
        }
        if(node.getNodeType().equalsIgnoreCase("folder"))
        {
		   imageKey = ISharedImages.IMG_OBJ_FOLDER;
		   return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
        return null;
        
    }
    @Override
    public void dispose() {
    	if(contentRoot != null) contentRoot.dispose();
    	super.dispose();
    }

	public Color getBackground(Object arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Color getForeground(Object element) {
		Color color = null;
		if (element instanceof ContentNode) 
		{
			ContentNode node = (ContentNode) element;
			if(node.getNodeType().equalsIgnoreCase(ContentNode.ITEM))
			{
				if(node.getActiveVersionStateId() ==  null)
				{
					color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
				}
				else if (node.getActiveVersionStateId().equals(ContentVersionVO.PUBLISH_STATE)) {
					color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
				}
				else if (node.getActiveVersionStateId().equals(ContentVersionVO.PUBLISHED_STATE)) {
					color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
				}
			}
			if(node.isRoot() && node.getConnection().isConnected() == false)
			{
				color = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			}
		}
		return color;
	}

	

}