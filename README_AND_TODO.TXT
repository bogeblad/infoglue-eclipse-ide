Author: Stefan Sik, stefan.sik@omxgroup.com
Author: --

====================================================================================
Information to new developers: If you want to study this code. The place to start is 
org.infoglue.igide.view.ContentExplorer (createPartControl) and follow the trails. If
you want to run, open /plugin.xml in eclipse and click "Launch eclipse application"
when the ide starts, select "Window | Show view | Other..." and browse for "Infoglue
Development / Infoglue Content Explorer"
====================================================================================

TODO BEFORE RELEASE:

Implement actions: new, delete, move (d&d), copy, publish / commit

Model handling with three models (local, remote igserver, and cvs) how do we handle conflict 
of versions??? 15% done

If possible, Automatically attach the javadoc url for Infoglue to the infoglue jar file.
and possibly the other libraries. 
(org.infoglue.igide.facets.IGFacetInstallAction) so that context sensitive help is provided
from start.

Use HttpClient!! with session support and user definable authentication schemes 
(j_security, basic, digest, NTLM) (see InfoglueConnection) 10% done

Decorate labels (icons) by compositing images:  
for working mode, published mode, cvs:ed etc. see org.infoglue.igide.view.ViewLabelProvider 
and eclipse api for DecoratingLabelProvider 

Fix layout on the form editor. Add more information, like last updated and by whom.

Debugging capabilities (Infoglue core changes required. Among other things, the dispatchJSP 
method should write out some information to let us identify the component being debugged.

versioning support (cvs etc) /partly done

Component properties editor

Relations editor

Structure browser

TESTING, TESTING, TESTING: Especially encoding for different platforms etc. see 
org.infoglue.igide.cms.InfoglueCMS, saveContentVersion and openContentVersion. 5% done


Adapt Infoglue so that we can specify a content type on each attribute in the content
type definition. It would make it simpler in both infoglue and the IDE. For example
HTMLTemplate.Template - text/jsp. Maybe that would require some refactoring on infoglue
to get rid of the hard coded "HTMLTemplate" and maybe open for a more dynamic approach
(JSPComponent.Code (text/jsp), JSPComponent.ComponentProperties (text/xml) 
VelocityTemplate.Template (text/vtl??) FreemarkerTemplate.Template (text/fm??) 
Article.Text (text/html) etc...) This approach will also make the component/template
dispatching simpler in infoglue. 

Do some major improvements on the webservice interface in infoglue.


We have a problem with project facets. An exception is thrown 
stating: org.eclipse.core.runtime.CoreException: One or more constraints have not been satisfied.
this only happens if the project already exists.



Check all TODO: marks in the code!!!

===================================================================================
Moslty finished and finished todos (more than 60%)
===================================================================================
CONNECTION AND NOTIFICATION - Om vi inte kan ansluta till servern, fixa så att ett meddelande om det går 
till lyssnaren, så att vi kan testa igen om en stund. Vi kanske skall schedulera
ett Job för detta??. /mostly done, not rescheduling though. User has to refresh. 90%

extended jspeditor with serverside lookup on objects for codeassist, see MyVeloEditor. /Codeassist Done 
(not serverside though) 90%

cvs-like server handling, so we can connect to an unlimited number of infoglue servers, 
or project handling of this issue. See comments in org.infoglue.igide.InfoglueConnectorPlugin 
/ Partly done, could be nicer, 70% (for 100% fix project sensitivity or let the user rightclick 
the view and add servers, just like the cvs view.)

Fix preferences so we can define a file suffix and/or editor class for each attribute. 
/Partly done 90% (ui needs improvement)



=================================================================================
Working set: A little out of scope for this todo but anyway...
It would be nice to introduce a new state for contentversions in infoglue: "Private working set - n" with a 
dynamic and unique stateid for contentversions belonging to the current user.

