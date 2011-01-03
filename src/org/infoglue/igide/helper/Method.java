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
 * Created on 2005-apr-21
 *
 */
package org.infoglue.igide.helper;

/**
 * @author Stefan Sik
 * 
 */
public class Method
{
    private String name;
    private String returnType;
    private String args;
    
    public Method(String name, String returnType, String args)
    {
        this.name = name;
        this.returnType = returnType;
        this.args = args;
    }
    
    public int getArgCount()
    {
        return args.split(",").length;
    }
    
    public String toString()
    {
	    return name + "(" + simplifyArgs(args) + ") - " + removePackage(returnType);
    }
    
    private String removePackage(String s)
    {
        if(s.indexOf(".") > -1)
            s = s.substring(s.lastIndexOf(".") +1);
        return s;
    }
    
    private String simplifyArgs(String args)
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
    
    
    public String getArgs()
    {
        return args;
    }
    public void setArgs(String args)
    {
        this.args = args;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getReturnType()
    {
        return returnType;
    }
    public void setReturnType(String returnType)
    {
        this.returnType = returnType;
    }
}
