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
 * Created on 2004-nov-18
 *
 */
package org.infoglue.igide.model;

import org.eclipse.core.resources.IResource;
import org.infoglue.igide.cms.connection.InfoglueConnection;


/**
 * @author Stefan Sik
 * 
 */
public abstract class ConnectedInfoglueNode extends LocalResourceNode implements INode 
{
	private InfoglueConnection connection = null;
    private boolean root = false;
    
	public ConnectedInfoglueNode(IResource r, InfoglueConnection c) {
		super(r);
		this.connection = c;
	}
	
    
    @Override
    public boolean equals(Object obj) {
    	try {
    		if(obj instanceof ConnectedInfoglueNode)
    		{
    			ConnectedInfoglueNode o = (ConnectedInfoglueNode) obj;
   				return (o.root == root) && connection.equals(o.getConnection());
    		}
    	}
    	catch(Exception e)
    	{
    		
    	}
    	return false;
    }

    @Override
    public int hashCode() {
    	try {
    		// TODO: Safe??? what if getBaseUrl returns null? we still
    		// want to return a valid hashCode
    		return connection.getBaseUrl().hashCode();
    	}
    	catch(Exception e)
    	{
    		
    	}
    	return super.hashCode();
    }
    
    public InfoglueConnection getConnection()
    {
        return isRoot() ? connection: ((ConnectedInfoglueNode) getParentNode()).getConnection();
    }
    protected void setConnection(InfoglueConnection connection)
    {
        this.connection = connection;
    }
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
}
