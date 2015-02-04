/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.ide.workspace.ui.viewer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;

import com.sap.dirigible.ide.repository.ui.viewer.ArtifactLabelProvider;

public class WorkspaceLabelProvider extends ArtifactLabelProvider {

	private static final long serialVersionUID = 6141865080631032831L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			return resource.getName();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof IProject) {
			return createImage(TYPE_PROJECT_ICON_URL);
		}
		if (element instanceof IFolder) {
			return getCollectionImageByName(((IFolder) element).getName());
		}
		if (element instanceof IFile) {
			return getResourceImage(((IFile) element).getName());
		}
		return null;
	}

//	private static String getExtension(String filename) {
//		if (filename == null) {
//			return ""; //$NON-NLS-1$
//		}
//		int dotIndex = filename.lastIndexOf("."); //$NON-NLS-1$
//		if (dotIndex != -1) {
//			return filename.substring(dotIndex + 1);
//		} else {
//			return ""; //$NON-NLS-1$
//		}
//	}

}
