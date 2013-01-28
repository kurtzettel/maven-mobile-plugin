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

public class ResourcesProperties {

	@XmlAttribute(name = "hasTitleResource")
	public boolean hasTitleResource;

	@XmlAttribute(name = "TitleResourceBundleName")
	public String titleResourceBundleName = "";

	@XmlAttribute(name = "TitleResourceBundleRelativePath")
	public String titleResourceRelativePath = "";

	@XmlAttribute(name = "TitleResourceBundleClassName")
	public String titleResourceBundleClassName = "";

	@XmlAttribute(name = "TitleResourceBundleKey")
	public String applicationTitleBundleKey = "";

	@XmlAttribute(name = "DescriptionId")
	public String applicationDescriptionBundleKey = "";

	@XmlElement(name = "Icons")
	public IconDescriptors iconDescriptors = new IconDescriptors();
}
