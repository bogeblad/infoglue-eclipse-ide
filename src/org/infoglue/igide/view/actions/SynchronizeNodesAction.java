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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.helper.ProjectHelper;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.content.ContentNode;


public class SynchronizeNodesAction extends Action {
	private TreeViewer viewer = null;
	private int progess = 0;
	public SynchronizeNodesAction(String text, TreeViewer viewer) {
		super(text);
		this.viewer = viewer;
	}

	public void run() {
		
		final ContentNode node = Utils.getSelectedContentNode(viewer);
		
		Job sync = new Job("Downloading from server: " + node.getConnection().getBaseUrl().toExternalForm()) {
			@Override
			protected IStatus run(IProgressMonitor p) {
				p.beginTask("Downloading content from server...",100);
				/*
				 * Recursively traverse the tree and download content
				 */
				try 
				{
					dumpFromNode(node, p);
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
				p.done();
				return Status.OK_STATUS;
			}
			
			
		};
		sync.schedule();
	}
	
	private void dumpFromNode(ContentNode node, IProgressMonitor p) throws Exception
	{
		p.worked(progess++ );
        IFolder tmp = ProjectHelper.getProject(node).getFolder("WebContent");
        
        /*
         * Dump this node
         */
        try 
        {
			InfoglueCMS.openContentVersion(node);
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
		
        /*
         * Dump the children
         */
        for(Object n: node.getChildren())
        {
        	dumpFromNode((ContentNode) n, p);
        }
        
	}
	
	
}
