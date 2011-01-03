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
package org.infoglue.igide.model.content;

import org.eclipse.core.resources.IResource;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.model.ConnectedInfoglueNode;
import org.infoglue.igide.model.MasterNode;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class ContentNode extends ConnectedInfoglueNode
{
	public static final String FOLDER = "Folder";
	public static final String ITEM = "Item";
	public static final String REPOSITORY = "Repository";

	private String baseUrl = "";
	private Integer id = null;
	private Integer parent = null;
	private Integer repositoryId = null;
	private Integer activeVersion = null;
	private Integer activeVersionStateId = null;
	private String activeVersionModifier = null;
	private Integer contentTypeId = null;
	private String text = "";
	private String nodeType = "folder";
	private String src = null;
	private ContentNode parentNode = null;
	private boolean children = false;

	/**
	 * TESTING!!!
	 */
	MasterNode masterNode = null;
	private IResource localrecource;

	ContentNode(IResource localrecource, ContentNode parentNode, String id, String parent, String text, String type, String src)
	{
		super(localrecource, parentNode.getConnection());
		this.localrecource = localrecource;
		this.parentNode = parentNode;
		setMasterNode(parentNode.getMasterNode());
		try
		{
			this.id = new Integer(id);
			this.parent = new Integer(parent);
		}
		catch (Exception e)
		{

		}
		this.text = text;
		this.setSrc(src);
		this.setNodeType(type);
	}

	ContentNode(IResource localrecource, MasterNode master, InfoglueConnection connection)
	{
		super(localrecource, connection);
		this.localrecource = localrecource;
		setSrc("");
		setConnection(connection);
		setRoot(true);
		setChildren(true);
		setMasterNode(master);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ContentNode)
		{
			ContentNode o = (ContentNode) obj;
			try
			{
				return (isRoot() ? super.equals(obj) : super.equals(obj) && id.equals(o.id) && repositoryId.equals(o.repositoryId) && src.equals(o.src)
						&& text.equals(o.text));
			}
			catch (Exception e)
			{
			}
		}
		return super.equals(obj);
	}

	public Integer getActiveVersion()
	{
		return activeVersion;
	}

	public String getActiveVersionModifier()
	{
		return activeVersionModifier;
	}

	public Integer getActiveVersionStateId()
	{
		return activeVersionStateId;
	}

	/*
	 * public IGNode(String src) { this.src = src; }
	 */

	/**
	 * @return Returns the baseUrl.
	 */
	public String getBaseUrl()
	{
		return baseUrl;
	}

	public Object[] getChildren() throws Exception
	{
		if (hasChildren()) return (Object[]) getConnection().getInfoglueProxy().fetchNodeChildren(this).toArray();
		else return new Object[0];
	}

	public Integer getContentTypeId()
	{
		return contentTypeId;
	}

	public String getEditorId()
	{
		return id + "_" + repositoryId + "_" + baseUrl;
	}

	/**
	 * @return Returns the id.
	 */
	public Integer getId()
	{
		return id;
	}

	public MasterNode getMasterNode()
	{
		return masterNode;
	}

	public String getNodeType()
	{
		return nodeType;
	}

	public Integer getParentId()
	{
		return parent;
	}

	public ContentNode getParentNode()
	{
		return parentNode;
	}

	/**
	 * @return Returns the repositoryId.
	 */
	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public String getSrc()
	{
		return this.src;
	}

	public String getText()
	{
		return isRoot() && (text == null || "".equals(text)) ? getConnection().getBaseUrl().toString() : text;
	}

	public boolean hasChildren()
	{
		return children;
	}

	@Override
	public int hashCode()
	{
		return (id + "_" + src + "_" + text).hashCode();
	}

	public void setActiveVersion(Integer activeVersion)
	{
		this.activeVersion = activeVersion;
	}

	public void setActiveVersionModifier(String activeVersionModifier)
	{
		this.activeVersionModifier = activeVersionModifier;
	}

	public void setActiveVersionStateId(Integer activeVersionStateId)
	{
		this.activeVersionStateId = activeVersionStateId;
	}

	/**
	 * @param baseUrl
	 *              The baseUrl to set.
	 */
	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public void setChildren(boolean children)
	{
		this.children = children;
	}

	public void setContentTypeId(Integer contentTypeId)
	{
		this.contentTypeId = contentTypeId;
	}

	/**
	 * @param id
	 *              The id to set.
	 */
	public void setId(Integer id)
	{
		this.id = id;
	}

	private void setMasterNode(MasterNode masterNode)
	{
		this.masterNode = masterNode;
	}

	public void setNodeType(String type)
	{
		this.nodeType = type;
	}

	public void setParentId(Integer parent)
	{
		this.parent = parent;
	}

	public void setParentNode(ContentNode parentNode)
	{
		this.parentNode = parentNode;
	}

	/**
	 * @param repositoryId
	 *              The repositoryId to set.
	 */
	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public void setSrc(String src)
	{
		this.src = src;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String toString()
	{
		return getText();
	}

	public IResource getLocalrecource()
	{
		return localrecource;
	}

	public void setLocalrecource(IResource localrecource)
	{
		this.localrecource = localrecource;
	}
}