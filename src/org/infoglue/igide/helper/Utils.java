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
package org.infoglue.igide.helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jst.j2ee.jsp.JSPConfig;
import org.eclipse.jst.j2ee.jsp.JspFactory;
import org.eclipse.jst.j2ee.jsp.TagLibRefType;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.infoglue.igide.InfoglueConnectorPlugin;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.preferences.InfogluePreferencePage;
import org.osgi.framework.Bundle;
/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public final class Utils
{
    public static int showPrefs(String desc)
    {
        desc = "There was an error connecting to CMS\n\n" + desc + "\n\n";

        PreferenceManager p = new PreferenceManager();

        PreferenceNode node = new PreferenceNode(
                "org.infoglue.igide.preferences.InfogluePreferencePage",
                new InfogluePreferencePage());
        node.getPage().setTitle("Infoglue Connection");
        node.getPage().setDescription(desc);
        p.addToRoot(node);
        PreferenceDialog dia = new PreferenceDialog(PlatformUI.getWorkbench()
                .getDisplay().getActiveShell(), p);
        dia.setBlockOnOpen(true);
        dia.open();
        return dia.getReturnCode();
    }
    
    public static String getPref(String key)
    {
        IPreferenceStore pStore = InfoglueConnectorPlugin.getDefault().getPreferenceStore();
        String v = pStore.getString(key);
        return v;
    }
    
	
	public static ImageDescriptor getImage(String img)
	{
		return InfoglueConnectorPlugin.imageDescriptorFromPlugin(InfoglueConnectorPlugin.id, "icons/" + img);
	}
    /**
     * Returns the location of the web project's WEB-INF/lib directory.
     *
     * @param pj the web project
     * @return the location of the WEB-INF/lib directory
     */

    public static IFolder getWebInfLibDir( final IProject pj )
    {
        final IVirtualComponent vc = ComponentCore.createComponent( pj );
        final IVirtualFolder vf = vc.getRootFolder().getFolder( "WEB-INF/lib" );
        return (IFolder) vf.getUnderlyingFolder();
    }
    public static IFolder getWebInfTldDir( final IProject pj )
    {
        final IVirtualComponent vc = ComponentCore.createComponent( pj );
        final IVirtualFolder vf = vc.getRootFolder().getFolder( "WEB-INF/tld" );
        return (IFolder) vf.getUnderlyingFolder();
    }

    /**
     * Copies a resource from within the FormGen plugin to a destination in
     * the workspace.
     *
     * @param src the path of the resource within the plugin
     * @param dest the destination path within the workspace
     */

    public static void copyFromPlugin( final IPath src,
                                       final IFile dest )

        throws CoreException

    {
        try
        {
        	final Bundle bundle = InfoglueConnectorPlugin.getDefault().getBundle();
            final InputStream in = FileLocator.openStream( bundle, src, false );
            dest.create( in, true, null );
        }
        catch( IOException e )
        {
            // throw new CoreException( FormGenPlugin.createErrorStatus( e.getMessage(), e ) );
        }
    }


    /**
     */
    public static void registerInfoglueTaglibs( final IProject pj )
    {
        final WebArtifactEdit artifact
            = WebArtifactEdit.getWebArtifactEditForWrite( ComponentCore.createComponent( pj ) );

        try
        {
	        final WebApp root = artifact.getWebApp();
	
	        root.setDescription("Infoglue Project");
	        
	        JSPConfig jsp = JspFactory.eINSTANCE.createJSPConfig();
	        
	        IFolder tldfolder = getWebInfTldDir(pj);
	        for(IResource r: tldfolder.members())
	        {
	        	System.out.println(r.getName());
		        TagLibRefType taglib = JspFactory.eINSTANCE.createTagLibRefType();
		        taglib.setTaglibURI(r.getName().split("\\.")[0]);
		        taglib.setTaglibLocation("/WEB-INF/tld/" + r.getName());
		        jsp.getTagLibs().add(taglib);
	        }
	        
	        root.setJspConfig(jsp);
	        
	        artifact.save(null);
	        // artifact.saveIfNecessary( null );
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	artifact.dispose();
        }
    }
    
    public static IProgressMonitor getMonitor(IProgressMonitor m)
    {
    	return m != null ? m: new NullProgressMonitor();
    }
    
    public static MessageConsole findConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++)
           if (name.equals(existing[i].getName()))
              return (MessageConsole) existing[i];
        //no console found, so create a new one
        MessageConsole myConsole = new MessageConsole(name, null);
        conMan.addConsoles(new IConsole[]{myConsole});
        return myConsole;
     }

	public static ContentNode getSelectedContentNode(final TreeViewer viewer)
	{
		ContentNode node = null;
		try {
		    ISelection selection = viewer.getSelection();
		    node = (ContentNode) ((IStructuredSelection) selection).getFirstElement();
		}
		finally	{}
		
		return node;
	}

	public static void main(String...strings) throws IOException
	{
		FileReader r = new FileReader("c:\\test.txt");
		BufferedReader rr = new BufferedReader(r);
		StringBuilder b = new StringBuilder();
		String l = null;
		while ((l=rr.readLine())!=null)
		{
			b.append(l);
		}
		
		Integer i = uglyParseContentId(b.toString());
		System.out.println(i);
		
	}
	
	    /**
     * @deprecated Method uglyParseRepositoryId is deprecated
     */

    public static Integer uglyParseRepositoryId(String returnData)
    {
        Integer value = null;
        for(int tStart = returnData.indexOf("name=\"repositoryId\""); tStart > 0;)
            if(returnData.charAt(tStart--) == '<')
            {
                returnData = returnData.substring(tStart + 1, returnData.indexOf(">", tStart + 1) + 1);
                Document doc = null;
                try
                {
                    doc = DocumentHelper.parseText(returnData);
                }
                catch(DocumentException documentexception) { }
                if(doc == null)
                    try
                    {
                        doc = DocumentHelper.parseText((new StringBuilder(String.valueOf(returnData))).append("</input>").toString());
                    }
                    catch(DocumentException documentexception1) { }
                if(doc != null)
                {
                    Element r = doc.getRootElement();
                    if(r.getName().equals("input") && r.valueOf("@name").equals("repositoryId"))
                        try
                        {
                            value = new Integer(doc.getRootElement().valueOf("@value"));
                        }
                        catch(Exception exception) { }
                }
                break;
            }

        return value;
    }
	
	/**
	 * Make this method go away as soon as possible (I have tried to make it
	 * as safe as possible, but its far from great.)
	 * @deprecated
	 */
	public static Integer uglyParseContentId(String returnData) {
		Integer value = null;
		int tStart = returnData.indexOf("name=\"contentId\"");
		while(tStart > 0)
		{
			if(returnData.charAt(tStart--)=='<')
			{
				returnData = returnData.substring(tStart + 1, returnData.indexOf(">", tStart + 1) + 1);
				Document doc = null;
				try 
				{
					doc = DocumentHelper.parseText(returnData);
				} 
				catch (DocumentException e)	{}
				if(doc == null)
				{
					try 
					{
						doc = DocumentHelper.parseText(returnData + "</input>");
					} 
					catch (DocumentException e) {}
				}
				
				if(doc != null)
				{
					Element r = doc.getRootElement();
					if(r.getName().equals("input") && r.valueOf("@name").equals("contentId"))
					{
						try {
							value = new Integer(doc.getRootElement().valueOf("@value"));							
						}
						catch(Exception e){}
					}
				}
				break;
			}
		}
		return value;
	}
    
    public static String getIFileContentAsString(IResource resource)
    {
//        StringBuffer sb = new StringBuffer();'
    	String result = "";
        Logger.logConsole("resource: " + resource.getClass().getName());
        if(resource instanceof IFile)
        {
            IFile file = (IFile)resource;
//            InputStreamReader isr = null;
            try
            {
            	Logger.logConsole("file: " + file.toString() + ", encoding: " + file.getCharset());
                InputStream is = file.getContents();
                
                result = IOUtils.toString(is, file.getCharset());
                
//                isr = new InputStreamReader(is, file.getCharset());
//
//                int c;
//                while((c = isr.read()) != -1)
//                    sb.append((char)c);
//                is.close();
            }
            catch(Exception e)
            {
                Logger.logConsole("Error: " + e.getMessage() + ", type: " + e.getClass());
            }
//            finally
//            {
//            	if (isr != null)
//            	{
//            		try
//					{
//            			isr.close();
//					}
//					catch (IOException e)
//					{
//						Logger.logConsole("Failed to close input stream. Message: " + e.getMessage());
//					}
//            	}
//            }
        }

        return result;
    }
    
    public static String cleanFileName(String fileName)
    {
    	if (fileName == null)
    	{
    		return null;
    	}
    	return fileName.replaceAll("\\W+", "");
    }
}
