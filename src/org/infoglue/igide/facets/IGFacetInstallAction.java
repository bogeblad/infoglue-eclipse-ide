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
package org.infoglue.igide.facets;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.infoglue.igide.helper.Utils;


/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class IGFacetInstallAction implements IDelegate {

	public void execute(IProject arg0, IProjectFacetVersion arg1, Object arg2, IProgressMonitor arg3) throws CoreException 
	{
		System.out.println("IGFacetInstallAction running");

		final IFolder webInfTld = Utils.getWebInfTldDir( arg0 );
		final IFolder webInfLib = Utils.getWebInfLibDir( arg0 );
		if(!webInfTld.exists())
		{
			webInfTld.create(true, true, arg3);
		}

		Utils.copyFromPlugin( new Path( "res/tld/domtags.tld" ),webInfTld.getFile( "domtags.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/domutil.tld" ),webInfTld.getFile( "domutil.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-cms-content.tld" ),webInfTld.getFile( "infoglue-cms-content.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-cms-management.tld" ),webInfTld.getFile( "infoglue-cms-management.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-common.tld" ),webInfTld.getFile( "infoglue-common.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-content.tld" ),webInfTld.getFile( "infoglue-content.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-management.tld" ),webInfTld.getFile( "infoglue-management.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-page.tld" ),webInfTld.getFile( "infoglue-page.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-structure.tld" ),webInfTld.getFile( "infoglue-structure.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/infoglue-workflow.tld" ),webInfTld.getFile( "infoglue-workflow.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/portlet.tld" ),webInfTld.getFile( "portlet.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/taglibs-image.tld" ),webInfTld.getFile( "taglibs-image.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/taglibs-mailer.tld" ),webInfTld.getFile( "taglibs-mailer.tld" ) );

		/*
		 * Standard taglibs
		 */
		Utils.copyFromPlugin( new Path( "res/tld/c.tld" ),webInfTld.getFile( "c.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/c-1_0.tld" ),webInfTld.getFile( "c-1_0.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/c-1_0-rt.tld" ),webInfTld.getFile( "c-1_0-rt.tld" ) );
		
		Utils.copyFromPlugin( new Path( "res/tld/fmt.tld" ),webInfTld.getFile( "fmt.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/fmt-1_0.tld" ),webInfTld.getFile( "fmt-1_0.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/fmt-1_0-rt.tld" ),webInfTld.getFile( "fmt-1_0-rt.tld" ) );
		
		Utils.copyFromPlugin( new Path( "res/tld/fn.tld" ),webInfTld.getFile( "fn.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/permittedTaglibs.tld" ),webInfTld.getFile( "permittedTaglibs.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/scriptfree.tld" ),webInfTld.getFile( "scriptfree.tld" ) );
		
		Utils.copyFromPlugin( new Path( "res/tld/sql.tld" ),webInfTld.getFile( "sql.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/sql-1_0.tld" ),webInfTld.getFile( "sql-1_0.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/sql-1_0-rt.tld" ),webInfTld.getFile( "sql-1_0-rt.tld" ) );
		
		Utils.copyFromPlugin( new Path( "res/tld/x.tld" ),webInfTld.getFile( "x.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/x-1_0.tld" ),webInfTld.getFile( "x-1_0.tld" ) );
		Utils.copyFromPlugin( new Path( "res/tld/x-1_0-rt.tld" ),webInfTld.getFile( "x-1_0-rt.tld" ) );
		
		/*
		 * Jar files
		 */
		Utils.copyFromPlugin( new Path( "res/lib/domtags.jar" ),webInfLib.getFile( "domtags.jar" ) );
		Utils.copyFromPlugin( new Path( "res/lib/jstl.jar" ),webInfLib.getFile( "jstl.jar" ) );
		Utils.copyFromPlugin( new Path( "res/lib/servlet-api.jar" ),webInfLib.getFile( "servlet-api.jar" ) );
		Utils.copyFromPlugin( new Path( "res/lib/standard.jar" ),webInfLib.getFile( "standard.jar" ) );
		Utils.copyFromPlugin( new Path( "res/lib/taglibs-mailer.jar" ),webInfLib.getFile( "taglibs-mailer.jar" ) );
		Utils.copyFromPlugin( new Path( "res/lib/infoglue-2.5.jar" ),webInfLib.getFile( "infoglue-2.5.jar" ) );

		Utils.registerInfoglueTaglibs(arg0);

	}

}
