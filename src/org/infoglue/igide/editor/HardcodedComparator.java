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

package org.infoglue.igide.editor;

import java.util.Comparator;

public class HardcodedComparator implements Comparator
{

    public HardcodedComparator(String namesInOrderString)
    {
        this.namesInOrderString = namesInOrderString;
    }

    public int compare(Object o1, Object o2)
    {
        Comparable valueOne = (String)o1;
        Comparable valueTwo = (String)o2;
        return !after(valueOne, valueTwo) ? -1 : 1;
    }

    private boolean after(Comparable valueOne, Comparable valueTwo)
    {
        int index1 = namesInOrderString.indexOf(valueOne.toString());
        int index2 = namesInOrderString.indexOf(valueTwo.toString());
        if(index1 != -1 && index2 != -1)
            return index1 > index2;
        if(index1 == -1 && index2 != -1)
            return true;
        if(index2 == -1 && index1 != -1)
            return false;
        int result = valueOne.compareTo(valueTwo);
        return result > 0;
    }

    private String namesInOrderString;
}
