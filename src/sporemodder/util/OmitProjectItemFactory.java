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
package sporemodder.util;

import java.io.File;

import javafx.scene.control.TreeItem;

/**
 * A ProjectItemFactory for all those items that must not be shown, such as config.properties.
 *
 */
public class OmitProjectItemFactory implements ProjectItemFactory {

	@Override
	public boolean isSupported(File file, Project project, TreeItem<ProjectItem> parent) {
		String name = file.getName();
		return name.equals("config.properties");
	}

	@Override
	public ProjectItem create(File file, Project project, TreeItem<ProjectItem> parent) {
		return null;
	}

}
