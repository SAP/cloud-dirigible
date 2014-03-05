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

package com.sap.dirigible.ide.bridge.ext;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

	public XSSRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getParameter(String name) {
		String parameter = super.getParameter(name);
		return stripXSS(parameter);
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(name);
		return stripXSS(values);
	}

	@Override
	public String getQueryString() {
		String query = super.getQueryString();
		return stripXSS(query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> parameterMap = super.getParameterMap();
		return stripXSS(parameterMap);
	}

	@Override
	public String getHeader(String name) {
		String header = super.getHeader(name);
		return stripXSS(header);
	}

	private String stripXSS(String value) {
		if (value != null) {
			value = StringEscapeUtils.escapeHtml(value);
			value = StringEscapeUtils.escapeJavaScript(value);
			value = value.replaceAll("", ""); //$NON-NLS-1$ //$NON-NLS-2$
			value = Jsoup.clean(value, Whitelist.none());
		}
		return value;
	}

	private String[] stripXSS(String[] values) {
		String encodedValues[] = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			encodedValues[i] = stripXSS(values[i]);
		}
		return encodedValues;
	}

	private Map<String, String[]> stripXSS(Map<String, String[]> parameterMap) {
		Map<String, String[]> encodedMap = new TreeMap<String, String[]>();
		for (Entry<String, String[]> next : parameterMap.entrySet()) {
			String key = next.getKey();
			String[] values = next.getValue();
			encodedMap.put(stripXSS(key), stripXSS(values));
		}
		return encodedMap;
	}
}
