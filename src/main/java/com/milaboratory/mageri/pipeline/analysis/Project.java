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

package com.milaboratory.mageri.pipeline.analysis;

import com.milaboratory.mageri.pipeline.input.Input;
import com.milaboratory.mageri.pipeline.input.InputChunk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Project structure that could be deduced from input
public class Project implements Serializable {
    private final List<SampleGroup> sampleGroups = new ArrayList<>();
    private final List<Sample> samples = new ArrayList<>();
    private final String name;

    public static Project fromInput(Input input) {
        Project project = new Project(input.getProjectName());

        for (InputChunk inputChunk : input.getInputChunks()) {
            SampleGroup group = project.createSampleGroup(inputChunk.getName(),
                    inputChunk.isPairedEnd());
            if (inputChunk.getCheckoutRule().hasSubMultiplexing()) {
                for (String sampleName : inputChunk.getCheckoutRule().getSampleNames()) {
                    group.createSample(sampleName);
                }
            } else {
                group.createSample();
            }
        }

        return project;
    }

    public Project(String name) {
        this.name = name;
    }

    protected void addSample(Sample sample) {
        sample.setId(samples.size());
        samples.add(sample);
    }

    public SampleGroup createSampleGroup(String name, boolean pairedEnd) {
        SampleGroup sampleGroup = new SampleGroup(name, pairedEnd, this);
        sampleGroups.add(sampleGroup);
        return sampleGroup;
    }

    public String getName() {
        return name;
    }

    public List<Sample> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    public List<SampleGroup> getSampleGroups() {
        return Collections.unmodifiableList(sampleGroups);
    }

    public Sample getSample(int id) {
        return samples.get(id);
    }
}