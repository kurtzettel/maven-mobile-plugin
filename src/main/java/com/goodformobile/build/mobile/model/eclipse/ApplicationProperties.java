/**
 * Copyright (C) 2011 Kurt Zettel kurt@goodformobile.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goodformobile.build.mobile.model.eclipse;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.goodformobile.build.mobile.model.ApplicationType;

@XmlType(propOrder = { "applicationType", "mainMIDletName", "mainArguments", "homeScreenPosition", "startupTier", "systemModule",
		"autoRunOnStartup" })
public class ApplicationProperties {

	@XmlAttribute(name = "Type")
	public ApplicationType applicationType;

	@XmlAttribute(name = "MainMIDletName")
	public String mainMIDletName = "";

	@XmlAttribute(name = "MainArgs")
	public String mainArguments = "";

	@XmlAttribute(name = "HomeScreenPosition")
	public int homeScreenPosition = 0;

	@XmlAttribute(name = "StartupTier")
	private int startupTier = 7; // Cannot change. RIM's law.

	@XmlAttribute(name = "IsSystemModule")
	public boolean systemModule = false;

	@XmlAttribute(name = "IsAutostartup")
	public boolean autoRunOnStartup = false;
}
