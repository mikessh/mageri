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
 * Last modified on 15.3.2015 by mikesh
 */

package sample

@Grab(group = 'com.milaboratory', module = 'micommons', version = '1.1-SNAPSHOT')

import com.milaboratory.core.sequence.quality.QualityFormat
import com.milaboratory.core.sequencing.io.fastq.SFastqReader
import com.milaboratory.core.sequencing.io.fastq.SFastqWriter
import com.milaboratory.core.sequencing.read.SSequencingReadImpl
import com.milaboratory.util.CompressionType

/**
 * Created by mikesh on 3/15/15.
 */


def reader = new SFastqReader("${args[0]}.fastq.gz"),
    writer = new SFastqWriter("${args[0]}_RC.fastq.gz", QualityFormat.Phred33, CompressionType.GZIP)

def read
int i = 0
while ((read = reader.take()) != null) {
    writer.write(new SSequencingReadImpl(read.description, read.getData().getRC(), i++))
}
writer.close()