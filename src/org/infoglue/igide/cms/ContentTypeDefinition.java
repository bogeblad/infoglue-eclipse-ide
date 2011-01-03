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
package org.infoglue.igide.cms;

import java.util.Date;

/**
 * @author Stefan Sik
 * 
 */
public class ContentTypeDefinition implements Comparable<ContentTypeDefinition>
{
    private Integer id;
    private Integer type;
    private String name;
    private String schemaValue;
    private Date mod;

    public String toString()
    {
        return "CONTENTTYPEDEFINITION: \nid:" + id + "\n" +
		"type:" + type + "\n" +
		"name:" + name + "\n";
    }
    
    public Integer getId()
    {
        return id;
    }
    public void setId(Integer id)
    {
        this.id = id;
    }
    public Date getMod()
    {
        return mod;
    }
    public void setMod(Date mod)
    {
        this.mod = mod;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getSchemaValue()
    {
        return schemaValue;
    }
    public void setSchemaValue(String schemaValue)
    {
        this.schemaValue = schemaValue;
    }
    public Integer getType()
    {
        return type;
    }
    public void setType(Integer type)
    {
        this.type = type;
    }

	public int compareTo(ContentTypeDefinition other) {
		return this.getName().compareTo(other.getName());
	}
}
