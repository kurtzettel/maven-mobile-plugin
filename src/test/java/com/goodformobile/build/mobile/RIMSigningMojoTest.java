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
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecrypter;
import org.junit.Test;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

public class RIMSigningMojoTest extends AbstractRIMMojoTest<RIMSigningMojo> {

	@Test
	public void testExecuteOnBasicApplicationWithoutProfile() throws Exception {

		File projectDirectory = getRelativeFile("projects/maven-test-app-preverified/pom.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMSigningMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		setVariableValueToObject(mojo, "settings", getSettings());
		setVariableValueToObject(mojo, "signingCredentialsServerId", "wrong-server");

		try {
			mojo.execute();
			fail("Expected Exception when passing in a bad server id.");
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains("Please configure this in your settings.xml"));
		}
	}

	@Test
	public void testExecuteOnBasicApplicationWithWrongPassword() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-dependency-unsigned/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMSigningMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		Settings settings = getSettings();
		setVariableValueToObject(mojo, "settings", settings);
		setVariableValueToObject(mojo, "signingCredentialsServerId", "rim-signing");

		// Manipulate settings.
		settings.getServer("rim-signing").setPassword("fail8888");

		try {
			mojo.execute();
			fail("Expected Exception when signature tool fails.");
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains("Failed"));
		}
	}

	@Test
	public void testExecuteOnBasicApplicationWithWrongEncryptedPassword() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-dependency-unsigned/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMSigningMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		Settings settings = getSettings();
		setVariableValueToObject(mojo, "settings", settings);
		setVariableValueToObject(mojo, "signingCredentialsServerId", "rim-signing");

		// Manipulate settings.
		settings.getServer("rim-signing").setPassword("{AAAAAAAAAAEHBikVYlGQIppFIr14BDdzao4vr6wwbHM=}");

		setupDecrypter(mojo);

		try {
			mojo.execute();
			fail("Expected Exception when signature tool fails.");
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains("Failed"));
		}
	}

	@Test
	public void testExecuteOnBasicApplication() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-dependency-unsigned/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMSigningMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		setVariableValueToObject(mojo, "settings", getSettings());
		setVariableValueToObject(mojo, "signingCredentialsServerId", "rim-signing");

		byte[] originalChecksum = createChecksum(workProjectDirectory.getAbsolutePath() + "/target/deliverables/app_with_dependency.cod");

		mojo.execute();

		byte[] signedChecksum = createChecksum(workProjectDirectory.getAbsolutePath() + "/target/deliverables/app_with_dependency.cod");
		boolean same = isChecksumSame(originalChecksum, signedChecksum);
		assertFalse("Expected the checksum to change on signed cod file.", same);
	}

	@Test
	public void testSkipSigning() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-dependency-unsigned/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMSigningMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		setVariableValueToObject(mojo, "settings", getSettings());
		setVariableValueToObject(mojo, "signingCredentialsServerId", "rim-signing");
		setVariableValueToObject(mojo, "skipSigning", true);

		byte[] originalChecksum = createChecksum(workProjectDirectory.getAbsolutePath() + "/target/deliverables/app_with_dependency.cod");

		mojo.execute();

		byte[] signedChecksum = createChecksum(workProjectDirectory.getAbsolutePath() + "/target/deliverables/app_with_dependency.cod");
		boolean same = isChecksumSame(originalChecksum, signedChecksum);
		assertTrue("Expected the checksum to be unchanged for cod file when signing is skipped.", same);
	}	
	protected boolean isChecksumSame(byte[] originalChecksum, byte[] signedChecksum) {
		boolean same = true;
		if (originalChecksum.length != signedChecksum.length) {
			same = false;
		} else {
			for (int i = 0; i < signedChecksum.length; i++) {
				if (originalChecksum[i] != signedChecksum[i]) {
					same = false;
					break;
				}
			}
		}
		return same;
	}

	@Test
	public void testExecuteOnBasicApplicationWithEncryptedPassword() throws Exception {

		File projectDirectory = getRelativeFile("projects/app-with-dependency-unsigned/BlackBerry_App_Descriptor.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMSigningMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		setVariableValueToObject(mojo, "settings", getSettings());
		setVariableValueToObject(mojo, "signingCredentialsServerId", "rim-signing-encrypted");

		setupDecrypter(mojo);

		mojo.execute();
	}

	protected void setupDecrypter(RIMSigningMojo mojo) throws IllegalAccessException, PlexusCipherException {
		DefaultSettingsDecrypter decrypter = new DefaultSettingsDecrypter();

		SecDispatcher secDispatcher = new DefaultSecDispatcher();
		setVariableValueToObject(decrypter, "securityDispatcher", secDispatcher);

		PlexusCipher cipher = new DefaultPlexusCipher();
		setVariableValueToObject(secDispatcher, "_cipher", cipher);

		setVariableValueToObject(mojo, "settingsDecrypter", decrypter);
	}

	@Override
	protected String getPluginConfigFileName() {
		return "com/goodformobile/build/mobile/execute-rapc-plugin-config.xml";
	}

	@Override
	protected String getGoal() {
		return "sign";
	}

	protected RIMSigningMojo setupMojo() throws java.net.URISyntaxException, Exception {
		RIMSigningMojo mojo = super.setupMojo();
		setVariableValueToObject(mojo, "signaturePath", new File(getVariableFromRimProfile("signature.path")));
		return mojo;
	};

	public static byte[] createChecksum(String filename) throws
			Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("SHA1");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}
}
