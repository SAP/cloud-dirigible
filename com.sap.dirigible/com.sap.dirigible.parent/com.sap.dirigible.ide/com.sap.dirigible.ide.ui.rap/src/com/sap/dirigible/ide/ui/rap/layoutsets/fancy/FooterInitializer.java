/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package com.sap.dirigible.ide.ui.rap.layoutsets.fancy;

import org.eclipse.rap.ui.interactiondesign.layout.model.ILayoutSetInitializer;
import org.eclipse.rap.ui.interactiondesign.layout.model.LayoutSet;

import com.sap.dirigible.ide.ui.rap.shared.LayoutSetConstants;

public class FooterInitializer implements ILayoutSetInitializer {

	public void initializeLayoutSet(final LayoutSet layoutSet) {
		String path = LayoutSetConstants.IMAGE_PATH_FANCY;
		layoutSet.addImagePath(LayoutSetConstants.FOOTER_LEFT, path
				+ "footer_left.png"); //$NON-NLS-1$
		layoutSet.addImagePath(LayoutSetConstants.FOOTER_BG, path
				+ "footer_bg.png"); //$NON-NLS-1$
		layoutSet.addImagePath(LayoutSetConstants.FOOTER_RIGHT, path
				+ "footer_right.png"); //$NON-NLS-1$
	}
}