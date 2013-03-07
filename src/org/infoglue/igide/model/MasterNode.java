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
package org.infoglue.igide.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.infoglue.igide.helper.Logger;


/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class MasterNode implements INode {

	String id = "";
	private List<ConnectedInfoglueNode> nodes = new ArrayList<ConnectedInfoglueNode>();
	
	public MasterNode(String id) 
	{
		this.id = id;
        nodes = new ArrayList();
	}
	
	public boolean contains(ConnectedInfoglueNode o) {
		return nodes.contains(o);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			return (obj instanceof MasterNode && ((MasterNode) obj).id.equals(id));
		}
		catch(Exception e)
		{}
		return false;
	}
	
	public Object[] getChildren() throws Exception {
		return nodes.toArray();
	}

	public String getText() {
		return null;
	}

	public boolean hasChildren() {
		return true;
	}

	public void setText(String text) {
	}
	
	public boolean add(ConnectedInfoglueNode node)
	{
		return nodes.add(node);
	}
	
	public boolean addIfNotContains(ConnectedInfoglueNode node)
	{
		if(nodes.contains(node))
		{
			return false;
		}
		return nodes.add(node);
	}

    public boolean removeIfNameContains(ConnectedInfoglueNode node)
    {
        for(Iterator connectedNodes = nodes.iterator(); connectedNodes.hasNext();)
        {
            ConnectedInfoglueNode connectedNode = (ConnectedInfoglueNode)connectedNodes.next();
            Logger.logConsole((new StringBuilder("connectedNode:")).append(connectedNode.getName()).append(" - ").append(node.getName()).toString());
            if(connectedNode.getName().equals(node.getName()))
            {
                Logger.logConsole("Removing node:" + connectedNode);
                connectedNode.getConnection().onEndConnection(null);
                return nodes.remove(connectedNode);
            }
        }

        return false;
    }

	public INode getParentNode() {
		return null;
	}

}
