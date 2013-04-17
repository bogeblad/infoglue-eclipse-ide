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

import java.io.IOException;

import org.dom4j.DocumentException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.exceptions.InvalidLoginException;
import org.infoglue.igide.helper.Logger;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class RefreshContentTypeDefinitions extends Job {

	InfoglueConnection connection = null;
	
	public RefreshContentTypeDefinitions(InfoglueConnection connection) 
	{
		this("Refreshing contenttype definitions", connection);
	}
	
	public RefreshContentTypeDefinitions(String name, InfoglueConnection connection) 
	{
		super(name);
		this.connection = connection;
		setPriority(Job.SHORT);
	}

	@Override
	protected IStatus run(IProgressMonitor p) 
	{
		Logger.logConsole("Refreshing contenttype definitions from " + connection.getBaseUrl().toString());
		p.beginTask("Refreshing contenttype definitions from " + connection.getBaseUrl().toString(), IProgressMonitor.UNKNOWN);

		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					Logger.logConsole("Start refreshing...");
					connection.getInfoglueProxy().refreshContentTypeDefinitions();
				}
				catch(IllegalStateException e)
				{
					// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					Logger.logConsole("Failed to refresh content types for " + connection.getBaseUrl());
					Shell shell = Display.getCurrent().getActiveShell();
					MessageDialog.openInformation(shell, "Retrieving content types and transaction history failed", "Retrieving content types and transaction history. This could be due to a slow query in InfoGlue.\nTry adding the following index to the database.\n\nRun: create index transactionObjectNameIndex ON cmTransactionHistory (transactionObjectId(255)) if it's slow");
				}
				catch (InvalidLoginException e) 
				{
					Logger.logConsole("Error refreshing:" + e.getMessage());
				}
				catch (IOException e) 
				{
					Logger.logConsole("Error refreshing:" + e.getMessage());
				}
				catch (DocumentException e) 
				{
					Logger.logConsole("Error refreshing:" + e.getMessage());
				}
				catch (Exception e) 
				{
					Logger.logConsole("Error refreshing:" + e.getMessage());
				}
				Logger.logConsole("Done refreshing...");
			}
		});

		p.done();
		return Status.OK_STATUS;
	}

}
