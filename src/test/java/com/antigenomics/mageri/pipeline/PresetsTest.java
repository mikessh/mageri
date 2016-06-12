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

package com.antigenomics.mageri.pipeline;

import com.antigenomics.mageri.TestUtil;
import com.antigenomics.mageri.FastTests;
import com.antigenomics.mageri.misc.Basics;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PresetsTest {
    @Test
    @Category(FastTests.class)
    public void serializationTest() throws IOException {
        Presets presets = Presets.DEFAULT;
        Element e = presets.toXml();
        Basics.printXml(e);
        Assert.assertEquals("XML (de)serialization successful", presets, Presets.fromXml(e));

        TestUtil.serializationCheck(presets);
    }

    @Test
    @Category(FastTests.class)
    public void ioTest() throws IOException, JDOMException {
        Presets presets = Presets.DEFAULT;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        presets.writeToStream(output);

        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        Presets recoveredPresets = Presets.readFromStream(input);

        Basics.printXml(recoveredPresets.toXml());
        Basics.printXml(presets.toXml());

        Assert.assertEquals("Parameters I/O successful", presets, recoveredPresets);
    }
}
