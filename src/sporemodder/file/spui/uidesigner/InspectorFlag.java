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
package sporemodder.file.spui.uidesigner;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import sporemodder.view.inspector.InspectorValue;

public class InspectorFlag extends CheckBox implements InspectorValue<Boolean> {

	@Override
	public Boolean getValue() {
		return isSelected();
	}

	@Override
	public void setValue(Boolean value) {
		setSelected(value);
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public void addValueListener(ChangeListener<Boolean> listener) {
		this.selectedProperty().addListener(listener);
	}

	@Override
	public void removeValueListener(ChangeListener<Boolean> listener) {
		this.selectedProperty().removeListener(listener);
	}

}
