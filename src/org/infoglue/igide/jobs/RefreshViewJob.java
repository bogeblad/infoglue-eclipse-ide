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
package org.infoglue.igide.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.progress.UIJob;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.model.content.ContentNode;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class RefreshViewJob extends UIJob {
	
	TreeViewer view;
	Queue<InfoglueConnection> q = new LinkedList<InfoglueConnection>(); 
	
	public void addConnection(InfoglueConnection o)
	{
		synchronized(q)
		{
			q.offer(o);
		}
	}
	
	public RefreshViewJob(TreeViewer viewer) 
	{
		super("Refreshing view");
		this.view = viewer;
		setPriority(Job.SHORT);
	}
	
	@Override
	public IStatus runInUIThread(IProgressMonitor p) {
		synchronized(q)
		{
			InfoglueConnection c = null;
			while( (c = q.poll()) != null)
			{
				ContentNode node  = findConnectedRootNode(c);
				view.refresh(node);
			}
			p.done();
		}
		return Status.OK_STATUS;
	}
	
	private ContentNode findConnectedRootNode(InfoglueConnection connection)
	{
		ContentNode found = null;
		try {
			Object[] nodelist = getNodes();
			for(Object n: nodelist)
			{
				if(n instanceof ContentNode && connection != null)
				{
					ContentNode node = (ContentNode) n;
					if(connection.equals(node.getConnection()))
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
        Object[] elems = view.getExpandedElements();
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

	

}
