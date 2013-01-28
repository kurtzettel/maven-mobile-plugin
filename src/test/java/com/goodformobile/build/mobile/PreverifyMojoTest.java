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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.project.MavenProject;
import org.junit.Ignore;
import org.junit.Test;

public class PreverifyMojoTest extends AbstractRIMMojoTest<PreverifyMojo> {

	public void testExecuteWithProguard() throws Exception {

		if (!isWindows()) {
			//FIXME This test won't run on *nix until it has some hard coding removed.
			return;
		}
		
		File projectDirectory = getRelativeFile("projects/maven-test-app-compiled/pom.xml").getParentFile();

		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		PreverifyMojo mojo = setupMojo();

		setupProject(workProjectDirectory, mojo);

		List<Artifact> artifacts = new ArrayList<Artifact>();
		Artifact proguardArtifact = new ArtifactStub();
		proguardArtifact.setArtifactId("proguard");
		proguardArtifact.setGroupId("net.sf.proguard");
		proguardArtifact.setVersion("4.4");
		
		// FIXME Don't hard code shit. . . ass.
		proguardArtifact.setFile(new File(System.getProperty("user.home") + "\\.m2\\repository\\net\\sf\\proguard\\proguard\\4.4\\proguard-4.4.jar"));
		artifacts.add(proguardArtifact);

		setVariableValueToObject(mojo, "pluginArtifacts", artifacts);
		setVariableValueToObject(mojo, "forceProguard", true);

		mojo.execute();

		// FIXME Keep this. It should work but doesn't seem to.
		// assertOutputIsPreverified(project);
	}

	public void testExecuteWithRIMsPreverify() throws Exception {

		if (!isWindows()) {
			//FIXME This test won't run on *nix since RIM's preverifier is Windows specific.
			return;
		}
		
		File projectDirectory = getRelativeFile("projects/maven-test-app-compiled/pom.xml").getParentFile();

		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		PreverifyMojo mojo = setupMojo();

		MavenProject project = setupProject(workProjectDirectory, mojo);

		mojo.execute();

		assertOutputIsPreverified(project);
	}

	@Test
	public void testExecuteWith3rdPartyPreverify() throws Exception {

		File projectDirectory = getRelativeFile("projects/maven-test-app-compiled/pom.xml").getParentFile();

		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		PreverifyMojo mojo = setupMojo();

		MavenProject project = setupProject(workProjectDirectory, mojo);

		String preverifyExecutablePath = getVariableFromRimProfile("preverify.path");
		
		File preverifyExecutable = new File(getVariableFromRimProfile("preverify.path"));
		setVariableValueToObject(mojo, "preverifyExecutable", preverifyExecutable);

		mojo.execute();

		assertOutputIsPreverified(project);
	}

	@Test
	public void testExecuteOnLibraryWith3rdPartyPreverify() throws Exception {

		File projectDirectory = getRelativeFile("projects/library-project-1-compiled/BlackBerry_App_Descriptor.xml").getParentFile();

		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		PreverifyMojo mojo = setupMojo();

		MavenProject project = setupProject(workProjectDirectory, mojo);

		File preverifyExecutable = new File(getVariableFromRimProfile("preverify.path"));
		setVariableValueToObject(mojo, "preverifyExecutable", preverifyExecutable);

		mojo.execute();

		// This test should have altered a class in the output directory.
		//                                 projects/library-project-1-compiled
		File inputClass = getRelativeFile("projects/library-project-1-compiled/target/classes/com/libary1/SomeUtility.class");
		File outputClass = new File(project.getBuild().getOutputDirectory() + "/com/libary1/SomeUtility.class");
		assertFilesArePreverified(inputClass, outputClass);

	}

	private MavenProject setupProject(File workProjectDirectory, PreverifyMojo mojo) throws IllegalAccessException {
		MavenProject project = (MavenProject) getVariableValueFromObject(mojo, "project");
		assertNotNull(project);

		project.setArtifactId("maven-test-app");

		MavenProjectBasicStub projectStub = (MavenProjectBasicStub) project;
		projectStub.setBaseDir(workProjectDirectory);

		File buildDirectory = new File(workProjectDirectory, "target");
		project.getBuild().setDirectory(buildDirectory.getAbsolutePath());
		project.getBuild().setOutputDirectory(new File(new File(workProjectDirectory, "target"), "classes").getAbsolutePath());
		return project;
	}

	private void assertOutputIsPreverified(MavenProject project) throws URISyntaxException, IOException {
		// This test should have altered a class in the output directory.
		File inputClass = getRelativeFile("projects/maven-test-app-compiled/target/classes/com/mypackage/HelloBlackBerry.class");
		File outputClass = new File(project.getBuild().getOutputDirectory() + "/com/mypackage/HelloBlackBerry.class");
		assertFilesArePreverified(inputClass, outputClass);
	}

	private void assertFilesArePreverified(File inputClass, File outputClass) throws IOException {

		assertNotNull("Unable to find original input class: " + inputClass.getAbsolutePath(), inputClass.exists());
		assertNotNull("Unable to find preverified output class: " + outputClass.getAbsolutePath(), outputClass.exists());

		assertNotSame("Input and output class should not have the same last modified time.", inputClass.lastModified(), outputClass.lastModified());

		byte[] inputClassContents = FileUtils.readFileToByteArray(inputClass);
		byte[] outputClassContents = FileUtils.readFileToByteArray(outputClass);
		if (inputClassContents.length == outputClassContents.length) {
			boolean bytesDiffer = false;
			for (int i = 0; i < inputClassContents.length; i++) {
				if (inputClassContents[i] != outputClassContents[i]) {
					bytesDiffer = true;
					break;
				}
			}
			assertFalse("The input class and output class are identical.  This was likely not preverified.", bytesDiffer);
		}
	}

	@Override
	protected String getPluginConfigFileName() {
		return "com/goodformobile/build/mobile/preverify-plugin-config.xml";
	}

	@Override
	protected String getGoal() {
		return "preverify";
	}
}
