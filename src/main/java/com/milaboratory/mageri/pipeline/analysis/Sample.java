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

import com.milaboratory.mageri.core.ReadSpecific;

import java.io.Serializable;

public class Sample implements Comparable<Sample>, Serializable, ReadSpecific {
    private final SampleGroup parent;
    private final String name;
    private int id = -1;
    private final boolean submultiplexed;

    public static Sample create(String sampleName, boolean pairedEnd, String groupName, String projectName) {
        return new Project(projectName).createSampleGroup(groupName, pairedEnd).createSample(sampleName);
    }

    public static Sample create(String sampleName, boolean pairedEnd) {
        return create(sampleName, pairedEnd, "dummy", "dummy");
    }

    public Sample(String name, SampleGroup parent) {
        this.name = name;
        this.parent = parent;
        this.submultiplexed = true;
    }

    protected Sample(SampleGroup parent) {
        this.name = null;
        this.parent = parent;
        this.submultiplexed = false;
    }

    public SampleGroup getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return submultiplexed ? name : parent.getName();
    }

    public String getGroupName() {
        return parent.getName();
    }

    public String getProjectName() {
        return parent.getParent().getName();
    }

    public String getFullName() {
        return parent.getFullName() + (submultiplexed ? ("." + name) : "");
    }

    public boolean isSubmultiplexed() {
        return submultiplexed;
    }

    @Override
    public int compareTo(Sample o) {
        return this.getFullName().compareTo(o.getFullName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        if (!getFullName().equals(sample.getFullName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getFullName().hashCode();
    }

    @Override
    public boolean isPairedEnd() {
        return parent.isPairedEnd();
    }
}
