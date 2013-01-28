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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;
import org.junit.Test;

public class RIMPackageMojoTest extends AbstractRIMMojoTest<RIMPackageMojo> {

	@Test
	public void testExecuteOnBasicApplication() throws Exception {

		File projectDirectory = getRelativeFile("projects/maven-test-app-preverified/pom.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMPackageMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		mojo.execute();

		// Ensure a jad is generated.
		File targetDirectory = new File(workProjectDirectory, "target");
		File expectedJad = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "maven_test_app.jad");
		assertTrue("Unable to find generated jad: " + expectedJad.getAbsolutePath(), expectedJad.exists());

		// Ensure a cod file is generated.
		File expectedCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "maven_test_app.cod");
		assertTrue("Unable to find generated cod: " + expectedCod.getAbsolutePath(), expectedCod.exists());
	}

	@Test
	public void testExecuteOnBasicLibrary() throws Exception {

		File projectDirectory = getRelativeFile("projects/library-project-1-preverified/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMPackageMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		project.setArtifactId("library-project-1");

		MavenProjectBasicStub projectStub = (MavenProjectBasicStub) project;
		projectStub.setBaseDir(workProjectDirectory);

		setupRIMStyleProject(workProjectDirectory, project);

		setVariableValueToObject(mojo, "systemModule", true);
		setVariableValueToObject(mojo, "bundleCodsInOutputJar", true);

		mojo.execute();

		// Ensure a jad is generated.
		File targetDirectory = new File(workProjectDirectory, "target");
		File expectedJad = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "library_project_1.jad");
		assertTrue("Unable to find generated jad: " + expectedJad.getAbsolutePath(), expectedJad.exists());

		Properties result = PropertyUtils.loadProperties(expectedJad);
		assertNotNull(result);
		assertEquals("library-project-1", result.get("MIDlet-Name"));
		assertEquals("1.0.0", result.get("MIDlet-Version"));
		assertEquals("MIDP-2.0", result.get("MicroEdition-Profile"));
		assertEquals("CLDC-1.1", result.get("MicroEdition-Configuration"));
		assertEquals("BlackBerry Developer", result.get("MIDlet-Vendor"));
		assertEquals("2", result.get("RIM-Library-Flags"));
		assertEquals("library-project-1.jar", result.get("MIDlet-Jar-URL"));
		assertEquals("library_project_1.cod", result.get("RIM-COD-URL"));

		// Ensure a cod file is generated.
		File expectedCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "library_project_1.cod");
		assertTrue("Unable to find generated cod: " + expectedCod.getAbsolutePath(), expectedCod.exists());

		// Ensure a cod file is copied to target
		File expectedTargetCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "classes" + File.separator + "library_project_1.cod");
		assertTrue("Unable to find generated cod: " + expectedTargetCod.getAbsolutePath(), expectedTargetCod.exists());
	}

	@Test
	public void testExecuteOnBasicLibraryExcludeCods() throws Exception {

		File projectDirectory = getRelativeFile("projects/library-project-1-preverified/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMPackageMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		project.setArtifactId("library-project-1");

		MavenProjectBasicStub projectStub = (MavenProjectBasicStub) project;
		projectStub.setBaseDir(workProjectDirectory);

		setupRIMStyleProject(workProjectDirectory, project);

		setVariableValueToObject(mojo, "systemModule", true);
		setVariableValueToObject(mojo, "bundleCodsInOutputJar", false);

		mojo.execute();

		// Ensure a jad is generated.
		File targetDirectory = new File(workProjectDirectory, "target");
		File expectedJad = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "library_project_1.jad");
		assertTrue("Unable to find generated jad: " + expectedJad.getAbsolutePath(), expectedJad.exists());

		Properties result = PropertyUtils.loadProperties(expectedJad);
		assertNotNull(result);
		assertEquals("library-project-1", result.get("MIDlet-Name"));
		assertEquals("1.0.0", result.get("MIDlet-Version"));
		assertEquals("MIDP-2.0", result.get("MicroEdition-Profile"));
		assertEquals("CLDC-1.1", result.get("MicroEdition-Configuration"));
		assertEquals("BlackBerry Developer", result.get("MIDlet-Vendor"));
		assertEquals("2", result.get("RIM-Library-Flags"));
		assertEquals("library-project-1.jar", result.get("MIDlet-Jar-URL"));
		assertEquals("library_project_1.cod", result.get("RIM-COD-URL"));

		// Ensure a cod file is generated.
		File expectedCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "library_project_1.cod");
		assertTrue("Unable to find generated cod: " + expectedCod.getAbsolutePath(), expectedCod.exists());

		// Ensure a cod file is not copied to target
		File expectedTargetCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "classes" + File.separator + "library_project_1.cod");
		assertFalse("Found unexpected cod in target directory: " + expectedTargetCod.getAbsolutePath(), expectedTargetCod.exists());

	}
	
	@Test
	public void testExecuteOnBasicApplicationShouldFailWithMissingDependency() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-dependency-preverified/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMPackageMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);
		project.setArtifactId("app-with-dependency");

		MavenProjectBasicStub projectStub = (MavenProjectBasicStub) project;
		projectStub.setBaseDir(workProjectDirectory);		
		
		
		try {
			mojo.execute();
			fail("Expected Exception when calling with a missing dependency.");
		} catch (MojoExecutionException e) {
			//Expected an exception.
		}
	}
	
	@Test
	public void testExecuteOnBasicApplicationWithSingleDependency() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-dependency-preverified/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMPackageMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);
		project.setArtifactId("app-with-dependency");

		MavenProjectBasicStub projectStub = (MavenProjectBasicStub) project;
		projectStub.setBaseDir(workProjectDirectory);		
		
		List<String> classPathElements = new ArrayList<String>();
		classPathElements.add(getRelativeFile("library_project_1.jar").getAbsolutePath());
		setVariableValueToObject(mojo, "classpathElements", classPathElements);

		mojo.execute();

		// Ensure a jad is generated.
		File targetDirectory = new File(workProjectDirectory, "target");
		File expectedJad = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "app_with_dependency.jad");
		assertTrue("Unable to find generated jad: " + expectedJad.getAbsolutePath(), expectedJad.exists());

		// Ensure a cod file is generated.
		File expectedCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "app_with_dependency.cod");
		assertTrue("Unable to find generated cod: " + expectedCod.getAbsolutePath(), expectedCod.exists());
	}	

	protected void setupRIMStyleProject(File workProjectDirectory, MavenProject project) {
		File buildDirectory = new File(workProjectDirectory, "target");
		project.getBuild().setDirectory(buildDirectory.getAbsolutePath());
		project.getBuild().setOutputDirectory(new File(new File(workProjectDirectory, "target"), "classes").getAbsolutePath());

		File source = new File(workProjectDirectory, "src");
		File resources = new File(workProjectDirectory, "res");
		// Setup the source directories.
		project.getCompileSourceRoots().add(source.getAbsolutePath());
		project.getCompileSourceRoots().add(resources.getAbsolutePath());
	}

	@Test
	public void testExecuteOnProguardPreverifiedClasses() throws Exception {

		File projectDirectory = getRelativeFile("projects/maven-test-app-preverified-with-proguard/pom.xml").getParentFile();

		File workProjectDirectory = new File(getBasedir(), "target/temp/project");
		if (workProjectDirectory.exists()) {
			FileUtils.cleanDirectory(workProjectDirectory);
		}
		FileUtils.copyDirectory(projectDirectory, workProjectDirectory);

		RIMPackageMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		mojo.execute();

		// Ensure a jad is generated.
		File targetDirectory = new File(workProjectDirectory, "target");
		File expectedJad = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "maven_test_app.jad");
		assertTrue("Unable to find generated jad: " + expectedJad.getAbsolutePath(), expectedJad.exists());

		// Ensure a cod file is generated.
		File expectedCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "maven_test_app.cod");
		assertTrue("Unable to find generated cod: " + expectedCod.getAbsolutePath(), expectedCod.exists());

	}
	
	@Test
	public void testResourcesMustBeInResultingCod() throws Exception {

		File projectDirectory = getRelativeFile("projects/test-app-with-large-resource/pom.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMPackageMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);
		project.setArtifactId("test-app-with-large-resource");

		mojo.execute();

		File targetDirectory = new File(workProjectDirectory, "target");

		// Ensure a cod file is generated.
		File expectedCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "deliverables" + File.separator + "test_app_with_large_resource.cod");
		assertTrue("Unable to find generated cod: " + expectedCod.getAbsolutePath(), expectedCod.exists());
		
		// Ensure the resulting cod is over 500K
		assertTrue("Expected cod should be over 500k but is " + expectedCod.length() + " bytes.", expectedCod.length() > 500000);
	}	

	@Override
	protected String getPluginConfigFileName() {
		return "com/goodformobile/build/mobile/execute-rapc-plugin-config.xml";
	}

	@Override
	protected String getGoal() {
		return "rapc";
	}
}
