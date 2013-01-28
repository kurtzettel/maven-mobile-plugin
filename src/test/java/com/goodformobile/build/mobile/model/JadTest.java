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
package com.goodformobile.build.mobile.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class JadTest {

    @Test
    public void testParseJad() throws IOException {
	String jadFileName = "/projects/app-with-dependency/deliverables/Standard/4.5.0/library_project_1.jad";
	InputStream inputStream = JadTest.class.getResourceAsStream(jadFileName);
	Jad jad = new Jad();
	jad.parseFile(inputStream);
	
	assertNotNull(jad.getPropertyList());
	assertTrue(jad.getPropertyList().size() > 0);
	assertNotNull(jad.getPropertyList().get(0));
    }

}
