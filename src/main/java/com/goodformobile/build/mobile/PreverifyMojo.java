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
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Goal which preverifies an application. Uses Proguard or a specified preverify
 * executable for j2me preverification.
 * 
 * @goal preverify
 * 
 * @phase process-classes
 * 
 * @requiresDependencyResolution compile
 */
public class PreverifyMojo extends AbstractRIMBuildMojo {

	/**
	 * Dependency Plugins. This is required to be the proguard dependency.
	 * 
	 * @parameter expression="${plugin.artifacts}"
	 * @required
	 */
	protected List<Artifact> pluginArtifacts;

	/**
	 * Optionally override the preverify executable.
	 * 
	 * @parameter default-value="${preverify.path}"
	 */
	protected File preverifyExecutable;

	/**
	 * If true the application will not attempt to use a preverify executable
	 * instead of proguard.
	 * 
	 * @parameter default-value="false"
	 */
	protected boolean forceProguard;

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String> classpathElements;

	/**
	 * Determines if the application should attempt to run preverify.
	 * 
	 * @parameter default-value="${preverify.skip}"
	 */
	private boolean skipPreverify;

	public void execute() throws MojoExecutionException {

		getLog().info("PreverifyMojo.execute()");

		if (skipPreverify) {
			getLog().info("Skipping preverify.");
			return;
		}

		boolean useProguard = false;

		File preverifyExecutable = null;
		if (!forceProguard && this.preverifyExecutable == null) {
			// Check for preverify in the rim bin dir..
			preverifyExecutable = new File(rapcDirectory.getAbsolutePath() + File.separator + "bin" + File.separator + "preverify.exe");
			if (!preverifyExecutable.exists()) {
				useProguard = true;
			}
		} else if (!forceProguard) {
			preverifyExecutable = this.preverifyExecutable;
			if (!preverifyExecutable.exists()) {
				throw new MojoExecutionException("Unable to find preverify executable:" + preverifyExecutable.getAbsolutePath());
			}
		} else {
			useProguard = true;
		}

		String targetDirectoryPath = project.getBuild().getDirectory() + File.separator + "preverified";
		FileUtils.mkdir(targetDirectoryPath);

		String inputClassesDirectory = project.getBuild().getOutputDirectory();

		Commandline commandLine = null;

		StringBuilder importCommand = new StringBuilder();

		importCommand.append(rapcDirectory.getAbsolutePath() + File.separator + "lib" + File.separator + "net_rim_api.jar");

		if (classpathElements != null) {
			for (String classpathElement : classpathElements) {
				if (!new File(classpathElement).isDirectory()) {
					importCommand.append(File.pathSeparator);
					importCommand.append(classpathElement);
				}
			}
		}

		if (useProguard) {
			Artifact proguardArtifact = null;
			// execute proguard.
			for (Artifact artifact : pluginArtifacts) {
				if (artifact.getArtifactId().equals("proguard")) {
					getLog().info("Found Proguard: " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion());
					proguardArtifact = artifact;
					break;
				}
			}

			assert proguardArtifact != null;

			String verbose = "";
			// if (getLog().isDebugEnabled()) {
			verbose = " -verbose";
			// }

			String optimizeOptions = " -dontoptimize";

			commandLine = new Commandline("java -jar " + proguardArtifact.getFile().getAbsolutePath() + " -microedition -dontshrink" + optimizeOptions + " -dontobfuscate" + verbose + " -libraryjars "
					+ importCommand.toString() + " -injars " + inputClassesDirectory + " -outjars " + targetDirectoryPath);

		} else {

			commandLine = new Commandline(preverifyExecutable.getAbsolutePath() + " -classpath " + importCommand.toString() + " -d " + targetDirectoryPath + " " + inputClassesDirectory);
		}

		commandLine.setWorkingDirectory(project.getBasedir());

		executeCommandLineToLogger(commandLine);

		try {
			FileUtils.copyDirectoryStructure(new File(targetDirectoryPath), new File(inputClassesDirectory));
			FileUtils.deleteDirectory(targetDirectoryPath);
		} catch (IOException e) {
			throw new MojoExecutionException("Error copying files.", e);
		}
	}
}
