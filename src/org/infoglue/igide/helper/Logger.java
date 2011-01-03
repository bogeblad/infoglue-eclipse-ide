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
 * Created on 2004-nov-19
 *
 */
package org.infoglue.igide.helper;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Stefan Sik
 *
 */
public class Logger {
	private static final String CONSOLE_NAME = "Infoglue";
	

	public static void logInfo(Object object)
	{
		// System.out.println(object);
	}
	
	public static synchronized void logConsole(String message)
	{
		MessageConsole myConsole = Utils.findConsole(CONSOLE_NAME);
		synchronized (myConsole)
		{
			MessageConsoleStream out = myConsole.newMessageStream();
			out.println(message);
		}
	}
}
