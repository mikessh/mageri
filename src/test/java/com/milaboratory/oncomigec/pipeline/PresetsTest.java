package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.util.Basics;
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
        Presets presets = new Presets();
        Element e = presets.toXml();
        Basics.printXml(e);
        Assert.assertEquals("(De)serialization successful", presets, Presets.fromXml(e));
    }

    @Test
    public void ioTest() throws IOException, JDOMException {
        Presets presets = new Presets();

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        presets.writeToStream(output);

        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        Presets recoveredPresets = Presets.readFromStream(input);

        Basics.printXml(recoveredPresets.toXml());

        Assert.assertEquals("Parameters I/O successful", presets, recoveredPresets);
    }
}
