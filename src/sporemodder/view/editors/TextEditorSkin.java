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
package sporemodder.view.editors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import javafx.scene.control.SkinBase;

class TextEditorSkin extends SkinBase<TextEditor> {
	
	private VirtualizedScrollPane<CodeArea> scrollPane;
	
	public TextEditorSkin(TextEditor control) {
		super(control);
		scrollPane = new VirtualizedScrollPane<>(getSkinnable().getCodeArea());
		scrollPane.setPrefWidth(Double.MAX_VALUE);
		scrollPane.setPrefHeight(Double.MAX_VALUE);
		
		getChildren().add(scrollPane);
	}
	
}
