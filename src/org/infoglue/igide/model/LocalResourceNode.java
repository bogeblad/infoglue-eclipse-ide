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
/**
 * @author Stefan Sik
 */
import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.infoglue.igide.helper.Logger;

public class LocalResourceNode
{
	IResource r;
   	
   	public LocalResourceNode(IResource r)
    {
        this.r = r;
    }

    public void accept(IResourceVisitor arg0)
        throws CoreException
    {
        r.accept(arg0);
    }

    public void accept(IResourceProxyVisitor arg0, int arg1)
        throws CoreException
    {
        r.accept(arg0, arg1);
    }

    public void accept(IResourceVisitor arg0, int arg1, boolean arg2)
        throws CoreException
    {
        r.accept(arg0, arg1, arg2);
    }

    public void accept(IResourceVisitor arg0, int arg1, int arg2)
        throws CoreException
    {
        r.accept(arg0, arg1, arg2);
    }

    public void clearHistory(IProgressMonitor arg0)
        throws CoreException
    {
        r.clearHistory(arg0);
    }

    public void copy(IPath arg0, boolean arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.copy(arg0, arg1, arg2);
    }

    public void copy(IPath arg0, int arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.copy(arg0, arg1, arg2);
    }

    public void copy(IProjectDescription arg0, boolean arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.copy(arg0, arg1, arg2);
    }

    public void copy(IProjectDescription arg0, int arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.copy(arg0, arg1, arg2);
    }

    public IMarker createMarker(String arg0)
        throws CoreException
    {
        return r.createMarker(arg0);
    }

    public IResourceProxy createProxy()
    {
        return r.createProxy();
    }

    public void delete(boolean arg0, IProgressMonitor arg1)
        throws CoreException
    {
        r.delete(arg0, arg1);
    }

    public void delete(int arg0, IProgressMonitor arg1)
        throws CoreException
    {
        r.delete(arg0, arg1);
    }

    public void deleteMarkers(String arg0, boolean arg1, int arg2)
        throws CoreException
    {
        r.deleteMarkers(arg0, arg1, arg2);
    }

    public boolean exists()
    {
        return r.exists();
    }

    public IMarker findMarker(long arg0)
        throws CoreException
    {
        return r.findMarker(arg0);
    }

    public IMarker[] findMarkers(String arg0, boolean arg1, int arg2)
        throws CoreException
    {
        return r.findMarkers(arg0, arg1, arg2);
    }

    public int findMaxProblemSeverity(String arg0, boolean arg1, int arg2)
        throws CoreException
    {
        return r.findMaxProblemSeverity(arg0, arg1, arg2);
    }

    public String getFileExtension()
    {
        return r.getFileExtension();
    }

    public IPath getFullPath()
    {
        return r.getFullPath();
    }

    public long getLocalTimeStamp()
    {
        return r.getLocalTimeStamp();
    }

    public IPath getLocation()
    {
        return r.getLocation();
    }

    public URI getLocationURI()
    {
        return r.getLocationURI();
    }

    public IMarker getMarker(long arg0)
    {
        return r.getMarker(arg0);
    }

    public long getModificationStamp()
    {
        return r.getModificationStamp();
    }

    public String getName()
    {
        return r.getName();
    }

    public IContainer getParent()
    {
        return r.getParent();
    }

    public String getPersistentProperty(QualifiedName arg0)
        throws CoreException
    {
        return r.getPersistentProperty(arg0);
    }

    public IProject getProject()
    {
        return r.getProject();
    }

    public IPath getProjectRelativePath()
    {
        return r.getProjectRelativePath();
    }

    public IPath getRawLocation()
    {
        return r.getRawLocation();
    }

    public URI getRawLocationURI()
    {
        return r.getRawLocationURI();
    }

    public ResourceAttributes getResourceAttributes()
    {
        return r.getResourceAttributes();
    }

    public Object getSessionProperty(QualifiedName arg0)
        throws CoreException
    {
        return r.getSessionProperty(arg0);
    }

    public int getType()
    {
        return r.getType();
    }

    public IWorkspace getWorkspace()
    {
        return r.getWorkspace();
    }

    public boolean isAccessible()
    {
        return r.isAccessible();
    }

    public boolean isDerived()
    {
        return r.isDerived();
    }

    public boolean isLinked()
    {
        return r.isLinked();
    }

    public boolean isLinked(int arg0)
    {
        return r.isLinked(arg0);
    }

    public boolean isLocal(int arg0)
    {
        return r.isLocal(arg0);
    }

    public boolean isPhantom()
    {
        return r.isPhantom();
    }

    public boolean isReadOnly()
    {
        return r.isReadOnly();
    }

    public boolean isSynchronized(int arg0)
    {
        return r.isSynchronized(arg0);
    }

    public boolean isTeamPrivateMember()
    {
        return r.isTeamPrivateMember();
    }

    public void move(IPath arg0, boolean arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.move(arg0, arg1, arg2);
    }

    public void move(IPath arg0, int arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.move(arg0, arg1, arg2);
    }

    public void move(IProjectDescription arg0, int arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.move(arg0, arg1, arg2);
    }

    public void move(IProjectDescription arg0, boolean arg1, boolean arg2, IProgressMonitor arg3)
        throws CoreException
    {
        r.move(arg0, arg1, arg2, arg3);
    }

    public void refreshLocal(int arg0, IProgressMonitor arg1)
        throws CoreException
    {
        Logger.logConsole("----refreshLocal----");
        r.refreshLocal(arg0, arg1);
    }

    public void revertModificationStamp(long arg0)
        throws CoreException
    {
        r.revertModificationStamp(arg0);
    }

    public void setDerived(boolean arg0)
        throws CoreException
    {
        r.setDerived(arg0);
    }

    public void setLocal(boolean arg0, int arg1, IProgressMonitor arg2)
        throws CoreException
    {
        r.setLocal(arg0, arg1, arg2);
    }

    public long setLocalTimeStamp(long arg0)
        throws CoreException
    {
        return r.setLocalTimeStamp(arg0);
    }

    public void setPersistentProperty(QualifiedName arg0, String arg1)
        throws CoreException
    {
        r.setPersistentProperty(arg0, arg1);
    }

    public void setReadOnly(boolean arg0)
    {
        r.setReadOnly(arg0);
    }

    public void setResourceAttributes(ResourceAttributes arg0)
        throws CoreException
    {
        r.setResourceAttributes(arg0);
    }

    public void setSessionProperty(QualifiedName arg0, Object arg1)
        throws CoreException
    {
        r.setSessionProperty(arg0, arg1);
    }

    public void setTeamPrivateMember(boolean arg0)
        throws CoreException
    {
        r.setTeamPrivateMember(arg0);
    }

    public void touch(IProgressMonitor arg0)
        throws CoreException
    {
        Logger.logConsole("----touch----");
        r.touch(arg0);
    }

    public Object getAdapter(Class arg0)
    {
        return r.getAdapter(arg0);
    }

    public boolean contains(ISchedulingRule arg0)
    {
        return r.contains(arg0);
    }

    public boolean isConflicting(ISchedulingRule arg0)
    {
        return r.isConflicting(arg0);
    }

    public Map getPersistentProperties()
        throws CoreException
    {
        return r.getPersistentProperties();
    }

    public Map getSessionProperties()
        throws CoreException
    {
        return r.getSessionProperties();
    }

    public boolean isDerived(int arg0)
    {
        return r.isDerived(arg0);
    }

    public boolean isHidden()
    {
        return r.isHidden();
    }

    public void setHidden(boolean arg0)
        throws CoreException
    {
        r.setHidden(arg0);
    }

    public boolean isHidden(int arg0)
    {
        return r.isHidden(arg0);
    }

    public boolean isTeamPrivateMember(int arg0)
    {
        return r.isTeamPrivateMember(arg0);
    }
    
}
