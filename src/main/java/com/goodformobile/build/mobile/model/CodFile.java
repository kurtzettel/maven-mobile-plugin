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

public class CodFile {

    private String codName;
    private long codSize;
    private String sha;
    private long createTime;

    public String getCodName() {
	return codName;
    }

    public void setCodName(String codName) {
	this.codName = codName;
    }

    public long getCodSize() {
	return codSize;
    }

    public void setCodSize(long codSize) {
	this.codSize = codSize;
    }

    public String getSha() {
	return sha;
    }

    public void setSha(String sha) {
	this.sha = sha;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
