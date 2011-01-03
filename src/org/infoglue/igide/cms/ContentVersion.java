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
package org.infoglue.igide.cms;

import java.util.Date;

/**
 * @author Stefan Sik
 * 
 */
public class ContentVersion
{
    private Integer id;
    private Integer contentId;
    private String versionModifier = null;
    private Integer stateId = null;
    private Date mod;
    private Date localMod;
    private Integer languageId;
    private String languageName;
    private String value;
    private boolean active;
    
    
    @Override
    public String toString() {
    	return "id:" + id + " - contentId:" + contentId + " - mod: " + mod.getTime() + " - languageId: " + languageId + " - value.hashCode:" + value.hashCode() + " - active: " + active + " - Modifier: " + versionModifier;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return (languageId + "_" + stateId + "_" + id +"_" + contentId).hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof ContentVersion)
    	{
    		try {
        		ContentVersion o = (ContentVersion) obj;
        		return languageId.equals(o.languageId) && id.equals(o.id) && contentId.equals(o.contentId) && stateId.equals(o.stateId);
    		}
    		catch(Exception e) {}
    	}
    	return super.equals(obj);
    }
    
    public Integer getId()
    {
        return id;
    }
    public void setId(Integer id)
    {
        this.id = id;
    }
    public Integer getLanguageId()
    {
        return languageId;
    }
    public void setLanguageId(Integer languageId)
    {
        this.languageId = languageId;
    }
    public String getLanguageName()
    {
        return languageName;
    }
    public void setLanguageName(String languageName)
    {
        this.languageName = languageName;
    }
    public Date getMod()
    {
        return mod;
    }
    public void setMod(Date mod)
    {
        this.mod = mod;
    }
    public String getValue()
    {
        return value;
    }
    public void setValue(String value)
    {
        this.value = value;
    }
    
    public Date getLocalMod()
    {
        return localMod;
    }
    public void setLocalMod(Date localMod)
    {
        this.localMod = localMod;
    }
    public boolean isActive()
    {
        return active;
    }
    public void setActive(boolean active)
    {
        this.active = active;
    }
    public Integer getContentId()
    {
        return contentId;
    }
    public void setContentId(Integer contentId)
    {
        this.contentId = contentId;
    }
	public String getVersionModifier() {
		return versionModifier;
	}
	public void setVersionModifier(String versionModifier) {
		this.versionModifier = versionModifier;
	}
	public Integer getStateId() {
		return stateId;
	}
	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
}
