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

package org.infoglue.igide.cms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentPropertyDefinitionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.InfoglueProxy;
import org.infoglue.igide.editor.EditableInfoglueContent;
import org.infoglue.igide.editor.HardcodedComparator;
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
    private static List openedFileNames = new ArrayList();
    private static List openedFullFileNames = new ArrayList();
    private static Map projectContentVersions = new HashMap();

    public static List getOpenedFileNames()
    {
        return openedFileNames;
    }

    public static List getOpenedFullFileNames()
    {
        return openedFullFileNames;
    }

    public static ContentVersion getProjectContentVersion(String projectName, Integer contentId)
    {
        Map versions;
        Logger.logConsole((new StringBuilder("getProjectContentVersion:")).append(projectName).toString());
        versions = (Map)projectContentVersions.get(projectName);
        if(versions == null)
        {
            Logger.logConsole((new StringBuilder("No project found - creating:")).append(projectName).toString());
            versions = new HashMap();
            projectContentVersions.put(projectName, versions);
        }
        Map map = versions;
        ContentVersion cv;
        cv = (ContentVersion)versions.get((new StringBuilder("content_")).append(contentId).toString());
        Logger.logConsole((new StringBuilder("cv:")).append(cv).append(" - ").append(cv.getValue().substring(113, 200)).toString());
        return cv;
    }

	public static void setProjectContentVersion(String projectName, Integer contentId, ContentVersion cv)
    {
        Logger.logConsole((new StringBuilder("setProjectContentVersion:")).append(projectName).toString());
        Map versions = (Map)projectContentVersions.get(projectName);
        if(versions == null)
        {
            Logger.logConsole((new StringBuilder("No project found - creating:")).append(projectName).toString());
            versions = new HashMap();
            projectContentVersions.put(projectName, versions);
        }
        synchronized(versions)
        {
            Logger.logConsole((new StringBuilder("cv:")).append(cv).append(" - ").append(cv.getValue().substring(113, 200)).toString());
            versions.put((new StringBuilder("content_")).append(contentId).toString(), cv);
        }
    }
    
	@SuppressWarnings("unchecked")
	public static List getComponentPropertyDefinitions(String componentPropertiesXML)
	{
	    return ComponentPropertyDefinitionController.getController().parseComponentPropertyDefinitions(componentPropertiesXML);
	}
	
	public static InfoglueEditorInput openContentVersion(ContentNode node) throws Exception
	{
        if(node == null)
            return null;
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
            setProjectContentVersion(node.getProject().getName(), node.getId(), contentVersion);
            Logger.logConsole((new StringBuilder("refetched contentVersion:")).append(contentVersion).append("\n").append(contentVersion.getValue().substring(115, 200)).toString());
            contentVersion.setContentId(node.getId());

            contentTypeDefinition = contentFetcher.getContentTypeDefinition(node.getContentTypeId());
            Logger.logConsole("contentTypeDefinition:" + contentTypeDefinition.getSchemaValue().substring(0,300) + " for " + node.getContentTypeId());
            Map attributes = getContentTypeAttributes(contentTypeDefinition.getSchemaValue());
            Logger.logConsole("attributes:" + attributes);

            EditableInfoglueContent content = new EditableInfoglueContent(node, node.getEditorId(), node.getConnection());
            Logger.logConsole((new StringBuilder("Adding ")).append(content.getId()).append(":").append(content.getNode().getId()).toString());
            openedFileNames.add(content.getNode().getId());
            Logger.logConsole((new StringBuilder("Adding fullPath")).append(content.getNode().getFullPath().toString()).toString());
            openedFullFileNames.add(content.getNode().getFullPath().toString());
            content.setName(node.getText());

            input = new InfoglueEditorInput(content);

            ArrayList<String> attributesOrder = InfoglueCMS.getContentTypeAttributeNamesComparator(contentTypeDefinition.getSchemaValue());
            input.getContent().setAttributesOrder(attributesOrder);
            
            List keys = new ArrayList(attributes.keySet());
            Logger.logConsole("keys:" + keys);
            Collections.sort(keys, new HardcodedComparator("Template,PreTemplate,ComponentProperties,ComponentLabels"));
            ContentTypeAttribute attribute;
            
            InfoglueCMS.saveLocalXML(input.getContent().getNode(), contentVersion);
            
            for(Iterator iterator = keys.iterator(); iterator.hasNext(); input.getContent().addAttribute(attribute))
            {
                String key = (String)iterator.next();
                Logger.logConsole("key:" + key);
                attribute = (ContentTypeAttribute)attributes.get(key);
                String value = getAttributeValue(contentVersion, attribute.getName());
                attribute.setValue(value);
                String assocKey = (new StringBuilder(String.valueOf(contentTypeDefinition.getName()))).append(".").append(attribute.getName()).toString();
                String prefix = (new StringBuilder("_$")).append(node.getText()).append("_").append(attribute.getName()).toString();
                String suffix = PreferenceHelper.getFileExtensionForAttributeKey(assocKey);
                String filename = (new StringBuilder(String.valueOf(prefix))).append(suffix).toString();
                if(attribute.getInputType().equalsIgnoreCase("textarea"))
                {
                    IFile file = parentFolder.getFile(filename);
                    if(ContentNodeFactory.primaryAttributeKeys.contains(assocKey) && (node.getLocalrecource() instanceof IFile))
                        file = (IFile)node.getLocalrecource();
                    attribute.setFile(file);
                    Logger.logConsole((new StringBuilder("FILE CHARSET: ")).append(file.getCharset()).toString());
                    InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));
                    if(file.exists())
                    {
                        if(file.getLocalTimeStamp() > contentVersion.getMod().getTime())
                            Logger.logConsole("Warning: the local file is newer than the remote.");
                        file.setContents(is, 3, new ProgressMonitor(attribute, input));
                        file.setLocalTimeStamp(contentVersion.getMod().getTime());
                    } else
                    {
                        file.create(is, 1, new ProgressMonitor(attribute, input));
                        file.setLocalTimeStamp(contentVersion.getMod().getTime());
                    }
                    file.setCharset("UTF-8", Utils.getMonitor(null));
                    Logger.logConsole((new StringBuilder(" local file mod: ")).append(file.getLocalTimeStamp()).toString());
                    Logger.logConsole((new StringBuilder("remote file mod: ")).append(contentVersion.getMod().getTime()).toString());
                    try
                    {
                        if(!contentVersion.getStateId().equals(ContentVersionVO.WORKING_STATE))
                            file.getResourceAttributes().setReadOnly(true);
                        else
                            file.getResourceAttributes().setReadOnly(false);
                    }
                    catch(Exception exception) { }
                }
            }

        }
        input.setIdPath("");
        return input;
    }

	public static Map createRepository(InfoglueConnection connection, String name, String description, String dnsName)
    {
        Logger.logConsole((new StringBuilder("in InfoglueCMS.createRepository: name=")).append(name).toString());
        Repository rep = new Repository();
        rep.setName(name);
        rep.setDescription("No desc");
        rep.setDnsName("undefined");
        return createRepository(connection, rep);
    }

    public static Map createRepository(InfoglueConnection connection, Repository repository)
    {
        Logger.logConsole((new StringBuilder("in InfoglueCMS.createRepository(with VO): repository=")).append(repository).toString());
        Map result = null;
        InfoglueProxy proxy = connection.getInfoglueProxy();
        try
        {
            result = proxy.createRepository(repository);
            Logger.logConsole((new StringBuilder("result:")).append(result).toString());
        }
        catch(Exception e)
        {
            Logger.logConsole((new StringBuilder("Error in a:")).append(e.getMessage()).toString());
        }
        return result;
    }
	
	    public static Integer createContent(InfoglueConnection connection, Integer parentContentId, Integer repositoryId, String name, Integer typeId, boolean isBranch)
    {
        Logger.logConsole((new StringBuilder("in InfoglueCMS.createContent 1: repositoryId= ")).append(repositoryId).append(", parent=").append(parentContentId).append(", name=").append(name).append(", type=").append(typeId).toString());
        Content content = null;
        try
        {
            content = new Content();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Logger.logConsole((new StringBuilder("Error:")).append(e.getMessage()).toString());
        }
        Calendar cal = Calendar.getInstance();
        Date publish = new Date(cal.getTime().getTime());
        cal.add(Calendar.YEAR, 50);
        Date expire = new Date(cal.getTime().getTime());
        content.setPublishdatetime(publish);
        content.setExpiredatetime(expire);
        content.setName(name);
        content.setTypedefid(typeId);
        content.setBranch(isBranch);
        content.setRepositoryId(repositoryId);
        content.setParentContentId(parentContentId);
        
        return createContent(connection, content);
    }
	
	
    public static Integer createContent(InfoglueConnection connection, Integer parentContentId, Integer repositoryId, String name, Integer typeId, String versionValue)
	{
		Logger.logConsole("in InfoglueCMS.createContent: parent=" + parentContentId + ", name=" + name + ", type=" + typeId);
		Content content = null;
		try 
		{
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
        return createContent(connection, content, versionValue);
	}

    public static Integer createContent(InfoglueConnection connection, Content content)
	{
        Logger.logConsole((new StringBuilder("in InfoglueCMS.createContent(with VO): content=")).append(content).toString());
        Integer contentId = null;
		InfoglueProxy proxy = connection.getInfoglueProxy();
		try 
		{
			contentId = proxy.createContent(content);
			Logger.logConsole("contentId: " + contentId);
			ContentTypeDefinition def = proxy.getContentTypeDefinition(content.getTypedefid());
            Logger.logConsole("def: " + def.getName());
           
            Map attributes = getContentTypeAttributes(def.getSchemaValue());
			Logger.logConsole("attributes: " + attributes);
			Logger.logConsole("content.getRepositoryId(): " + content.getRepositoryId());
            ContentVersion version = new ContentVersion();
            version.setContentId(contentId);
            Integer languageId = proxy.getMasterLanguageId(content.getRepositoryId());
   			Logger.logConsole("languageId: " + languageId);
            version.setLanguageId(languageId);
            Logger.logConsole("Language:" + version.getLanguageId());
            version.setValue(buildVersionValue(attributes.values()));
            Logger.logConsole("After");
            proxy.updateContentVersion(version);
            Logger.logConsole("Done updating content version");
 		} 
		catch (Exception e) 
		{
            Logger.logConsole((new StringBuilder("Error in a:")).append(e.getMessage()).toString(), e);
		}
		
        return contentId;
	}

    public static Integer createContent(InfoglueConnection connection, Content content, String versionValue)
    {
        Integer contentId = null;
        InfoglueProxy proxy = connection.getInfoglueProxy();
        try
        {
            contentId = proxy.createContent(content);
            Logger.logConsole((new StringBuilder("contentId after create:")).append(contentId).toString());
            ContentVersion version = new ContentVersion();
            version.setContentId(contentId);
            Logger.logConsole((new StringBuilder("MasterLanguage:")).append(proxy.getMasterLanguageId(content.getRepositoryId())).toString());
            version.setLanguageId(proxy.getMasterLanguageId(content.getRepositoryId()));
            version.setValue(versionValue);
            proxy.updateContentVersion(version);
            Logger.logConsole("Done updating content version");
        }
        catch(Exception e)
        {
            Logger.logConsole((new StringBuilder("Error in b:")).append(e.getMessage()).toString());
            e.printStackTrace();
        }
        return contentId;
    }
    public static ContentVersion saveContentVersion(EditableInfoglueContent content, ContentVersion version, InfoglueConnection connection, boolean saveLocalXML, IProgressMonitor monitor)
        throws Exception
	{
	    ContentVersion updated = null;
        Logger.logConsole("------------------------------- SAVING CONTENTVERSION!! -------------------------------");
		Utils.getMonitor(monitor).subTask("Saving to CMS");

		updated = connection.getInfoglueProxy().updateContentVersion(version);
        try
        {
            if(saveLocalXML)
                saveLocalXML(content.getNode(), version);
        }
        catch(Exception e)
        {
            Logger.logConsole("Error in saveLocal");
        }
		version.setMod(updated.getMod());
		return version;
	}
	
    public static void saveLocalXML(ContentNode node, ContentVersion version)
        throws Exception
    {
        IFolder parentFolder = null;
        if(node.getLocalrecource() instanceof IFile)
            parentFolder = (IFolder)node.getParentNode().getLocalrecource();
        else
            parentFolder = (IFolder)node.getLocalrecource();
        Logger.logConsole("version.getLanguageName():" + version.getLanguageName());
        IFile file = parentFolder.getFile((new StringBuilder(String.valueOf(node.getText()))).append("_").append(version.getLanguageName()).append(".xml").toString());
        byte bytes[] = version.getValue().getBytes("UTF-8");
//        byte bytes[] = version.getValue().getBytes();
//        System.out.println("###value UTF " + new String(bytes, "utf-8"));
//        System.out.println("###value ISO " + new String(bytes, "iso-8859-1"));
        InputStream input = new ByteArrayInputStream(bytes);
        if(file.exists())
            file.setContents(input, 1, null);
        else
            file.create(input, 1, null);
        Logger.logConsole("Writing value:" + version.getValue());
        file.setCharset("UTF-8", Utils.getMonitor(null));
    }

    public static void updateLocalFile(IFile file, String value) throws Exception
    {
        file.setContents(new StringBufferInputStream(value), 1, null);
        file.setCharset("UTF-8", Utils.getMonitor(null));
    }

    public static String buildVersionValue(Collection attributes)
    {
        String versionValue = "<?xml version='1.0' encoding='UTF-8'?>";
        versionValue = (new StringBuilder(String.valueOf(versionValue))).append("<article xmlns='x-schema:ArticleSchema.xml'>").toString();
        versionValue = (new StringBuilder(String.valueOf(versionValue))).append("<attributes>").toString();
        for(Iterator i = attributes.iterator(); i.hasNext();)
        {
            ContentTypeAttribute attr = (ContentTypeAttribute)i.next();
            Logger.logConsole((new StringBuilder("attr:")).append(attr.getName()).toString());
            if(attr.getInputType().compareTo("digitalAsset") != 0)
            {
                if(attr.getName().equals("Template") || attr.getName().equals("PreTemplate") || attr.getName().equals("ComponentLabels"))
                {
                    Logger.logConsole("buildVersionValue attr:" + (attr.getValue().length() <= 30 ? attr.getValue() : attr.getValue().substring(0, 30)) + " on " + attr);
                }
                versionValue = (new StringBuilder(String.valueOf(versionValue))).append("<").append(attr.getName()).append(">").toString();
                versionValue = (new StringBuilder(String.valueOf(versionValue))).append("<![CDATA[").append(attr.getValue()).append("]]>").toString();
                versionValue = (new StringBuilder(String.valueOf(versionValue))).append("</").append(attr.getName()).append(">").toString();
            }
        }

        versionValue = (new StringBuilder(String.valueOf(versionValue))).append("</attributes>").toString();
        versionValue = (new StringBuilder(String.valueOf(versionValue))).append("</article>").toString();
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
				if (nodeValue != null)
				{
					value = nodeValue.getText();
				}
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		}
		return value;
	}

    public static Map getAttributeValueMap(IResource resource)
    {
        Map attributeValueMap = new HashMap();
        String contentVersionXML = Utils.getIFileContentAsString(resource);
        if(contentVersionXML != null)
            try
            {
                Document doc = DocumentHelper.parseText(contentVersionXML);
                Element root = doc.getRootElement();
                Element attributesElement = root.element("attributes");
                List attributeElementList = attributesElement.elements();
                String name;
                String value;
                for(Iterator attributeElementListIterator = attributeElementList.iterator(); attributeElementListIterator.hasNext(); attributeValueMap.put(name, value))
                {
                    Element attributeElement = (Element)attributeElementListIterator.next();
                    name = attributeElement.getName();
                    value = attributeElement.getText();
                }

            }
            catch(Exception e)
            {
                Logger.logConsole((new StringBuilder("Error parsing xml:")).append(e.getMessage()).toString());
                /*
                try
                {
                    PrintWriter pout = new PrintWriter(new FileWriter(new File((new StringBuilder("c:\\logs\\debug_")).append(resource.getName()).toString()), false));
                    pout.println(contentVersionXML);
                    pout.close();
                }
                catch(Exception e2)
                {
                    Logger.logConsole((new StringBuilder("Error e2:")).append(e2.getMessage()).toString());
                }
                */
            }
        return attributeValueMap;
    }

    public static String getAttributeValue(String versionValue)
    {
        String value = null;
        if(versionValue != null)
            try
            {
                Document doc = DocumentHelper.parseText(versionValue);
                Element root = doc.getRootElement();
                Element attributesElement = root.element("attributes");
                List attributeElementList = attributesElement.elements();
                Iterator attributeElementListIterator = attributeElementList.iterator();
                if(attributeElementListIterator.hasNext())
                {
                    Element attributeElement = (Element)attributeElementListIterator.next();
                    String name = attributeElement.getName();
                    value = attributeElement.getText();
                }
            }
            catch(Exception e)
            {
                Logger.logConsole((new StringBuilder("Error parsing xml:")).append(e.getMessage()).toString());
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

			@SuppressWarnings("unchecked")
			List<Element> anl = document.selectNodes(attributesXPath); 
				// XPathAPI.selectNodeList(document.getDocumentElement(), attributesXPath);

			int cnt = 0;
            for(Iterator<Element> i = anl.iterator(); i.hasNext();)
			{
				cnt++;
				Element child = i.next();
				String attributeName = child.valueOf("@name");
				String attributeType = child.valueOf("@type");

				ContentTypeAttribute contentTypeAttribute = new ContentTypeAttribute();
				contentTypeAttribute.setPosition(cnt);
				contentTypeAttribute.setName(attributeName);
				contentTypeAttribute.setInputType(attributeType);
				
				//attributes.put(contentTypeAttribute.getName(), contentTypeAttribute);
				attributes.put(contentTypeAttribute.getName(), contentTypeAttribute);
				
				// Get extra parameters
				org.dom4j.Node paramsNode = child.selectSingleNode("xs:annotation/xs:appinfo/params");
				if (paramsNode != null)
				{	
					@SuppressWarnings("unchecked")
					List<Element> childnl = paramsNode.selectNodes("param");
					
					for(Iterator<Element> i2= childnl.iterator();i2.hasNext();)
					{
						Element param = i2.next();
						String paramId = param.valueOf("@id");
						String paramInputTypeId = param.valueOf("@inputTypeId");
						
						ContentTypeAttributeParameter contentTypeAttributeParameter = new ContentTypeAttributeParameter();
						contentTypeAttributeParameter.setId(paramId);
						if(paramInputTypeId != null && paramInputTypeId.length() > 0)
							contentTypeAttributeParameter.setType(Integer.parseInt(paramInputTypeId));
				
						contentTypeAttribute.putContentTypeAttributeParameter(paramId, contentTypeAttributeParameter);

						@SuppressWarnings("unchecked")
						List<Element> valuesNodeList = param.selectNodes("values");
                        for(Iterator<Element> i3 = valuesNodeList.iterator(); i3.hasNext();)
                        {
                            Element values = i3.next();
                            @SuppressWarnings("unchecked")
							List<Element> valueNodeList = values.selectNodes("value");
                            String valueId;
                            ContentTypeAttributeParameterValue contentTypeAttributeParameterValue;
                            for(Iterator<Element> i4 = valueNodeList.iterator(); i4.hasNext(); contentTypeAttributeParameter.addContentTypeAttributeParameterValue(valueId, contentTypeAttributeParameterValue))
                            {
                                Element value = i4.next();
                                valueId = value.valueOf("@id");
                                contentTypeAttributeParameterValue = new ContentTypeAttributeParameterValue();
                                contentTypeAttributeParameterValue.setId(valueId);
                                @SuppressWarnings("unchecked")
								List<Attribute> nodeMap = value.attributes();
                                String valueAttributeName;
                                String valueAttributeValue;
                                for(Iterator<Attribute> i5 = nodeMap.iterator(); i5.hasNext(); contentTypeAttributeParameterValue.addAttribute(valueAttributeName, valueAttributeValue))
                                {
                                    Attribute attribute = i5.next();
                                    valueAttributeName = attribute.getName();
                                    valueAttributeValue = attribute.getValue();
                                }

                            }

                        }
					}
				}
				// End extra parameters
				//comparator.appendToStaticCompareString(contentTypeAttribute.getName());
				
			}

		}
		catch(Exception e)
		{
			Logger.logInfo("An error occurred when we tried to get the attributes of the content type: " + e.getMessage());	
		}

		return attributes;
	}

	public static ArrayList<String> getContentTypeAttributeNamesComparator(String schemaValue)
	{
//		StringBuilder staticCompareString = new StringBuilder();
		ArrayList<String> attributeList = new ArrayList<String>();

		try
		{
			SAXReader reader = new SAXReader();
			Document document = reader.read(new StringReader(schemaValue));
			String attributesXPath = "/xs:schema/xs:complexType/xs:all/xs:element/xs:complexType/xs:all/xs:element";

			@SuppressWarnings("unchecked")
			List<Element> anl = document.selectNodes(attributesXPath); 

			int cnt = 0;
            for(Iterator<Element> i = anl.iterator(); i.hasNext();)
			{
				Element child = i.next();
				String attributeName = child.valueOf("@name");

				attributeList.add(attributeName);
			}
		}
		catch(Exception e)
		{
			Logger.logInfo("An error occurred when we tried to get the attributes of the content type: " + e.getMessage());	
		}

		return attributeList;
	}

	private static class AttributeComparator implements Comparator<Element>
	{
		private ArrayList<String> staticCompareString;

		// Prevent instancing
		private AttributeComparator()
		{
		}
		
		public AttributeComparator(ArrayList<String> staticCompareString)
		{
			this.staticCompareString = staticCompareString;
			Logger.logConsole("Created attribute comparator with input string:_" + staticCompareString);
		}

		@Override
		public int compare(Element element1, Element element2)
		{
			int index1 = staticCompareString.indexOf(element1);
			int index2 = staticCompareString.indexOf(element2);

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
			
			//System.out.println("Comparing e1: " + element1.getName() + ", e2: " + element2.getName() + ". Result: " + (staticCompareString.indexOf(element1.getName()) - staticCompareString.indexOf(element2.getName())));
			//return staticCompareString.indexOf(element1.getName()) - staticCompareString.indexOf(element2.getName());
		}
	};

}
