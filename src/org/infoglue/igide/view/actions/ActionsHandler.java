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

import java.io.IOException;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.infoglue.igide.cms.ContentTypeDefinition;
import org.infoglue.igide.cms.exceptions.InvalidLoginException;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.view.ContentExplorerView;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.jobs.RefreshServerJob;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class ActionsHandler
{
	
	private final TreeViewer viewer;
	private ViewPart viewPart;
	
	private Action refresh;
	private Action newFolder;
	private Action publish;
	private Action createWorking;
    private Action synchronize;
	private Action synchronizeNodes;
	private Action rename;
	private Action deleteVersion;
	private Action delete;
	private Action openInBrowser;
	private Action openVersion;
	private Action preview;
	private Action doubleClickAction;
	

	public ActionsHandler(TreeViewer viewer, ViewPart viewPart) 
    {
        Logger.logConsole((new StringBuilder("New actions handler for:")).append(viewer.getClass().getName()).append(":").append(viewPart.getClass().getName()).toString());
		this.viewer = viewer;
		this.viewPart = viewPart;
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
	}
	
	private void hookDoubleClickAction() 
	{
		viewer.addDoubleClickListener(new IDoubleClickListener() 
		{
			public void doubleClick(DoubleClickEvent event) 
			{
                Logger.logConsole((new StringBuilder("doubleClick:")).append(event).toString());
				doubleClickAction.run();
			}
		});
	}
	
	private void hookContextMenu() 
	{
		Logger.logConsole("hookContextMenu");
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) 
			{
				try 
				{
			        ISelection selection = viewer.getSelection();
			        Logger.logConsole("selection:" + selection);
			        ContentNode node = (ContentNode) ((IStructuredSelection) selection).getFirstElement();
			        Logger.logConsole("node:" + node);
			        fillContextMenu(node, manager);
				}
				catch (Exception e) 
				{
					Logger.logConsole("Error:" + e.getMessage());
				}
				finally
				{
					fillContextMenu(manager);
				}
				Logger.logConsole("Ready to show:" + manager);
				Logger.logConsole("Ready to show:" + manager.getItems());
				Logger.logConsole("Ready to show:" + manager.getItems() + ":" + manager.isVisible());
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		Logger.logConsole("menu:" + menu);
		viewer.getControl().setMenu(menu);
		Logger.logConsole("viewer:" + viewer);
		viewPart.getSite().registerContextMenu(menuMgr, viewPart.getSite().getSelectionProvider());
		viewPart.getSite().registerContextMenu(menuMgr, viewer);
		Logger.logConsole("Site:" + viewPart.getSite() + ":" + viewPart.getSite().getRegisteredName());
	}
	
	
	/*
	 * Standard contextmenu
	 */
	public void fillContextMenu(IMenuManager manager) 
	{
		Logger.logConsole("FillContext menu:" + manager);
		manager.add(new Separator());
		manager.add(refresh);
		manager.add(new Separator("additions"));
		Logger.logConsole("FillContext menu end:" + manager);
	}
	

	/*
	 * Folder and repository context menu
	 */
	public void fillFolderContextMenu(ContentNode node, IMenuManager manager) {
		
		try {
			manager.add(createNewActionSubMenu(node));
            manager.add(synchronize);
		} catch (InvalidLoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		//manager.add(synchronizeNodes);
		
	}
	
	private MenuManager createNewActionSubMenu(ContentNode node) throws InvalidLoginException, IOException, DocumentException
	{
		MenuManager menu = new MenuManager("New");
		try
		{
			menu.add(newFolder);
			
			ContentTypeDefinition htmlTemplate = node.getConnection().getInfoglueProxy().getContentTypeDefinition("HTMLTemplate");
			//Logger.logConsole("htmlTemplate:" + htmlTemplate);
			menu.add(new CreateNewContentAction(node, htmlTemplate, this.viewer));
			menu.add(new Separator("#ContentTypeDefinitions"));
			
			for(Iterator<ContentTypeDefinition> iterator = node.getConnection().getInfoglueProxy().getContentTypeDefinitions().iterator(); iterator.hasNext();)
	        {
	            ContentTypeDefinition definition = iterator.next();
				//Logger.logConsole("definition:" + definition);
	            if(!definition.getName().equals("HTMLTemplate"))
	                menu.add(new CreateNewContentAction(node, definition, this.viewer));
	        }		
		}
		catch (Exception e) 
		{
			Logger.logConsole("Error generating content type list: " + e.getMessage(), e);
		}
		return menu;
	}
	
	/*
	 * Document context menu
	 */
	public void fillDocContextMenu(ContentNode node, IMenuManager manager) 
	{
		Logger.logConsole("node.getActiveVersionStateId():" + node.getActiveVersionStateId());
		if(node.getActiveVersionStateId().equals(0))
		{
			manager.add(publish);
		}
		else
		{
			manager.add(createWorking);
		}
	}
	
	public void fillContextMenu(ContentNode node, IMenuManager manager) 
	{
		Logger.logConsole("node:" + node.getName() + ":" + node.getClass().getName());
		if(node.getNodeType().equals(ContentNode.REPOSITORY))
		{
			Logger.logConsole("ITS A REPO");
			fillFolderContextMenu(node, manager);
		}
		else if(node.getNodeType().equals(ContentNode.FOLDER))
		{
			Logger.logConsole("ITS A FOLDER");
			fillFolderContextMenu(node, manager);
		}
		else if(node.getNodeType().equals(ContentNode.ITEM))
		{
			Logger.logConsole("ITS A DOC");
			fillDocContextMenu(node, manager);
		}
	}

	/* ---------------------------------------
	 * ACTIONS 
	 * --------------------------------------- 
	 */
	private void makeActions() 
	{
        doubleClickAction = new DoubleClickAction(viewer);
        refresh = new Action("Refresh") {

            public void run()
            {
                checkConnection(Utils.getSelectedContentNode(viewer), (ContentExplorerView)viewPart);
                viewer.refresh(Utils.getSelectedContentNode(viewer));
            }
        };
		//refresh.setAccelerator(SWT.F5);
		refresh.setToolTipText("Refreshes this node with remote system");
		refresh.setImageDescriptor(Utils.getImage("actions/reload.png"));
		
		newFolder = new Action("Folder")
		{
			public void run() {
				ContentNode node = Utils.getSelectedContentNode(viewer);
				MessageDialog.openInformation(viewer.getControl().getShell(),"Folder", node.toString());
			}
		};
		newFolder.setImageDescriptor(Utils.getImage("actions/folder.png"));

		publish = new Action("Publish...")
		{
			public void run() {
				ContentNode node = Utils.getSelectedContentNode(viewer);
				MessageDialog.openInformation(viewer.getControl().getShell(),"publish",node.toString());
			}
		};
		publish.setImageDescriptor(Utils.getImage("actions/db_comit.png"));
		
		createWorking = new Action("Create working copy")
		{
			public void run() {
				ContentNode node = Utils.getSelectedContentNode(viewer);
				MessageDialog.openInformation(viewer.getControl().getShell(),"Create working copy",node.toString());
			}
		};
		createWorking.setImageDescriptor(Utils.getImage("actions/edit.png"));
	
	    synchronize = new Action("Synchronize project") {

            public void run()
            {
                Logger.logConsole("Synchronize project-----------------..");
                ContentNode node = Utils.getSelectedContentNode(viewer);
                Logger.logConsole((new StringBuilder("node:")).append(node).toString());
                ISynchronizeManager mgr = TeamUI.getSynchronizeManager();
                ISynchronizeView view = mgr.showSynchronizeViewInActivePage();
                view.setFocus();
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                Logger.logConsole((new StringBuilder("workspace:")).append(workspace).toString());
                IProject project = node.getProject();
                Logger.logConsole((new StringBuilder("project:")).append(project).toString());
                try
                {
                    IResource resources[] = project.members();
                    Logger.logConsole((new StringBuilder("resources:")).append(resources.length).toString());
                    IResource webContentsFolder = null;
                    for(int i = 0; i < resources.length; i++)
                    {
                        IResource resource = resources[i];
                        Logger.logConsole((new StringBuilder("resource:")).append(resource.getName()).toString());
                        if(resource.getName().indexOf("WebContent") > -1)
                            webContentsFolder = resource;
                    }

                    Logger.logConsole((new StringBuilder("webContentsFolder:")).append(webContentsFolder).toString());
                    if(webContentsFolder != null)
                    {
                        org.eclipse.team.ui.synchronize.ISynchronizeScope scope = new ResourceScope(new IResource[] {
                            webContentsFolder
                        });
                        WorkspaceSynchronizeParticipant participant = new WorkspaceSynchronizeParticipant(scope);
                        participant.refresh(new IResource[] {
                            webContentsFolder
                        }, view.getSite());
                        participant.reset();
                        mgr.addSynchronizeParticipants(new ISynchronizeParticipant[] {
                            participant
                        });
                    } else
                    {
                        MessageDialog.openInformation(viewer.getControl().getShell(), "Project was not correctly configured", node.toString());
                    }
                }
                catch(Exception e)
                {
                    Logger.logConsole((new StringBuilder("Error:")).append(e.getMessage()).toString());
                }
            }
		 };
		 
	 	synchronize.setImageDescriptor(Utils.getImage("actions/db_comit.png"));
        synchronizeNodes = new SynchronizeNodesAction("Update from CMS", viewer);
        synchronizeNodes.setImageDescriptor(Utils.getImage("actions/download.png"));
	}
	
	private void contributeToActionBars() 
	{
		IActionBars bars = viewPart.getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
	}
	
    private void checkConnection(ContentNode contentNode, ContentExplorerView view)
    {
        Logger.logConsole("Checking connection...");
        RefreshServerJob refreshServerJob = new RefreshServerJob(view, contentNode);
        refreshServerJob.schedule();
    }
}
