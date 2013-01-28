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
import java.net.URISyntaxException;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class GenerateRIMLocalizationFilesTest extends AbstractRIMMojoTest<GenerateRIMLocalizationFiles> {

	@Test
	public void testExecuteForBasicApplication() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-properties/pom.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		AbstractRIMBuildMojo mojo = setupMojo();
		

		setupProject(workProjectDirectory, getProject(mojo));
		
		setVariableValueToObject(mojo, "localizationDirectory", new File(workProjectDirectory + File.separator + "src"+File.separator+"main"+File.separator+"localized-properties"));
		setVariableValueToObject(mojo, "localizationPackage", getProject(mojo).getGroupId());
		setVariableValueToObject(mojo, "localizationOutputDirectory", new File(workProjectDirectory + File.separator + "target"+File.separator+"generated-sources"+File.separator+"localization-file"));
		setVariableValueToObject(mojo, "projectBuildDirectory", new File(workProjectDirectory + File.separator + "target"));
		
		getProject(mojo).setArtifactId("test-app-rapced");

		getProject(mojo).setPackaging("bbapp");

		MavenProject project = getProject(mojo);

		mojo.execute();

		File propertiesFile = new File(workProjectDirectory, "target/generated-sources/localization-file/org/apache/maven/plugin/test/messages.rrc");
		assertTrue("Localization File not found:" + propertiesFile.getAbsolutePath(), propertiesFile.exists());
		propertiesFile = new File(workProjectDirectory, "target/generated-sources/localization-file/org/apache/maven/plugin/test/messages.rrh");
		assertTrue("Localization File not found:" + propertiesFile.getAbsolutePath(), propertiesFile.exists());
		propertiesFile = new File(workProjectDirectory, "target/generated-sources/localization-file/org/apache/maven/plugin/test/messages_en.rrc");
		assertTrue("Localization File not found:" + propertiesFile.getAbsolutePath(), propertiesFile.exists());
		propertiesFile = new File(workProjectDirectory, "target/generated-sources/localization-file/org/apache/maven/plugin/test/messages_es.rrc");
		assertTrue("Localization File not found:" + propertiesFile.getAbsolutePath(), propertiesFile.exists());
	}
	

	@Override
	protected String getPluginConfigFileName() {
		return "com/goodformobile/build/mobile/execute-rapc-plugin-config.xml";
	}

	@Override
	protected String getGoal() {
		return "createRIMLocalizationFiles";
	}

}
