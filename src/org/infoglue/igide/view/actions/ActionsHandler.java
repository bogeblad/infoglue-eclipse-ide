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

import org.dom4j.DocumentException;
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

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class ActionsHandler {
	
	private final TreeViewer viewer;
	private ViewPart viewPart;
	
	private Action refresh;
	private Action newFolder;
	private Action publish;
	private Action createWorking;
	private Action synchronizeNodes;
	private Action rename;
	private Action deleteVersion;
	private Action delete;
	private Action openInBrowser;
	private Action openVersion;
	private Action preview;
	private Action doubleClickAction;
	

	public ActionsHandler(final TreeViewer viewer, ViewPart viewPart) {
		this.viewer = viewer;
		this.viewPart = viewPart;
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void hookContextMenu() {
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) 
			{
				try {
			        ISelection selection = viewer.getSelection();
			        ContentNode node = (ContentNode) ((IStructuredSelection) selection).getFirstElement();
			        fillContextMenu(node, manager);
				}
				finally{
					fillContextMenu(manager);
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		viewPart.getSite().registerContextMenu(menuMgr, viewer);
	}
	
	
	/*
	 * Standard contextmenu
	 */
	public void fillContextMenu(IMenuManager manager) {
		
		 manager.add(new Separator());
		 manager.add(refresh);
		 
		 // Other plug-ins can contribute there actions here
		 manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));		
		 
	}
	

	/*
	 * Folder and repository context menu
	 */
	public void fillFolderContextMenu(ContentNode node, IMenuManager manager) {
		
		try {
			manager.add(createNewActionSubMenu(node));
		} catch (InvalidLoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		manager.add(synchronizeNodes);
		
	}
	
	private MenuManager createNewActionSubMenu(final ContentNode node) throws InvalidLoginException, IOException, DocumentException
	{
		MenuManager menu = new MenuManager("New");
		menu.add(newFolder);
		
		ContentTypeDefinition htmlTemplate = node.getConnection().getInfoglueProxy().getContentTypeDefinition("HTMLTemplate");
		menu.add(new CreateNewContentAction(node, htmlTemplate));
		menu.add(new Separator("#ContentTypeDefinitions"));
		
		for(ContentTypeDefinition definition: node.getConnection().getInfoglueProxy().getContentTypeDefinitions())
		{
			if(!definition.getName().equals("HTMLTemplate"))
			{
				menu.add(new CreateNewContentAction(node, definition));
			}
		}
		
		
		return menu;
	}
	
	/*
	 * Document context menu
	 */
	public void fillDocContextMenu(ContentNode node, IMenuManager manager) {
		if(node.getActiveVersionStateId().equals(0))
		{
			manager.add(publish);
		}
		else
		{
			manager.add(createWorking);
		}
	}
	
	public void fillContextMenu(ContentNode node, IMenuManager manager) {
		if(node.getNodeType().equals(ContentNode.REPOSITORY))
		{
			System.out.println("ITS A REPO");
			fillFolderContextMenu(node, manager);
		}
		else if(node.getNodeType().equals(ContentNode.FOLDER))
		{
			System.out.println("ITS A FOLDER");
			fillFolderContextMenu(node, manager);
		}
		else if(node.getNodeType().equals(ContentNode.ITEM))
		{
			System.out.println("ITS A DOC");
			fillDocContextMenu(node, manager);
		}
	}

	/* ---------------------------------------
	 * ACTIONS 
	 * --------------------------------------- 
	 */
	private void makeActions() {
		/*
		 * Doubleclick
		 */
		doubleClickAction = new DoubleClickAction(viewer);
		
		
		
		/*
		 * Refresh
		 */
		refresh = new Action("Refresh") {
			public void run() {
		        viewer.refresh(Utils.getSelectedContentNode(viewer));
			}
		};
		refresh.setAccelerator(SWT.F5);
		refresh.setToolTipText("Refreshes this node with remote system");
		refresh.setImageDescriptor(Utils.getImage("actions/reload.png"));
		
		/*
		 * New folder action
		 */
		newFolder = new Action("Folder")
		{
			public void run() {
				ContentNode node = Utils.getSelectedContentNode(viewer);
				MessageDialog.openInformation(viewer.getControl().getShell(),"Folder", node.toString());
			}
		};
		newFolder.setImageDescriptor(Utils.getImage("actions/folder.png"));

		/*
		 * Publish action
		 */
		publish = new Action("Publish...")
		{
			public void run() {
				ContentNode node = Utils.getSelectedContentNode(viewer);
				MessageDialog.openInformation(viewer.getControl().getShell(),"publish",node.toString());
			}
		};
		publish.setImageDescriptor(Utils.getImage("actions/db_comit.png"));
		
		/*
		 * Create working version action
		 */
		createWorking = new Action("Create working copy")
		{
			public void run() {
				ContentNode node = Utils.getSelectedContentNode(viewer);
				MessageDialog.openInformation(viewer.getControl().getShell(),"Create working copy",node.toString());
			}
		};
		createWorking.setImageDescriptor(Utils.getImage("actions/edit.png"));
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
	
}
