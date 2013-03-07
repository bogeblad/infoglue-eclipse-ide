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
package org.infoglue.igide.view.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.infoglue.igide.cms.ContentTypeDefinition;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.content.ContentNode;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class CreateNewContentAction extends Action {

	private ContentNode parentNode;
	private ContentTypeDefinition definition;
	private TreeViewer viewer;
	
	public CreateNewContentAction(ContentNode parentNode, ContentTypeDefinition definition, TreeViewer viewer)
	{
		this.parentNode = parentNode;
		this.definition = definition;
		this.viewer = viewer;

		//Logger.logConsole("name:" + getName());
		setText(getName());
		try 
		{
			setImageDescriptor(getImage());
		}
		catch(Exception e)
		{
			
		}

	}
	
	private String getName() 
	{
		return definition.getName().equals("HTMLTemplate") ? "Component (HTMLTemplate)": definition.getName();
	}

	private ImageDescriptor getImage() 
	{
		if(definition.getName().equals("HTMLTemplate"))
		{
			return Utils.getImage("contentTypes/make.png");
		}
		if(definition.getName().toLowerCase().indexOf("doc")>-1)
		{
			return Utils.getImage("contentTypes/doc.png");
		}
		if(definition.getName().toLowerCase().indexOf("article")>-1)
		{
			return Utils.getImage("contentTypes/doc.png");
		}
		if(definition.getName().toLowerCase().indexOf("image")>-1)
		{
			return Utils.getImage("contentTypes/image.png");
		}
		if(definition.getName().toLowerCase().indexOf("taskdefinition")>-1)
		{
			return Utils.getImage("contentTypes/shellscript.png");
		}
		if(definition.getName().toLowerCase().indexOf("news")>-1)
		{
			return Utils.getImage("contentTypes/news.png");
		}
		if(definition.getName().toLowerCase().indexOf("flash")>-1)
		{
			return Utils.getImage("contentTypes/flash.png");
		}
		if(definition.getName().toLowerCase().indexOf("contact")>-1)
		{
			return Utils.getImage("contentTypes/personal.png");
		}
		return Utils.getImage("file_doc.png");
	}

	@Override
	public void run() 
	{
		InputDialog dialog = new InputDialog(null, "Create new " + definition.getName(), "State the name of the new " + definition.getName(), "New " + definition.getName(), null);
		dialog.open();
		if(dialog.getReturnCode()== Window.OK)
		{
			Logger.logConsole("Will create content node");
			String name = dialog.getValue();
            InfoglueCMS.createContent(parentNode.getConnection(), parentNode.getId(), parentNode.getRepositoryId(), name, definition.getId(), false);
			//InfoglueCMS.createContent(parentNode.getConnection(), parentNode.getId(), parentNode.getRepositoryId(), name, definition.getId());
            viewer.refresh(Utils.getSelectedContentNode(viewer));
		}
		else
		{
			Logger.logConsole("returncode: " + dialog.getReturnCode());
		}
		// MessageDialog.openInformation(null,"Create new " + definition.getName(), "Create a new " + definition.getName() + " in folder " + parentNode.toString());
		
	}
	
}
