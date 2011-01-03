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


import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.infoglue.igide.InfoglueConnectorPlugin;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.jobs.ReloadMasterJob;
import org.infoglue.igide.model.MasterNode;
import org.infoglue.igide.preferences.InfogluePreferencePage;
import org.infoglue.igide.view.actions.ActionsHandler;


/**
 * @author Stefan Sik
 * 
 */

public class ContentExplorerView extends ViewPart implements IPropertyChangeListener {
	
	private TreeViewer viewer;
	private boolean initialized;
	private Job reloadMasterJob;
	
	/**
	 * The constructor.
	 */
	public ContentExplorerView() {
		reloadMasterJob = new ReloadMasterJob(this);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	
	public void createPartControl(Composite parent) 
	{
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(new MasterNode("master"));
		viewer.setSorter(null);
		new ActionsHandler(viewer, this);
		initialized = true;
		reloadMasterJob.schedule();
		
		/*
		 * Listen for preference changes
		 */
		InfoglueConnectorPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		
		/*
		 * Experiment, listen for workspace changes
		 */
		/*
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener(){

			public void resourceChanged(IResourceChangeEvent r) {
				Logger.logConsole("Resource changed!!!, type: " + r.getType() + ", projname: " + r.getResource().getProject().getName());
				Logger.logConsole(r.getDelta().toString());
				
			}}, IResourceChangeEvent.POST_CHANGE);
		*/
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) 
	{
		if(event.getProperty().equals(InfogluePreferencePage.P_PROJECTS))
		{
			reloadMasterJob.schedule();
		}
	}
}