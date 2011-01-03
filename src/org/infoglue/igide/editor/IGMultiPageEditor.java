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
package org.infoglue.igide.editor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.igide.cms.Content;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.cms.ContentVersion;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.InfoglueProxy;
import org.infoglue.igide.cms.exceptions.ConcurrentModificationException;
import org.infoglue.igide.cms.exceptions.InvalidVersionException;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.content.ContentNode;

import org.infoglue.igide.cms.connection.NotificationMessage;
import org.infoglue.igide.helper.NotificationListener;
import org.infoglue.igide.helper.ProjectHelper;

/**
 * 
 * @author Stefan Sik
 *
 */


/*
 * This is the mutlipage editor main class, it provides native and specific editors to 
 * the contentversions all textarea attributes in separate pages. it also assamble all 
 * other attributes to a single page called "properties" those attributes are of type
 * text, boolean, list and so on.
 * 
 * TODO: Work on the layout of the properties page, provide more functionality in terms of
 * links and alerts from the remote system.
 * 
 * also create more specialized editors, for example, a relations editor and a component properties
 * editor would be nice.
 * 
 * One challenge is to provide the same code completion as I have implemented in MyVeloEditor for 
 * "System Editors"
 * 
 * To let the user decide what editor to use on a certain attribute, extend the settings to let the user
 * specify file extension or editors on each attribute.
 * 
 */

public class IGMultiPageEditor extends MultiPageEditorPart implements
		IResourceChangeListener, NotificationListener {

	public static final String ID = "org.infoglue.igide.editor.IGMultiPageEditor";


	private InfoglueFormEditor formEditor;


	private boolean saving = false;

	public IGMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	void createPage(IEditorPart editor, IEditorInput input) {
		try {
			int index = addPage(editor, input);
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, e.getStatus());
		}
	}

	/*
	@Override
	protected IEditorSite createSite(IEditorPart editor) 
	{
		IEditorSite site = null;
		site = super.createSite(editor);
		
		new MultiPageEditorSite(this, editor) {
			@Override
			public String getId() {
				return super.getId();
			}
			
		};
		
		return site;
	}
	*/
	
	/*
	 * TODO: Create the editor in a safer way, not using internal api (done)
	 * possibly also look at our configuration to determine what editor to create
	 */
	
	private IEditorPart getEditorForAttribute(ContentTypeAttribute attribute) throws CoreException
	{
		IEditorPart editor = null;
		boolean enableComponentPropertiesEditor = false;
		Integer stateId = getInfoglueEditorInput().getContent().getContentVersion().getStateId();
		final boolean isWorkingState = (stateId == null || ContentVersionVO.WORKING_STATE.equals(getInfoglueEditorInput().getContent().getContentVersion().getStateId()));
		
		try 
		{
			enableComponentPropertiesEditor = Boolean.parseBoolean(attribute.getContentTypeAttribute("enableComponentPropertiesEditor").getContentTypeAttributeParameterValue().getValue("label"));
		}
		catch(Exception e) {}
		
		System.out.println("Getting editor for attribute: " + attribute.getName());
		System.out.println("enableComponentPropertiesEditor=" + enableComponentPropertiesEditor);
		System.out.println("-----------------------------------");
		
		editor = new StructuredTextEditor()
		{
			@Override
			public boolean isDirty() {
				// TODO Auto-generated method stub
				return super.isDirty();
			}
			@Override
			public void doSave(IProgressMonitor progressMonitor) {
			
				super.doSave(progressMonitor);
				String document = getDocumentProvider().getDocument(getEditorInput()).get();
				getAttributeEditorInput().getAttribute().setValue(document);
				
			}
			
			@Override
			public boolean isEditable() 
			{
				return isWorkingState;
			}
			
			private AttributeEditorInput getAttributeEditorInput() {
				return (AttributeEditorInput) getEditorInput();
			}
			
		};
		 return editor;
	}
	
	public void createStructuredEditor(ContentTypeAttribute attribute) {
		IEditorPart editor = null;
		
		 try 
		 {
			editor = getEditorForAttribute(attribute);
			int index = addPage(editor, new AttributeEditorInput(attribute,	getInfoglueEditorInput().getContent().getConnection()));
			setPageText(index, attribute.getName());
		 } 
		 catch (PartInitException e) 
		 {
		 } 
		 catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		// Here we will create the form according to
		// ContentTypeDefinition
		InfoglueEditorInput input = getInfoglueEditorInput();

		/*
		 * Create the textarea editors
		 */
		EditableInfoglueContent content = input.getContent();
		for (Iterator i = content.getAttributes().iterator(); i.hasNext();) {
			ContentTypeAttribute attribute = (ContentTypeAttribute) i.next();
			if (attribute.getInputType().equalsIgnoreCase(ContentTypeAttribute.TEXTAREA)) {
				// Here we should wait for the file to load.
				// or dump the attribute here instead of in filehelper
				createStructuredEditor(attribute);

			}

		}

		/*
		 * Create the form editor
		 */
		formEditor = new InfoglueFormEditor();
		try {
			int index = addPage(formEditor, input);
			setPageText(index, "Properties");

		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// setTitle(content.getName());
		setPartName(content.getName());

	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        getInfoglueEditorInput().getContent().getConnection().removeNotificationListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		saving = true;
		boolean dirtyflag = isDirty();
		/*
		 * Save all editors
		 */
		Utils.getMonitor(monitor).beginTask("Saving content to CMS",100);
		
		for (int i = 0; i < getPageCount(); i++) {
			getEditor(i).doSave(monitor);
		}
		Utils.getMonitor(monitor).worked(25);
		InfoglueEditorInput input = getInfoglueEditorInput();
		input.getContent().doSave(monitor);
		
		try {
			InfoglueCMS.saveContentVersion(input.getContent().getContentVersion(), input.getContent().getConnection(), monitor);
		} 
		catch (ConcurrentModificationException e) 
		{
        	MessageDialog.openInformation(this.getActiveEditor().getSite().getShell(), "Unhandled situation/Unable to save", "The content was updated on the server, the local version is not the latest and synchronization/update feature is still not implemented. Please close the editor and open it again to get the latest version");
		}
		catch (IllegalStateException e) 
		{
        	MessageDialog.openInformation(this.getActiveEditor().getSite().getShell(), "Unhandled situation/Unable to save", "The content is not in a working state, automatic working change is not yet implemented, sorry");
		}
		catch (InvalidVersionException e)
		{
        	MessageDialog.openInformation(this.getActiveEditor().getSite().getShell(), "Unhandled situation/Unable to save", "The content you are trying to save is not the active version at the server. Someone has create a new active version, sorry");
		}
		catch (Exception e) 
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			// TODO: display the stacktrace in the details.
        	MessageDialog.openInformation(this.getActiveEditor().getSite().getShell(), "Unhandled situation/Unable to save", "We recieved the following from the server: " + e.getMessage() + "\n\n" + sw.toString());
		}
		
		Utils.getMonitor(monitor).worked(100);
		Utils.getMonitor(monitor).done();
		
		saving = false;
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
		System.out.println("DOSAVEAS!!!");
		/*
		 * IEditorPart editor = getEditor(0); editor.doSaveAs(); setPageText(0,
		 * editor.getTitle()); setInput(editor.getEditorInput());
		 */
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		// ((InfoglueEditorInput) editorInput).setEditor(this);
		if (!(editorInput instanceof InfoglueEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be InfoglueEditorInput");
		super.init(site, editorInput);
		
		try
        {
            getInfoglueEditorInput().getContent().getConnection().addNotificationListener(this);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

	}
	
	
	private InfoglueEditorInput getInfoglueEditorInput() {
		return (InfoglueEditorInput) getEditorInput();
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					/*
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
							.getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput())
								.getFile().getProject().equals(
										event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor
									.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
					*/
				}
			});
		}
	}

	private InfoglueProxy getProxy()
	{
		return getInfoglueEditorInput().getContent().getConnection().getInfoglueProxy();
	}
	
	
	public void doReload(ContentNode node, ContentVersion updated) throws Exception
	{
		if(updated.getMod().equals(getInfoglueEditorInput().getContent().getContentVersion().getMod()))
		{
			System.out.println("This version seem to be up to date.");
			return;
		}
		
        if(!isDirty() || MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "CMS Message", "The content " + getInfoglueEditorInput().getContent().getName() + " was changed remotely by user: " + updated.getVersionModifier() + ". Do you want to reload the changed content?"))
        {
            // Only save the editors to remove dirty flag, not the editorinput (sent to cms)
            // TODO: ??????
    		for (int i = 0; i < getPageCount(); i++) {
    			getEditor(i).doSave(null);
    		}
    		
    		
            IFolder tmp = ProjectHelper.getProject(node).getFolder("WebContent");
            setInput(InfoglueCMS.openContentVersion(node));
            
        }
        else if(isDirty())
        {
        	MessageDialog.openInformation(this.getActiveEditor().getSite().getShell(), "Unhandled situation", "The content was updated on the server, the local version is not the latest and synchronization/update feature is still not implemented. Your changes might not get saved to CMS");
        }

		
	}
	
    /*
     * @see org.infoglue.igide.helper.NotificationListener#recieveNotification(org.infoglue.igide.cms.NotificationMessage)
     */
    public void recieveCMSNotification(NotificationMessage message)
    {    	
        ContentVersion version = getInfoglueEditorInput().getContent().getContentVersion();
        ContentNode node = getInfoglueEditorInput().getContent().getNode();
        Integer activeVersion = version.getId();
        
        /*
         * ContentVersion updates, someone has saved the same version.
         */
        if(!saving && message.getClassName().indexOf("ContentVersionImpl") > -1)
        {
            try {
                Integer objectId = new Integer((String) message.getObjectId());
                if(objectId.equals(activeVersion))
                {
                	synchronized(getInfoglueEditorInput().getContent().getContentVersion())
                	{
            			ContentVersion updated = getProxy().fetchContentVersionHead(version.getId());
            			doReload(node, updated);
                	}
                }
            }
            catch(Exception e)
            {
            	e.printStackTrace();
            }
        }

        
        /*
         * Content updates, some has changed the content. Maybe published??, or deleted version
         * I think the reload responsibility maybe belong in class ViewContentProvider. Just make 
         * a note in the log here for now...
         */
        if(message.getClassName().indexOf("ContentImpl") > -1)
        {
        	System.out.println("Editor: contentChange:" + message);
            try {
                Integer objectId = new Integer((String) message.getObjectId());
                Integer oldState = node.getActiveVersionStateId();
                
                if(objectId.equals(node.getId()))
                {
                	System.out.println("Its this content");
                	Content content = getProxy().fetchContent(node.getId());
        			if(!content.getActiveVersion().equals(node.getActiveVersion()))
        			{
            			// ContentVersion updated = getProxy().fetchContentVersionHead(content.getActiveVersion());
            			if(!oldState.equals(content.getActiveVersionStateId()))
            			{
            				System.out.println("This version is not active any more, the new version has state: " + content.getActiveVersionStateId() + " and it was modified by " + content.getActiveVersionModifier());
            			}
        			}
                	
                }
            }
            catch(Exception e)
            {
            	e.printStackTrace();
            }
        }
         
    }

	public void connectedToRemoteSystem(InfoglueConnection connection) {
	}

	public void connectionException(InfoglueConnection connection, Exception e) {
	}

	public void disconnectedFromRemoteSystem(InfoglueConnection connection) {
	}
}