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
package org.infoglue.igide.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.infoglue.igide.model.content.ContentNode;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public final class InfoglueEditorInput implements IEditorInput,
		IPersistableElement {
	
	/*
	 * TODO: Understand and implement state using memento, so that editors can reopen between eclipse sessions.
	 */
	public static final String MEMENTO_KEY = "contentId";
	private IGMultiPageEditor editor;
	private EditableInfoglueContent content = null;
	private String id = null;
	private String idpath = null;

	
	public InfoglueEditorInput(EditableInfoglueContent content) {
		this.content = content;
		this.id=  content.getId();
	}

	// IEditorInput interface
	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return content.getName();
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		return "" + content.getName() + " - " + content.getNode().getActiveVersion();
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public void saveState(IMemento memento) {
		ContentNode node = content.getNode();

		System.out.println("Saving memento: " + idpath);
		int cnt = 0;
		memento.putInteger("contentId", content.getNode().getId());
		memento.putString("path", idpath);
	}

	public String getId() {
		return id;
	}

	// Support methods
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof InfoglueEditorInput) {
			InfoglueEditorInput o = (InfoglueEditorInput) obj;
			return content.equals(o.content);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return content.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	public String getFactoryId() {
		return InfoglueEditorInputFactory.ID;	
	}

	public IGMultiPageEditor getEditor() {
		return editor;
	}
	public void setEditor(IGMultiPageEditor editor) {
		this.editor = editor;
	}
	public EditableInfoglueContent getContent() {
		return content;
	}
	public void setContent(EditableInfoglueContent content) {
		this.content = content;
	}

	public void setIdPath(String idpath) {
		this.idpath  = idpath;
		
	}
}