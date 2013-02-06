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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Goal which calls RAPC to build an application.
 * 
 * @requiresDependencyResolution compile
 * @goal rapc
 * 
 * @phase package
 * @requiresDependencyResolution compile
*/
public final class RIMPackageMojo extends AbstractRIMBuildMojo {

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String> classpathElements;

	/**
	 * Determines if the application should attempt to run RAPC.
	 * 
	 * @parameter default-value="${rapc.skip}"
	 */
	private boolean skipRapc;

	public void execute() throws MojoExecutionException {

		getLog().info("RIMPackageMojo.execute()");

		if (skipRapc) {
			getLog().info("Skipping RAPC.");
			return;
		}

		// File tempDirectory = new File(System.getProperty("java.io.tmpdir"));

		// String codeName = project.getBuild().getDirectory() + File.separator
		// + "deliverables" + File.separator + getPackagingFileName(); //
		// Original is: deliverables\\Standard\\6.0.0\\sample_application
		
		//The deliverables directory has to be created or 4.5.0 builds will fail.
		File deliverablesDirectory = new File(project.getBasedir() + File.separator + "target" + File.separator + "deliverables");
		if (!deliverablesDirectory.exists() && !deliverablesDirectory.mkdir()) {
			throw new MojoExecutionException("Unable to create deliverables directory:" + deliverablesDirectory.getAbsolutePath());
		}

		String codeName = "target" + File.separator + "deliverables"
				+ File.separator + getPackagingFileName();
		// String rapcDescriptorFileLocation = project.getBuild().getDirectory()
		// + File.separator + "rapc" + File.separator + getPackagingFileName() +
		// ".rapc"; // Original is
		// deliverables\\Standard\\6.0.0\\sample_application
		String rapcDescriptorFileLocation = "target" + File.separator + "rapc"
				+ File.separator + getPackagingFileName() + ".rapc"; // Original
																		// is
																		// deliverables\\Standard\\6.0.0\\sample_application

		// project.getBuild().getDirectory() resolves to target/classes I think.

		String inputClassesDirectory = project.getBuild().getOutputDirectory();
		String workingDirectory = new File(inputClassesDirectory).getParent();
		
		String allFilesArg = ".";
		if (isWindows()) {
			allFilesArg = "*";
		}
		//Jar the directory.
		Commandline commandLine = new Commandline("jar cf "
				+ workingDirectory + File.separator + getPackagingFileName() + ".jar " + allFilesArg);

		getLog().debug("Using working directory:" + workingDirectory);
		commandLine.setWorkingDirectory(inputClassesDirectory);

		String logOutput = executeCommandLineToLogger(commandLine);
		
		String sourceDirectoryList = sourceDirectoryList = StringUtils.join(project
				.getCompileSourceRoots().iterator(), File.pathSeparator); // For
																			// the
																			// rapc
																			// sourceroot
																			// parameter.

		StringBuilder additionalArguments = new StringBuilder();
		if (isSystemModule()) {
			additionalArguments.append(" -nomain");
		}

		String outputLevel;
		if (getLog().isDebugEnabled()) {
			outputLevel = " -verbose";
		} else {
			outputLevel = " -quiet";
		}

		String codenameParameter;
		if (isSystemModule()) {
			// If you use codename instead of library you will get a warning
			// about unused classes.
			codenameParameter = "library";
		} else {
			codenameParameter = "codename";
		}

		StringBuilder importCommand = new StringBuilder();

		importCommand.append(rapcDirectory.getAbsolutePath() + File.separator + "lib" + File.separator + "net_rim_api.jar");

		if (classpathElements != null && (!isApplication() || !bundleDependenciesInAppCod)) {
			for (String classpathElement : classpathElements) {
				if (!new File(classpathElement).isDirectory()) {
					importCommand.append(File.pathSeparator);
					importCommand.append(classpathElement);
				}
			}
		}
		
		List<File> filesToDelete = new ArrayList<File>();
		
		//Prior to running rapc we need to copy each icon file or it won't work on older platforms.
		if (!isSystemModule()) {
			setupIcons();
			//
			try {
				filesToDelete.add(copyIcon(applicationIcon, project, project.getBasedir()));
				filesToDelete.add(copyIcon(rolloverApplicationIcon, project, project.getBasedir()));
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to copy the application icon [" + applicationIcon + "] or rollover application icon [" + rolloverApplicationIcon + "] to the rapc directory.", e);
			}
		}
		
		commandLine = new Commandline("java -jar "
				+ rapcDirectory.getAbsolutePath() + File.separator + "bin"
				+ File.separator + "rapc.jar -nopreverified"
				+ additionalArguments.toString() + outputLevel
				+ " -nojar -convertpng " + codenameParameter + "=" + codeName
				+ " " + rapcDescriptorFileLocation + " -sourceroot="
				+ sourceDirectoryList + " -import=" + importCommand.toString()
				+ " " + workingDirectory + "/" +  getPackagingFileName() +".jar ");

		getLog().debug("Using working directory:" + project.getBasedir().getAbsolutePath());
		commandLine.setWorkingDirectory(project.getBasedir());

		logOutput = executeCommandLineToLogger(commandLine);

		if (logOutput.contains("Warning!: No definition found")) {
			throw new MojoExecutionException(
					"Missing class from rapc.  Please ensure dependencies are correctly configured.");
		}

		if (bundleCodsInOutputJar) {
			try {
				FileUtils.copyDirectory(new File(project.getBuild()
						.getDirectory() + File.separator + "deliverables"),
						new File(inputClassesDirectory), "*.cod", "");
			} catch (IOException e) {
				throw new MojoExecutionException("Error copying files.", e);
			}
		}
		//Cleanup.
		for (File file : filesToDelete) {
			if (file != null && file.exists() && !file.delete()) {
				throw new MojoExecutionException("Unable to cleanup file:" + file.getAbsolutePath());
			}
		}
	}

	private File copyIcon(String icon, MavenProject project,
			File basedir) throws IOException {
		if (icon != null) {
			File file = new File(project.getBuild().getOutputDirectory() + File.separator + icon);
			if (file.exists()) {
				File copiedIconFile = new File(basedir.getAbsoluteFile() + File.separator + icon);
				FileUtils.copyFile(file, copiedIconFile);
				return copiedIconFile;
			}
		}
		return null;
	}
	
	protected boolean isWindows() {

		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);
	}
	
}
