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
package org.infoglue.igide.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class ConnectionDialogNew extends Dialog {
	Text projectName;
	Text server;
	Text username;
	Text password;

	Shell shell = null;
	private String value = null;

	public ConnectionDialogNew(Shell parent) {
		super(parent);
	}

	protected Control createDialogArea(Composite parent) {
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setSize(500,300);
		createControls(composite);
		// add controls to composite as necessary
		return composite;
	}

	private void createControls(Composite composite) {
		shell = composite.getShell();
		shell.setText("New Infoglue connection");
		Rectangle rectangle = new Rectangle(0,0,500,300);
		
        Group group = new Group(composite, SWT.None);
		group.setText("");

		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout(2, false));
        		
		Label l_projectname = new Label(group, SWT.None);
		l_projectname.setText("Local Project name");
		projectName = new Text(group,SWT.None);
		
		Label l_server = new Label(group, SWT.None);
		l_server.setText("Server Base Url");
		server = new Text(group,SWT.None);

		Label l_user = new Label(group, SWT.None);
		l_user.setText("Username");
		username = new Text(group,SWT.None);
		
		Label l_pwd = new Label(group, SWT.None);
		l_pwd.setText("Password");
		password = new Text(group,SWT.None);
		
		group.pack();
		composite.pack();
		
		group.setBounds(rectangle);
		composite.setBounds(rectangle);

	}

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void okPressed() {
		value = projectName.getText() + "," +
				server.getText() + "," + 
				username.getText() + "," +
				password.getText();
		super.okPressed();
	}
	public String getValue() 
	{
		return value;
	}

}