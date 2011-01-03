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
 * Created on 2005-dec-11
 *
 */
package org.infoglue.igide.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * 
 * @author Stefan Sik
 *
 */

public abstract class StreamHelper
{
    private static final int DEFAULT_CHUNKSIZE = 4096;

	public static void copyStream(InputStream in, OutputStream out, int chunkSize) throws IOException
    {
          byte b[] = new byte[chunkSize];
          int inumber = 0;
          int cnt = 0;
          while(true)
          {
              inumber = in.read(b);
              cnt += inumber;
              if( inumber <= 0)
                  break;
              out.write(b, 0, inumber);
              out.flush();
          }
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException
    {
    	copyStream(in, out,DEFAULT_CHUNKSIZE);
    }
    
    public static void copyReader(Reader in, Writer out) throws IOException
    {
        char[] b = new char[DEFAULT_CHUNKSIZE];
        int inumber = 0;
        int cnt = 0;
        while(true)
        {
            inumber = in.read(b);
            cnt += inumber;
            if( inumber <= 0)
                break;
            out.write(b, 0, inumber);
            out.flush();
        }
    }
}
