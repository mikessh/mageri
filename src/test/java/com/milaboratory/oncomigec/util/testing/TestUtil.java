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
 * Last modified on 4.3.2015 by mikesh
 */

package com.milaboratory.oncomigec.util.testing;

import com.milaboratory.oncomigec.core.PipelineBlock;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestUtil {
    public static void serializationCheck(Serializable original) {
        byte[] data = SerializationUtils.serialize(original);
        assertTrue("Serialization successful", data.length > 0);

        Serializable recovered = SerializationUtils.deserialize(data);
        if (original instanceof PipelineBlock) {
            assertEquals("Plain-text data match", ((PipelineBlock) original).getBody(),
                    ((PipelineBlock) recovered).getBody());
        }
    }

    public static InputStream getResourceAsStream(String resourceName) throws IOException {
        InputStream is = TestUtil.class.getClassLoader().getResourceAsStream(resourceName);
        return resourceName.endsWith(".gz") ? new GZIPInputStream(is) : is;
    }
}
