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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.NotificationMessage;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.NotificationListener;
import org.infoglue.igide.jobs.RefreshContentTypeDefinitions;
import org.infoglue.igide.jobs.RefreshViewJob;
import org.infoglue.igide.model.INode;
import org.infoglue.igide.model.content.ContentNode;


/**
 * 
 * @author Stefan Sik
 *
 */

/**
 * This class provides a suitable view of the model (infoglueCMS) for the Infoglue Explorer tree
 * it also registeres for notification of modelchanges sent out by the cms server.
 */

public class ViewContentProvider implements ITreeContentProvider, NotificationListener {
	
	private TreeViewer viewer;
	RefreshViewJob refreshView;

	public ViewContentProvider() {
	}

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ContentNode)
		{
			ContentNode parent = (ContentNode) parentElement;
			if(parent.getConnection().isConnected())
			{
				try 
				{
					return parent.getChildren();
				} 
				catch (Exception e) 
				{
					return new Object[0];		
				}
			}
		}
		else
		{
			try {
				return ((INode) parentElement).getChildren();
			} catch (Exception e) {
				return new Object[0];
			}
		}
		
		return new Object[0];		
	}

	public Object getParent(Object element) {
		try {
			return ((INode) element).getParentNode();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean hasChildren(Object element) {
		if(element == null)
			return false;
		
		INode node = (INode) element;
		return node.hasChildren();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
	
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TreeViewer)
		{
			this.viewer = (TreeViewer) viewer;
			refreshView = new RefreshViewJob(this.viewer);
		}
	}
	
	private ContentNode findOpenNode(Integer nodeId)
	{
		ContentNode found = null;
		try {
			Object[] nodelist = getNodes();
			for(Object n: nodelist)
			{
				if(n instanceof ContentNode && nodeId != null)
				{
					ContentNode node = (ContentNode) n;
					if(nodeId.equals(node.getId()))
					{
						found = node;
						break;
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return found;
	}
	
	private Object[] getNodes() throws Exception
	{
		List<Object> l = new ArrayList<Object>();
        Object[] elems = viewer.getExpandedElements();
        for(Object elm: elems)
        {
        	l.add(elm);
        	if(elm instanceof ContentNode)
        	{
        		ContentNode node = (ContentNode) elm;
            	l.addAll(Arrays.asList(node.getChildren()));
        	}
        }
		return l.toArray();
	}

	public void recieveCMSNotification(NotificationMessage message) 
	{
        System.out.println("Recieved not from CMS: " + message.getClassName());
        Logger.logConsole(message.toString());
        
		if(viewer != null)
		{
	        if(message.getClassName().equals("org.infoglue.cms.entities.content.impl.simple.ContentImpl"))
	        {
	        	System.out.println("Recieved a content change, we will try to refresh the tree.");
	            ContentNode node = findOpenNode(new Integer("" + message.getObjectId()));
	            
	        	if(node != null)
	        	{
	        		System.out.println("ContentChange!!!, content: " + node.getText());
	        		viewer.refresh(node);
	        	}
	        }
			
		}
		
        if(message.getClassName().equals("org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl"))
        {
        	new RefreshContentTypeDefinitions(message.getConnection()).schedule();
        }
	}

	public void connectedToRemoteSystem(InfoglueConnection connection) {
		refreshView.addConnection(connection);
		refreshView.schedule();
	}

	public void connectionException(InfoglueConnection connection, Exception e) {
		refreshView.addConnection(connection);
		refreshView.schedule();
	}

	public void disconnectedFromRemoteSystem(InfoglueConnection connection) {
		refreshView.addConnection(connection);
		refreshView.schedule();
	}
}