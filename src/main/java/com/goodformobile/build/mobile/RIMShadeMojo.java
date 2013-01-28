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
 * Goal which uses the shader from the Maven Shade Plugin to pre-process project
 * libraries and remove cod files. This allows a work around for the class
 * multiply-defined error.
 * 
 * @requiresDependencyResolution compile
 * @goal shade
 * 
 * @phase package
 * @requiresDependencyResolution compile
 */
public final class RIMShadeMojo extends AbstractRIMBuildMojo
{

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String>	classpathElements;

	public void execute() throws MojoExecutionException
	{

		getLog().info("RIMShadeMojo.execute()");

		if (true) {
			return;
		}
		
		// File tempDirectory = new File(System.getProperty("java.io.tmpdir"));

		// String codeName = project.getBuild().getDirectory() + File.separator
		// + "deliverables" + File.separator + getPackagingFileName(); //
		// Original is: deliverables\\Standard\\6.0.0\\sample_application

		// The deliverables directory has to be created or 4.5.0 builds will
		// fail.
		File deliverablesDirectory = new File(project.getBasedir() + File.separator + "target" + File.separator + "deliverables");
		if (!deliverablesDirectory.exists() && !deliverablesDirectory.mkdir())
		{
			throw new MojoExecutionException("Unable to create deliverables directory:" + deliverablesDirectory.getAbsolutePath());
		}

		String codeName = "target" + File.separator + "deliverables" + File.separator + getPackagingFileName();
		// String rapcDescriptorFileLocation = project.getBuild().getDirectory()
		// + File.separator + "rapc" + File.separator + getPackagingFileName() +
		// ".rapc"; // Original is
		// deliverables\\Standard\\6.0.0\\sample_application
		String rapcDescriptorFileLocation = "target" + File.separator + "rapc" + File.separator + getPackagingFileName() + ".rapc"; // Original
																																	// is
																																	// deliverables\\Standard\\6.0.0\\sample_application

		// project.getBuild().getDirectory() resolves to target/classes I think.

		String inputClassesDirectory = project.getBuild().getOutputDirectory();
		String workingDirectory = new File(inputClassesDirectory).getParent();

		String allFilesArg = ".";
		if (isWindows())
		{
			allFilesArg = "*";
		}
		// Jar the directory.
		Commandline commandLine = new Commandline("jar cf " + workingDirectory + File.separator + getPackagingFileName() + ".jar " + allFilesArg);

	}

}
