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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Properties")
@XmlType(propOrder = { "generalProperties", "applicationProperties", "resourceProperties", "compileOptions", "packagingOptions", "hiddenProperties", "alternateEntryPoints" })
public class BlackBerryAppDescriptor {

	@XmlAttribute(name = "ModelVersion")
	public String modelVersion = "1.1.2";

	@XmlElement(name = "General")
	public GeneralProperties generalProperties;

	@XmlElement(name = "Application")
	public ApplicationProperties applicationProperties;

	@XmlElement(name = "Resources")
	public ResourcesProperties resourceProperties;

	@XmlElement(name = "Compile")
	public CompileOptions compileOptions;

	@XmlElement(name = "Packaging")
	public PackagingOptions packagingOptions;

	@XmlElement(name = "HiddenProperties")
	public HiddenProperties hiddenProperties;

	@XmlElement(name = "AlternateEntryPoints")
	public AlternateEntryPoints alternateEntryPoints;

	public BlackBerryAppDescriptor() {
		generalProperties = new GeneralProperties();
		applicationProperties = new ApplicationProperties();
		resourceProperties = new ResourcesProperties();
		compileOptions = new CompileOptions();
		packagingOptions = new PackagingOptions();
		hiddenProperties = new HiddenProperties();
		alternateEntryPoints = new AlternateEntryPoints();
	}

	public AlternateEntryPoints getAlternateEntryPoints() {
		return alternateEntryPoints;
	}
}
