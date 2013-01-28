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
package com.goodformobile.build.mobile.preprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

public class RIMFilePreprocessorTest {

	@Test
	public void testPreprocessFile() throws Exception {
		File nonPreprocessedResource = getRelativeFile("com/goodformobile/build/mobile/preprocess-test1.txt");
		File nonPreprocessedResource2 = getRelativeFile("com/goodformobile/build/mobile/preprocess-test2.txt");

		RIMFilePreprocessor preprocessor = new RIMFilePreprocessor();

		// Setup a working directory.
		File workProjectDirectory = new File(PlexusTestCase.getBasedir(), "target");

		List<String> variables = new ArrayList<String>();
		variables.add("VARIABLE1");
		File processedResultFile = new File(workProjectDirectory, "processed");
		preprocessor.preprocessFile(variables, nonPreprocessedResource, processedResultFile);
		File expectedResultFile = getRelativeFile("com/goodformobile/build/mobile/preprocess-result1.txt");
		String expectedResult = FileUtils.fileRead(expectedResultFile);
		String actualResult = FileUtils.fileRead(processedResultFile);
		assertEquals(expectedResult, actualResult);
		processedResultFile.delete();

		preprocessor.preprocessFile(new ArrayList<String>(), nonPreprocessedResource, processedResultFile);
		expectedResultFile = getRelativeFile("com/goodformobile/build/mobile/preprocess-result1-2.txt");
		expectedResult = FileUtils.fileRead(expectedResultFile);
		actualResult = FileUtils.fileRead(processedResultFile);
		assertEquals(expectedResult, actualResult);
		processedResultFile.delete();

		// Test ifndef and else
		preprocessor.preprocessFile(variables, nonPreprocessedResource2, processedResultFile);
		expectedResultFile = getRelativeFile("com/goodformobile/build/mobile/preprocess-result1-2.txt");
		expectedResult = FileUtils.fileRead(expectedResultFile);
		actualResult = FileUtils.fileRead(processedResultFile);
		assertEquals(expectedResult, actualResult);
		processedResultFile.delete();

		preprocessor.preprocessFile(new ArrayList<String>(), nonPreprocessedResource2, processedResultFile);
		expectedResultFile = getRelativeFile("com/goodformobile/build/mobile/preprocess-result1.txt");
		expectedResult = FileUtils.fileRead(expectedResultFile);
		actualResult = FileUtils.fileRead(processedResultFile);
		assertEquals(expectedResult, actualResult);
		processedResultFile.delete();
	}

	@Test
	public void testPreprocessHandlesOrs() throws Exception {
		
		File nonPreprocessedResource = getRelativeFile("com/goodformobile/build/mobile/preprocess-test3.txt");

		RIMFilePreprocessor preprocessor = new RIMFilePreprocessor();

		// Setup a working directory.
		File workProjectDirectory = new File(PlexusTestCase.getBasedir(), "target");

		List<String> variables = new ArrayList<String>();
		variables.add("VARIABLE1");
		File processedResultFile = new File(workProjectDirectory, "processed");
		preprocessor.preprocessFile(variables, nonPreprocessedResource, processedResultFile);
		File expectedResultFile = getRelativeFile("com/goodformobile/build/mobile/preprocess-result3.txt");
		String expectedResult = FileUtils.fileRead(expectedResultFile);
		String actualResult = FileUtils.fileRead(processedResultFile);
		assertEquals(expectedResult, actualResult);
		processedResultFile.delete();
		
	}
	
	@Test
	public void testPreprocessShouldNotCauseNPE() throws Exception {
		
		File nonPreprocessedResource = getRelativeFile("com/goodformobile/build/mobile/RatingFieldPreProcessor.test");

		RIMFilePreprocessor preprocessor = new RIMFilePreprocessor();

		// Setup a working directory.
		File workProjectDirectory = new File(PlexusTestCase.getBasedir(), "target");

		List<String> variables = new ArrayList<String>();
		variables.add("VARIABLE1");
		File processedResultFile = new File(workProjectDirectory, "processed");
		preprocessor.preprocessFile(variables, nonPreprocessedResource, processedResultFile);
		
	}
	
	
	private File getRelativeFile(String path) throws URISyntaxException {
		URL url = getClass().getClassLoader().getResource(path);
		File pluginConfig = new File(url.toURI());
		return pluginConfig;
	}

}
