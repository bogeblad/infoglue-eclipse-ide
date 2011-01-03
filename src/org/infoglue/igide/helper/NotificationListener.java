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
 * Created on 2005-apr-26
 *
 */
package org.infoglue.igide.helper;

import org.infoglue.igide.cms.connection.InfoglueConnection;
import org.infoglue.igide.cms.connection.NotificationMessage;

/**
 * @author Stefan Sik
 * 
 */
public interface NotificationListener
{
    public void recieveCMSNotification(NotificationMessage message);
    public void connectedToRemoteSystem(InfoglueConnection connection);
    public void disconnectedFromRemoteSystem(InfoglueConnection connection);
    public void connectionException(InfoglueConnection connection, Exception e);
}
