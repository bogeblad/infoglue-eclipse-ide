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

import java.util.Map;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.infoglue.igide.InfoglueConnectorPlugin;
import org.infoglue.igide.ProjectResourceChangeListener;
import org.infoglue.igide.cms.*;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.InfoglueProxy;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.jobs.ReloadMasterJob;
import org.infoglue.igide.model.MasterNode;
import org.infoglue.igide.model.content.ContentNode;
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
	
	private static ContentExplorerView contentExplorerView;
	
	public static ContentExplorerView getInstance()
	{
		return ContentExplorerView.contentExplorerView;
	}
	
	/**
	 * The constructor.
	 */
	public ContentExplorerView() 
	{
		Logger.logConsole("Initializing ContentExplorerView");
		reloadMasterJob = new ReloadMasterJob(this);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	
	public void createPartControl(Composite parent) 
	{
		Logger.logConsole("createPartControl");

		viewer = new TreeViewer(parent);
		ContentExplorerView.contentExplorerView = this;
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(new MasterNode("master"));
		viewer.setSorter(null);
		new ActionsHandler(viewer, this);
        ProjectResourceChangeListener.setContentExplorerView(this, viewer);
		initialized = true;
		Logger.logConsole("reloadMasterJob.schedule() start");
		reloadMasterJob.schedule();
		InfoglueConnectorPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

    public ContentNode getNodeWithPath(IResource file, IPath path, boolean createIfNotExists)
    {
        ContentNode node = null;
        Logger.logConsole((new StringBuilder("path:")).append(path).toString());
        int pathIndex = 2;
        try
        {
            MasterNode masterNode = (MasterNode)viewer.getInput();
            Logger.logConsole((new StringBuilder("masterNode:")).append(masterNode.toString()).toString());
            ContentNode rootContentNode = null;
            for(int i = 0; i < masterNode.getChildren().length; i++)
            {
                ContentNode rootContentNodeCandidate = (ContentNode)masterNode.getChildren()[i];
                String siteNodeName = rootContentNodeCandidate.getName();
                String pathSegment = path.segment(0);
                Logger.logConsole((new StringBuilder("siteNodeName:")).append(siteNodeName).toString());
                Logger.logConsole((new StringBuilder("pathSegment:")).append(pathSegment).append("(").append(path.segment(1)).append(")").toString());
                if(!siteNodeName.equals(pathSegment))
                    continue;
                rootContentNode = rootContentNodeCandidate;
                break;
            }

            if(rootContentNode != null)
            {
                Logger.logConsole((new StringBuilder("Using rootContentNode:")).append(rootContentNode.getName()).toString());
                node = matchCurrentEntry(file, path, rootContentNode, pathIndex, createIfNotExists, rootContentNode.getConnection());
                Logger.logConsole((new StringBuilder("node:")).append(node).toString());
            }
        }
        catch(Exception e)
        {
            Logger.logConsole((new StringBuilder("Error looking for node by path:")).append(e.getMessage()).toString(), e);
        }
        return node;
    }

    private ContentNode matchCurrentEntry(IResource iFile, IPath path, ContentNode parentContentNode, int pathIndex, boolean createIfNotExists, InfoglueConnection conn)
        throws Exception
    {
        ContentNode node = null;
        String pathSegment = path.segment(pathIndex);
        Logger.logConsole((new StringBuilder("Name1:")).append(parentContentNode.getName()).toString());
        Logger.logConsole((new StringBuilder("RepId:")).append(parentContentNode.getRepositoryId()).toString());
        Logger.logConsole((new StringBuilder("pathSegment:")).append(pathSegment).append(":").append(parentContentNode.getName()).append(":").append(pathIndex).toString());
        boolean found = false;
        Object children[] = parentContentNode.getChildren();
        for(int i = 0; i < children.length; i++)
        {
            ContentNode subContentNode = (ContentNode)children[i];
            String name = subContentNode.getName();
            String text = subContentNode.getText();
            Logger.logConsole((new StringBuilder("\tsubContentNode:")).append(name).append("(").append(text).append(")").append("=").append(pathSegment).toString());
            if(!pathSegment.equalsIgnoreCase(text) && !pathSegment.startsWith((new StringBuilder(String.valueOf(text))).append("_").toString()))
                continue;
            Logger.logConsole((new StringBuilder("\tFound match:")).append(pathIndex).append(":").append(path.segmentCount()).append(path.segment(pathIndex)).append(":").append(path.segment(pathIndex + 1)).toString());
            if(pathIndex < path.segmentCount() && path.segment(pathIndex + 1) != null)
                node = matchCurrentEntry(iFile, path, subContentNode, pathIndex + 1, createIfNotExists, conn);
            else
                node = subContentNode;
            found = true;
            break;
        }

        if(!found && createIfNotExists)
        {
            Logger.logConsole((new StringBuilder("The pathSegment:")).append(pathSegment).append(" did not exist - let's create it").toString());
            String name = pathSegment;
            Logger.logConsole((new StringBuilder("name1:")).append(name).toString());
            name = name.replaceFirst("_\\d.xml", "");
            Logger.logConsole((new StringBuilder("name2:")).append(name).toString());
            ContentTypeDefinition defComponent = conn.getInfoglueProxy().getContentTypeDefinition("HTMLTemplate");
            ContentTypeDefinition defFolder = conn.getInfoglueProxy().getContentTypeDefinition("Folder");
            Integer contentId = null;
            boolean wasLeaf = false;
            Logger.logConsole((new StringBuilder("parentContentNode.getRepositoryId():")).append(parentContentNode.getRepositoryId()).toString());
            if(parentContentNode.getRepositoryId() == null)
            {
                Logger.logConsole((new StringBuilder("Must be a repository:")).append(parentContentNode.getRepositoryId()).toString());
                Map result = InfoglueCMS.createRepository(conn, name, "Auto", "undefined");
                contentId = (Integer)result.get("contentId");
            } else
            if(pathSegment.indexOf(".xml") > -1)
            {
                String versionValue = Utils.getIFileContentAsString(iFile);
                if(name.endsWith(".xml"))
                    name = name.substring(0, name.lastIndexOf(".xml"));
                Logger.logConsole((new StringBuilder("name3:")).append(name).toString());
                if(name.lastIndexOf("_") > -1)
                    name = name.substring(0, name.lastIndexOf("_"));
                Logger.logConsole((new StringBuilder("name4:")).append(name).toString());
                if(parentContentNode.getParentNode().getRepositoryId() == null)
                {
                    Logger.logConsole((new StringBuilder("Must be a first level node:")).append(name).toString());
                    Content rootContent = conn.getInfoglueProxy().fetchRootContent(parentContentNode.getRepositoryId());
                    Logger.logConsole((new StringBuilder("rootContent:")).append(rootContent).toString());
                    contentId = InfoglueCMS.createContent(conn, rootContent.getId(), parentContentNode.getRepositoryId(), name, defComponent.getId(), versionValue);
                } else
                {
                    contentId = InfoglueCMS.createContent(conn, parentContentNode.getId(), parentContentNode.getRepositoryId(), name, defComponent.getId(), versionValue);
                }
                wasLeaf = true;
            } else
            {
                Logger.logConsole((new StringBuilder("Creating content:")).append(name).toString());
                Logger.logConsole((new StringBuilder("Creating content:")).append(parentContentNode.getRepositoryId()).toString());
                Logger.logConsole((new StringBuilder("Creating content:")).append(parentContentNode.getParentNode().getRepositoryId()).toString());
                if(parentContentNode.getParentNode().getRepositoryId() == null)
                {
                    Logger.logConsole((new StringBuilder("Must be a first level node:")).append(name).toString());
                    Content rootContent = conn.getInfoglueProxy().fetchRootContent(parentContentNode.getRepositoryId());
                    Logger.logConsole((new StringBuilder("rootContent:")).append(rootContent).toString());
                    contentId = InfoglueCMS.createContent(conn, rootContent.getId(), parentContentNode.getRepositoryId(), name, defFolder.getId(), true);
                } else
                {
                    contentId = InfoglueCMS.createContent(conn, parentContentNode.getId(), parentContentNode.getRepositoryId(), name, defFolder.getId(), true);
                }
            }
            if(contentId != null && !wasLeaf)
            {
                Logger.logConsole((new StringBuilder("Content created:")).append(contentId).toString());
                Thread.sleep(2000L);
                Logger.logConsole("We should continue now... press forward with the new node...");
                Object children2[] = parentContentNode.getChildren();
                for(int i = 0; i < children2.length; i++)
                {
                    ContentNode subContentNode = (ContentNode)children2[i];
                    String subContentName = subContentNode.getName();
                    String subContentText = subContentNode.getText();
                    Logger.logConsole((new StringBuilder("\tsubContentNode:")).append(subContentName).append("(").append(subContentText).append(")").append("=").append(pathSegment).append(" should match name:").append(name).toString());
                    if(!pathSegment.equalsIgnoreCase(subContentText) && !pathSegment.startsWith((new StringBuilder(String.valueOf(subContentText))).append("_").toString()) && !subContentName.equals(name))
                        continue;
                    Logger.logConsole((new StringBuilder("\tFound match:")).append(pathIndex).append(":").append(path.segmentCount()).append(path.segment(pathIndex)).append(":").append(path.segment(pathIndex + 1)).toString());
                    if(pathIndex < path.segmentCount() && path.segment(pathIndex + 1) != null)
                    {
                        node = matchCurrentEntry(iFile, path, subContentNode, pathIndex + 1, createIfNotExists, conn);
                    } else
                    {
                        node = subContentNode;
                        found = true;
                    }
                    break;
                }

            }
        }
        return node;
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