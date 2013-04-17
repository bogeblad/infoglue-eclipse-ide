package org.infoglue.igide.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.eclipse.core.resources.IFile;
import org.infoglue.igide.cms.ContentVersion;
import org.infoglue.igide.cms.InfoglueCMS;
import org.infoglue.igide.editor.*;

// Referenced classes of package org.infoglue.igide.helper:
//            Logger, Utils

public class DelayedFileWriter
    implements Runnable
{

    public DelayedFileWriter(Map fileValueMap, InfoglueEditorInput input, IGMultiPageEditor editor, IFile masterFile)
    {
        this.fileValueMap = null;
        this.fileValueMap = fileValueMap;
        this.input = input;
        this.editor = editor;
        this.masterFile = masterFile;
    }

    public void run()
    {
        try
        {
            Thread.sleep(500L);
            Logger.logConsole((new StringBuilder("Getting projectContentVersion for:")).append(input.getContent().getNode().getId()).toString());
            ContentVersion cv = InfoglueCMS.getProjectContentVersion(input.getContent().getNode().getProject().getName(), input.getContent().getNode().getId());
            for(Iterator fileValueMapIterator = fileValueMap.entrySet().iterator(); fileValueMapIterator.hasNext();)
            {
                java.util.Map.Entry entry = (java.util.Map.Entry)fileValueMapIterator.next();
                IFile file = (IFile)entry.getKey();
                String attributeName = file.getName().substring(2);
                if(attributeName.indexOf("_") > -1)
                    attributeName = attributeName.substring(attributeName.lastIndexOf("_") + 1, attributeName.lastIndexOf("."));
                else
                    attributeName = "Template";
                Logger.logConsole((new StringBuilder("attributeName:")).append(attributeName).toString());
                Logger.logConsole((new StringBuilder("MasterFile:")).append(masterFile).toString());
                Logger.logConsole((new StringBuilder("File:")).append(file).toString());
                Logger.logConsole((new StringBuilder("masterFileLocalDateTime:")).append(masterFile.getLocalTimeStamp()).toString());
                Logger.logConsole((new StringBuilder("file.getLocalTimeStamp():")).append(file.getLocalTimeStamp()).toString());
                Logger.logConsole((new StringBuilder("Diff:")).append(masterFile.getLocalTimeStamp() - file.getLocalTimeStamp()).toString());
                if(masterFile.getLocalTimeStamp() - file.getLocalTimeStamp() > 5000L)
                {
                    String contentVersionXMLFromMasterFile = Utils.getIFileContentAsString(masterFile);
                    //System.out.println("Value of file:\n" + contentVersionXMLFromMasterFile);
                    cv.setValue(contentVersionXMLFromMasterFile);
                    InfoglueCMS.updateLocalFile(file, InfoglueCMS.getAttributeValue(cv, attributeName));
                } else
                {
                    Logger.logConsole((new StringBuilder("Skipping writing to ")).append(file.getName()).toString());
                }
            }

            try
            {
                Logger.logConsole((new StringBuilder("DoSAVE on content:")).append(cv.getContentId()).append(":").append(input.getContent().getId()).append(":").append(cv.getValue().substring(113, 200)).toString());
                input.getContent().doSave(null);
                InfoglueCMS.saveContentVersion(input.getContent(), cv, input.getContent().getConnection(), false, null);
            }
            catch(Exception e)
            {
                Logger.logConsole((new StringBuilder("Error:")).append(e.getMessage()).toString());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                Logger.logConsole((new StringBuilder("Error in saveContentVersion:")).append(e.getMessage()).toString());
            }
        }
        catch(Exception e)
        {
            Logger.logConsole((new StringBuilder("Error in delayed thread:")).append(e.getMessage()).toString());
        }
    }

    private Map fileValueMap;
    private InfoglueEditorInput input;
    private IGMultiPageEditor editor;
    private IFile masterFile;
}
