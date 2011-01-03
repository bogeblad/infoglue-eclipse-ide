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
 * Created on 2004-nov-16
 */
package org.infoglue.igide.view.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.editor.IGMultiPageEditor;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.content.ContentNode;



/**
 * @author Stefan Sik
 * 
 */
public class DoubleClickAction extends Action
{
    private TreeViewer viewer;
    
    public DoubleClickAction(TreeViewer viewer)
    {
        this.viewer = viewer;
    }

    public void run()
    {
    	/*
        ISelection selection = viewer.getSelection();
        ContentNode node = (ContentNode) ((IStructuredSelection) selection).getFirstElement();
		*/
    	ContentNode node = Utils.getSelectedContentNode(viewer);
    	
        /*
         * If the node is expandable, expand it, else it must be a content, so open it.
         * TODO: this simple logic may change later, when we perhaps present the attributes in tree form, or
         * if we want to present the languageversions. 
         */
        if (viewer.isExpandable(node))
        {
            viewer.setExpandedState(node, !viewer.getExpandedState(node));
        }
        else
        {
            try
            {
                /*
                 * Open our multipage editor on the contentversion this node represents
                 */
                
                IDE.openEditor(
	        		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
	                InfoglueCMS.openContentVersion(node), 
	                IGMultiPageEditor.ID, 
	                true);
            	
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
}