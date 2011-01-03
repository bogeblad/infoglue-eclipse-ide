/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
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

/**
 * @author Stefan Sik
 * 
 */
package org.infoglue.igide.cms.connection;


public class NotificationMessage
{
	public final static int TRANS_CREATE      = 0;
	public final static int TRANS_UPDATE      = 1;
	public final static int TRANS_DELETE      = 2;
	public final static int PUBLISHING        = 10;
	public final static int DENIED_PUBLISHING = 20;
	public final static int UNPUBLISHING      = 30;
	
	public final static String TRANS_CREATE_TEXT 	= "create";
	public final static String TRANS_UPDATE_TEXT 	= "update";
	public final static String TRANS_DELETE_TEXT		= "delete";
	public final static String PUBLISHING_TEXT   	= "publishing";
	public final static String DENIED_PUBLISHING_TEXT = "publishing denied";
	public final static String UNPUBLISHING_TEXT   	= "unpublishing";


	private String name;
	private String systemUserName;
	private int type;
	private String className;
	private Object objectId;
	private String objectName;
	private int hashCode = 0;
	private String versionModifier = null;
	private InfoglueConnection connection = null;
	
	public String toString()
	{
	    return getSystemUserName() + ", " + getTransactionTypeName(type) + ", " + getSimpleClassName() + ", " + getObjectId() + ", " + getObjectName();
	}
	
	public NotificationMessage(InfoglueConnection connection, String name, String objectName, String systemUserName, int type, Object objectId, String className)
	{
		this.connection = connection;
		this.name = name;
		this.className = className;
		this.systemUserName = systemUserName;
		this.type = type;
		this.objectId = objectId;
		this.objectName = objectName;	
	}
	

	public String getName()
	{
		return this.name;
	}

	public String getClassName()
	{
		return this.className;
	}
	
	public String getSimpleClassName()
	{
		return this.className.substring(this.className.lastIndexOf("."));
	}

	public String getSystemUserName()
	{
		return this.systemUserName;
	}

	public int getType()
	{
		return this.type;
	}

	public Object getObjectId()
	{
		return this.objectId;
	}

	public String getObjectName()
	{
		return this.objectName;
	}

	public static String getTransactionTypeName(int transactionType)
	{
		switch (transactionType)
		{
			case (int) (TRANS_CREATE):
				return TRANS_CREATE_TEXT;
			case (TRANS_DELETE):
				return TRANS_DELETE_TEXT;
			case (TRANS_UPDATE):
				return TRANS_UPDATE_TEXT;
			case (PUBLISHING):
				return PUBLISHING_TEXT;
			case (DENIED_PUBLISHING):
				return DENIED_PUBLISHING_TEXT;
		}
		return "unknown - map " + transactionType + " to correct text";
	}
    public int getHashCode()
    {
        return hashCode;
    }
    public void setHashCode(int hashCode)
    {
        this.hashCode = hashCode;
    }
    public String getVersionModifier()
    {
        return versionModifier;
    }
    public void setVersionModifier(String versionModifier)
    {
        this.versionModifier = versionModifier;
    }

	public InfoglueConnection getConnection() {
		return connection;
	}
}