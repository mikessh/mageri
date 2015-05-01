package com.milaboratory.oncomigec.core.mutations;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.AffineGapAlignmentScoring;
import com.milaboratory.core.sequence.alignment.LocalAligner;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.core.sequence.nucleotide.NucleotideSequence;
import com.milaboratory.oncomigec.core.genomic.Reference;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class MutationsExtractorTest {
    protected static LocalAlignment align(NucleotideSequence reference, NucleotideSequence query) {
        return LocalAligner.align(AffineGapAlignmentScoring.getNucleotideBLASTScoring(),
                reference, query);
    }

    @Test
    public void exactTest() {
        String ref = "ATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAGATAGCAGAAATAAAAGAAAAGATTGGAACTAGTCAG";

        // Major mutations

        //            000000000011111111112222222222333333333344444444445555555555666666666677
        //            012345678901234567890123456789012345678901234567890123456789012345678901
        //              2A>G   9A>G(bad)        26ins AA      40C>T  47delAAA               70A>G(bad)
        String seq = "ATGGCAGAAGTAAAAGAAAAGATTGGAAAACTAGTCAGATAGTAGAAATAGAAAAGATTGGAACTAGTCGG";
        String qua = "HHHHHHHHH5HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH5H";

        Reference reference = new Reference(null, -1, "dummy", new NucleotideSequence(ref));
        NucleotideSequence query = new NucleotideSequence(seq);
        NucleotideSQPair consensus = new NucleotideSQPair(seq, qua);
        LocalAlignment alignment = align(reference.getSequence(), query);

        Set<String> expectedMajors = new HashSet<>(), observedMajors = new HashSet<>();
        expectedMajors.add("S2-3:A>G");
        expectedMajors.add("I26-26:>AA");
        expectedMajors.add("S40-41:C>T");
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
