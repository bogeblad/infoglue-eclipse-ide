// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RefreshServerJob.java

package org.infoglue.igide.jobs;

import java.net.MalformedURLException;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.infoglue.igide.InfoglueConnectorPlugin;
import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.helper.Logger;
import org.infoglue.igide.helper.ProjectHelper;
import org.infoglue.igide.model.MasterNode;
import org.infoglue.igide.model.content.ContentNode;
import org.infoglue.igide.model.content.ContentNodeFactory;
import org.infoglue.igide.view.ContentExplorerView;
import org.infoglue.igide.view.ViewContentProvider;

public class RefreshServerJob extends UIJob
{

    public RefreshServerJob(ContentExplorerView view, ContentNode contentNode)
    {
        super("Refreshing Infoglue Content Explorer: ");
        this.view = view;
        this.contentNode = contentNode;
        setPriority(20);
    }

    public IStatus runInUIThread(IProgressMonitor p)
    {
        p.beginTask(getName(), 100);
        refreshServer();
        p.worked(80);
        view.getViewer().refresh();
        p.done();
        return Status.OK_STATUS;
    }

    public void refreshServer()
    {
    	Logger.logConsole("view: " + view);
    	Logger.logConsole("viewer: " + view.getViewer());
        ViewContentProvider provider = (ViewContentProvider)view.getViewer().getContentProvider();
        MasterNode masterNode = (MasterNode)view.getViewer().getInput();
        Logger.logConsole("masterNode: " + masterNode);
        Logger.logConsole("refreshMaster..");
        ContentNode contentNode = getInfoglueRoot(masterNode);
        if(contentNode != null)
        {
        	Logger.logConsole("contentNode:" + contentNode + ":" + contentNode.getName() + ":" + contentNode.getText());
            masterNode.removeIfNameContains(contentNode);
            if(masterNode.addIfNotContains(contentNode))
            {
            	try
                {
                    contentNode.getConnection().addNotificationListener(provider);
                }
                catch(MalformedURLException e)
                {
                    Logger.logConsole((new StringBuilder("Wrong url:")).append(e.getMessage()).toString());
                }
            }
        }
        Logger.logConsole("Done refreshing...");
    }

    private ContentNode getInfoglueRoot(MasterNode master)
    {
        ContentNode root = null;
        Logger.logConsole((new StringBuilder("master:")).append(master).append(":").append(contentNode == null ? "null" : contentNode.getName()).toString());
        String value = InfoglueConnectorPlugin.getDefault().getPreferenceStore().getString("projects");
        String sRoots[] = value.split(";");
        String as[];
        int j = (as = sRoots).length;
        for(int i = 0; i < j; i++)
        {
            String s = as[i];
            String r[] = s.split(",");
            try
            {
                Logger.logConsole((new StringBuilder("r[0]:")).append(r[0]).append("=").append(contentNode == null ? "null" : contentNode.getName()).toString());
                if(contentNode == null || r[0].equals(contentNode.getName()))
                {
                	try
                	{
	                    Logger.logConsole((new StringBuilder("Connecting to CMS at ")).append(r[0]).append(":").append(r[2]).append(":").append(r[3]).toString());
	                    InfoglueConnection connection = new InfoglueConnection(r[1], r[2], r[3], view.getViewer());
	                    Logger.logConsole((new StringBuilder("connection ")).append(connection).toString());
	                    org.eclipse.core.resources.IProject proj = ProjectHelper.getOrCreateProject(r[0], false);
	                    Logger.logConsole((new StringBuilder("proj ")).append(proj).toString());
	                    root = ContentNodeFactory.createContentRootNode(proj, master, connection);
	                    Logger.logConsole((new StringBuilder("root ")).append(root).toString());
                	}
                	catch(IllegalStateException e)
        			{
        				// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        				Logger.logConsole("Failed to get content types for " + r[0] + "(" + r[1] + ")");
        				final Shell shell = view.getSite().getShell();
        				MessageDialog.openInformation(shell, "Retrieving content types and transaction history failed", "Retrieving content types and transaction history. This could be due to a slow query in InfoGlue.\nTry adding the following index to the database.\n\nRun: create index transactionObjectNameIndex ON cmTransactionHistory (transactionObjectId(255)) if it's slow");
        			}
                }
            }
            catch(Exception e)
            {
                Logger.logConsole((new StringBuilder("Error connecting to CMS at ")).append(r[0]).toString());
            }
        }

        return root;
    }

    ContentExplorerView view;
    ContentNode contentNode;
}
