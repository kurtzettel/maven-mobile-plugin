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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

import com.goodformobile.build.mobile.model.Jad;
import com.goodformobile.build.mobile.model.JadProperty;

public class RIMAppPackagingMojoTest extends AbstractRIMMojoTest<RIMAppPackagingMojo> {

	@Test
	public void testExecuteOnBasicApplication() throws Exception {

		AbstractRIMBuildMojo mojo = setupTestAppRapcedProject();

		MavenProject project = getProject(mojo);
		String classesDirectory = project.getBuild().getOutputDirectory();
		mojo.execute();

		assertFalse(classesDirectory.equals(project.getBuild().getOutputDirectory()));
		assertTrue(project.getBuild().getOutputDirectory().contains("deliverables"));
	}

	@Test
	public void testLibraryCodsShouldBeExtracted() throws Exception {

		AbstractRIMBuildMojo mojo = setupTestAppRapcedProject();

		List<String> classPathElements = new ArrayList<String>();
		classPathElements.add(getRelativeFile("library_project_1.jar").getAbsolutePath());
		setVariableValueToObject(mojo, "classpathElements", classPathElements);

		mojo.execute();

		// Assert the deliverables directory includes library_project_1.cod.
		MavenProject project = getProject(mojo);
		File outputLibraryCod = new File(project.getBuild().getOutputDirectory(), "library_project_1.cod");
		assertTrue("Expected library cod not found:" + outputLibraryCod.getAbsolutePath(), outputLibraryCod.exists());
	}

	@Test
	public void testJadShouldIncludeExtractedCodsFromLibraries() throws Exception {
		AbstractRIMBuildMojo mojo = setupTestAppRapcedProject();

		List<String> classPathElements = new ArrayList<String>();
		classPathElements.add(getRelativeFile("library_project_1.jar").getAbsolutePath());
		setVariableValueToObject(mojo, "classpathElements", classPathElements);

		mojo.execute();

		// Assert the final jad includes library_project_1.cod.
		MavenProject project = getProject(mojo);
		File outputJad = new File(project.getBuild().getOutputDirectory(), "test_app_rapced.jad");
		Jad jad = new Jad();
		FileInputStream is = new FileInputStream(outputJad);
		jad.parseFile(is);
		is.close();

		boolean libraryCodInJad = false;
		for (JadProperty property : jad.getPropertyList()) {
			if (property.getName().startsWith("RIM-COD-URL")) {
				if (property.getValue().equals("library_project_1.cod")) {
					libraryCodInJad = true;
				}
			}
		}
		assertTrue("Expected library_project_1.cod to be listed in jad.", libraryCodInJad);
	}

	@Test
	public void testJadShouldIncludeInflatedCods() throws Exception {

		File projectDirectory = getRelativeFile("projects/test-large-app-rapced/pom.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		AbstractRIMBuildMojo mojo = setupMojo();

		setupProject(workProjectDirectory, getProject(mojo));
		getProject(mojo).setArtifactId("test-app-rapced");

		getProject(mojo).setPackaging("bbapp");
		
		mojo.execute();

		// Assert the deliverables directory includes test-large-app-rapced.cod.
		MavenProject project = getProject(mojo);
		File outputCod = new File(project.getBuild().getOutputDirectory(), "test_app_rapced.cod");
		assertTrue("Expected cod not found:" + outputCod.getAbsolutePath(), outputCod.exists());		
		outputCod = new File(project.getBuild().getOutputDirectory(), "test_app_rapced-1.cod");
		assertTrue("Expected cod not found:" + outputCod.getAbsolutePath(), outputCod.exists());		
	}

	@Override
	protected String getPluginConfigFileName() {
		return "com/goodformobile/build/mobile/execute-rapc-plugin-config.xml";
	}

	private AbstractRIMBuildMojo setupTestAppRapcedProject() throws URISyntaxException, IOException, Exception, IllegalAccessException {
		File projectDirectory = getRelativeFile("projects/test-app-rapced/pom.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		AbstractRIMBuildMojo mojo = setupMojo();

		setupProject(workProjectDirectory, getProject(mojo));
		getProject(mojo).setArtifactId("test-app-rapced");

		getProject(mojo).setPackaging("bbapp");
		return mojo;
	}

	@Override
	protected String getGoal() {
		return "setup-app-deliverables";
	}
}
