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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import com.goodformobile.build.mobile.preprocessor.RIMFilePreprocessor;

/**
 * Goal which pre processes classes with RIM's preprocessor.  Supports ifdef, ifndef, the or operator, and else blocks.
 * 
 * @goal preprocess
 * 
 * @phase process-sources
 */
public class PreprocessClassesMojo extends AbstractRIMBuildMojo {

	/**
	 * The source directories containing the sources to be compiled.
	 * 
	 * @parameter default-value="${project.compileSourceRoots}"
	 * @required
	 * @readonly
	 */
	private List<String> compileSourceRoots;

	/**
	 * Preprocessor directives.
	 * 
	 * @parameter
	 */
	private List<String> preprocessorDefines;

	private Set<String> includes = new HashSet<String>();

	private Set<String> excludes = new HashSet<String>();

	public void execute() throws MojoExecutionException {

		getLog().info("PreprocessClassesMojo.execute()");

		// Create a directory to put all pre-processed classes.
		String targetPreprocessFolder = project.getBuild().getDirectory() + File.separator + ".preprocessed";
		FileUtils.mkdir(targetPreprocessFolder);

		List<String> compileSourceRoots = removeEmptyCompileSourceRoots(this.compileSourceRoots);

		if (compileSourceRoots.isEmpty()) {
			getLog().info("No sources to preprocess");

			return;
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug("Source directories: " + compileSourceRoots.toString().replace(',', '\n'));
			getLog().debug("Output directory: " + targetPreprocessFolder);
		}

		SourceInclusionScanner scanner = getSourceInclusionScanner();

		SourceMapping mapping = new SuffixMapping(".java", ".java");
		scanner.addSourceMapping(mapping);

		RIMFilePreprocessor rimFilePreprocessor = new RIMFilePreprocessor();
		for (String compileSourceRootString : compileSourceRoots) {
			File compileSourceRoot = new File(compileSourceRootString);
			getLog().info("Scanning Compile Source Root: " + compileSourceRootString);
			try {
				Set<File> filesToPreprocess = scanner.getIncludedSources(compileSourceRoot, new File(targetPreprocessFolder));
				String definesString = "";
				if (preprocessorDefines != null) {
					definesString = StringUtils.join(preprocessorDefines.iterator(), ",");
				}
				getLog().info("Found " + filesToPreprocess.size() + " in " + compileSourceRootString + ".  Using defines: " + definesString);
				for (File file : filesToPreprocess) {
					getLog().debug("Preprocessing:" + file.getAbsolutePath());
					try {
						String fileAfterSource = file.getAbsolutePath().substring(compileSourceRoot.getAbsolutePath().length());
						File output = new File(targetPreprocessFolder + File.separator + fileAfterSource);
						if (output.exists() && !output.delete()) {
							throw new MojoExecutionException("Unabled to delete:" + output.getAbsolutePath());
						}
						rimFilePreprocessor.preprocessFile(preprocessorDefines, file, output);
					} catch (Exception e) {
						throw new MojoExecutionException("Unable to preprocess File:" + file.getAbsolutePath(), e);
					}
				}
			} catch (InclusionScanException e) {
				throw new MojoExecutionException("Unable to scan for files to be preprocessed.", e);
			}
		}

		project.getCompileSourceRoots().clear();
		project.getCompileSourceRoots().add(targetPreprocessFolder);
	}

	protected SourceInclusionScanner getSourceInclusionScanner() {
		SourceInclusionScanner scanner = null;

		if (includes.isEmpty()) {
			includes.add("**/*.java");
		}
		getLog().info("Includes:" + includes + ", Excludes:" + excludes);
		scanner = new SimpleSourceInclusionScanner(includes, excludes);
		return scanner;
	}

	/**
	 * @todo also in ant plugin. This should be resolved at some point so that
	 *       it does not need to be calculated continuously - or should the
	 *       plugins accept empty source roots as is?
	 */
	private static List<String> removeEmptyCompileSourceRoots(List<String> compileSourceRootsList) {
		List<String> newCompileSourceRootsList = new ArrayList<String>();
		if (compileSourceRootsList != null) {
			// copy as I may be modifying it
			for (String srcDir : compileSourceRootsList) {
				if (!newCompileSourceRootsList.contains(srcDir) && new File(srcDir).exists()) {
					newCompileSourceRootsList.add(srcDir);
				}
			}
		}
		return newCompileSourceRootsList;
	}
}
