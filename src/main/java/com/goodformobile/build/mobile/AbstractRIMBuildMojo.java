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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Base class for RIM building Mojos
 * 
 * @author Kurt
 * 
 */
public abstract class AbstractRIMBuildMojo extends AbstractMojo {

	private static final String[] POTENTIAL_ICON_LOCATIONS = new String[] {
			"icon.png", "img/icon.png", "images/icon.png" };

	private static final String[] POTENTIAL_ROLLOVER_ICON_LOCATIONS = new String[] {
			"iconr.png", "img/iconr.png", "images/iconr.png", "icon_r.png",
			"img/icon_r.png", "images/icon_r.png" };

	/**
	 * Name of the Project. Used for name in the ALX and the JAD.
	 * 
	 * @parameter default-value="${project.name}"
	 */
	protected String name;

	/**
	 * Description of the Project. Used for description in the ALX and the JAD
	 * 
	 * @parameter default-value="${project.description}"
	 */
	protected String description;

	/**
	 * Arguments used to call the application main method.
	 * 
	 * @parameter default-value=""
	 */
	protected String mainArguments = "";

	/**
	 * Vendor of the project. Used for vendor in the ALX and the JAD. Be careful
	 * changing this as it may affect some BlackBerry application update
	 * mechanisms.
	 * 
	 * @parameter default-value="<unknown>"
	 */
	protected String vendor;

	/**
	 * Icon used for the project. Will be ignored if file does not exist. Checks
	 * for icon.png, img/icon.png, images/icon.png if not specified.
	 * 
	 * @parameter
	 */
	protected String applicationIcon;

	/**
	 * Rollover Icon used for the project. Will be ignored if file does not
	 * exist. Checks for iconr.png, img/iconr.png, images/iconr.png if not
	 * specified.
	 * 
	 * @parameter
	 */
	protected String rolloverApplicationIcon;

	/**
	 * Determines if the application will automatically run when the device is
	 * started or application is first installed.
	 * 
	 * @parameter default-value="false"
	 */
	protected boolean runOnStartup;

	/**
	 * Determines if the application is a System Module. A System Module
	 * generally doesn't have an icon and provides some sort of service to other
	 * applications.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean systemModule;

	/**
	 * Used to determine where the application is position on the home screen.
	 * 
	 * @parameter default-value="0"
	 */
	protected int homeScreenPosition;

	/**
	 * Used to determine if dependencies should be bundled into a single
	 * application cod or included as library cods. By default this will include
	 * the cod files from dependent projects in the zip. If set to true this
	 * will include dependent content in a single cod.
	 * 
	 * @parameter default-value="false"
	 */
	protected boolean bundleDependenciesInAppCod;

	/**
	 * The Maven project.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The directory containing compiled classes.
	 * 
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @required
	 * @readonly
	 */
	protected File classesDirectory;

	/**
	 * Classifier used for platform specific jar. For example rim-6.0.0
	 * 
	 * @parameter
	 * @readonly
	 */
	protected String platformClassifier;

	/**
	 * Used for determining the output file name. Should just contain letters
	 * and underscores. Defaults to the artifact id with dashes and spaces
	 * replaced with underscores.
	 * 
	 * @parameter
	 */
	protected String packagingFileName;

	/**
	 * Directory containing RIM's tools. This should contain a bin directory
	 * with rapc.jar and a lib directory with net_rim_api.jar. If you are using
	 * Eclipse this directory would be something like
	 * "C:\tools\eclipse\eclipse-rim-3.0.201102031007-19\plugins\net.rim.ejde.componentpack6.0.0_6.0.0.30\com
	 * p o n e n t s "
	 * 
	 * @parameter default-value="${jde.directory}"
	 */
	protected File rapcDirectory;

	/**
	 * Include cod files in jar. If set to false the final jar will not include
	 * cod files. This is only applicable to library projects.
	 * 
	 * @parameter default-value="true"
	 */
	protected boolean bundleCodsInOutputJar;

	protected String getPackagingFileName() {
		if (StringUtils.isEmpty(packagingFileName)) {
			return project.getArtifactId().replaceAll("-", "_")
					.replaceAll(" ", "_");
		} else if (packagingFileName.contains("-")
				|| packagingFileName.contains(" ")) {
			throw new IllegalArgumentException(
					"The packagingFileName ["
							+ packagingFileName
							+ "] is invalid.  Please specify a name without spaces or dashes.");
		} else {
			return packagingFileName;
		}
	}

	/**
	 * Executes a command line and sends the standard out to the logger at info
	 * level and the standard error to the log at a warn level.
	 * 
	 * @param commandLine
	 *            to execute
	 * @throws MojoExecutionException
	 *             if the command line returns a non-0 result or if a
	 *             CommandLineException is thrown.
	 */
	protected String executeCommandLineToLogger(Commandline commandLine)
			throws MojoExecutionException {

		final StringBuilder logOutput = new StringBuilder();
		try {
			// if (getLog().isDebugEnabled()) {
			getLog().info("Executing:" + commandLine.toString());
			// }

			final int result = CommandLineUtils.executeCommandLine(commandLine,
					new InputStream() {

						public int read() {
							return -1;
						}

					}, new StreamConsumer() {

						public void consumeLine(final String line) {
							getLog().info(line);
							logOutput.append(line);
							logOutput.append("\n");
						}

					}, new StreamConsumer() {

						public void consumeLine(final String line) {
							getLog().warn(line);
							logOutput.append(line);
							logOutput.append("\n");
						}

					}, 600);
			if (result != 0) {
				throw new MojoExecutionException("Command Failed: result["
						+ result + "] " + commandLine);
			}
		} catch (CommandLineException e) {
			throw new MojoExecutionException("Command Failed: " + commandLine,
					e);
		}
		return logOutput.toString();
	}

	protected boolean isSystemModule() {

		if (!systemModule && project.getModel().getPackaging().equals("bblib")) {
			systemModule = true;
		}
		return systemModule;
	}

	protected boolean isApplication() {
		return project.getModel().getPackaging().equals("bbapp");
	}
	
	protected void extractCompressedCods(String outputDirectory)
			throws MojoExecutionException {
		getLog().debug("Extracting cods in " + outputDirectory);
		File deliverablesDirectory = new File(outputDirectory);
		File[] deliverableFiles = deliverablesDirectory.listFiles();
		if (deliverableFiles != null) {
			for (File deliverableFile : deliverableFiles) {
				if (deliverableFile.getName().endsWith(".cod")) {
					// Check if it is a zipped code.
					try {
						ZipFile zipFile = new ZipFile(deliverableFile);
						Enumeration<? extends ZipEntry> zipEntries = zipFile
								.entries();
						if (zipEntries.hasMoreElements()) {
							// This is a valid zip. Close it and rename it.
							zipFile.close();
							File codAsZip = new File(deliverablesDirectory,
									deliverableFile.getName() + ".zip");
							deliverableFile.renameTo(codAsZip);
							zipFile = new ZipFile(codAsZip);
							zipEntries = zipFile.entries();
							while (zipEntries.hasMoreElements()) {
								ZipEntry zipEntry = (ZipEntry) zipEntries
										.nextElement();
								File newCod = new File(outputDirectory,
										zipEntry.getName());
								if (newCod.exists() && !newCod.delete()) {
									throw new MojoExecutionException(
											"Unable to delete:"
													+ newCod.getAbsolutePath());
								}
								if (!newCod.createNewFile()) {
									throw new MojoExecutionException(
											"Unable to create cod:"
													+ newCod.getAbsolutePath());
								}
								OutputStream output = new FileOutputStream(
										newCod);
								try {
									IOUtil.copy(
											zipFile.getInputStream(zipEntry),
											output);
								} finally {
									IOUtil.close(output);
								}
							}
							zipFile.close();
							if (!codAsZip.delete()) {
								throw new MojoExecutionException(
										"Unable to delete cod as zip:"
												+ codAsZip.getAbsolutePath());
							}

						}
					} catch (ZipException e) {
						// Ignore. This is probably just a non large cod.
					} catch (IOException e) {
						// Ignore. This is probably just a non large cod.
					}

				}
			}
		}
	}

	protected void setupIcons() {
		if (applicationIcon == null) {
			for (String potentialIcon : POTENTIAL_ICON_LOCATIONS) {
				File potentialIconFile = new File(project.getBuild()
						.getOutputDirectory() + File.separator + potentialIcon);
				if (potentialIconFile.exists()) {
					getLog().info(
							"Found file:" + potentialIcon
									+ " and using it as the project icon.");
					applicationIcon = potentialIcon;
					break;
				}
			}
		} else {
			File potentialIconFile = new File(project.getBuild()
					.getOutputDirectory() + File.separator + applicationIcon);
			if (!potentialIconFile.exists()) {
				getLog().warn(
						"The specified application icon " + applicationIcon
								+ " does not exist.");
			}
		}

		if (rolloverApplicationIcon == null) {
			for (String potentialIcon : POTENTIAL_ROLLOVER_ICON_LOCATIONS) {
				File potentialIconFile = new File(project.getBuild()
						.getOutputDirectory() + File.separator + potentialIcon);
				if (potentialIconFile.exists()) {
					getLog().info(
							"Found file:"
									+ potentialIcon
									+ " and using it as the project rollover icon.");
					rolloverApplicationIcon = potentialIcon;
					break;
				}
			}
		} else {
			File potentialIconFile = new File(project.getBuild()
					.getOutputDirectory()
					+ File.separator
					+ rolloverApplicationIcon);
			if (!potentialIconFile.exists()) {
				getLog().warn(
						"The specified rollover application icon "
								+ rolloverApplicationIcon + " does not exist.");
			}
		}
	}

	protected boolean isWindows()
	{
	
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);
	}
}
