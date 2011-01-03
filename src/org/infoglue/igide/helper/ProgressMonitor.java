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

import org.eclipse.core.runtime.IProgressMonitor;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.editor.InfoglueEditorInput;


/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */	
public class ProgressMonitor implements IProgressMonitor {

		/**
		 * @param inputType
		 */
		ContentTypeAttribute attribute;
		InfoglueEditorInput input;

		/**
		 * @param attribute
		 */
		public ProgressMonitor(ContentTypeAttribute attribute, InfoglueEditorInput input) {
			this.attribute = attribute;
			this.input = input;
		}

		public void beginTask(String name, int totalWork) {
		}

		public void done() {
			
			attribute.setFileLoaded(true);
			// input.getContent().addAttribute(attribute);

			/*
			IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(attribute.getFile().getName());
            try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(attribute.getFile()), descriptor.getId());
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}

		public void internalWorked(double work) {
		}
		public boolean isCanceled() {
			return false;
		}

		public void setCanceled(boolean value) {
		}

		public void setTaskName(String name) {
		}

		public void subTask(String name) {
		}

		public void worked(int work) {
		}}
