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

public class PackagingOptions {

	@XmlAttribute(name = "PreBuildStep")
	public String preBuildStep = "";

	@XmlAttribute(name = "PostBuildStep")
	public String postBuildStep = "";

	@XmlAttribute(name = "CleanStep")
	public String cleanStep = "";

	@XmlAttribute(name = "OutputFileName")
	public String outputFileName = "";

	@XmlAttribute(name = "OutputFolder")
	public String outputFolder = "";

	@XmlAttribute(name = "GenerateALXFile")
	public boolean generateALXFile = true;

	@XmlElement(name = "AlxFiles")
	public AlxFiles aLXFiles = new AlxFiles();

	public PackagingOptions() {

	}
}
