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

/**
 * @author Stefan Sik
 */
package org.infoglue.igide.cms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Content {
	private Integer id;
	private String name;
	private String creatorName;
	private Integer typedefid;
	private Date expiredatetime;
	private Date publishdatetime;
	private boolean branch;
	private Integer activeVersion;
	private Integer activeVersionStateId;
	private String activeVersionModifier;
	private Integer repositoryId;
	private Integer parentContentId;
	private Integer masterLanguageId;
	
	private List<ContentVersion> versions = new ArrayList<ContentVersion>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public Integer getTypedefid() {
		return typedefid;
	}

	public void setTypedefid(Integer typedefid) {
		this.typedefid = typedefid;
	}

	public Date getExpiredatetime() {
		return expiredatetime;
	}

	public void setExpiredatetime(Date expiredatetime) {
		this.expiredatetime = expiredatetime;
	}

	public Date getPublishdatetime() {
		return publishdatetime;
	}

	public void setPublishdatetime(Date publishdatetime) {
		this.publishdatetime = publishdatetime;
	}

	public boolean isBranch() {
		return branch;
	}

	public void setBranch(boolean branch) {
		this.branch = branch;
	}

	public Integer getActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(Integer activeVersion) {
		this.activeVersion = activeVersion;
	}

	public Integer getActiveVersionStateId() {
		return activeVersionStateId;
	}

	public void setActiveVersionStateId(Integer activeVersionStateId) {
		this.activeVersionStateId = activeVersionStateId;
	}

	public String getActiveVersionModifier() {
		return activeVersionModifier;
	}

	public void setActiveVersionModifier(String activeVersionModifier) {
		this.activeVersionModifier = activeVersionModifier;
	}

	public List<ContentVersion> getVersions() {
		return versions;
	}

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	public Integer getParentContentId() {
		return parentContentId;
	}

	public void setParentContentId(Integer parentContentId) {
		this.parentContentId = parentContentId;
	}

	public Integer getMasterLanguageId()
	{
		return masterLanguageId;
	}

	public void setMasterLanguageId(Integer masterLanguageId)
	{
		this.masterLanguageId = masterLanguageId;
	}

}
