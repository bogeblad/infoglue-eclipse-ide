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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentPropertyDefinitionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.InfoglueProxy;
import org.infoglue.igide.editor.EditableInfoglueContent;
import org.infoglue.igide.editor.InfoglueEditorInput;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.ProgressMonitor;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.model.content.ContentNodeFactory;
import org.infoglue.igide.preferences.PreferenceHelper;

/**
 * @author Stefan Sik
 * 
 */
public class InfoglueCMS
{

	
	@SuppressWarnings("unchecked")
	public static List getComponentPropertyDefinitions(String componentPropertiesXML)
	{
	    return ComponentPropertyDefinitionController.getController().parseComponentPropertyDefinitions(componentPropertiesXML);
	}
	
	public static InfoglueEditorInput openContentVersion(ContentNode node) throws Exception
	{
        Integer activeVersion = node.getActiveVersion();
        ContentVersion contentVersion = null;
	    InfoglueEditorInput input=  null;
        ContentTypeDefinition contentTypeDefinition = null;
        
        IFolder parentFolder = null;
        if(node.getLocalrecource() instanceof IFile)
        {
        	parentFolder = (IFolder) node.getParentNode().getLocalrecource();
        }
        else
        {
        	parentFolder = (IFolder) node.getLocalrecource();
        }
        
        if(activeVersion!=null)
        {
            InfoglueProxy contentFetcher = node.getConnection().getInfoglueProxy();
            contentVersion = contentFetcher.fetchContentVersion(activeVersion);
            contentVersion.setContentId(node.getId());
            contentTypeDefinition = contentFetcher.getContentTypeDefinition(node.getContentTypeId());
            Map<String, ContentTypeAttribute> attributes = InfoglueCMS.getContentTypeAttributes(contentTypeDefinition.getSchemaValue());

            EditableInfoglueContent content = new EditableInfoglueContent(node, node.getEditorId(), contentVersion, node.getConnection());
            content.setName(node.getText());
            
            input = new InfoglueEditorInput(content);
            
            for(String key: attributes.keySet())
            {
                ContentTypeAttribute attribute = (ContentTypeAttribute) attributes.get(key);
                String value = InfoglueCMS.getAttributeValue(contentVersion, attribute.getName());
                attribute.setValue(value);
                String assocKey = contentTypeDefinition.getName() + "." + attribute.getName();
                
                
                String prefix = node.getText() + "_" + attribute.getName();
                String suffix = PreferenceHelper.getFileExtensionForAttributeKey(assocKey);
                String filename = prefix + suffix; 
                
                /*
                 * Dump the textareas on disk for now.
                 * So that they can be opened in a standard editor
                 */
                
                if(attribute.getInputType().equalsIgnoreCase("textarea"))
                {
                    IFile file = parentFolder.getFile(filename);
                    
                    // TODO: move primaryAttributeKeys out of ContentNodeFactory, it belongs in the preference classes
                	if(ContentNodeFactory.primaryAttributeKeys.contains(assocKey) && (node.getLocalrecource() instanceof IFile))
                	{
                		file = (IFile) node.getLocalrecource();
                	}
                	
                    attribute.setFile(file);
                    System.out.println("FILE CHARSET: " + file.getCharset());
                	InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));
                	// TODO: here, check if local file is newer maybe???
                    if(file.exists())
                    {
                    	//file.setContents(new StringBufferInputStream(value), IFile.FORCE, new ProgressMonitor(attribute, input));
                    	if(file.getLocalTimeStamp() > contentVersion.getMod().getTime())
                    	{
                    		System.out.println("Warning: the local file is newer than the remote.");
                    	}
                    	file.setContents(is, IFile.FORCE | IFile.KEEP_HISTORY, new ProgressMonitor(attribute, input));
                    	file.setLocalTimeStamp(contentVersion.getMod().getTime());
                    }
                    else
                    {
                    	
                    	// file.create(new StringBufferInputStream(value), IFile.FORCE, new ProgressMonitor(attribute, input));
                    	file.create(is, IFile.FORCE, new ProgressMonitor(attribute, input));
                    	file.setLocalTimeStamp(contentVersion.getMod().getTime());
                    }
                    
                    //?? 
                    file.setCharset("UTF-8", Utils.getMonitor(null));
                    System.out.println(" local file mod: " + file.getLocalTimeStamp());
                    System.out.println("remote file mod: " + contentVersion.getMod().getTime());
                    
                    try
                    {
                        if(!contentVersion.getStateId().equals(ContentVersionVO.WORKING_STATE))
                        {
                        	file.getResourceAttributes().setReadOnly(true);
                        }
                        else
                        {
                        	file.getResourceAttributes().setReadOnly(false);
                        }
                    }
                    catch(Exception e)
                    {
                    }
                    
                }
                input.getContent().addAttribute(attribute);
                
            }
        }
        input.setIdPath("");
        
        return input;
	}
	
	
	public static void createContent(InfoglueConnection connection, Integer parentContentId, Integer repositoryId, String name, Integer typeId)
	{
		System.out.println("in InfoglueCMS.createContent: parent=" + parentContentId + ", name=" + name + ", type=" + typeId);
		Content content = null;
		try {
			content = new Content();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		Calendar cal = Calendar.getInstance();
		Date publish = new Date(cal.getTime().getTime());
		cal.add(Calendar.YEAR, 50);
		Date expire = new Date(cal.getTime().getTime());
		content.setPublishdatetime(publish);
		content.setExpiredatetime(expire);
		content.setName(name);
		content.setTypedefid(typeId);
		content.setBranch(false);
		content.setRepositoryId(repositoryId);
		content.setParentContentId(parentContentId);
		// content.setCreatorName("IGIDE");
		
		createContent(connection, content);
	}

	public static void createContent(InfoglueConnection connection, Content content)
	{
		// System.out.println("in InfoglueCMS.createContent(with VO): parent=" + parent + ", contentVO=" + content);
		// InfoglueConnection connection = parent.getConnection();
		final Integer contentId;
		InfoglueProxy proxy = connection.getInfoglueProxy();
		try {
			contentId = proxy.createContent(content);
			Content newContent = proxy.fetchContent(contentId);
			ContentTypeDefinition def = proxy.getContentTypeDefinition(newContent.getTypedefid());
            Map<String, ContentTypeAttribute> attributes = InfoglueCMS.getContentTypeAttributes(def.getSchemaValue());
            ContentVersion version = new ContentVersion();
            
            version.setContentId(newContent.getId());
            version.setLanguageId(newContent.getMasterLanguageId());
            version.setValue(buildVersionValue(attributes.values()));
            proxy.updateContentVersion(version);
            
			// proxy.createDefaultWorking(contentId);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
    public static ContentVersion saveContentVersion(ContentVersion version, InfoglueConnection connection, IProgressMonitor monitor) throws Exception
	{
	    ContentVersion updated = null;
		System.out.println("------------------------------- SAVING CONTENTVERSION!! -------------------------------");
		System.out.println(version);
		System.out.println("------------------------------- /SAVING CONTENTVERSION!! -------------------------------");
		Utils.getMonitor(monitor).subTask("Saving to CMS");

		updated = connection.getInfoglueProxy().updateContentVersion(version);
        
		System.out.println("------------------------------- UPDATED CONTENTVERSION!! -------------------------------");
        System.out.println(updated);
		System.out.println("------------------------------- /UPDATED CONTENTVERSION!! -------------------------------");
		version.setMod(updated.getMod());
		return version;
	}
	
    public static String buildVersionValue(Collection<ContentTypeAttribute> attributes)
    {
			String versionValue = "<?xml version='1.0' encoding='UTF-8'?>";
			versionValue += "<article xmlns='x-schema:ArticleSchema.xml'>";
			versionValue += "<attributes>";
			
			for(Iterator i = attributes.iterator();i.hasNext();)
			{
			    ContentTypeAttribute attr = (ContentTypeAttribute) i.next();
			    if(attr.getInputType().compareTo("digitalAsset") != 0)
			    {
			        versionValue += "<" + attr.getName() + ">";
					versionValue += "<![CDATA[" + attr.getValue() + "]]>";
					versionValue += "</" + attr.getName() + ">";
			    }
			}
			
			versionValue += "</attributes>";
			versionValue += "</article>";
			
			return versionValue;
    }
    
    
	public static String getAttributeValue(ContentVersion contentVersionVO, String key)
	{
		String value = "";
		String xpath = "//*[local-name()='" + key + "']";
		if(contentVersionVO != null)
		{
			try
	        {
				Document doc = DocumentHelper.parseText(contentVersionVO.getValue());
				Element root = doc.getRootElement();
				Node nodeValue = root.selectSingleNode(xpath);
				value = nodeValue.getText();
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		}
		return value;
	}

	/**
	 * @deprecated
	 */
	public static String old_getAttributeValue(ContentVersion contentVersionVO, String key)
	{
		String value = "";
		if(contentVersionVO != null)
		{
			try
	        {
		        Logger.logInfo("key:" + key);
		        Logger.logInfo("VersionValue:" + contentVersionVO.getValue());
		
				String xml = contentVersionVO.getValue();
				
				int startTagIndex = xml.indexOf("<" + key + ">");
				int endTagIndex   = xml.indexOf("]]></" + key + ">");

				if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
				{
					value = xml.substring(startTagIndex + key.length() + 11, endTagIndex);
				}					
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		}
		return value;
	}
	
	public static Map<String, ContentTypeAttribute> getContentTypeAttributes(String schemaValue)
	{
		Map<String, ContentTypeAttribute> attributes = new HashMap<String, ContentTypeAttribute>();
		
		try
		{
			SAXReader reader = new SAXReader();
			Document document = reader.read(new StringReader(schemaValue));
	        // Document document = reader.read(new java.io.ByteArrayInputStream(schemaValue.getBytes()));
			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element";
			
			List anl = document.selectNodes(attributesXPath); 
				// XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);
			
			int cnt = 0;
			for(Iterator i=anl.iterator(); i.hasNext();)
			{
				cnt++;
				Element child = (Element)i.next();
				String attributeName = child.valueOf("@name");
				String attributeType = child.valueOf("@type");
								
				ContentTypeAttribute contentTypeAttribute = new ContentTypeAttribute();
				contentTypeAttribute.setPosition(cnt);
				contentTypeAttribute.setName(attributeName);
				contentTypeAttribute.setInputType(attributeType);

				// Get extra parameters
				org.dom4j.Node paramsNode = child.selectSingleNode("xs:annotation/xs:appinfo/params");
				if (paramsNode != null)
				{	
					List childnl = paramsNode.selectNodes("param");
					
					for(Iterator i2= childnl.iterator();i2.hasNext();)
					{
						Element param = (Element) i2.next();
						String paramId = param.valueOf("@id");
						String paramInputTypeId = param.valueOf("@inputTypeId");
						
						ContentTypeAttributeParameter contentTypeAttributeParameter = new ContentTypeAttributeParameter();
						contentTypeAttributeParameter.setId(paramId);
						if(paramInputTypeId != null && paramInputTypeId.length() > 0)
							contentTypeAttributeParameter.setType(Integer.parseInt(paramInputTypeId));
				
						contentTypeAttribute.putContentTypeAttributeParameter(paramId, contentTypeAttributeParameter);
						
						List valuesNodeList = param.selectNodes("values");
						// System.out.println(param.asXML());
						for(Iterator i3=valuesNodeList.iterator();i3.hasNext();)
						{
							Element values = (Element)i3.next();
							// System.out.println(values.asXML());

							List valueNodeList = values.selectNodes("value");
							for(Iterator i4=valueNodeList.iterator();i4.hasNext();)
							{
								Element value = (Element)i4.next();
								// System.out.println(value.asXML());

								String valueId = value.valueOf("@id");
								
								ContentTypeAttributeParameterValue contentTypeAttributeParameterValue = new ContentTypeAttributeParameterValue();
								contentTypeAttributeParameterValue.setId(valueId);
								
								List nodeMap = value.attributes();
								for(Iterator i5=nodeMap.iterator();i5.hasNext();)
								{
									Attribute attribute = (Attribute)i5.next();
									String valueAttributeName = attribute.getName();
									String valueAttributeValue = attribute.getValue();
									contentTypeAttributeParameterValue.addAttribute(valueAttributeName, valueAttributeValue);
								}
								
								contentTypeAttributeParameter.addContentTypeAttributeParameterValue(valueId, contentTypeAttributeParameterValue);
							}
						}								
					}
				}
				// End extra parameters

				attributes.put(contentTypeAttribute.getName(), contentTypeAttribute);
			}
			
		}
		catch(Exception e)
		{
			Logger.logInfo("An error occurred when we tried to get the attributes of the content type: " + e.getMessage());	
		}
	    		
		return attributes;
	}

}
