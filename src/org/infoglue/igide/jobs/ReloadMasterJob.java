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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.infoglue.igide.InfoglueConnectorPlugin;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.ProjectHelper;
import org.infoglue.igide.model.MasterNode;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.model.content.ContentNodeFactory;
import org.infoglue.igide.preferences.InfogluePreferencePage;
import org.infoglue.igide.view.ContentExplorerView;
import org.infoglue.igide.view.ViewContentProvider;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class ReloadMasterJob extends UIJob {
	
	ContentExplorerView view;
	
	public ReloadMasterJob(final ContentExplorerView view) 
	{
		super("Refreshing Infoglue Content Explorer: ");
		this.view = view;
		setPriority(Job.SHORT);
	}
	
	@Override
	public IStatus runInUIThread(IProgressMonitor p) {
		p.beginTask(this.getName(), 100);
		reloadMaster();
		p.worked(80);
		view.getViewer().refresh();
		p.done();
		return Status.OK_STATUS;
	}

	
	public void reloadMaster()
	{
		ViewContentProvider provider = (ViewContentProvider) view.getViewer().getContentProvider();
		MasterNode masterNode = (MasterNode) view.getViewer().getInput();
		
        for(ContentNode contentNode: getInfoglueRoots(masterNode))
        {
    		if(masterNode.addIfNotContains(contentNode))
    		{
        		/*
        		 * Set up a notification listener for each connection,
        		 * let the provider class (ViewContentProvider)
        		 * handle the CMS model changes.
        		 */
        		try 
        		{
    				contentNode.getConnection().addNotificationListener(provider);
    			} 
        		catch (MalformedURLException e) 
    			{
    				e.printStackTrace();
    			}
    		}
        }
	}

	private List<ContentNode> getInfoglueRoots(MasterNode master)
	{
		// projectname,baseurl,username,password
	    String value = InfoglueConnectorPlugin.getDefault().getPreferenceStore().getString(InfogluePreferencePage.P_PROJECTS);
	    String[] sRoots = value.split(";");
		
		List<ContentNode> roots = new ArrayList<ContentNode>();
		for(String s: sRoots)
		{
			String[] r = s.split(",");
			try 
			{
				// Create the connection for this root
				InfoglueConnection connection = new InfoglueConnection(r[1], r[2], r[3]);
				// Create the local project for this root
				IProject proj = ProjectHelper.getOrCreateProject(r[0], false);
				System.out.println("Created project");
				roots.add(ContentNodeFactory.createContentRootNode(proj, master, connection));
			}
			catch(Exception e)
			{
				Logger.logConsole("Error connecting to CMS at " + r[0]);
			}
		}
		return roots;
	}
	


}
