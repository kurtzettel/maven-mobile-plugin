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
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;
import org.junit.Test;

import com.goodformobile.build.mobile.GenerateRAPCMojo;

public class GenerateRAPCMojoTest extends AbstractRIMMojoTest<GenerateRAPCMojo> {

	private String pluginConfigFileName = null;

	@Test
	public void testExecuteForBasicApplication() throws Exception {

		setPluginConfigFileName("com/goodformobile/build/mobile/generate-rapc-plugin-config.xml");
		AbstractRIMBuildMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		File buildDirectory = new File(getBasedir(), "target/test/unit/generate-rapc-mojo-basic-test/target");
		setupBuildDirectory(buildDirectory);
		project.getBuild().setDirectory(buildDirectory.getAbsolutePath());

		mojo.execute();

		File propertiesFile = new File(getBasedir(), "target/test/unit/generate-rapc-mojo-basic-test/target/rapc/maven_blackberry_plugin_test.rapc");
		assertTrue("Properties File not found.", propertiesFile.exists());
		Properties result = PropertyUtils.loadProperties(propertiesFile);
		assertNotNull(result);
		assertEquals(project.getName(), result.get("MIDlet-Name"));
		assertEquals(project.getVersion().replace("-", "."), result.get("MIDlet-Version"));
		assertEquals("MIDP-2.0", result.get("MicroEdition-Profile"));
		assertEquals("CLDC-1.1", result.get("MicroEdition-Configuration"));
		assertEquals("Test Description", result.get("MIDlet-Description"));
		assertEquals("Good For Mobile", result.get("MIDlet-Vendor"));
		assertEquals(project.getName() + ",img/icon.png,", result.get("MIDlet-1"));
		assertEquals("0", result.get("RIM-MIDlet-Flags-1"));
		assertEquals(project.getArtifactId() + ".jar", result.get("MIDlet-Jar-URL"));
	}

	@Test
	public void testJadVersionShouldNotContainText() throws Exception {

		setPluginConfigFileName("com/goodformobile/build/mobile/generate-rapc-plugin-config.xml");
		AbstractRIMBuildMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		File buildDirectory = new File(getBasedir(), "target/test/unit/generate-rapc-mojo-basic-test/target");
		setupBuildDirectory(buildDirectory);
		project.getBuild().setDirectory(buildDirectory.getAbsolutePath());
		project.setVersion("1.0-SNAPSHOT");

		mojo.execute();

		File propertiesFile = new File(getBasedir(), "target/test/unit/generate-rapc-mojo-basic-test/target/rapc/maven_blackberry_plugin_test.rapc");
		assertTrue("Properties File not found.", propertiesFile.exists());
		Properties result = PropertyUtils.loadProperties(propertiesFile);
		assertNotNull(result);
		String midletVersion = (String) result.get("MIDlet-Version");
		
		assertFalse("Midlet version should not contain the word SNAPSHOT but is: " + midletVersion, midletVersion.contains("SNAPSHOT"));
	}	
	
	@Test
	public void testExecuteForLibraryWithoutDependencies() throws Exception {

		setPluginConfigFileName("com/goodformobile/build/mobile/generate-rapc-for-library-plugin-config.xml");
		AbstractRIMBuildMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);
		project.setArtifactId("library-project-1");
		project.setVersion("1.0.0");

		File buildDirectory = new File(getBasedir(), "target/test/unit/generate-rapc-for-library-mojo-basic-test/target");
		setupBuildDirectory(buildDirectory);

		project.getBuild().setDirectory(buildDirectory.getAbsolutePath());

		mojo.execute();

		File propertiesFile = new File(getBasedir(), "target/test/unit/generate-rapc-for-library-mojo-basic-test/target/rapc/library_project_1.rapc");
		assertTrue("Properties File not found.", propertiesFile.exists());
		Properties result = PropertyUtils.loadProperties(propertiesFile);
		assertNotNull(result);
		assertEquals(project.getName(), result.get("MIDlet-Name"));
		assertEquals("1.0.0", result.get("MIDlet-Version"));
		assertEquals("MIDP-2.0", result.get("MicroEdition-Profile"));
		assertEquals("CLDC-1.1", result.get("MicroEdition-Configuration"));
		assertEquals("BlackBerry Developer", result.get("MIDlet-Vendor"));
		assertEquals("2", result.get("RIM-Library-Flags"));
		assertEquals("library-project-1.jar", result.get("MIDlet-Jar-URL"));
	}

	private void setupBuildDirectory(File buildDirectory) throws IOException {
		if (!buildDirectory.exists()) {
			FileUtils.forceMkdir(buildDirectory);
		}
		FileUtils.cleanDirectory(buildDirectory);
	}

	@Test
	public void testPackagingFileNameCanBeOverridden() throws Exception {

		setPluginConfigFileName("com/goodformobile/build/mobile/generate-rapc-plugin-config.xml");
		AbstractRIMBuildMojo mojo = setupMojo();

		MavenProject project = (MavenProject) getVariableValueFromObject(mojo, "project");
		assertNotNull(project);

		project.setArtifactId("myartifact");
		assertEquals("myartifact", mojo.getPackagingFileName());

		setVariableValueToObject(mojo, "packagingFileName", "special_name");
		assertEquals("special_name", mojo.getPackagingFileName());
	}

	@Test
	public void testPackagingFileNameDoesNotHaveDashes() throws Exception {

		setPluginConfigFileName("com/goodformobile/build/mobile/generate-rapc-plugin-config.xml");
		AbstractRIMBuildMojo mojo = setupMojo();

		MavenProject project = (MavenProject) getVariableValueFromObject(mojo, "project");
		assertNotNull(project);

		project.setArtifactId("my-artifact");
		assertEquals("my_artifact", mojo.getPackagingFileName());

		setVariableValueToObject(mojo, "packagingFileName", "special-name");
		try {
			mojo.getPackagingFileName();
			fail("Packaging File names should throw an Exception when name contains dashes.");
		} catch (IllegalArgumentException e) {
			// Expected.
		}
	}

	@Test
	public void testPackagingFileNameDoesNotHaveSpaces() throws Exception {

		setPluginConfigFileName("com/goodformobile/build/mobile/generate-rapc-plugin-config.xml");
		AbstractRIMBuildMojo mojo = setupMojo();

		MavenProject project = (MavenProject) getVariableValueFromObject(mojo, "project");
		assertNotNull(project);

		project.setArtifactId("my artifact");
		assertEquals("my_artifact", mojo.getPackagingFileName());

		setVariableValueToObject(mojo, "packagingFileName", "special name");
		try {
			mojo.getPackagingFileName();
			fail("Packaging File names should throw an Exception when name contains spaces.");
		} catch (IllegalArgumentException e) {
			// Expected.
		}
	}
	

	@Test
	public void testIconIsInferredFromIconPNG() throws Exception {

		setPluginConfigFileName("com/goodformobile/build/mobile/generate-rapc-infer-icon-plugin-config.xml");
		AbstractRIMBuildMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);
		project.getBuild().setOutputDirectory(getBasedir() + File.separator + "src/test/resources/projects/app-with-dependency/res");

		File buildDirectory = new File(getBasedir(), "target/test/unit/generate-rapc-mojo-basic-test/target");
		setupBuildDirectory(buildDirectory);
		project.getBuild().setDirectory(buildDirectory.getAbsolutePath());

		mojo.execute();

		File propertiesFile = new File(getBasedir(), "target/test/unit/generate-rapc-mojo-basic-test/target/rapc/maven_blackberry_plugin_test.rapc");
		assertTrue("Properties File not found.", propertiesFile.exists());
		Properties result = PropertyUtils.loadProperties(propertiesFile);
		assertNotNull(result);
		assertEquals(project.getName() + ",img/icon.png,", result.get("MIDlet-1"));
	}	

	@Override
	protected String getPluginConfigFileName() {
		return pluginConfigFileName;
	}

	@Override
	protected String getGoal() {
		return "generate-rapc";
	}

	public void setPluginConfigFileName(String pluginConfigFileName) {
		this.pluginConfigFileName = pluginConfigFileName;
	}
}
