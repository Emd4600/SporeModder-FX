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

import sporemodder.file.shaders.MaterialStateLink;
import sporemodder.view.UserInterface;

public class SmtTextEditor extends ArgScriptEditor<MaterialStateLink> {
	
	public SmtTextEditor() {
		super();
		
		MaterialStateLink unit = new MaterialStateLink();
		stream = unit.generateStream(false);
	}
	
	@Override protected void onStreamParse() {
		stream.getData().reset();
	}
	
	@Override protected void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Spore Material (.smt)", "smt", null);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}
}
