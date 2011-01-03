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
/*
 * Created on 2004-nov-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.infoglue.igide.editor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.model.content.ContentNode;

/**
 * @author Stefan Sik
 *
 */
public class InfoglueEditorInputFactory implements IElementFactory {
    public static final String ID =
        "org.infoglue.igide.editor.InfoglueEditorInputFactory";

	/*
	 * TODO: Understand and implement state using memento, so that editors can reopen between eclipse sessions.
	 */
    public IAdaptable createElement(IMemento memento) {
    	
    	String path = memento.getString("path");
    	Integer contentId = memento.getInteger("contentId");
    	
    	String p[] = path.split(",");
    	String url = p[0];
    	
    	
    	//InfoglueView view = InfoglueView.getView();
    	//TreeViewer viewer = view.getViewer();
    	// viewer.set
    	
    	
    	ContentNode node = null;
    	IFolder localfolder = null;
    	
    	
    	
        try {
			return InfoglueCMS.openContentVersion(node);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
    }
} 