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
package com.goodformobile.build.mobile;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

/**
 * Stub
 */
public class ModelStub extends Model {
	public ModelStub() {

	}

	String version = "0.0-TEST";

	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	public String getModelVersion() {
		return "0.0-TEST";
	}

	public String getName() {
		return "Test Model";
	}

	public String getGroupId() {
		return "org.apache.maven.test";
	}

	public String getPackaging() {
		return "jar";
	}

	public Parent getParent() {
		return new Parent();
	}

	public String getArtifactId() {
		return "maven-test-plugin";
	}

	public Properties getProperties() {
		return new Properties();
	}

	public List getPackages() {
		return new LinkedList();
	}

	public List getProfiles() {
		return new LinkedList();
	}

	public List getModules() {
		return new LinkedList();
	}
}
