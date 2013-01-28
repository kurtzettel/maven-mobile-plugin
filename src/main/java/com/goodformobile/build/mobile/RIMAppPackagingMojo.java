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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.IOUtil;

import com.goodformobile.build.mobile.model.CodFile;
import com.goodformobile.build.mobile.model.Jad;
import com.goodformobile.build.mobile.model.JadProperty;

/**
 * Goal which prepares the rapc output to be zipped. Used exclusively for
 * Applications (not libraries). It will extract .cod files from dependent jars,
 * inflate any compressed cods, and add dependent cods to the jad.
 * 
 * @goal setup-app-deliverables
 * 
 * @phase package
 * @requiresDependencyResolution compile
 * 
 */
public final class RIMAppPackagingMojo extends AbstractRIMBuildMojo {

	/**
	 * Project classpath.
	 * 
	 * @parameter default-value="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List<String> classpathElements;

	public void execute() throws MojoExecutionException {

		getLog().info("RIMAppPackagingMojo.execute()");

		String outputDirectory = project.getBuild().getDirectory() + File.separator + "deliverables";

		// FIXME Ideally this would only extract from non-provided dependencies.
		// Currently it uses a hack to skip net-rim jars.
		// Extract cods from dependencies.
		if (classpathElements != null && !bundleDependenciesInAppCod) {
			for (String classPathElementString : classpathElements) {
				if (classPathElementString.contains("net_rim") || classPathElementString.contains("net-rim")) {
					continue;
				}
				File classpathElement = new File(classPathElementString);
				if (!classpathElement.isDirectory()) {
					try {
						ZipFile zipFile = new ZipFile(classpathElement);
						// Check for cod entries.
						Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
						while (zipEntries.hasMoreElements()) {
							ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
							if (zipEntry.getName().endsWith(".cod")) {
								getLog().info("Found dependent cod:" + zipEntry.getName() + " in " + classPathElementString);
								File newCod = new File(outputDirectory, zipEntry.getName());
								if (newCod.exists() && !newCod.delete()) {
									throw new MojoExecutionException("Unable to delete:" + newCod.getAbsolutePath());
								}
								if (!newCod.createNewFile()) {
									throw new MojoExecutionException("Unable to create cod:" + newCod.getAbsolutePath());
								}
								OutputStream output = new FileOutputStream(newCod);
								try {
									IOUtil.copy(zipFile.getInputStream(zipEntry), output);
								} finally {
									IOUtil.close(output);
								}
							}
						}

					} catch (ZipException e) {
						getLog().info("Unable to read zip:" + classPathElementString, e);
					} catch (IOException e) {
						getLog().info("Unable to read zip:" + classPathElementString, e);
					}
				}
			}
		}

		extractCompressedCods(outputDirectory);

		File deliverablesDirectory = new File(outputDirectory);

		// Update SHAs and Sizes.
		List<CodFile> codFiles = new ArrayList<CodFile>();
		File[] deliverableFiles = deliverablesDirectory.listFiles();
		if (deliverableFiles != null) {
			for (File file : deliverableFiles) {
				if (file.getName().endsWith(".cod")) {
					CodFile codFile = new CodFile();
					codFile.setCodName(file.getName());
					codFile.setCodSize(file.length());
					// FIXME Calculate this.
					// codFile.setSha("");
					// codFile.setCreateTime(file.)
					codFiles.add(codFile);
				} else {
					//We need to delete any jar or debug files.
					if (file.getName().endsWith(".debug") || file.getName().endsWith(".jar") || file.getName().endsWith(".csl") || file.getName().endsWith(".cso")) {
						if (!file.delete()) {
							throw new MojoExecutionException("Unable to delete:" + file.getAbsolutePath());
						}						
					}
				}
			}
		}

		// Combine jads
		String jadName = getPackagingFileName() + ".jad";
		File jadFile = new File(outputDirectory, jadName);
		Jad jad = new Jad();
		try {
			InputStream inputStream = new FileInputStream(jadFile);
			try {
				jad.parseFile(inputStream);
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to read jad.", e);
		}
		jad.removeCodProperties();

		for (int i = 0; i < codFiles.size(); i++) {
			CodFile codFile = codFiles.get(i);
			String suffix = "";
			if (i > 0) {
				suffix = "-" + i;
			}
			JadProperty jadProperty = new JadProperty();
			jadProperty.setName("RIM-COD-URL" + suffix);
			jadProperty.setValue(codFile.getCodName());
			jad.getPropertyList().add(jadProperty);

			jadProperty = new JadProperty();
			jadProperty.setName("RIM-COD-Size" + suffix);
			jadProperty.setValue("" + codFile.getCodSize());
			jad.getPropertyList().add(jadProperty);

			if (codFile.getSha() != null) {
				jadProperty = new JadProperty();
				jadProperty.setName("RIM-COD-SHA1" + suffix);
				jadProperty.setValue(codFile.getSha());
				jad.getPropertyList().add(jadProperty);
			}
		}

		if (!jadFile.delete()) {
			throw new MojoExecutionException("Unable to delete old jad file.");
		}

		try {
			if (!jadFile.createNewFile()) {
				throw new MojoExecutionException("Unable to create new jad.");
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to create new jad.", e);
		}

		try {
			FileOutputStream output = new FileOutputStream(jadFile);
			try {
				jad.writeJad(output);
			} finally {
				output.close();
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to create new jad.", e);
		}

		project.getBuild().setOutputDirectory(outputDirectory);
	}
}
