package org.infoglue.igide;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.infoglue.igide.cms.ContentVersion;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.InfoglueProxy;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.Utils;
import org.infoglue.igide.model.MasterNode;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.view.ContentExplorerView;

public class ProjectResourceChangeListener implements Runnable
{
    private static ContentExplorerView contentExplorerView = null;
    private static TreeViewer viewer = null;
    private List resourcesToProcess;

    public static void setContentExplorerView(ContentExplorerView contentExplorerView, TreeViewer viewer)
    {
        Logger.logConsole((new StringBuilder("Regging.....:")).append(contentExplorerView).append(":").append(viewer).toString());
        contentExplorerView = contentExplorerView;
        viewer = viewer;
    }

    public ProjectResourceChangeListener()
    {
        resourcesToProcess = new ArrayList();
        Logger.logConsole("***********************************************");
        Logger.logConsole("**  Starting a ProjectResourceChangeListener **");
        Logger.logConsole("***********************************************");
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        IResourceChangeListener listener = new IResourceChangeListener() {

            public void resourceChanged(IResourceChangeEvent r)
            {
                IResourceDelta rootDelta = r.getDelta();
                IResourceDelta docDelta = rootDelta;
                if(docDelta == null)
                    return;
                Logger.logConsole("**********************************");
                Logger.logConsole("Something changed!!!!!!");
                try
                {
                    Logger.logConsole((new StringBuilder("Type:")).append(r.getType()).toString());
                    Logger.logConsole((new StringBuilder("Source:")).append(r.getSource()).toString());
                    Logger.logConsole((new StringBuilder("SourceName:")).append(r.getSource().getClass().getName()).toString());
                    Logger.logConsole((new StringBuilder("resource.getName():")).append(r.getResource().getName()).toString());
                }
                catch(Exception e)
                {
                    Logger.logConsole((new StringBuilder("Error:")).append(e.getMessage()).toString());
                }
                IResourceDeltaVisitor visitorTest = new IResourceDeltaVisitor() {

                    public boolean visit(IResourceDelta delta)
                    {
                        IResource resource = delta.getResource();
                        Logger.logConsole((new StringBuilder("delta.getKind():")).append(delta.getKind()).toString());
                        Logger.logConsole((new StringBuilder("resource.getType():")).append(resource.getType()).toString());
                        Logger.logConsole((new StringBuilder("resource.getName():")).append(resource.getName()).toString());
                        if(resource.exists() && resource.getType() == 1 && "xml".equalsIgnoreCase(resource.getFileExtension()) && resource.getName().indexOf("_$") == -1)
                        {
                            Logger.logConsole((new StringBuilder("Resource changed and it was a file:")).append(resource.getName()).append(" - ").append(resource.getFullPath()).toString());
                            synchronized(resourcesToProcess)
                            {
                                resourcesToProcess.add(resource);
                            }
                        }
                        return true;
                    }
                };
                try
                {
                    docDelta.accept(visitorTest);
                }
                catch(CoreException e)
                {
                    Logger.logConsole((new StringBuilder("CoreException:")).append(e.getMessage()).toString());
                }
                if(r.getType() == 2)
                    Display.getDefault().asyncExec(new Runnable() {

                        public void run()
                        {
                            Logger.logConsole("Running...");
                        }
                    });
            }
        };
        Thread t = new Thread(this);
        t.start();
        Logger.logConsole("Registrated new workspace listener..");
        workspace.addResourceChangeListener(listener);
    }

    public void processResource(IResource resource)
    {
        Logger.logConsole((new StringBuilder("Processing resource:")).append(resource.getFullPath()).toString());
        Logger.logConsole((new StringBuilder("Let's check if the file was allready in infoglue:")).append(resource.getFullPath()).append(":").append(resource.getName()).toString());
        boolean isResourceOpen = false;
        String resourceFullPath = resource.getFullPath().toString();
        int resourcePathEnd = resourceFullPath.lastIndexOf("/");
        if(resourcePathEnd > 0)
        {
            String resourcePath = resourceFullPath.substring(0, resourcePathEnd);
            String resourceName = resourceFullPath.substring(resourcePathEnd + 1);
            Logger.logConsole((new StringBuilder("resourcePath: ")).append(resourcePath).toString());
            Logger.logConsole((new StringBuilder("resourceName: ")).append(resourceName).toString());
            List openFullFileNames = InfoglueCMS.getOpenedFullFileNames();
            Logger.logConsole((new StringBuilder("openFullFileNames: ")).append(openFullFileNames).toString());
            Iterator openFullFileNamesIterator = openFullFileNames.iterator();
            while(openFullFileNamesIterator.hasNext()) 
            {
                String openFullFileName = (String)openFullFileNamesIterator.next();
                Logger.logConsole((new StringBuilder("openFullFileName:")).append(openFullFileName).toString());
                int pathEnd = openFullFileName.lastIndexOf("/");
                Logger.logConsole((new StringBuilder("pathEnd:")).append(pathEnd).toString());
                if(pathEnd <= 0)
                    continue;
                String path = openFullFileName.substring(0, pathEnd);
                String fileName = openFullFileName.substring(pathEnd + 1);
                Logger.logConsole((new StringBuilder("open file path: ")).append(path).toString());
                Logger.logConsole((new StringBuilder("open file name: ")).append(fileName).toString());
                Logger.logConsole((new StringBuilder()).append(resourcePath).append("=").append(path).toString());
                if(!resourcePath.equalsIgnoreCase(path))
                    continue;
                Logger.logConsole((new StringBuilder("Correct path - let's check fileName as well:")).append(resourceName).append("vs").append(fileName).toString());
                String cleanedResourceName = resourceName.substring(0, resourceName.lastIndexOf("_"));
                String cleanedFileName = fileName.substring(2, fileName.lastIndexOf("."));
                Logger.logConsole((new StringBuilder()).append(cleanedResourceName).append("=").append(cleanedFileName).toString());
                if(!cleanedResourceName.equalsIgnoreCase(cleanedFileName))
                    continue;
                isResourceOpen = true;
                break;
            }
        }
        Logger.logConsole((new StringBuilder("If false continue:")).append(isResourceOpen).append(". If the resource is open we should not as the sync is carried out by the editor then.").toString());
        if(!isResourceOpen)
        {
            ContentNode node = contentExplorerView.getNodeWithPath(resource, resource.getFullPath(), true);
            Logger.logConsole((new StringBuilder("node in resource change: ")).append(node).toString());
            if(node != null)
            {
                Logger.logConsole((new StringBuilder("New or existing node:")).append(node.getId()).toString());
                Logger.logConsole((new StringBuilder("Is it open and synced by the editor:")).append(InfoglueCMS.getOpenedFileNames().contains(node.getId())).toString());
                if(!InfoglueCMS.getOpenedFileNames().contains(node.getId()))
                {
                    Logger.logConsole("It is not sync by editor so we should sync it even though it exists");
                    try
                    {
                        String versionValue = Utils.getIFileContentAsString(resource);
                        MasterNode masterNode = (MasterNode)viewer.getInput();
                        Logger.logConsole((new StringBuilder("masterNode:")).append(masterNode).append(":").append(masterNode.getText()).append(":").append(masterNode).toString());
                        ContentNode rootContentNode = null;
                        for(int i = 0; i < masterNode.getChildren().length; i++)
                        {
                            ContentNode rootContentNodeCandidate = (ContentNode)masterNode.getChildren()[i];
                            String siteNodeName = rootContentNodeCandidate.getName();
                            String pathSegment = resource.getFullPath().segment(0);
                            Logger.logConsole((new StringBuilder("siteNodeName:")).append(siteNodeName).toString());
                            Logger.logConsole((new StringBuilder("pathSegment:")).append(pathSegment).append("(").append(resource.getFullPath().segment(1)).append(")").toString());
                            if(!siteNodeName.equals(pathSegment))
                                continue;
                            rootContentNode = rootContentNodeCandidate;
                            break;
                        }

                        if(rootContentNode != null)
                        {
                            Logger.logConsole((new StringBuilder("Using rootContentNode:")).append(rootContentNode.getName()).toString());
                            InfoglueConnection conn = rootContentNode.getConnection();
                            InfoglueProxy proxy = conn.getInfoglueProxy();
                            ContentVersion version = new ContentVersion();
                            version.setContentId(node.getId());
                            version.setLanguageId(proxy.getMasterLanguageId(node.getRepositoryId()));
                            version.setValue(versionValue);
                            proxy.updateContentVersion(version);
                        } else
                        {
                            Logger.logConsole((new StringBuilder("No matching root node:")).append(resource.getFullPath().segment(0)).toString());
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        Logger.logConsole((new StringBuilder("Error updating unopened content version:")).append(e.getMessage()).toString(), e);
                    }
                }
            } else
            {
                Logger.logConsole("*********************************************");
                Logger.logConsole("* ERROR: No node found                      *");
                Logger.logConsole("*********************************************");
            }
        }
    }

    public void run()
    {
        Logger.logConsole("Starting thread...");
        try
        {
            Thread.sleep(3000L);
        }
        catch(InterruptedException e)
        {
            Logger.logConsole("Error in thread");
        }

        List currentResourcesToProcess = new ArrayList();
        List processedLocalFiles = new ArrayList();
        do
        {
            synchronized(resourcesToProcess)
            {
                currentResourcesToProcess.addAll(resourcesToProcess);
                resourcesToProcess.clear();
                if(currentResourcesToProcess.size() > 0)
                    Logger.logConsole((new StringBuilder("Processing ")).append(currentResourcesToProcess.size()).append(" right now").toString());
            }
            for(Iterator currentResourcesToProcessIterator = currentResourcesToProcess.iterator(); currentResourcesToProcessIterator.hasNext();)
            {
                IResource resource = (IResource)currentResourcesToProcessIterator.next();
                if(processedLocalFiles.contains(resource.getFullPath().toString()))
                {
                    Logger.logConsole((new StringBuilder("Allready processed:")).append(resource.getFullPath()).toString());
                } else
                {
                    processedLocalFiles.add(resource.getFullPath().toString());
                    processResource(resource);
                }
            }

            currentResourcesToProcess.clear();
            processedLocalFiles.clear();
            try
            {
                Thread.sleep(3000L);
            }
            catch(InterruptedException e)
            {
                Logger.logConsole("Error in thread");
            }
        } while(true);

    }

}
