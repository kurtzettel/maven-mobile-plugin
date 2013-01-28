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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Goal which calls uses the RIM Signature Tool to sign application cod files.
 * This uses a signing password from your maven settings.xml. This password can
 * be encrypted using Maven's password encryption mechanism.
 * 
 * @goal sign
 * 
 * @phase package
 */
public final class RIMSigningMojo extends AbstractRIMBuildMojo {

	/**
	 * Directory containing RIM's SignatureTool.jar. This should contain the
	 * SignatureTool.jar, sigtool.csk, and sigtool.db. If you are using the
	 * Eclipse plugin this directory will look something like:
	 * C:\\tools\\eclipse
	 * \\eclipse-rim-3.0.201102031007-19\\plugins\\net.rim.ejde\\vmTools
	 * 
	 * @parameter default-value="${signature.path}"
	 */
	private File signaturePath;

	/**
	 * Determines if the application should attempt to sign the output cod
	 * files.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean skipSigning;

	/**
	 * Determines which server id in the settings.xml the application should use
	 * for looking up the signature password.
	 * 
	 * @parameter default-value="rim-signing"
	 */
	private String signingCredentialsServerId;

	/**
	 * The current user system settings for use in Maven.
	 * 
	 * @parameter expression="${settings}"
	 * @required
	 * @readonly
	 */
	private Settings settings;

	@Requirement
	private SettingsDecrypter settingsDecrypter;

	public void execute() throws MojoExecutionException {

		getLog().info("RIMSigningMojo.execute()");

		if (skipSigning) {
			getLog().info("Skipping signing.");
			return;
		}

		Settings settings = this.settings;
		Server server = settings.getServer(signingCredentialsServerId);
		if (server == null) {
			throw new MojoExecutionException("Unable to find server with the id[" + signingCredentialsServerId + "].  Please configure this in your settings.xml");
		}
		String password = server.getPassword();
		if (StringUtils.isEmpty(password)) {
			throw new MojoExecutionException("Unable to find password in settings.xml for server with the id[" + signingCredentialsServerId + "].");
		}

		if (password.contains("{")) {
			SettingsDecryptionRequest settingsDecryptionRequest = new DefaultSettingsDecryptionRequest(server);
			SettingsDecryptionResult result = settingsDecrypter.decrypt(settingsDecryptionRequest);
			if (result.getProblems() != null && result.getProblems().size() > 0) {
				throw new MojoExecutionException("Unable to decrypt password:" + result.getProblems().get(0).toString(), result.getProblems().get(0).getException());
			}
			password = result.getServer().getPassword();
		}

		File deliverablesDirectory = new File(project.getBuild().getDirectory() + File.separator + "deliverables");

		if (signaturePath == null) {
			throw new MojoExecutionException("signaturePath is null.  Please specify the <signature.path> property pointing to the directory with your signatureTools.jar");
		}
		
		Commandline commandLine = new Commandline("java -jar " + signaturePath.getAbsolutePath() + File.separator +
				"SignatureTool.jar -a -C -s -p " + password + " -r " + deliverablesDirectory.getAbsolutePath());

		commandLine.setWorkingDirectory(project.getBasedir());

		executeCommandLineToLogger(commandLine);

		String inputClassesDirectory =
				project.getBuild().getOutputDirectory();

		if (bundleCodsInOutputJar) {
			try {
				FileUtils.copyDirectory(new File(project.getBuild().getDirectory() + File.separator + "deliverables"), new File(inputClassesDirectory), "*.cod", "");
			} catch (IOException e) {
				throw new MojoExecutionException("Error copying files.", e);
			}
		}
	}
}
