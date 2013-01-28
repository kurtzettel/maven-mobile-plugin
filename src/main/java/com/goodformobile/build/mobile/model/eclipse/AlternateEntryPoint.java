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
import javax.xml.bind.annotation.XmlElement;

public class AlternateEntryPoint {

	@XmlAttribute(name = "Title")
	public String title = "";

	@XmlAttribute(name = "MainMIDletName")
	public String mainMidletName = "";

	@XmlAttribute(name = "ArgumentsForMain")
	public String argumentsForMain = "";

	@XmlAttribute(name = "HomeScreenPosition")
	public int homeScreenPosition = 1;

	@XmlAttribute(name = "StartupTier")
	private int startupTier = 7; // Cannot change. RIM's law.

	@XmlAttribute(name = "IsSystemModule")
	public boolean systemModule = false;

	@XmlAttribute(name = "IsAutostartup")
	public boolean autoRunOnStartup = false;

	@XmlAttribute(name = "hasTitleResource")
	public boolean hasTitleResource = false;

	@XmlAttribute(name = "TitleResourceBundleName")
	public String titleResourceBundleName = "";

	@XmlAttribute(name = "TitleResourceBundleClassName")
	public String titleResourceBundleClassName = "";

	@XmlAttribute(name = "TitleResourceBundleKey")
	public String titleResourceBundleKey = "";

	@XmlAttribute(name = "TitleResourceBundleRelativePath")
	public String titleResourceBundleRelativePath = "";

	@XmlElement(name = "Icons")
	public final IconDescriptors iconDescriptors = new IconDescriptors();

}
