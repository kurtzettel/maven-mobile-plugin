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
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;

/**
 * Creates an additional artifact with the platform classifier. This allows
 * multiple artifacts to be uploaded for varying RIM platforms.
 * 
 * @goal setup-classified-jar
 * 
 * @phase package
 * @requiresDependencyResolution compile
 * 
 */
public final class RIMClassifiedJarMojo extends AbstractRIMBuildMojo {

	/**
	 * @component
	 */
	private MavenProjectHelper projectHelper;

	public void execute() throws MojoExecutionException {

		getLog().info("RIMClassifiedJarMojo.execute()");

		String packagingType = "jar";
		if (!isSystemModule()) {
			packagingType = "zip";
		}
		if (platformClassifier != null) {
			projectHelper.attachArtifact(project, packagingType, platformClassifier, project.getArtifact().getFile());

			String artifactName = project.getArtifactId() + "-" + project.getVersion();
			try {
				FileUtils.copyFile(project.getArtifact().getFile(), new File(project.getArtifact().getFile().getParent() + File.separator + artifactName + "-" + platformClassifier + "." + packagingType));
			} catch (IOException e) {
				throw new MojoExecutionException("Unable to create classified file.", e);
			}

			getLog().debug("Checking for other classified artifacts to attach in " + project.getArtifact().getFile().getParent());

			File classesDirectory = new File(project.getArtifact().getFile().getParent());
			File[] children = classesDirectory.listFiles();
			for (int i = 0; i < children.length; i++) {
				File file = children[i];
				String fileName = file.getName();
				if (fileName.startsWith(artifactName + "-") && fileName.endsWith("." + packagingType)) {
					String foundClassifier = fileName.substring((artifactName + "-").length(), fileName.length() - (packagingType.length() + 1));
					if (!foundClassifier.equals(platformClassifier)) {
						//Attach it.
						getLog().info("Attaching " + file.getAbsolutePath() + " as classified artifact.");
						projectHelper.attachArtifact(project, packagingType, foundClassifier, file);			
					}
				}
			}

		}
	}
}
