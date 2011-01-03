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
 * Created on 2004-nov-20
 */
package org.infoglue.igide.editor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.cms.ContentTypeAttributeParameterValue;
import org.infoglue.igide.cms.connection.InfoglueConnection;

/**
 * @author Stefan Sik
 * 
 */
public class InfoglueFormEditor extends EditorPart {

	private FormToolkit toolkit;
	private Form form;
	
	private InfoglueEditorInput getInfoglueEditorInput()
	{
		return (InfoglueEditorInput) getEditorInput();
	}

	public void doSave(IProgressMonitor monitor) {
		System.out.println("InfoglueFormEditor: Will save...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		System.out.println("InfoglueFormEditor: doSaveAs, this is not allowed");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.setInput(input);
		super.setSite(site);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public static void openBrowser(URL url)
	{
		IWorkbenchBrowserSupport support =
			  PlatformUI.getWorkbench().getBrowserSupport();
			IWebBrowser browser = null;
			try {
				browser = support.createBrowser("someId");
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				browser.openURL(url);
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
	
	public void createPartControl(Composite parent) 
	{
		final Composite par = parent;
		
		InfoglueEditorInput input = getInfoglueEditorInput();
		EditableInfoglueContent content = input.getContent();  
		
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		toolkit.setBorderStyle(SWT.BORDER);
		
		form.setText(content.getName() + " (" + content.getContentVersion().getLanguageName() + ")");
		
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);

		/*Hyperlink link = toolkit.createHyperlink(form.getBody(), "Click here.",	SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link activated!");
			}
		});
		link.setText("This is an example of a form that is much longer and will need to wrap.");
		*/
		
		layout.numColumns = 2;
		
		
		Section section = toolkit.createSection(form.getBody(), Section.CLIENT_INDENT);
		section.setText("General Properties");
		TableWrapData td = new TableWrapData();
		td.colspan = 1;
		td.grabHorizontal = true;
		section.setLayoutData(td);
		toolkit.createCompositeSeparator(section);
		Composite sectionClient = toolkit.createComposite(section);
		TableWrapLayout pLayout = new TableWrapLayout();
		pLayout.numColumns = 2;
		sectionClient.setLayout(pLayout);

		Section section2 = toolkit.createSection(form.getBody(), Section.CLIENT_INDENT);
		section2.setText("Actions and alerts");
		td = new TableWrapData();
		td.colspan = 1;
		td.grabHorizontal = true;
		section.setLayoutData(td);
		toolkit.createCompositeSeparator(section2);
		Composite sectionClient2 = toolkit.createComposite(section2);
		TableWrapLayout pLayout2 = new TableWrapLayout();
		pLayout2.numColumns = 1;
		sectionClient2.setLayout(pLayout2);
		
		Hyperlink link = toolkit.createHyperlink(sectionClient2, "Open contentVersion in browser",
				SWT.NONE);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
				try {
					
					String params = "?contentId=" + getInfoglueEditorInput().getContent().getContentVersion().getContentId() +
									"&languageId=" + getInfoglueEditorInput().getContent().getContentVersion().getLanguageId() +
									"&forceWorkingChange=true";
					URL viewContentVersion = getInfoglueEditorInput().getContent().getConnection().getUrl(InfoglueConnection.VIEWCONTENTVERSIONSTANDALONE, params);
					openBrowser(viewContentVersion);
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
		});

		link = toolkit.createHyperlink(sectionClient2, "Preview site",
				SWT.NONE);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
				String spec = "ViewPage.action?repositoryId=" + getInfoglueEditorInput().getContent().getNode().getRepositoryId();
				URL context = getInfoglueEditorInput().getContent().getConnection().getDeliveryBaseUrl();
				try {
					openBrowser(new URL(context, spec));
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		// link.setLayoutData(td);
		
		for(Iterator i=content.getAttributes().iterator();i.hasNext();)
		{
			ContentTypeAttribute a = (ContentTypeAttribute) i.next();
			if(!a.getInputType().equalsIgnoreCase("textarea"))
			{
				String titleLabel = a.getParameter("title").getContentTypeAttributeParameterValue().getValue("label");
				if(titleLabel.startsWith("undefined") || titleLabel.trim().length()==0) 
					titleLabel = a.getName();
				else
					titleLabel += " (" + a.getName() + ")";
				
				/*
				 * Create the label
				 */
				Label label = toolkit.createLabel(sectionClient, titleLabel);
				
				/*
				 * Create the Input
				 */
				
				if(a.getInputType().equalsIgnoreCase("select"))
				{
					Combo combo = new Combo(sectionClient,SWT.DROP_DOWN );
					for(Iterator ii = a.getContentTypeAttribute("values").getContentTypeAttributeParameterValues().values().iterator();ii.hasNext();)
					{
						try {
						ContentTypeAttributeParameterValue entry = (ContentTypeAttributeParameterValue) ii.next();
						combo.add((String) entry.getValue("id"));
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					combo.setText(a.getValue());
					
					td = new TableWrapData();
					td.grabHorizontal = true;
					combo.setLayoutData(td);
					combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
				}
				else if(a.getInputType().equalsIgnoreCase("checkbox"))
				{
					Button button = toolkit.createButton(sectionClient, a.getValue(), SWT.CHECK);
					td = new TableWrapData();
					td.grabHorizontal = true;
					button.setLayoutData(td);
					button.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
				}
				else
				{
					Text text = toolkit.createText(sectionClient, a.getValue());
					
					td = new TableWrapData();
					// td.grabHorizontal = true;
					text.setLayoutData(td);
					text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
				}
			}
			
		}
		section.setClient(sectionClient);
		section2.setClient(sectionClient2);
		
		/*
		Label label = toolkit.createLabel(form.getBody(), "Text field label:");
		Text text = toolkit.createText(form.getBody(), "");
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		text.setLayoutData(td);

		Button button = toolkit.createButton(form.getBody(),
				"An example of a checkbox in a form", SWT.CHECK);
		td = new TableWrapData();
		td.colspan = 2;
		button.setLayoutData(td);
		*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}

}