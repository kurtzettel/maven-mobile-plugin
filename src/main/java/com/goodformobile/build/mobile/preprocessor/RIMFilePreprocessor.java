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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

public class RIMFilePreprocessor {

	public void preprocessFile(List<String> preprocessDirectives, File originalFile, File targetFile) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(originalFile));
		try {
			String firstLine = reader.readLine();
			if (firstLine != null && firstLine.trim().startsWith("//#preprocess")) {
				// We should re-write this file.
				File newFile = targetFile;
				if (!targetFile.getParentFile().exists()) {
					FileUtils.mkdir(targetFile.getParentFile().getAbsolutePath());
				}
				if (targetFile.exists() && !targetFile.delete()) {
					throw new IllegalArgumentException("Unable to delete file:" + newFile.getCanonicalPath());
				}
				boolean fileCreated = targetFile.createNewFile();
				if (!fileCreated) {
					throw new IllegalArgumentException("Unable to create file:" + newFile.getCanonicalPath());
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
				try {
					writer.write("\r\n");
					String line;

					boolean inIf = false;
					boolean inElse = false;
					boolean result = false;

					while ((line = reader.readLine()) != null) {

						if (line.trim().startsWith("//#")) {
							String command = line.trim().substring(3);
							command = command.trim();
							String condition = command;
							String variable = "";
							if (command.contains(" ")) {
								String[] commands = command.split(" ");
								condition = commands[0].trim();
								StringBuffer variableStringBuffer = new StringBuffer();
								for (int i = 1; i < commands.length; i++) {
									variableStringBuffer.append(commands[i]);
								}
								variable = variableStringBuffer.toString().trim();
							}
							if ("ifdef".equals(condition)) {
								inIf = true;
								if (variableMatchesDirective(preprocessDirectives, variable)) {
									// In True
									result = true;
								} else {
									result = false;
								}
							} else if ("ifndef".equals(condition)) {
								inIf = true;
								if (variableMatchesDirective(preprocessDirectives, variable)) {
									result = false;
								} else {
									result = true;
								}
							} else if ("else".equals(condition)) {
								inIf = false;
								inElse = true;
							} else if ("endif".equals(condition)) {
								inIf = false;
								inElse = false;
							}
							writer.write("\r\n");
						} else if (!inIf && !inElse || inIf && result || inElse && !result) {
							writer.write(line);
							writer.write("\r\n");
						} else {
							writer.write("\r\n");
						}
					}
				} finally {
					writer.close();
				}
			} else {
				FileUtils.copyFile(originalFile, targetFile);
			}
		} finally {
			reader.close();
		}
	}

	private boolean variableMatchesDirective(List<String> preprocessDirectives, String variable) {

		if (preprocessDirectives != null) {
			String[] variables = variable.split("\\|");
			for (String splitVariable : variables) {
				splitVariable = splitVariable.trim();
				if (preprocessDirectives.contains(splitVariable)) {
					return true;
				}
			}
		}
		return false;
	}
}
