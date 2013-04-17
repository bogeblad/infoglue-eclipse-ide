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
 * Created on 2004-nov-20
 */
package org.infoglue.igide.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.dom4j.Element;
import org.eclipse.core.runtime.IProgressMonitor;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.cms.ContentVersion;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.model.content.ContentNode;

/**
 * @author Stefan Sik
 *
 * This class holds an editable contentversion
 * and its local resources
 * 
 */
public class EditableInfoglueContent {
	
	private String name;
	private ContentNode node;
	private String id;
	private Collection attributes = new ArrayList();
	private InfoglueConnection connection;
	private ArrayList<String> attributesOrder = new ArrayList<String>();
	//private Comparator<Element> attributesComparator;

    public int hashCode()
    {
    	try {
        	return id.hashCode();
    	}
    	catch(Exception e) {}
    	return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof EditableInfoglueContent)
    	{
    		try {
    			EditableInfoglueContent o = (EditableInfoglueContent) obj;
    			return id.equals(o.id);
    		}
    		catch(Exception e) {}
    	}
    	return super.equals(obj);
    }
	
	public void doSave(IProgressMonitor monitor)
	{
        Logger.logConsole((new StringBuilder("attributes:")).append(attributes).append(" in ").append(this).append(" on content:").append(id).toString());
	}
	
    public EditableInfoglueContent(ContentNode node, String id, InfoglueConnection connection)
    {
        attributes = new ArrayList();
	    this.node = node;
		this.id = id;
		this.connection = connection;
	}
	
	public void addAttribute(ContentTypeAttribute a)
	{
		attributes.add(a);
	}

	public Collection getAttributes() {
		return attributes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}

    public InfoglueConnection getConnection()
    {
        return connection;
    }
    public void setConnection(InfoglueConnection connection)
    {
        this.connection = connection;
    }
    public ContentNode getNode()
    {
        return node;
    }
    public void setNode(ContentNode node)
    {
        this.node = node;
    }

    public ArrayList<String> getAttributesOrder()
    {
    	return this.attributesOrder;
    }

	public void setAttributesOrder(ArrayList<String> attributesOrder)
	{
		this.attributesOrder = attributesOrder;
	}
}
