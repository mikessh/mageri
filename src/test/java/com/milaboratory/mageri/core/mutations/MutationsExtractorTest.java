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

package com.milaboratory.mageri.core.mutations;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.mageri.FastTests;
import com.milaboratory.mageri.core.genomic.Reference;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.Set;

public class MutationsExtractorTest {
    protected static LocalAlignment align(NucleotideSequence reference, NucleotideSequence query) {
        return LocalAligner.align(AffineGapAlignmentScoring.getNucleotideBLASTScoring(),
                reference, query);
    }

    @Test
    @Category(FastTests.class)
    public void exactTest() {
        String ref = "ATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG";

        // Major mutations

        //            000000000011111111112222222222333333333344444444445555555555666666666677
        //            012345678901234567890123456789012345678901234567890123456789012345678901
        //              2A>G   9A>G(bad)        26ins AA      40C>T  47delAAA               70A>G(bad)
        String seq = "ATGGCAGAAGTAAAAGAAAAGATTGGAAAACTAGTCAGATAGTAGAAATAGAAAAGATTGGAACTAGTCGG";
        String qua = "HHHHHHHHH5HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH5H";

        Reference reference = new Reference(null, -1, "dummy", new NucleotideSequence(ref), null);
        NucleotideSequence query = new NucleotideSequence(seq);
        NucleotideSQPair consensus = new NucleotideSQPair(seq, qua);
        LocalAlignment alignment = align(reference.getSequence(), query);

        Set<String> expectedMajors = new HashSet<>(), observedMajors = new HashSet<>();
        expectedMajors.add("S2:A>G");
        expectedMajors.add("I26:>AA");
        expectedMajors.add("S40:C>T");
        expectedMajors.add("D47-50:AAA>");

        // Minor mutations

        // 3G>A 39G>A 58T>C
        String seq2 = "ATGACAGAAGTAAAAGAAAAGATTGGAAAACTAGTCAGATAATAGAAATAGAAAAGACTGGAACTAGTCGG";
        NucleotideSequence read = new NucleotideSequence(seq2);
        LocalAlignment alignmentQuery = align(query, read);

        Set<Integer> expectedMinors = new HashSet<>(), observedMinors = new HashSet<>();
        expectedMinors.add(Mutations.createSubstitution(3,
                NucleotideAlphabet.INSTANCE.codeFromSymbol('G'), NucleotideAlphabet.INSTANCE.codeFromSymbol('A')));
        expectedMinors.add(Mutations.createSubstitution(39,
                NucleotideAlphabet.INSTANCE.codeFromSymbol('G'), NucleotideAlphabet.INSTANCE.codeFromSymbol('A')));
        expectedMinors.add(Mutations.createSubstitution(58,
                NucleotideAlphabet.INSTANCE.codeFromSymbol('T'), NucleotideAlphabet.INSTANCE.codeFromSymbol('C')));

        for (int mutation : alignmentQuery.getMutations()) {
            observedMinors.add(mutation);
        }

        // Extract mutations

        MutationsExtractor mutationsExtractor = new MutationsExtractor(alignment,
                reference, consensus, observedMinors, (byte) 30, false);

        MutationArray mutationArray = mutationsExtractor.computeMajorMutations();
        observedMinors = mutationsExtractor.recomputeMinorMutations();

        // Check majors

        System.out.println("Checking majors");

        for (Mutation mutation : mutationArray.getMutations()) {
            observedMajors.add(mutation.toString());
        }

        for (String major : observedMajors) {
            System.out.println(major);
            Assert.assertTrue("Correct major observed", expectedMajors.contains(major));
        }

        for (String major : expectedMajors) {
            Assert.assertTrue("All majors observed", observedMajors.contains(major));
        }

        // Check minors

        System.out.println("Checking minors");

        for (int minor : observedMinors) {
            System.out.println(Mutations.toString(NucleotideAlphabet.INSTANCE, minor));
            Assert.assertTrue("Correct minor observed", expectedMinors.contains(minor));
        }

        for (int minor : expectedMinors) {
            Assert.assertTrue("All minor observed", observedMinors.contains(minor));
        }
    }
}
