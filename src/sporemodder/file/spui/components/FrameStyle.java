/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.spui.components;

import javafx.scene.paint.Color;
import sporemodder.file.spui.SpuiElement;

public class FrameStyle extends SpuiElement {
	
	public static final int BORDER_DEFAULT= 0;
	public static final int BORDER_SOLID = 2;
	public static final int BORDER_DOTTED = 3;
	public static final int BORDER_DASHED = 4;
	public static final int BORDER_INSET = 5;
	public static final int BORDER_OUTSET = 6;
	public static final int BORDER_GROOVE = 7;
	public static final int BORDER_RIDGE = 8;
	// same as inset and outset but width a black line
	public static final int BORDER_OUTSET_LINE = 9;
	public static final int BORDER_INSET_LINE = 10;
	
	public int borderType = BORDER_DEFAULT;
	public Color color = Color.WHITE;
}
