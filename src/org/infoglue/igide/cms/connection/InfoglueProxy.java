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
package org.infoglue.igide.cms.connection;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.infoglue.deliver.util.CompressionHelper;
import org.infoglue.igide.cms.Content;
import org.infoglue.igide.cms.ContentTypeAttribute;
import org.infoglue.igide.cms.ContentTypeDefinition;
import org.infoglue.igide.cms.ContentVersion;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.cms.exceptions.ConcurrentModificationException;
import org.infoglue.igide.cms.exceptions.InvalidLoginException;
import org.infoglue.igide.cms.exceptions.InvalidVersionException;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.Method;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.jobs.RefreshCMSApplicationSettings;
import org.infoglue.igide.jobs.RefreshContentTypeDefinitions;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.model.content.ContentNodeFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Stefan Sik
 * 
 */
public class InfoglueProxy
{
	/*
	 * Static map of contenttype attributes 
	 */
	private static Map<String, String> globalAttributes = new HashMap<String, String>();
	private static boolean enableCompression = false; 
    private InfoglueConnection connection;

	private Object contentTypeDefinitionsLock = new Object();
    private List<ContentTypeDefinition> contentTypeDefinitions = null;
    private List<Method> templateLogicMethods = null;

    private Object applicationSettingsLock = new Object();
    private Map<String, String> applicationSettings = null;
    private Map<String, Collection<Method>> methodCache = new HashMap<String, Collection<Method>>();

    protected InfoglueProxy(final InfoglueConnection connection) throws InvalidLoginException, IOException, InvocationTargetException
    {
        this.connection = connection;
        try {
			init();
		}
        catch(MalformedURLException e1)
        {
        	throw e1;
        }
        catch(InvalidLoginException e2)
        {
        	throw e2;
        }
        catch(IOException e3)
        {
        	throw e3;
        }
        catch (Throwable e) {
			e.printStackTrace();
			throw new InvocationTargetException(e);
		}
    }

    protected void init() throws Throwable
    {
    	new RefreshCMSApplicationSettings(connection).schedule();
    	new RefreshContentTypeDefinitions(connection).schedule();

    	if(InfoglueConnection.CONTENTVERSIONSERVICEACTION.indexOf("enableCompression=true") > -1)
    	{
    		enableCompression = true;
    	}
    }
    
    public synchronized void refreshContentTypeDefinitions() throws InvalidLoginException, IOException, DocumentException
    {
    	synchronized (contentTypeDefinitionsLock) {
			contentTypeDefinitions = fetchContentTypeDefinitions();
            Collections.sort(contentTypeDefinitions);
            fillGlobalAttributeKeys();
        }
    }

    public void refreshCMSSettings() throws InvalidLoginException, IOException, DocumentException {
    	synchronized (applicationSettingsLock) {
    		try 
    		{
    			applicationSettings = fetchApplicationSettings();
    		} 
    		catch (Exception e) 
    		{
    			e.printStackTrace();
    		}
    	}
		connection.setDeliveryBaseUrl(getCMSSetting("previewDeliveryUrl"));
	}
    
    private void fillGlobalAttributeKeys() {
    	for(Iterator<ContentTypeDefinition> i = contentTypeDefinitions.iterator(); i.hasNext();)
    	{
    		ContentTypeDefinition def = i.next();
            Map attributes = InfoglueCMS.getContentTypeAttributes(def.getSchemaValue());
            for(Iterator j=attributes.keySet().iterator(); j.hasNext();)
            {
                ContentTypeAttribute attribute = (ContentTypeAttribute) attributes.get(j.next());
                String key = def.getName() + "." + attribute.getName();
                globalAttributes.put(key, attribute.getInputType());
            }
    	}
		
	}

	public String getCMSSetting(String key) throws InvalidLoginException, IOException, DocumentException
    {
        Map<String, String> settings = getApplicationSettings();
        return settings.get(key);
    }
    
    public Collection<Method> getClassMethods(String className) throws DocumentException, InvalidLoginException, IOException, Exception
    {
        Collection<Method> methods = methodCache.get(className);
        if(methods == null)
        {
            methods = fetchClassMethods(className);
            methodCache.put(className, methods);
        }
        return methods;
    }
    
    public Map<String, String> getApplicationSettings() throws InvalidLoginException, IOException, DocumentException
    {
    	synchronized (applicationSettingsLock) {
            if (applicationSettings == null)
            {
                applicationSettings = fetchApplicationSettings();
            }
			
		}
        return applicationSettings;
    }

    public Collection<Method> getTemplateLogicMethods() throws InvalidLoginException, IOException, DocumentException, Exception
    {
        if (templateLogicMethods == null)
        {
            templateLogicMethods = fetchTemplateLogicMethods();
        }
        return templateLogicMethods;
    }
    
    public List<Method> fetchTemplateLogicMethods() throws InvalidLoginException, IOException, DocumentException, Exception
    {
        String xml = null;
        xml = connection.getXML("", InfoglueConnection.TEMPLATELOGICMETHODS);
        templateLogicMethods = deserializeMethods(xml);
        return templateLogicMethods;
    }

    public Collection<Method> fetchClassMethods(String className) throws DocumentException, InvalidLoginException, IOException, Exception
    {
        String xml = null;
        xml = connection.getXML("", InfoglueConnection.TEMPLATELOGICMETHODS + "className=" + className);
        Collection<Method> methods = deserializeMethods(xml);
        return methods;
    }
    
    public Collection<ContentTypeDefinition> getContentTypeDefinitions() throws InvalidLoginException, IOException, DocumentException
    {
        if (contentTypeDefinitions == null)
        {
        	synchronized (contentTypeDefinitionsLock) {
                contentTypeDefinitions = fetchContentTypeDefinitions();
                Collections.sort(contentTypeDefinitions);
			}
        }
        return contentTypeDefinitions;
    }
    
    public List<ContentTypeDefinition> fetchContentTypeDefinitions() throws InvalidLoginException, IOException, DocumentException
    {
    	try
    	{
            String xml = null;
            Logger.logConsole("Getting content type definitions...");
            xml = connection.getXML("", InfoglueConnection.CONTENTTYPEDEFSERVICEACTION);
            //Logger.logConsole("DOC: " + xml);
            contentTypeDefinitions = deserializeContentTypeDefinitions(xml);
            Logger.logConsole("contentTypeDefinitions:" + contentTypeDefinitions.size());
    	}
    	catch (Exception e) 
    	{
    		Logger.logConsole("Error fetching content types:" + e.getMessage());
		}
        return contentTypeDefinitions;
    }
    
    public Map<String, String> fetchApplicationSettings() throws InvalidLoginException, IOException, DocumentException
    {
    	
            String xml = null;
            xml = connection.getXML("", InfoglueConnection.APPLICATIONSETTINGS);
            
            xml = xml.replaceAll("<\\d", "<_");
            xml = xml.replaceAll("</\\d", "</_");
            
            
            // Logger.logInfo("DOC: " + xml);
            applicationSettings = deserializeApplicationSettings(xml);
            
            return applicationSettings;
    }
    
    public static void main(String[] arg)
    {
    	
    	System.out.println("<assas></assas><2dsssdda><12377.sdsdssdssd><1.fdfdfd>".replaceAll("<\\d", "<_"));
    	
    }
    

    public ContentTypeDefinition getContentTypeDefinition(Integer id) throws Exception
    {
        Collection<ContentTypeDefinition> definitions = getContentTypeDefinitions();
        for(Iterator<ContentTypeDefinition> i = definitions.iterator();i.hasNext();)
        {
            ContentTypeDefinition definition = i.next();
            if(definition.getId().compareTo(id)==0)
                return definition;
        }
        return null;
    }
	public ContentTypeDefinition getContentTypeDefinition(String string) throws InvalidLoginException, IOException, DocumentException 
	{
        Collection<ContentTypeDefinition> definitions = getContentTypeDefinitions();
        for(Iterator<ContentTypeDefinition> i = definitions.iterator();i.hasNext();)
        {
            ContentTypeDefinition definition = i.next();
            if(definition.getName().equals(string))
                return definition;
        }
        return null;
	}
    
    public ContentVersion fetchContentVersion(Integer contentVersionId) throws InvalidLoginException, IOException, DocumentException
    {
    	
        String xml  = null;
        String src = "?parent=" + contentVersionId;
        xml = connection.getXML(src, InfoglueConnection.CONTENTVERSIONSERVICEACTION);
        Logger.logInfo("DOC: " + xml);
        ContentVersion version = deserializeContentVersion(xml); 
        
        return version;
    }
    
    public ContentVersion fetchContentVersionHead(Integer contentVersionId) throws InvalidLoginException, IOException, DocumentException
    {
    	
        String xml  = null;
        String src = "?parent=" + contentVersionId;
        xml = connection.getXML(src, InfoglueConnection.CONTENTVERSIONHEADSERVICEACTION);
        ContentVersion version = deserializeContentVersion(xml, true); 
        
        return version;
    }
    
    public Content fetchContent(Integer contentId) throws InvalidLoginException, IOException, DocumentException
    {
    	
        String xml  = null;
        String src = "?parent=" + contentId;
        xml = connection.getXML(src, InfoglueConnection.CONTENTSERVICEACTION);
        Content content = deserializeContent(xml); 
        
        return content;
    }

    public Collection<ContentNode> fetchNodeChildren(ContentNode node) throws DocumentException, Exception
    {
        String nodexml  = null;
        try
        {
            nodexml = connection.getXML(node.getSrc(), InfoglueConnection.TREESERVICEACTION);
        }
        catch (Exception e)
        {
        	Logger.logConsole("Error fetchNodeChildren:" + e.getMessage());
            e.printStackTrace();
        }
        return deserializeNodes(nodexml, node);
    }
    
    /*
     * TODO:TODO:TODO: VERY IMPORTANT, CHANGE THE CALL TO IG WEBSERVICECALL
     * OR CREATE AN XMLVIEW IN INFOGLUE.
     */
    public Integer createContent(Content content) throws Exception
    {
    	Integer newContentId = null;
		// System.out.println("in proxy.createContent: parent=" + parent + "");
    	
    	// 2007-10-26 10:19
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	
        Map<String, String> data = new HashMap<String, String>();
        data.put("isBranch", "" + content.isBranch());
        data.put("parentContentId", "" + content.getParentContentId());
        data.put("repositoryId", "" + content.getRepositoryId());
        data.put("publishDateTime", "" + format.format(content.getPublishdatetime()));
        data.put("expireDateTime", "" + format.format(content.getExpiredatetime()));
        data.put("contentTypeDefinitionId", "" + content.getTypedefid());
        data.put("name", content.getName());
        
        System.out.println("Posting data:");
        String returnData = connection.postData("", InfoglueConnection.CREATECONTENT, data);
        newContentId = Utils.uglyParseContentId(returnData);
        return newContentId;
    }
    
	public void createDefaultWorking(Integer contentId) throws MalformedURLException {
		String params = "?contentId=" + contentId +
		/*"&languageId=" + parent +*/
		"&forceWorkingChange=true";
        Map<String, String> data = new HashMap<String, String>();
        data.put("contentId", "" + contentId);
        data.put("forceWorkingChange", "true");
        
        String returnData = connection.postData("", InfoglueConnection.VIEWCONTENTVERSIONSTANDALONE, data);
        System.out.println("Returndata:" + returnData);
	}
    
    public ContentVersion updateContentVersion(ContentVersion version) throws Exception 
    {
    	/*
    	 * TODO: discover and handle concurrent modification exception on the server side.
    	 */
    	synchronized(version)
    	{
        	String returnData = null;
        	ContentVersion updated = null;
        	String service = InfoglueConnection.UPDATECONTENTVERSION;
        	
            Map<String, String> data = new HashMap<String, String>();
            if(version.getId()!=null)
            {
            	data.put("contentVersionId", "" + version.getId());
            }
            else
            {
            	service = InfoglueConnection.UPDATECONTENTVERSIONSTANDALONE;
            }
            if(version.getMod()!=null)
            {
                data.put("oldModifiedDateTime", "" + version.getMod().getTime());
            }
            
            data.put("contentId", "" + version.getContentId());
            data.put("languageId", "" + version.getLanguageId());
            data.put("versionValue", version.getValue());
            
            returnData = connection.postData("", service, data);
            
            System.out.println("***********************************************************************************");
            System.out.println("[" + version.getValue() + "]");
            System.out.println("***********************************************************************************");
            System.out.println("***********************************************************************************");
            System.out.println(returnData);
            System.out.println("***********************************************************************************");
            
            /*
             * TODO: TODO: TODO: FIX THIS, JUST A QUICK FIX TO MAKE IT WORK.
             * DESERIALIZE THE DATA FROM CMS INSTEAD.
             */
            
            if(returnData.indexOf("<concurrentmodification/>")>-1)
        	{
            	throw new ConcurrentModificationException();
        	}
            
            if(returnData.indexOf("<invalidstate/>")>-1)
        	{
            	throw new IllegalStateException("ContentVersion is not in working mode");
        	}
            
            if(returnData.indexOf("<invalidversion/>")>-1)
            {
            	throw new InvalidVersionException();
            }
            
            if(returnData.indexOf("<org.infoglue.cms.entities.content.ContentVersionVO>")==-1)
            {
            	Exception serverException = null;
            	try {
        			String xml = returnData;
        			XStream xStream = new XStream(new DomDriver());
        			Object o = xStream.fromXML(xml);
        			if(o instanceof Exception)
        			{
        				serverException = (Exception) o;
        			}
            	}
            	catch(Exception e) 	
            	{
            		// no operation
            	}
            	if(serverException != null) 
            		throw serverException;
            	else
            		throw new Exception(returnData);
            }
            
            /*
             * TODO: TODO: TODO: remove this as soon as possible, 
             * what we need is the modification date set by the server.
             * we get that in the xml returndata, so parse it and use it. 
             */
            try {
    			updated = fetchContentVersionHead(version.getId());
    			System.out.println("Setting new mod on this version: " + updated.getMod());
    			version.setMod(updated.getMod());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            return version;
    	}
    	
    }

    private static Integer toInteger(String v)
    {
        try
        {
            return new Integer(v);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    private static List<ContentTypeDefinition> deserializeContentTypeDefinitions(String xml) throws DocumentException, UnsupportedEncodingException
    {
	    List<ContentTypeDefinition> result = new ArrayList<ContentTypeDefinition>();
		SAXReader reader = new SAXReader();
        Document document = reader.read(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));

        List list = document.selectNodes( "//definitions/definition");
        for (Iterator iter = list.iterator(); iter.hasNext(); ) 
        {
            ContentTypeDefinition def = new ContentTypeDefinition();
            // def.setConnection(connection);
            Element row = (Element) iter.next();
            def.setId(toInteger(row.valueOf("@id")));
            def.setType(toInteger(row.valueOf("@type")));
            def.setName(row.valueOf("@name"));
            Node value = row.selectSingleNode("schemaValue");
            String val = value.getStringValue();
            def.setSchemaValue(URLDecoder.decode(val, "UTF-8"));
            result.add(def);
            Logger.logInfo(def);
        }
		return result;
    }

    private static String removePackage(String s)
    {
        if(s.indexOf(".") > -1)
            s = s.substring(s.lastIndexOf(".") +1);
        return s;
    }
    
    private static String simplifyArgs(String args)
    {
        String ret = "";
        String[] a = args.split(",");
        for(int i=0; i<a.length; i++)
        {
            if(i!=0) ret+=", ";
            ret += removePackage(a[i].trim());
        }
        return ret;
    }
    
    private Collection<ContentNode> deserializeNodes(String xml, ContentNode parentNode) throws DocumentException, Exception
    {
	    Collection<ContentNode> result = new ArrayList<ContentNode>();
		SAXReader reader = new SAXReader();
        Document document = reader.read(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        List list = document.selectNodes( "//tree/tree");
        for (Iterator iter = list.iterator(); iter.hasNext(); ) 
        {
            Element row = (Element) iter.next();
            String src = row.valueOf("@src").trim();
            if(src.length()==0) src = null;
            

            Integer repositoryId 			= toInteger(row.valueOf("@repositoryId"));
            Integer activeVersion 			= toInteger(row.valueOf("@activeVersion"));
        	Integer activeVersionStateId 	= null;
        	String 	activeVersionModifier 	= null; 
            Integer contentTypeId 			= toInteger(row.valueOf("@contentTypeId")); 
            String 	hasChildren 			= row.valueOf("@hasChildren");
            boolean bHasChildren 			= hasChildren == null ? false: hasChildren.equalsIgnoreCase("true") ;
            
            try 
            {
            	activeVersionStateId 		= toInteger(row.valueOf("@activeVersionStateId"));
            	activeVersionModifier 		= row.valueOf("@activeVersionModifier"); 
            } 
            catch(Exception e)
            {e.printStackTrace();}

            
            ContentNode node = ContentNodeFactory.createContentNode
            	(
            			parentNode, 
            			row.valueOf("@id"), 
            			row.valueOf("@parent"), 
            			row.valueOf("@text"), 
            			row.valueOf("@type"), 
            			src, 
            			activeVersion, 
            			repositoryId, 
            			activeVersionStateId, 
            			contentTypeId, 
            			activeVersionModifier, 
            			bHasChildren
            	);
            
            result.add(node);
        }
		return result;
    }
    
    private static List<Method> deserializeMethods(String xml) throws DocumentException, Exception
    {
	    List<Method> result = new ArrayList<Method>();
		SAXReader reader = new SAXReader();
        Document document = reader.read(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));

        List list = document.selectNodes( "//methods/method");
        for (Iterator iter = list.iterator(); iter.hasNext(); ) 
        {
            Element row = (Element) iter.next();
            String name = row.valueOf("@name");
            String returnType = row.valueOf("@returnType");
            String args = row.valueOf("@args");
            result.add(new Method(name, returnType, args));
        }
		return result;
    }
    
    private static Map<String, String> deserializeApplicationSettings(String xml) throws DocumentException
    {
        Map<String, String> settings = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
        Document document = reader.read(new StringBufferInputStream(xml));
        
        for(Iterator i = document.getRootElement().elementIterator();i.hasNext();)
        {
            Element elm = (Element) i.next();
            settings.put(elm.getName(), elm.getText());
        }
        return settings;
    }
    
    private static ContentVersion deserializeContentVersion(String xml) throws DocumentException, UnsupportedEncodingException
    {
    	return deserializeContentVersion(xml, false);
    }

    private static Content deserializeContent(String xml) throws DocumentException, UnsupportedEncodingException
    {
    	// TODO: set repositoryId and parentContentId
		SAXReader reader = new SAXReader();
        Document document = reader.read(new StringBufferInputStream(xml));
        Content content = new Content();

        Node node = document.selectSingleNode("/content");
        content.setId(toInteger(node.valueOf("@id")));
        content.setName(node.valueOf("@name"));
        content.setCreatorName(node.valueOf("@creatorName"));
        content.setTypedefid(toInteger(node.valueOf("@typedefid")));
        content.setBranch(Boolean.parseBoolean(node.valueOf("@isbranch")));
        content.setActiveVersion(toInteger(node.valueOf("@activeVersion")));
        content.setActiveVersionStateId(toInteger(node.valueOf("@activeVersionStateId")));
        content.setActiveVersionModifier(node.valueOf("@activeVersionModifier"));
        content.setMasterLanguageId(toInteger(node.valueOf("@initialLanguageId")));
        
        try 
        {
        	content.setExpiredatetime(new Date(new Long(node.valueOf("@expiredatetime")).longValue()));
        	content.setPublishdatetime(new Date(new Long(node.valueOf("@publishdatetime")).longValue()));
        }
        catch(Exception e)
		{
        	System.out.println("failed to set date: " + node.valueOf("@expiredatetime"));
        	System.out.println("failed to set date: " + node.valueOf("@publishdatetime"));
		}
        
        List list = document.selectNodes( "//content/versions/contentVersion");
        for (Iterator iter = list.iterator(); iter.hasNext(); ) 
        {
            Element row = (Element) iter.next();
            System.out.println("Deserialize content: Found contentVersion: " + row.asXML());
        }
        
		return content;
    }
    
    private static ContentVersion deserializeContentVersion(String xml, boolean headonly) throws DocumentException, UnsupportedEncodingException
    {
		SAXReader reader = new SAXReader();
        Logger.logConsole("Parsing XML:" + xml);
		Document document = reader.read(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        //Document document = reader.read(new StringBufferInputStream(xml));
        Logger.logConsole("document:" + document);
        ContentVersion contentVersion = new ContentVersion();
        String value = "";

        Node node = document.selectSingleNode("/contentVersion/head");
        System.out.println("Creating contentVersion of: " + node.asXML());
        
        contentVersion.setId(toInteger(node.valueOf("@id")));
        contentVersion.setLanguageId(toInteger(node.valueOf("@languageId")));
        contentVersion.setContentId(toInteger(node.valueOf("@contentId")));
        contentVersion.setStateId(toInteger(node.valueOf("@activeVersionStateId")));
        contentVersion.setVersionModifier(node.valueOf("@activeVersionModifier"));
        contentVersion.setLanguageName(node.valueOf("@languageName"));
        try 
        {
        	contentVersion.setMod(new Date(new Long(node.valueOf("@mod")).longValue()));
        }
        catch(Exception e)
		{
        	System.out.println("failed to set date: " + node.valueOf("@mod"));
		}
        contentVersion.setActive(node.valueOf("@isActive").equalsIgnoreCase("true"));
        
        if(!headonly)
        {
            Node val = document.selectSingleNode("/contentVersion/value");
            value = val.getStringValue();
            if(enableCompression )
            {
                Base64Encoder decoder = new Base64Encoder();
                byte[] b_value = decoder.decode(value);
                CompressionHelper zip = new CompressionHelper();
                String decoded = zip.decompress(b_value);
                contentVersion.setValue(decoded);
            }
            else
            {
                contentVersion.setValue(URLDecoder.decode(value,"UTF-8"));
            }
        }
        
		return contentVersion;
    }

    public static NotificationMessage createNotificationMessage(InfoglueConnection connection, String data) throws DocumentException
    {
        System.out.println("Creating notification message:" + data);
		SAXReader reader = new SAXReader();
        Document document = reader.read(new StringBufferInputStream(data));
        Node node = document.selectSingleNode("/org.infoglue.cms.util.NotificationMessage");
        NotificationMessage message = new NotificationMessage(
        		connection, 
                node.selectSingleNode("name").getText(),
                node.selectSingleNode("objectName").getText(),
                node.selectSingleNode("systemUserName").getText(),
                toInteger(node.selectSingleNode("type").getText()).intValue(),
                node.selectSingleNode("objectId").getText(),
                node.selectSingleNode("className").getText()
                );
        Node extraInfo = node.selectSingleNode("extraInfo");
        
        try {
	        int hashCode = new Integer(extraInfo.valueOf("@hashCode")).intValue(); 
	        String modifier = extraInfo.valueOf("@versionModifier");
	        message.setVersionModifier(modifier);
	        message.setHashCode(hashCode);
        }
        catch(Exception e)
        {
            
        }
        
        String value = "";
		return message;
    }

	public static synchronized Map<String, String> getGlobalAttributes() {
		return globalAttributes;
	}


    
}
