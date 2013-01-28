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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Goal which creates a RIM rrc and rrh files from normal .properties files.
 * 
 * @goal createRIMLocalizationFiles
 * 
 * @phase generate-sources
 */
public class GenerateRIMLocalizationFiles extends AbstractRIMBuildMojo {

	/**
	 * The directory containing name-value pair property files.
	 * 
	 * @parameter
	 */
	protected File localizationDirectory;

	/**
	 * The directory to generate RIM's rrh and rrc files.
	 * 
	 * @parameter default-value=
	 *            "${project.build.directory}/generated-sources/localization-file"
	 */
	protected File localizationOutputDirectory;

	/**
	 * @parameter default-value= "${project.build.directory}"
	 */
	protected File projectBuildDirectory;

	/**
	 * Package used for generated resource files.
	 * 
	 * @parameter default-value="${project.groupId}"
	 */
	protected String localizationPackage;

	public void execute() throws MojoExecutionException {

		getLog().info("GenerateRIMLocalizationFiles.execute()");

		if (localizationDirectory != null) {
			getLog().info("Checking for properties in:" + localizationDirectory.getAbsolutePath());

			try {
				List<File> propertyFiles = FileUtils.getFiles(localizationDirectory, "**.properties", null);

				File packageDirectory = localizationOutputDirectory;
				makeDirectory(packageDirectory);

				if (propertyFiles.size() > 0) {
					// Create a package directory.
					String[] packageParts = localizationPackage.split("\\.");
					for (String packagePart : packageParts) {
						packageDirectory = new File(packageDirectory, packagePart);
						makeDirectory(packageDirectory);
					}

					for (File file : propertyFiles) {
						getLog().info("Found properties file:" + file);
						if (isBaseFile(file, propertyFiles)) {
							getLog().info("BaseFile:" + file);
							String currentFileName = getPropertyFileNameWithoutExtension(file);

							List<MappedProperty> mappedProperties = getMappedProperties(file);

							File rrhFile = new File(packageDirectory, currentFileName + ".rrh");
							if (rrhFile.exists() && !rrhFile.delete()) {
								throw new MojoExecutionException("Unable to delete existing rrh:" + rrhFile.getAbsolutePath());
							}
							rrhFile.createNewFile();

							StringBuilder rrhContent = new StringBuilder();
							rrhContent.append("package ");
							rrhContent.append(localizationPackage);
							rrhContent.append(";\n\n");

							Properties baseProperties = new Properties();
							FileInputStream propertyInputStream = new FileInputStream(file);
							try {
								baseProperties.load(propertyInputStream);
							} finally {
								propertyInputStream.close();
							}

							for (MappedProperty mappedProperty : mappedProperties) {
								rrhContent.append(mappedProperty.cleanName);
								rrhContent.append("#0=");
								rrhContent.append(mappedProperty.counter);
								rrhContent.append(";\n");
							}

							FileUtils.fileAppend(rrhFile.getAbsolutePath(), rrhContent.toString());

							StringBuilder rrcContent = new StringBuilder();
							File rrcFile = new File(packageDirectory, currentFileName + ".rrc");
							if (rrcFile.exists() && !rrcFile.delete()) {
								throw new MojoExecutionException("Unable to delete existing rrc:" + rrcFile.getAbsolutePath());
							}
							writeRrcFromProperties(rrcFile, mappedProperties, baseProperties);

							// Handle each of the child files.
							for (File potentialChildFile : propertyFiles) {
								String childFileName = getPropertyFileNameWithoutExtension(potentialChildFile);
								if (childFileName.startsWith(currentFileName) && childFileName.length() > currentFileName.length()) {
									String language = childFileName.substring(currentFileName.length());
									getLog().info("Creating rrc for language " + language + " for the property file: " + childFileName);

									Properties languageSpecificProperties = new Properties();
									propertyInputStream = new FileInputStream(potentialChildFile);
									try {
										languageSpecificProperties.load(propertyInputStream);
									} finally {
										propertyInputStream.close();
									}

									rrcFile = new File(packageDirectory, currentFileName + language + ".rrc");
									if (rrcFile.exists() && !rrcFile.delete()) {
										throw new MojoExecutionException("Unable to delete existing rrc:" + rrcFile.getAbsolutePath());
									}
									writeRrcFromProperties(rrcFile, mappedProperties, languageSpecificProperties);

								}
							}

						} else {
							getLog().info("ChildFile:" + file);
						}
					}
				}
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to find property files", e);
			}
		}
	}

	private void writeRrcFromProperties(File rrcFile, List<MappedProperty> mappedProperties, Properties baseProperties) throws IOException {
		rrcFile.createNewFile();

		StringBuilder rrcContent = new StringBuilder();

		for (MappedProperty mappedProperty : mappedProperties) {
			String propertyValue = baseProperties.getProperty(mappedProperty.propertyName);
			if (propertyValue != null) {
				rrcContent.append(mappedProperty.cleanName);
				rrcContent.append("#0=");
				rrcContent.append(escapeAndQuoteValue(propertyValue));
				rrcContent.append(";\n");
			}
		}

		FileUtils.fileAppend(rrcFile.getAbsolutePath(), rrcContent.toString());
	}

	private String escapeAndQuoteValue(String propertyValue) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\"");
		stringBuilder.append(propertyValue.replaceAll("\"", "\\\""));
		stringBuilder.append("\"");
		return stringBuilder.toString();
	}

	private List<MappedProperty> getMappedProperties(File file) throws IOException {

		FileReader fileReader = new FileReader(file);
		List<MappedProperty> list = new ArrayList<GenerateRIMLocalizationFiles.MappedProperty>();
		int counter = -1;
		try {
			BufferedReader reader = new BufferedReader(fileReader);
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() > 0 && !line.startsWith("#") && line.contains("=")) {
					String key = line.substring(0, line.indexOf("="));
					counter++;
					MappedProperty mappedProperty = new MappedProperty(key, getCleanPropertyName(key), counter);
					list.add(mappedProperty);
				}

				line = reader.readLine();
			}

		} finally {
			fileReader.close();
		}
		return list;
	}

	private String getCleanPropertyName(String key) {
		if (key.contains(".")) {
			String[] parts = key.split("\\.");
			StringBuilder fixedCamelCaseKey = new StringBuilder();
			for (int i = 0; i < parts.length; i++) {
				if (i == 0) {
					fixedCamelCaseKey.append(parts[i]);
				} else {
					fixedCamelCaseKey.append(parts[i].substring(0, 1).toUpperCase());
					if (parts[i].length() > 1) {
						fixedCamelCaseKey.append(parts[i].substring(1));
					}
				}
			}
		}
		return key;
	}

	protected void makeDirectory(File packageDirectory) throws MojoExecutionException {

		if (packageDirectory.getAbsolutePath().startsWith(projectBuildDirectory.getAbsolutePath())) {
			String newDirectories = packageDirectory.getAbsolutePath().substring(projectBuildDirectory.getAbsolutePath().length());
			String[] newDirectoryParts = newDirectories.split("\\" + File.separator);
			String directoryToCreate = projectBuildDirectory.getAbsolutePath();
			File directory = projectBuildDirectory;
			for (int i = 0; i < newDirectoryParts.length; i++) {
				directory = new File(directoryToCreate, newDirectoryParts[i]);
				if (!directory.exists() && !directory.mkdir()) {
					throw new MojoExecutionException("Unable to create directory:" + packageDirectory.getAbsolutePath());
				}
			}
		}

		if (!packageDirectory.exists() && !packageDirectory.mkdir()) {
			throw new MojoExecutionException("Unable to create directory:" + packageDirectory.getAbsolutePath());
		}
	}

	private boolean isBaseFile(File file, List<File> propertyFiles) {
		String currentFileName = getPropertyFileNameWithoutExtension(file);
		for (File propertyFile : propertyFiles) {
			String fileNameWithoutExtension = getPropertyFileNameWithoutExtension(propertyFile);
			getLog().info("Property File Name:" + fileNameWithoutExtension);
			if (fileNameWithoutExtension.startsWith(currentFileName) && fileNameWithoutExtension.length() > currentFileName.length()) {
				getLog().info(fileNameWithoutExtension + " is a child of " + currentFileName);
				return true;
			}
		}
		return false;
	}

	protected String getPropertyFileNameWithoutExtension(File file) {
		return file.getName().substring(0, file.getName().length() - 11);
	}

	private class MappedProperty {
		private final String propertyName;
		private final int counter;
		private final String cleanName;

		public MappedProperty(String propertyName, String cleanName, int counter) {
			this.propertyName = propertyName;
			this.cleanName = cleanName;
			this.counter = counter;
		}
	}
}
