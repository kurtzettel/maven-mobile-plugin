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
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;

/**
 * Goal which generates a rapc file. This will add icons if potential icons are
 * found. It will also setup the necessary flags for RIM's RAPC utility.
 * 
 * @goal generate-rapc
 * 
 * @phase prepare-package
 */
public class GenerateRAPCMojo extends AbstractRIMBuildMojo {

	/**
	 * Name used as the MIDlet-Name in the rapc.  This will be the same as the project name if not specified.
	 * 
	 * @parameter 
	 */
	protected String midletName;	

	/**
	 * Determines if the application should attempt to run RAPC.
	 * 
	 * @parameter default-value="${skip.rapc}"
	 */
	private boolean skipRapc;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("GenerateRAPCMojo.execute()");

		if (skipRapc) {
			getLog().info("Skipping RAPC.");
			return;
		}
		
		Writer writer = null;
		try {
			String targetDirectoryPath = project.getBuild().getDirectory() + File.separator + "rapc";
			FileUtils.mkdir(targetDirectoryPath);

			File rapcFile = new File(targetDirectoryPath + File.separator + getPackagingFileName() + ".rapc");

			writer = WriterFactory.newWriter(rapcFile, WriterFactory.FILE_ENCODING);
			String midletName = this.midletName;
			if (midletName == null || midletName.length() == 0) {
				midletName = project.getName();
			}
			appendValue(writer, "MIDlet-Name", midletName);
			
			String version = project.getVersion();
			
			if (version.contains("-")) {
				version = version.replace("-", ".");
			}
			if (version.contains("SNAPSHOT")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
				version = version.replace("SNAPSHOT", sdf.format(new Date()));
			}
			
			appendValue(writer, "MIDlet-Version", version);
			appendValue(writer, "MIDlet-Vendor", vendor);
			if (description != null) {
				appendValue(writer, "MIDlet-Description", description);
			}
			appendValue(writer, "MIDlet-Jar-URL", project.getArtifactId() + ".jar");
			appendValue(writer, "MIDlet-Jar-Size", "0");
			appendValue(writer, "MicroEdition-Profile", "MIDP-2.0");
			appendValue(writer, "MicroEdition-Configuration", "CLDC-1.1");

			if (!isSystemModule()) {
				setupIcons();
			}

			appendValue(writer, "MIDlet-1", project.getName() + "," + applicationIcon + "," + mainArguments);

			if (rolloverApplicationIcon != null && !rolloverApplicationIcon.equals(applicationIcon)) {
				appendValue(writer, "RIM-MIDlet-Icon-1-1", rolloverApplicationIcon + ",focused");
				appendValue(writer, "RIM-MIDlet-Icon-Count-1", "1");
			}

			if (runOnStartup && isSystemModule()) {
				appendValue(writer, "RIM-Library-Flags", "3");
			} else if (isSystemModule()) {
				appendValue(writer, "RIM-Library-Flags", "2");
			} else if (runOnStartup) {
				appendValue(writer, "RIM-MIDlet-Flags-1", "1");
			} else {
				appendValue(writer, "RIM-MIDlet-Flags-1", "0");
			}

			if (homeScreenPosition > 0) {
				appendValue(writer, "RIM-MIDlet-Position-1", homeScreenPosition + "");
			}

		} catch (IOException e) {
			throw new MojoExecutionException("Unable to generate rapc file.", e);
		} finally {
			IOUtil.close(writer);
		}

	}

	private void appendValue(Writer writer, String name, String value) throws IOException {
		writer.append(name);
		writer.append(": ");
		writer.append(value);
		writer.append("\n");
	}

}
