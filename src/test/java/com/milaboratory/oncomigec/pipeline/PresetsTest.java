package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.misc.Basics;
import com.milaboratory.oncomigec.misc.testing.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PresetsTest {
    @Test
    public void serializationTest() throws IOException {
        Presets presets = Presets.DEFAULT;
        Element e = presets.toXml();
        Basics.printXml(e);
        Assert.assertEquals("XML (de)serialization successful", presets, Presets.fromXml(e));

        TestUtil.serializationCheck(presets);
    }

    @Test
    public void ioTest() throws IOException, JDOMException {
        Presets presets = Presets.DEFAULT;

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        presets.writeToStream(output);

        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        Presets recoveredPresets = Presets.readFromStream(input);

        Basics.printXml(recoveredPresets.toXml());

        Assert.assertEquals("Parameters I/O successful", presets, recoveredPresets);
    }
}
