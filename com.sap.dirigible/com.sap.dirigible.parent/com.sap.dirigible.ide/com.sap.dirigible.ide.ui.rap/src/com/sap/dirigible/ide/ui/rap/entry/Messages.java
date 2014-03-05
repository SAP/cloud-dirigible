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

package com.sap.dirigible.ide.ui.rap.entry;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.sap.dirigible.ide.ui.rap.entry.messages"; //$NON-NLS-1$
	public static String DirigibleActionBarAdvisor_ABOUT;
	public static String DirigibleActionBarAdvisor_COULD_NOT_OPEN_WEB_PAGE;
	public static String DirigibleActionBarAdvisor_FILE;
	public static String DirigibleActionBarAdvisor_HELP;
	public static String DirigibleActionBarAdvisor_RUNNING_ON_RAP_VERSION;
	public static String DirigibleActionBarAdvisor_DIRIGIBLE_HOME;
	public static String DirigibleActionBarAdvisor_DIRIGIBLE_HELP;
	public static String DirigibleActionBarAdvisor_DIRIGIBLE_SAMPLES;
	public static String DirigibleActionBarAdvisor_DIRIGIBLE_FORUM;
	public static String DirigibleActionBarAdvisor_SHOW_PERSPECTIVE;
	public static String DirigibleActionBarAdvisor_SHOW_VIEW;
	public static String DirigibleActionBarAdvisor_WEB_PAGE_ERROR;
	public static String DirigibleActionBarAdvisor_WINDOW;
	public static String DirigibleActionBarAdvisor_WORKBENCH;
	public static String DirigibleWorkbench_ARE_YOU_SURE_YOU_WANT_TO_QUIT;
	public static String DirigibleWorkbenchWindowAdvisor_WORKBENCH;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
