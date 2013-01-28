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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which removes RIM resources from previous builds. Prevents old cods from
 * being included in unclean builds.
 * 
 * @goal rim-cleanup
 * 
 * @phase process-sources
 */
public class RIMCleanupMojo extends AbstractRIMBuildMojo {

	public void execute() throws MojoExecutionException {

		getLog().info("RIMCleanupMojo.execute()");

		String inputClassesDirectory = project.getBuild().getOutputDirectory();
		File classesDirectory = new File(inputClassesDirectory);
		if (classesDirectory.exists()) {
			File[] children = classesDirectory.listFiles();
			for (int i = 0; i < children.length; i++) {
				File file = children[i];
				if (isRimFile(file)) {
					getLog().info("Deleting file:" + file.getAbsolutePath());
					file.delete();
				}
			}
		}
	}

	private boolean isRimFile(File file) {

		if (isFileType(file, "cod") || isFileType(file, "jad")
				|| isFileType(file, "debug") || isFileType(file, "csl")) {
			return true;
		}
		return false;
	}

	private boolean isFileType(File file, String extension) {
		return file.getName().toLowerCase().endsWith("." + extension);
	}
}
