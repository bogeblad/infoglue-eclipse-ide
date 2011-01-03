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
package org.infoglue.igide.helper;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.infoglue.igide.model.content.ContentNode;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class ProjectHelper {

	public static IProject getProject(ContentNode node)
	{
        IProject project = node.getLocalrecource().getProject();
		return project;
	}

	public static IProject getOrCreateProject(String projectName, boolean recreate)
	{
	    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = root.getProject(projectName);
	    
		if(project.exists() && recreate)
		{
			try
			{
				project.delete(true,null);
			} 
			catch (CoreException e) 
			{
				System.out.println("Error deleting project");
			}
		}
		
	    if(!project.exists())
	    {
	    	try 
	    	{
	    		System.out.println("Creating project");
	    		/*
	    		 * Create project
	    		 */
	    		IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
	    		projectDescription.setComment("Infoglue Project");
				project.create(projectDescription, Utils.getMonitor(null));
			} 
	    	catch (CoreException e) 
	    	{
				e.printStackTrace();
			}
	    }
	    
		if(!project.isOpen())
		{
			try 
			{
				project.open(Utils.getMonitor(null));
			} 
			catch (CoreException e1) 
			{
				e1.printStackTrace();
			}
		}

		try {
		    /*
		     * Install facets, java and dynamic web, and infoglue
		     */
		    IFacetedProject fproject = null;
			fproject = ProjectFacetsManager.create(project, true, null);
		    //Set<IProjectFacet> availableFacets = ProjectFacetsManager.getProjectFacets();
		    Set<IProjectFacet> facets = new HashSet<IProjectFacet>();
		    IProjectFacet webFacet = ProjectFacetsManager.getProjectFacet("jst.web");
		    IProjectFacet javaFacet = ProjectFacetsManager.getProjectFacet("jst.java");
		    IProjectFacet igFacet = ProjectFacetsManager.getProjectFacet("igc.web");
		    facets.add(webFacet);
		    facets.add(javaFacet);
		    facets.add(igFacet);
		    
		    /*
		    for(IProjectFacet pf: facets)
		    {
		    	fproject.installProjectFacet(pf.getDefaultVersion(),null, null);
		    }
		    */
	    	fproject.installProjectFacet(javaFacet.getDefaultVersion(), null, null);
	    	fproject.installProjectFacet(webFacet.getDefaultVersion(), null, null);
	    	fproject.installProjectFacet(igFacet.getDefaultVersion(), null, null);
			fproject.setFixedProjectFacets(facets);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	    
		return project;
	}
}
