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
 * Created on 2005-apr-18
 *
 */
package org.infoglue.igide.editor;

import org.eclipse.ui.part.FileEditorInput;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.cms.connection.InfoglueConnection;

/**
 * @author Stefan Sik
 * 
 */
public class AttributeEditorInput extends FileEditorInput
{
    private ContentTypeAttribute attribute;
    private InfoglueConnection connection;
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof AttributeEditorInput)
    	{
    		AttributeEditorInput o = (AttributeEditorInput) obj;
    		return attribute.equals(o.attribute);
    	}
    	return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	if(attribute != null)
    		return attribute.hashCode();
    	return super.hashCode();
    }
    
    public AttributeEditorInput(ContentTypeAttribute attribute, InfoglueConnection connection)
    {
        super(attribute.getFile());
        setAttribute(attribute);
        setConnection(connection);
    }

    public ContentTypeAttribute getAttribute()
    {
        return attribute;
    }
    public void setAttribute(ContentTypeAttribute attribute)
    {
        this.attribute = attribute;
    }
    public InfoglueConnection getConnection()
    {
        return connection;
    }
    public void setConnection(InfoglueConnection connection)
    {
        this.connection = connection;
    }
    
    
}
