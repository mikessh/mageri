package com.milaboratory.migec2.pipeline;

import com.milaboratory.migec2.util.Basics;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MigecParameterSetTest {
    @Test
    public void serializationTest() throws IOException {
        MigecParameterSet migecParameterSet = new MigecParameterSet();
        Element e = migecParameterSet.toXml();
        Basics.printXml(e);
        Assert.assertEquals("(De)serialization successful", migecParameterSet, MigecParameterSet.fromXml(e));
    }

    @Test
    public void ioTest() throws IOException, JDOMException {
        MigecParameterSet migecParameterSet = new MigecParameterSet();

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        migecParameterSet.writeToStream(output);

        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        MigecParameterSet recoveredMigecParameterSet = MigecParameterSet.readFromStream(input);

        Basics.printXml(recoveredMigecParameterSet.toXml());

        Assert.assertEquals("Parameters I/O successful", migecParameterSet, recoveredMigecParameterSet);
    }
}
