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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
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
import org.infoglue.igide.cms.ContentTypeDefinition;
import org.infoglue.igide.cms.ContentVersion;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.InfoglueProxy;
import org.infoglue.igide.cms.exceptions.ConcurrentModificationException;
import org.infoglue.igide.cms.exceptions.InvalidVersionException;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.preferences.PreferenceHelper;

import org.infoglue.igide.cms.connection.NotificationMessage;
import org.infoglue.igide.helper.DelayedFileWriter;
import org.infoglue.igide.helper.Logger;
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
    private boolean isReloadCMSPushCall;

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
		final IGMultiPageEditor mpeditor = this;
        IEditorPart editor = null;
        boolean enableComponentPropertiesEditor = false;
        ContentVersion cv = InfoglueCMS.getProjectContentVersion(getInfoglueEditorInput().getContent().getNode().getProject().getName(), getInfoglueEditorInput().getContent().getNode().getId());
        Integer stateId = cv.getStateId();
        final boolean isWorkingState = stateId == null || ContentVersionVO.WORKING_STATE.equals(stateId);
        try
        {
            enableComponentPropertiesEditor = Boolean.parseBoolean(attribute.getContentTypeAttribute("enableComponentPropertiesEditor").getContentTypeAttributeParameterValue().getValue("label"));
        }
        catch(Exception exception) { }
        Logger.logConsole((new StringBuilder("Getting editor for attribute: ")).append(attribute.getName()).toString());
        Logger.logConsole((new StringBuilder("enableComponentPropertiesEditor=")).append(enableComponentPropertiesEditor).toString());
        Logger.logConsole("-----------------------------------");
        editor = new StructuredTextEditor() {

            public synchronized void doSave(IProgressMonitor progressMonitor)
            {
                Logger.logConsole("*************************************");
                Logger.logConsole("***   doSave in internal editor.  ***");
                Logger.logConsole((new StringBuilder("***   ")).append(getAttributeEditorInput().getAttribute().getName()).append(" ***").toString());
                Logger.logConsole("*************************************");
                if(!mpeditor.saving)
                    mpeditor.doSave(progressMonitor);
                super.doSave(progressMonitor);
                String document = getDocumentProvider().getDocument(getEditorInput()).get();
                getAttributeEditorInput().getAttribute().setValue(document);
//                ContentVersion existingCV = InfoglueCMS.getProjectContentVersion(getInfoglueEditorInput().getContent().getNode().getProject().getName(), getInfoglueEditorInput().getContent().getNode().getId());
//                
//                System.out.println("existingCV.value: " + existingCV.getValue());
//                
//                int start = existingCV.getValue().indexOf((new StringBuilder("<")).append(getAttributeEditorInput().getAttribute().getName()).append("><![CDATA[").toString());
//                if(start > -1)
//                {
//                    int end = existingCV.getValue().indexOf((new StringBuilder("]]></")).append(getAttributeEditorInput().getAttribute().getName()).append(">").toString());
//                    int length = (new StringBuilder("]]></")).append(getAttributeEditorInput().getAttribute().getName()).append(">").toString().length();
//                    if(end > -1)
//                    {
//                        String newValue1 = (new StringBuilder(String.valueOf(existingCV.getValue().substring(0, start)))).append("<").append(getAttributeEditorInput().getAttribute().getName()).append("><![CDATA[").append(document).append("]]></").append(getAttributeEditorInput().getAttribute().getName()).append(">").append(existingCV.getValue().substring(end + length)).toString();
//                        existingCV.setValue(newValue1);
//                        getAttributeEditorInput().getAttribute().setValue(document);
//                    }
//                }
//                else
//                {
////                	int check = existingCV.getValue().indexOf((new StringBuilder("<")).append(getAttributeEditorInput().getAttribute().getName()).toString());
////                	if (check != -1)
////                	{
////                		Logger.logConsole("Found ");
////                	}
////                	else
////                	{
//	                    String newValue1 = existingCV.getValue().replaceFirst("</attributes>", (new StringBuilder("<")).append(getAttributeEditorInput().getAttribute().getName()).append("><![CDATA[").append(document).append("]]></").append(getAttributeEditorInput().getAttribute().getName()).append("></attributes>").toString());
//	                    existingCV.setValue(newValue1);
//	                    getAttributeEditorInput().getAttribute().setValue(document);
////                	}
//                }
            }

            public boolean isEditable()
            {
                return isWorkingState;
            }

            private AttributeEditorInput getAttributeEditorInput()
            {
                return (AttributeEditorInput)getEditorInput();
            }

            //final IGMultiPageEditor this$0;
            //private final IGMultiPageEditor val$mpeditor;
            //private final boolean val$isWorkingState;
            //{
            //   this$0 = IGMultiPageEditor.this;
            //   mpeditor = igmultipageeditor1;
            //   isWorkingState = flag;
            //   super();
            //}
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
    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        getInfoglueEditorInput().getContent().getNode().getId();
        Logger.logConsole("Removing " + getInfoglueEditorInput().getContent().getNode().getId());
        InfoglueCMS.getOpenedFileNames().remove(getInfoglueEditorInput().getContent().getNode().getId());
        Logger.logConsole((new StringBuilder("Removing fullPath:")).append(getInfoglueEditorInput().getContent().getNode().getFullPath().toString()).toString());
        InfoglueCMS.getOpenedFullFileNames().remove(getInfoglueEditorInput().getContent().getNode().getFullPath().toString());
        getInfoglueEditorInput().getContent().getConnection().removeNotificationListener(this);
        super.dispose();
    }


	/**
	 * Saves the multi-page editor's document.
	 */
    public void doSave(IProgressMonitor monitor)
    {
        Logger.logConsole("YES - doSave: " + monitor + ":" + isReloadCMSPushCall);
        saving = true;
        boolean dirtyflag = isDirty();
        Utils.getMonitor(monitor).beginTask("Saving content to CMS", 100);

        for(int i = 0; i < getPageCount(); i++)
        {
        	IEditorPart editor = getEditor(i);
            editor.doSave(monitor);

        }

        Logger.logConsole("Saved each editor part...");
        Utils.getMonitor(monitor).worked(25);
        InfoglueEditorInput input = getInfoglueEditorInput();
        Logger.logConsole("input: " + input);
        input.getContent().doSave(monitor);
        System.out.println("isReloadCMSPushCall: " + isReloadCMSPushCall);
        if(!isReloadCMSPushCall)
            try
            {
            	Logger.logConsole("saveLocalXML called");
                ContentVersion cv = InfoglueCMS.getProjectContentVersion(input.getContent().getNode().getProject().getName(), input.getContent().getNode().getId());

                SAXReader reader = new SAXReader();

                Document document = reader.read(new StringReader(cv.getValue()));
                Map<String, String> namespaceUris = new HashMap<String, String>();
                namespaceUris.put("art", "x-schema:ArticleSchema.xml");

                XPath xPath = DocumentHelper.createXPath("/art:article/art:attributes");
                xPath.setNamespaceURIs(namespaceUris);

                Element attributesNode = (Element) xPath.selectSingleNode(document); //(Element)document.selectSingleNode("/article/attributes");

                @SuppressWarnings("unchecked")
				List<Element> attributes = attributesNode.elements();//document.selectNodes("//attributes/*");

                EditableInfoglueContent content = input.getContent();
                final ArrayList<String> contentAttributes = content.getAttributesOrder();

                Map<String, Element> attributeMap = new HashMap<String, Element>();

                // This loop remove elements from the DOM element
                for (Element attribute : attributes)
                {
                	// DOM4j will shorten empty attributes, which is not good for InfoGlue
                	if ("".equals(attribute.getText()))
        			{
						attribute.clearContent();
						attribute.addCDATA("");
        			}

                	if (attributeMap.containsKey(attribute.getName()))
                	{
                		Logger.logConsole("Found duplicate attribute. Removing it. Name: " + attribute.getName());
                		attributesNode.remove(attribute);
                	}
                	else
                	{
                		String attributeName = attribute.getName();
                		if (contentAttributes.contains(attributeName))
                		{
                			attributeMap.put(attributeName, attribute);
                		}
                		else if (!"IGAuthorFullName".equals(attributeName) && !"IGAuthorEmail".equals(attributeName))
                		{
                			Logger.logConsole("Found attribute in version that is not in the content type. Removing. Name: " + attributeName);
                			attributesNode.remove(attribute);
                		}
                	}
                }

                // This loop add elements to the DOM element
            	for(int i = 0; i < getPageCount(); i++)
                {
                	IEditorPart editor = getEditor(i);
                    editor.doSave(monitor);
                    IEditorInput editorInput = editor.getEditorInput();
                    AttributeEditorInput attributeInput = null;
                    if (editorInput instanceof AttributeEditorInput)
                    {
                    	attributeInput = (AttributeEditorInput)editorInput;
                    	ContentTypeAttribute cta = attributeInput.getAttribute();
                    	Element attributeNode = attributeMap.get(cta.getName());
                    	if (attributeNode == null)
                    	{
                    		Logger.logConsole("Found no attribute for editor, name: " + cta.getName());
                    		Element attributeElement = attributesNode.addElement(cta.getName());
                    		attributeElement.clearContent();
                    		attributeElement.addCDATA(cta.getValue());
                    	}
                    	else
                    	{
                    		System.out.println("Setting value: " + cta.getValue() + " on node: " + cta.getName());
                    		attributeNode.clearContent();
                    		attributeNode.addCDATA(cta.getValue());
                    	}
                    }
                }

            	// Sort the attributes
            	attributes = (List<Element>)attributesNode.elements();
            	Collections.sort(attributes, new Comparator<Element>()
				{
            		@Override
            		public int compare(Element element1, Element element2)
            		{
            			int index1 = contentAttributes.indexOf(element1);
            			int index2 = contentAttributes.indexOf(element2);

            			if (index1 != -1 && index2 != -1)
            			{
            				return index1 - index2;
            			}
            			else if (index1 == -1 && index2 != -1)
            			{
            				return 1;
            			}
            			else if (index1 != -1 && index2 == -1)
            			{
            				return -1;
            			}
            			else
            			{
            				return 0;
            			}
            		}
				});

            	// Re-set the attributes after manipulation and sorting
            	attributesNode.setContent(attributes);

            	cv.setValue(document.asXML());
            	

                InfoglueCMS.saveLocalXML(input.getContent().getNode(), cv);
                Logger.logConsole((new StringBuilder("Part in doSave:")).append(cv.getValue().substring(113, 200)).toString());
            }
            catch(Exception e)
            {
                Logger.logConsole("Error in saveLocal");
                System.out.println("Exception: " + e.getMessage() + ", class: " + e.getClass());
                e.printStackTrace();
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

    public int getEditorPageCount()
    {
        return getPageCount();
    }

    public IEditorPart getEditorPart(int i)
    {
        return getEditor(i);
    }

    public void resourceChanged(IResourceChangeEvent r)
    {
        Logger.logConsole("**********************************");
        IResourceDelta rootDelta = r.getDelta();
        IResourceDelta docDelta = rootDelta;
        if(docDelta == null)
            return;
        final List<IResource> changed = new ArrayList<IResource>();
        IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

            public boolean visit(IResourceDelta delta)
            {
                IResource resource = delta.getResource();
                if(delta.getKind() != 4)
                    return true;
                if((delta.getFlags() & 0x100) == 0)
                    return true;
                if(resource.getType() == 1 && "xml".equalsIgnoreCase(resource.getFileExtension()) && resource.getName().indexOf("_$") == -1)
                {
                	System.out.println("Name " + resource.getName());
                    changed.add(resource);
                }
                return true;
            }
        };
        try
        {
            docDelta.accept(visitor);
        }
        catch(CoreException e)
        {
            Logger.logConsole((new StringBuilder("CoreException:")).append(e.getMessage()).toString());
        }
        Logger.logConsole((new StringBuilder("Changed number of files:")).append(changed.size()).toString());
        for(Iterator<IResource> changedIterator = changed.iterator(); changedIterator.hasNext();)
        {
            IResource resource = changedIterator.next();
            IFile file = (IFile)resource;
            Logger.logConsole((new StringBuilder("Resource:")).append(resource.getName()).toString());
            InfoglueEditorInput editorInput = getInfoglueEditorInput();
            if (editorInput == null)
            {
            	Logger.logConsole("No InfoglueEditorInput for this resource. Resource name: " + resource.getName());
            	continue;
            }
            String editorInputName = getInfoglueEditorInput().getName();
            String resourceName = resource.getName();
            Logger.logConsole("EditorInputName:" + editorInputName + "=" + resourceName);
            System.out.println("EditorInputName:" + editorInputName + "=" + resourceName);
            int lastIndexUnderscore = resourceName.lastIndexOf("_");
            String compareResourceName = resourceName;
            if (lastIndexUnderscore == -1)
            {
            	Logger.logConsole("Something is wrong with the resource name. It should contain a underscore. Name: " + resourceName);
            	int lastIndexExtension = resourceName.lastIndexOf(".xml");
            	if (lastIndexExtension != -1)
            	{
            		compareResourceName = resourceName.substring(0, lastIndexExtension);
            	}
            }
            else
            {
            	compareResourceName = resourceName.substring(0, lastIndexUnderscore);
            }
            if(compareResourceName.equals(editorInputName))
            {
                Logger.logConsole("As the editor contains the changed file we ??????");
                Map attributeValueMap = InfoglueCMS.getAttributeValueMap(resource);
                Logger.logConsole((new StringBuilder("AttributeValueMap was size:")).append(attributeValueMap.size()).append(" for XML-file on disk").toString());
                Iterator attributeValueMapIterator = attributeValueMap.keySet().iterator();
                Map fileValueMap = new HashMap();
                while(attributeValueMapIterator.hasNext()) 
                {
                    String attributeName = (String)attributeValueMapIterator.next();
                    String value = (String)attributeValueMap.get(attributeName);
                    String attributeFileName = (new StringBuilder("_$")).append(resource.getName().replaceAll("_.*?xml", "")).append("_").append(attributeName).append(PreferenceHelper.getFileExtensionForAttributeKey(attributeName)).toString();
                    if(attributeName.equals("Template"))
                        attributeFileName = (new StringBuilder("_$")).append(resource.getName().replaceAll("_.*?xml", "")).append(".jsp").toString();
                    if(attributeName.equals("PreTemplate"))
                        attributeFileName = (new StringBuilder("_$")).append(resource.getName().replaceAll("_.*?xml", "")).append("_").append(attributeName).append(".jsp").toString();
                    if(attributeName.equals("ComponentProperties"))
                        attributeFileName = (new StringBuilder("_$")).append(resource.getName().replaceAll("_.*?xml", "")).append("_").append(attributeName).append(".xml").toString();
                    Logger.logConsole((new StringBuilder("attributeFileName:")).append(attributeFileName).toString());
                    IFile attributeFile = ((IFolder)file.getParent()).getFile(attributeFileName);
                    Logger.logConsole((new StringBuilder("attributeFile:")).append(attributeFile).toString());
                    if(attributeFile.exists())
                    {
                        Logger.logConsole((new StringBuilder("File found for:")).append(attributeFile).toString());
                        fileValueMap.put(attributeFile, value);
                    } else
                    {
                        Logger.logConsole((new StringBuilder("No file found... skipping writing to:")).append(attributeFile).toString());
                    }
                }
                Logger.logConsole((new StringBuilder("Creating writer for:")).append(fileValueMap.size()).toString());
                try
                {
                    DelayedFileWriter mywriter = new DelayedFileWriter(fileValueMap, getInfoglueEditorInput(), this, file);
                    Logger.logConsole("Writing to file....");
                    Display.getDefault().asyncExec(mywriter);
                }
                catch(Exception e)
                {
                    Logger.logConsole((new StringBuilder("Error creating writer: ")).append(e.getMessage()).toString());
                }
            } else
            {
                Logger.logConsole((new StringBuilder("The resource change does not affect this editor... skipping:")).append(editorInputName).append("!=").append(resourceName).toString());
            }
        }

        Logger.logConsole("**********************************");
        if(r.getType() == 2)
            Display.getDefault().asyncExec(new Runnable() {

                public void run()
                {
                }
            });
    }

	private InfoglueProxy getProxy()
	{
		return getInfoglueEditorInput().getContent().getConnection().getInfoglueProxy();
	}
	
    public void doReload(ContentNode node, ContentVersion updated)
        throws Exception
    {
        Logger.logConsole((new StringBuilder("doReload triggered with:")).append(node.getName()).toString());
        ContentVersion cv = InfoglueCMS.getProjectContentVersion(node.getProject().getName(), node.getId());
        if(updated.getMod().equals(cv.getMod()))
        {
            Logger.logConsole("This version seem to be up to date - returning.");
            return;
        }
        if(!isDirty() || MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "CMS Message", (new StringBuilder("The content ")).append(getInfoglueEditorInput().getContent().getName()).append(" was changed remotely by user: ").append(updated.getVersionModifier()).append(". Do you want to reload the changed content?").toString()))
        {
            isReloadCMSPushCall = true;
            Logger.logConsole("Saving each page in multi editor as a reload was called.");
            for(int i = 0; i < getPageCount(); i++)
            {
                Logger.logConsole((new StringBuilder("Editor(i):")).append(getEditor(i).getClass().getName()).toString());
                getEditor(i).doSave(null);
                Logger.logConsole((new StringBuilder("Title for editor:")).append(getEditor(i).getTitle()).append(":").append(getEditor(i).getEditorInput().getName()).toString());
            }

            Logger.logConsole("Saving each page in multi editor as a reload was called.");
            IFolder tmp = ProjectHelper.getProject(node).getFolder("WebContent");
            setInput(InfoglueCMS.openContentVersion(node));
            isReloadCMSPushCall = false;
            Logger.logConsole("Populated the multipage editor again with the content version.");
        } else
        if(isDirty())
            MessageDialog.openInformation(getActiveEditor().getSite().getShell(), "Unhandled situation", "The content was updated on the server, the local version is not the latest and synchronization/update feature is still not implemented. Your changes might not get saved to CMS");
    }
	
    /*
     * @see org.infoglue.igide.helper.NotificationListener#recieveNotification(org.infoglue.igide.cms.NotificationMessage)
     */
    public void recieveCMSNotification(NotificationMessage message)
    {    	
        ContentNode node = getInfoglueEditorInput().getContent().getNode();
        ContentVersion version = InfoglueCMS.getProjectContentVersion(node.getProject().getName(), node.getId());
        Integer activeVersion = version.getId();
        if(!saving && message.getClassName().indexOf("ContentVersionImpl") > -1)
            try
            {
                Integer objectId = new Integer((String)message.getObjectId());
                if(objectId.equals(activeVersion))
                {
                    ContentVersion currentVersion = InfoglueCMS.getProjectContentVersion(node.getProject().getName(), node.getId());
                    synchronized(currentVersion)
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
        if(message.getClassName().indexOf("ContentImpl") > -1)
        {
            System.out.println((new StringBuilder("Editor: contentChange:")).append(message).toString());
            try
            {
                Integer objectId = new Integer((String)message.getObjectId());
                Integer oldState = node.getActiveVersionStateId();
                if(objectId.equals(node.getId()))
                {
                    System.out.println("Its this content");
                    Content content = getProxy().fetchContent(node.getId());
                    if(!content.getActiveVersion().equals(node.getActiveVersion()) && !oldState.equals(content.getActiveVersionStateId()))
                        System.out.println((new StringBuilder("This version is not active any more, the new version has state: ")).append(content.getActiveVersionStateId()).append(" and it was modified by ").append(content.getActiveVersionModifier()).toString());
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