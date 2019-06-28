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

import sporemodder.file.anim.SPAnimation;
import sporemodder.view.UserInterface;

public class AnimTextEditor extends ArgScriptEditor<SPAnimation> {
	
	public AnimTextEditor() {
		super();
		
		SPAnimation unit = new SPAnimation();
		stream = unit.generateStream();
	}
	
	@Override protected void onStreamParse() {
		stream.getData().clear();
	}
	
	@Override protected void showInspector(boolean show) {
		if (show) {
			UserInterface.get().getInspectorPane().configureDefault("Animation", "anim", null);
		} else {
			UserInterface.get().getInspectorPane().reset();
		}
	}
	
	@Override public void setActive(boolean isActive) {
		super.setActive(isActive);
		showInspector(isActive);
	}
}
