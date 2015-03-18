/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified on 17.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.pipeline.input;

import java.io.InputStream;
import java.io.Serializable;

public class InputStreamWrapper implements Serializable {
    private final String protocol;
    private final String path;
    private transient final InputStream inputStream;

    public InputStreamWrapper(String protocol, String path, InputStream inputStream) {
        this.protocol = protocol;
        this.path = path;
        this.inputStream = inputStream;
    }

    public String getFullPath() {
        return protocol + "://" + path;

    }

    public String getProtocol() {
        return protocol;
    }

    public String getPath() {
        return path;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
