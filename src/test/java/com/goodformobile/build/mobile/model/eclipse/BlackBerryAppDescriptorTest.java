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
package com.goodformobile.build.mobile.model.eclipse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.plexus.util.IOUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.goodformobile.build.mobile.model.ApplicationType;
import com.goodformobile.build.mobile.model.eclipse.AlternateEntryPoint;
import com.goodformobile.build.mobile.model.eclipse.BlackBerryAppDescriptor;
import com.goodformobile.build.mobile.model.eclipse.IconDescriptor;
import com.goodformobile.build.mobile.model.eclipse.PreprocessorTag;

public class BlackBerryAppDescriptorTest {

	@Test
	public void testLibraryIsSerialized() throws Exception {

		BlackBerryAppDescriptor blackBerryAppDescriptor = new BlackBerryAppDescriptor();
		blackBerryAppDescriptor.generalProperties.setTitle("Sample Library");
		blackBerryAppDescriptor.generalProperties.setVersion("1.0.0");
		blackBerryAppDescriptor.generalProperties.setVendor("Good For Mobile");
		blackBerryAppDescriptor.generalProperties.setDescription("");

		blackBerryAppDescriptor.applicationProperties.applicationType = ApplicationType.LIBRARY;
		blackBerryAppDescriptor.applicationProperties.systemModule = true;
		blackBerryAppDescriptor.applicationProperties.autoRunOnStartup = false;

		blackBerryAppDescriptor.resourceProperties.hasTitleResource = false;

		blackBerryAppDescriptor.resourceProperties.iconDescriptors.addIconDescriptor(new IconDescriptor("src/main/resources/img/icon.png", false));
		blackBerryAppDescriptor.resourceProperties.iconDescriptors.addIconDescriptor(new IconDescriptor("src/main/resources/img/iconr.png", true));

		blackBerryAppDescriptor.packagingOptions.outputFileName = "sample_application";
		blackBerryAppDescriptor.packagingOptions.outputFolder = "deliverables";
		blackBerryAppDescriptor.packagingOptions.generateALXFile = true;

		assertSerializedXmlEqualsExpectedFile(blackBerryAppDescriptor, "library_BlackBerry_App_Descriptor.xml");
	}

	@Test
	public void testApplicationIsSerialized() throws Exception {

		BlackBerryAppDescriptor blackBerryAppDescriptor = new BlackBerryAppDescriptor();
		blackBerryAppDescriptor.generalProperties.setTitle("Sample Application");
		blackBerryAppDescriptor.generalProperties.setVersion("1.0.0");
		blackBerryAppDescriptor.generalProperties.setVendor("Good For Mobile");
		blackBerryAppDescriptor.generalProperties.setDescription("A Sample Application for a JUnit Test");

		blackBerryAppDescriptor.applicationProperties.applicationType = ApplicationType.BLACKBERRY_APPLICATION;
		blackBerryAppDescriptor.applicationProperties.mainArguments = "thats some argument";
		blackBerryAppDescriptor.applicationProperties.systemModule = false;
		blackBerryAppDescriptor.applicationProperties.autoRunOnStartup = false;

		blackBerryAppDescriptor.resourceProperties.hasTitleResource = true;
		blackBerryAppDescriptor.resourceProperties.titleResourceBundleName = "myresourcefile";
		blackBerryAppDescriptor.resourceProperties.titleResourceRelativePath = "src/main/java/com/goodformobile/sample/myresourcefile.rrh";
		blackBerryAppDescriptor.resourceProperties.titleResourceBundleClassName = "com.goodformobile.sample.myresourcefile";
		blackBerryAppDescriptor.resourceProperties.applicationTitleBundleKey = "TEST_NAME";
		blackBerryAppDescriptor.resourceProperties.applicationDescriptionBundleKey = "TEST_DESCRIPTION";

		blackBerryAppDescriptor.resourceProperties.iconDescriptors.addIconDescriptor(new IconDescriptor("src/main/resources/img/icon.png", false));

		blackBerryAppDescriptor.compileOptions.outputCompilerMessages = true;
		blackBerryAppDescriptor.compileOptions.convertImages = true;
		blackBerryAppDescriptor.compileOptions.createWarningForNoExportedRoutine = true;
		blackBerryAppDescriptor.compileOptions.compressResources = true;

		List<PreprocessorTag> preProcessorDefines = blackBerryAppDescriptor.compileOptions.preprocessorDefines.preprocessorDefines;
		addPreProcessorDefine(preProcessorDefines, true, "JDE_4_7_0");
		addPreProcessorDefine(preProcessorDefines, true, "TEST3");
		addPreProcessorDefine(preProcessorDefines, true, "TEST4");
		addPreProcessorDefine(preProcessorDefines, true, "TEST_PRE_PROCESSOR_2");

		blackBerryAppDescriptor.packagingOptions.outputFileName = "sample_application";
		blackBerryAppDescriptor.packagingOptions.outputFolder = "deliverables";
		blackBerryAppDescriptor.packagingOptions.generateALXFile = true;

		AlternateEntryPoint alternateEntryPoint = new AlternateEntryPoint();
		alternateEntryPoint.title = "AlternateEntryPointTitle1";
		alternateEntryPoint.mainMidletName = "";
		alternateEntryPoint.argumentsForMain = "alternateEntryPointArgument1";
		alternateEntryPoint.homeScreenPosition = 3;
		alternateEntryPoint.systemModule = false;
		alternateEntryPoint.autoRunOnStartup = true;
		alternateEntryPoint.hasTitleResource = true;
		alternateEntryPoint.titleResourceBundleKey = "TEST_KEY";
		alternateEntryPoint.titleResourceBundleName = "myresourcefile";
		alternateEntryPoint.titleResourceBundleClassName = "com.goodformobile.sample.myresourcefile";
		alternateEntryPoint.titleResourceBundleRelativePath = "src/main/java/com/goodformobile/sample/myresourcefile.rrh";
		alternateEntryPoint.iconDescriptors.addIconDescriptor(new IconDescriptor("src/main/resources/img/icon.png", false));

		blackBerryAppDescriptor.getAlternateEntryPoints().alternateEntryPoints.add(alternateEntryPoint);

		AlternateEntryPoint secondAlternateEntryPoint = new AlternateEntryPoint();
		secondAlternateEntryPoint.title = "AlternateEntryPointTitle2";
		secondAlternateEntryPoint.mainMidletName = "";
		secondAlternateEntryPoint.systemModule = true;
		secondAlternateEntryPoint.autoRunOnStartup = false;

		blackBerryAppDescriptor.getAlternateEntryPoints().alternateEntryPoints.add(secondAlternateEntryPoint);

		assertSerializedXmlEqualsExpectedFile(blackBerryAppDescriptor, "application_BlackBerry_App_Descriptor.xml");
	}

	@Test
	public void testApplicationIsDeserialized() throws Exception {

		JAXBContext jaxbContext = JAXBContext.newInstance(BlackBerryAppDescriptor.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

		Object result = unmarshaller.unmarshal(this.getClass().getResourceAsStream("application_BlackBerry_App_Descriptor.xml"));
		BlackBerryAppDescriptor blackBerryAppDescriptor = (BlackBerryAppDescriptor) result;

		assertNotNull(blackBerryAppDescriptor);

		assertNotNull(blackBerryAppDescriptor.generalProperties);
		assertEquals("Sample Application", blackBerryAppDescriptor.generalProperties.getTitle());

		assertSerializedXmlEqualsExpectedFile(blackBerryAppDescriptor, "application_BlackBerry_App_Descriptor.xml");
	}

	private void addPreProcessorDefine(List<PreprocessorTag> preProcessorDefines, boolean active, String defineValue) {
		PreprocessorTag preprocessorTag = new PreprocessorTag();
		preprocessorTag.active = active;
		preprocessorTag.preprocessorDefine = defineValue;
		preProcessorDefines.add(preprocessorTag);
	}

	private void assertSerializedXmlEqualsExpectedFile(BlackBerryAppDescriptor blackBerryAppDescriptor, String expectedFileName) throws JAXBException,
			PropertyException, IOException, SAXException {
		JAXBContext jaxbContext = JAXBContext.newInstance(BlackBerryAppDescriptor.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		Writer writer = new StringWriter();
		marshaller.marshal(blackBerryAppDescriptor, writer);

		String expectedXml = IOUtil.toString(this.getClass().getResourceAsStream(expectedFileName));
		XMLUnit.setIgnoreWhitespace(true);
		XMLAssert.assertXMLEqual(expectedXml, writer.toString());
	}
}
