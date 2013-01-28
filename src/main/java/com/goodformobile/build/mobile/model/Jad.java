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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Jad {

    private List<JadProperty> properties = new ArrayList<JadProperty>();

    public void addProperty(JadProperty property) {
	properties.add(property);
    }

    public void parseFile(InputStream inputStream) throws IOException {
	BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	String line = reader.readLine();
	while (line != null) {

	    String property = line.substring(0, line.indexOf(":")).trim();
	    String value = line.substring(line.indexOf(":") + 1).trim();
	    JadProperty jadProperty = new JadProperty();
	    jadProperty.setName(property);
	    jadProperty.setValue(value);
	    addProperty(jadProperty);

	    line = reader.readLine();
	}
    }

    public void removeCodProperties() {
	Iterator<JadProperty> propertyIterator = properties.iterator();
	while (propertyIterator.hasNext()) {
	    JadProperty jadProperty = (JadProperty) propertyIterator.next();
	    if (jadProperty.getName().startsWith("RIM-COD-")) {
		propertyIterator.remove();
	    }
	}
    }

    public List<JadProperty> getPropertyList() {
	return properties;
    }

    public void writeJad(FileOutputStream output) throws IOException {
	for (JadProperty jadProperty : properties) {
	    output.write(jadProperty.getName().getBytes());
	    output.write(": ".getBytes());
	    output.write(jadProperty.getValue().getBytes());
	    output.write("\n".getBytes());
	}
	
    }
}
