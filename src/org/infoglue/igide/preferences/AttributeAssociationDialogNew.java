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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.cms.connection.InfoglueProxy;
/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class AttributeAssociationDialogNew extends Dialog {
	Combo attributes;
	Combo filetypes;

	Shell shell = null;
	private String value = null;
	private static final String[] extensions = 
	{
		".jsp",
		".js",
		".css",
		".html",
		".xml",
		".vm",
		".java",
		".ddl",
		".sql"
	}; 

	public AttributeAssociationDialogNew(Shell parent) {
		super(parent);
		attributes = null;
		filetypes=null;
	}

	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);
		createControls(composite);
		// add controls to composite as necessary
		return composite;
	}

	private void createControls(Composite composite) {
		// whatall component you want here you can create
		shell = composite.getShell();
		shell.setText("ContentType Attribute association");
		Group group = new Group(composite, SWT.None);
		group.setText("");

		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout(2, true));
		Label attribute = new Label(group, SWT.None);
		attribute.setText("TextArea Attribute");

		attributes = new Combo(group,SWT.DROP_DOWN);
		
		Map globalAttributes = InfoglueProxy.getGlobalAttributes();
		for(Iterator i = globalAttributes.keySet().iterator();i.hasNext();)
		{
			String key = (String) i.next();
			String type = (String) globalAttributes.get(key);
			if(type.equals(ContentTypeAttribute.TEXTAREA))
			{
				attributes.add(key);
			}
		}
		
		// attributes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filetype = new Label(group, SWT.None);
		filetype.setText("File suffix");

		filetypes = new Combo(group,SWT.DROP_DOWN);
		for(int i=0;i<extensions.length;i++)
		{
			filetypes.add(extensions[i]);
		}
		
		group.pack();
		composite.pack();

	}

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void okPressed() {
		value = attributes.getText() + "," + filetypes.getText(); 
		super.okPressed();
	}
	public String getValue() 
	{
		return value;
	}

}