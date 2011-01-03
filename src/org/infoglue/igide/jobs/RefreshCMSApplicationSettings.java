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
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.exceptions.InvalidLoginException;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class RefreshCMSApplicationSettings extends Job {
	
	InfoglueConnection connection = null;
	
	public RefreshCMSApplicationSettings(InfoglueConnection connection) {
		super("Getting application settings from " + connection.getBaseUrl());
		this.connection = connection;
	}
	
	protected IStatus run(IProgressMonitor monitor) 
	{
		monitor.beginTask("Getting application settings from " + connection.getBaseUrl().toString(), 100);
		try {
			connection.getInfoglueProxy().refreshCMSSettings();
		} catch (InvalidLoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		monitor.done();
		return Status.OK_STATUS;
	}

}
