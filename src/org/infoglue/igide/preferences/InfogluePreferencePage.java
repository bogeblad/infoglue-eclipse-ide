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

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.infoglue.igide.InfoglueConnectorPlugin;

/**
 * @author Stefan Sik
 * 
 * <b></b>
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */


public class InfogluePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	// public static final String xP_WORKSPACE_PROJECT = "workspaceproject";
	public static final String P_BASE_SERVER_URL = "baseUrl";
	public static final String P_TMP_FOLDER = "tmpFolder";
	public static final String P_USERNAME = "username";
	public static final String P_PASSWORD = "password";
	public static final String P_ACC_TYPES = "types";
	public static final String P_PROJECTS = "projects";
	

	public InfogluePreferencePage() {
		super(GRID);
		setPreferenceStore(InfoglueConnectorPlugin.getDefault().getPreferenceStore());
		setDescription("Infoglue content explorer settings");
		initializeDefaults();
	}
/**
 * Sets the default values of the preferences.
 */
	private void initializeDefaults() {
		// InfoglueConnectorPlugin.getDefault().internalInitializeDefaultPluginPreferences();
		/*IPreferenceStore store = getPreferenceStore();
		store.setDefault(P_WORKSPACE_PROJECT, "InfoGlue Connector Project");
		store.setDefault(P_BASE_SERVER_URL, "http://localhost:8080/infoglueCMS/");
		store.setDefault(P_TMP_FOLDER, ".tmp");
		store.setDefault(P_USERNAME, "");
		store.setDefault(P_PASSWORD, "");*/
	}
	
/**
 * Creates the field editors. Field editors are abstractions of
 * the common GUI blocks needed to manipulate various types
 * of preferences. Each field editor knows how to save and
 * restore itself.
 */

	public void createFieldEditors() {
		addField(new ListEditor(P_PROJECTS, "Connections and projects", getFieldEditorParent()) {
			
			
			@Override
			protected String[] parseString(String stringList) {
				return stringList.split(";");
			}
		
			@Override
			protected String getNewInputObject() 
			{
				ConnectionDialogNew dialog = new ConnectionDialogNew(getShell());
				dialog.setBlockOnOpen(true);
				String value = null;
				int dialogCode = dialog.open();
				if (dialogCode == InputDialog.OK) {
					value = dialog.getValue();
					System.out.println(value);
					if (value != null) {
						value = value.trim();
						if (value.length() == 0)
							return null;
					}
				}
				return value;				
			}
		
			@Override
			protected String createList(String[] items) {
				StringBuffer buf = new StringBuffer();
				for(int i=0;i<items.length;i++)
				{
					buf.append(items[i]).append(";");
					
				}
				return buf.toString();
			}
		
		});
		
		addField(new ListEditor(P_ACC_TYPES, "Content Type Attribute Association", getFieldEditorParent()) {
		
			@Override
			protected String[] parseString(String stringList) {
				return stringList.split(";");
			}
		
			@Override
			protected String getNewInputObject() 
			{
				AttributeAssociationDialogNew dialog = new AttributeAssociationDialogNew(getShell());
				dialog.setBlockOnOpen(true);
				String value = null;
				int dialogCode = dialog.open();
				if (dialogCode == InputDialog.OK) {
					value = dialog.getValue();
					System.out.println(value);
					if (value != null) {
						value = value.trim();
						if (value.length() == 0)
							return null;
					}
				}
				return value;				
			}
		
			@Override
			protected String createList(String[] items) {
				StringBuffer buf = new StringBuffer();
				for(int i=0;i<items.length;i++)
				{
					buf.append(items[i]).append(";");
					
				}
				return buf.toString();
			}
		
		});

		
		
		
	}
	
	public void init(IWorkbench workbench) {
	}
}