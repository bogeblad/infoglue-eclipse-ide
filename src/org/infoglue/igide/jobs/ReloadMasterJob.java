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

import java.awt.Composite;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
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
	public IStatus runInUIThread(IProgressMonitor p) 
	{
		p.beginTask(this.getName(), 100);
		reloadMaster();
		p.worked(80);
		view.getViewer().refresh();
		p.done();
		return Status.OK_STATUS;
	}

	
	public void reloadMaster()
	{
		Logger.logConsole("reloadMaster begins");
		ViewContentProvider provider = (ViewContentProvider) view.getViewer().getContentProvider();
		MasterNode masterNode = (MasterNode) view.getViewer().getInput();
		Logger.logConsole("masterNode:" + masterNode);
        for(ContentNode contentNode: getInfoglueRoots(masterNode))
        {
    		Logger.logConsole("contentNode:" + contentNode.getName());
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
    	    		Logger.logConsole("addNotificationListener:" + provider);
    			} 
        		catch (MalformedURLException e) 
    			{
    				e.printStackTrace();
    	    		Logger.logConsole("Error:" + e.getMessage());
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
                InfoglueConnection connection = new InfoglueConnection(r[1], r[2], r[3], view.getViewer());
				// Create the local project for this root
				IProject proj = ProjectHelper.getOrCreateProject(r[0], false);
				Logger.logConsole("Created project:" + proj);
				roots.add(ContentNodeFactory.createContentRootNode(proj, master, connection));
			}
			catch(IllegalStateException e)
			{
				// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				Logger.logConsole("Failed to get content types for " + r[0] + "(" + r[1] + ")");
				final Shell shell = view.getSite().getShell();
				MessageDialog.openInformation(shell, "Retrieving content types and transaction history failed", "Retrieving content types and transaction history. This could be due to a slow query in InfoGlue.\nTry adding the following index to the database.\n\nRun: create index transactionObjectNameIndex ON cmTransactionHistory (transactionObjectId(255)) if it's slow");
			}
			catch(Exception e)
			{
				Logger.logConsole("Error connecting to CMS at " + r[0]);
			}
		}
		return roots;
	}
	
//	
//	private static class MyDialog extends TitleAreaDialog
//	{
//		private Text firstNameText;
//		private Text lastNameText;
//		private String firstName;
//		private String lastName;
//
//		public MyDialog(Shell parentShell)
//		{
//			super(parentShell);
//		}
//
//		@Override
//		public void create()
//		{
//			super.create();
//			// Set the title
//			setTitle("This is my first own dialog");
//			// Set the message
//			setMessage("This is a TitleAreaDialog", IMessageProvider.INFORMATION);
//
//		}
//
//		@Override
//		protected Control createDialogArea(Composite parent)
//		{
//			GridLayout layout = new GridLayout();
//			layout.numColumns = 2;
//			// layout.horizontalAlignment = GridData.FILL;
//			parent.setLayout(layout);
//
//			// The text fields will grow with the size of the dialog
//			GridData gridData = new GridData();
//			gridData.grabExcessHorizontalSpace = true;
//			gridData.horizontalAlignment = GridData.FILL;
//
//			Label label1 = new Label(parent, SWT.NONE);
//			label1.setText("First Name");
//
//			firstNameText = new Text(parent, SWT.BORDER);
//			firstNameText.setLayoutData(gridData);
//
//			Label label2 = new Label(parent, SWT.NONE);
//			label2.setText("Last Name");
//			// You should not re-use GridData
//			gridData = new GridData();
//			gridData.grabExcessHorizontalSpace = true;
//			gridData.horizontalAlignment = GridData.FILL;
//			lastNameText = new Text(parent, SWT.BORDER);
//			lastNameText.setLayoutData(gridData);
//			return parent;
//		}
//
//		@Override
//		protected void createButtonsForButtonBar(Composite parent)
//		{
//			GridData gridData = new GridData();
//			gridData.verticalAlignment = GridData.FILL;
//			gridData.horizontalSpan = 3;
//			gridData.grabExcessHorizontalSpace = true;
//			gridData.grabExcessVerticalSpace = true;
//			gridData.horizontalAlignment = SWT.CENTER;
//
//			parent.setLayoutData(gridData);
//			// Create Add button
//			// Own method as we need to overview the SelectionAdapter
//			createOkButton(parent, OK, "Add", true);
//			// Add a SelectionListener
//
//			// Create Cancel button
//			Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
//			// Add a SelectionListener
//			cancelButton.addSelectionListener(new SelectionAdapter()
//			{
//				public void widgetSelected(SelectionEvent e)
//				{
//					setReturnCode(CANCEL);
//					close();
//				}
//			});
//		}
//
//		protected Button createOkButton(Composite parent, int id, String label,
//				boolean defaultButton)
//		{
//			// increment the number of columns in the button bar
//			((GridLayout) parent.getLayout()).numColumns++;
//			Button button = new Button(parent, SWT.PUSH);
//			button.setText(label);
//			button.setFont(JFaceResources.getDialogFont());
//			button.setData(new Integer(id));
//			button.addSelectionListener(new SelectionAdapter()
//			{
//				public void widgetSelected(SelectionEvent event)
//				{
//					if (isValidInput())
//					{
//						okPressed();
//					}
//				}
//			});
//			if (defaultButton)
//			{
//				Shell shell = parent.getShell();
//				if (shell != null)
//				{
//					shell.setDefaultButton(button);
//				}
//			}
//			setButtonLayoutData(button);
//			return button;
//		}
//
//		private boolean isValidInput()
//		{
//			boolean valid = true;
//			if (firstNameText.getText().length() == 0)
//			{
//				setErrorMessage("Please maintain the first name");
//				valid = false;
//			}
//			if (lastNameText.getText().length() == 0)
//			{
//				setErrorMessage("Please maintain the last name");
//				valid = false;
//			}
//			return valid;
//		}
//
//		@Override
//		protected boolean isResizable()
//		{
//			return true;
//		}
//
//		// Coyy textFields because the UI gets disposed
//		// and the Text Fields are not accessible any more.
//		private void saveInput()
//		{
//			firstName = firstNameText.getText();
//			lastName = lastNameText.getText();
//		}
//
//		@Override
//		protected void okPressed()
//		{
//			saveInput();
//			super.okPressed();
//		}
//
//		public String getFirstName()
//		{
//			return firstName;
//		}
//
//		public String getLastName()
//		{
//			return lastName;
//		}
//	}


}
