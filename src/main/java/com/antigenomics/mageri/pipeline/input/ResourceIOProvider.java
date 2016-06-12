/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.pipeline.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class ResourceIOProvider extends IOProvider {
    public static final ResourceIOProvider INSTANCE = new ResourceIOProvider();

    private ResourceIOProvider() {
        super("resources");
    }

    @Override
    public InputStream getStream(String path) throws IOException {
        InputStream is = ResourceIOProvider.class.getClassLoader().getResourceAsStream(path);
        return path.endsWith(".gz") ? new GZIPInputStream(is) : is;
    }
}
