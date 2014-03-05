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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import com.sap.dirigible.ide.repository.RepositoryFacade;
import com.sap.dirigible.ide.repository.ui.viewer.ArtifactPropertySource;
import com.sap.dirigible.repository.api.IEntity;

public class WorkspacePropertiesAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IResource) {
			final IResource resource = (IResource) adaptableObject;
			if (adapterType.equals(IPropertySource.class)) {
				IEntity entity = RepositoryFacade.getInstance().getRepository()
						.getResource(resource.getRawLocation().toString());
				return new ArtifactPropertySource(entity);
			}
		}
		return null;
	}

	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IPropertySource.class };
	}

}
