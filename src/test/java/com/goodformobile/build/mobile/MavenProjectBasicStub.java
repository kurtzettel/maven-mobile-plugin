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

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Organization;
import org.apache.maven.project.MavenProject;

/**
 * Stub
 */
public class MavenProjectBasicStub extends MavenProject {
    protected String testRootDir;

    protected Properties properties;

    public MavenProjectBasicStub() throws Exception {
	super(new ModelStub());
	properties = new Properties();
    }

    public Set getArtifacts() {
	return new HashSet();
    }

    public String getName() {
	return "Test Project";
    }

    private File baseDir;

    public File getBasedir() {
	return baseDir;
    }

    public void setBaseDir(File baseDir) {
	this.baseDir = baseDir;
    }

    public String getGroupId() {
	return "org.apache.maven.plugin.test";
    }

    private String artifactId = "maven-blackberry-plugin-test";

    public String getArtifactId() {
	return artifactId;
    }

    @Override
    public void setArtifactId(String artifactId) {
	this.artifactId = artifactId;
    }

    public String getPackaging() {
	return "jar";
    }

    public void addProperty(String key, String value) {
	properties.put(key, value);
    }

    public Properties getProperties() {
	return properties;
    }

    public String getDescription() {
	return "Test Description";
    }

    public Organization getOrganization() {
	return new Organization() {
	    public String getName() {
		return "Test Name";
	    }
	};
    }

    private Build build = new Build();

    @Override
    public Build getBuild() {
	return build;
    }

    public Organization getOrganizationOrganization() {
	return new Organization() {
	    public String getName() {
		return "Test Name";
	    }
	};
    }

}
