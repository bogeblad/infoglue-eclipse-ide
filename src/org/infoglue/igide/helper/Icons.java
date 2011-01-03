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
package org.infoglue.igide.helper;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

/**
 * 
 * @author <a href="mailto:stefan.sik@omxgroup.com">Stefan Sik</a>
 *
 */
public class Icons
{
  private static ImageRegistry image_registry;

  public static URL newURL(String url_name)
  {
    try
    {
      return new URL(url_name);
    }
    catch (Exception e)
    {
        e.printStackTrace();
      throw new RuntimeException("Malformed URL " + url_name, e);
    }
  }

  public static ImageRegistry getImageRegistry()
  {
    if (image_registry == null)
    {
      image_registry = new ImageRegistry();
      image_registry.put(
        "folder",
        ImageDescriptor.createFromURL(newURL("file:/icons/sample.gif")));
      image_registry.put(
        "file",
        ImageDescriptor.createFromURL(newURL("file:/icons/sample.gif")));
    }
    return image_registry;
  }
}
