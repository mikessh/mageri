/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

package com.milaboratory.oncomigec.pipeline.analysis;

import com.milaboratory.oncomigec.core.ReadSpecific;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleGroup implements Serializable, ReadSpecific {
    private final Project parent;
    private final boolean pairedEnd;
    private final String name;
    private final List<Sample> samples = new ArrayList<>();

    public SampleGroup(String name, boolean pairedEnd, Project parent) {
        this.name = name;
        this.pairedEnd = pairedEnd;
        this.parent = parent;
    }

    public Sample createSample(String name) {
        Sample sample = new Sample(name, this);
        samples.add(sample);
        parent.addSample(sample);
        return sample;
    }

    public Sample createSample() {
        Sample sample = new Sample(this);
        samples.add(sample);
        parent.addSample(sample);
        return sample;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return parent.getName() + "." + getName();
    }

    public List<Sample> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    public Project getParent() {
        return parent;
    }

    @Override
    public boolean isPairedEnd() {
        return pairedEnd;
    }
}
