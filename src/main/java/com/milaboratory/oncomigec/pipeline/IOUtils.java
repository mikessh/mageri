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

package com.milaboratory.oncomigec.pipeline;

import com.milaboratory.oncomigec.core.io.misc.UmiHistogram;
import com.milaboratory.oncomigec.preproc.demultiplex.processor.CheckoutProcessor;
import org.apache.commons.io.FileUtils;

import java.io.*;

public class IOUtils {
    /*
     * Base IO routines
     */
    public static void writeStringToFile(File file, String string) throws IOException {
        // in case someone want to concatenate the output later
        FileUtils.writeStringToFile(file, string.endsWith("\n") ? string : (string + "\n"));
    }

    public static void writeObjectToStream(OutputStream os, Serializable object) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(object);
        out.close();
    }

    public static void writeObjectToFile(File file, Serializable object) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        writeObjectToStream(fileOut, object);
    }

    private static Object readObjectFromStream(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(is);
        Object object = in.readObject();
        in.close();
        is.close();
        return object;
    }

    /*
     * Object IO
     */
    public static CheckoutProcessor readCheckoutProcessorData(InputStream is) throws IOException, ClassNotFoundException {
        Object object = readObjectFromStream(is);
        if (!(object instanceof CheckoutProcessor))
            throw new RuntimeException("File doesn't contain a serialized CheckoutProcessor");
        return (CheckoutProcessor) object;
    }

    public static CheckoutProcessor readCheckoutProcessorData(File file) throws IOException, ClassNotFoundException {
        return readCheckoutProcessorData(new FileInputStream(file));
    }

    public static UmiHistogram readUmiHistogramData(InputStream is) throws IOException, ClassNotFoundException {
        Object object = readObjectFromStream(is);
        if (!(object instanceof UmiHistogram))
            throw new RuntimeException("File doesn't contain a serialized UmiHistogram");
        return (UmiHistogram) object;
    }

    public static UmiHistogram readUmiHistogramData(File file) throws IOException, ClassNotFoundException {
        return readUmiHistogramData(new FileInputStream(file));
    }
}
