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
package sporemodder.view.syntax;

import sporemodder.view.editors.TextEditor;

@FunctionalInterface
public interface SyntaxFormat {
	/**
	 * Generates the style spans (that is, what css styles must be applied to certain parts of text) so that the RichText CodeArea can use it.
	 * @param text The piece of text where syntax highlighting must be computed.
	 */
	public void generateStyle(String text, SyntaxHighlighter syntax);
	
	default public boolean toggleBlockComment(TextEditor editor, int start, int end) {
		return false;
	}
	
	default public boolean toggleLineComment(TextEditor editor, int position) {
		return false;
	}
}
