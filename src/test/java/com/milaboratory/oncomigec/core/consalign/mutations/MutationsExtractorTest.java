package com.milaboratory.oncomigec.core.consalign.mutations;

import com.milaboratory.core.sequence.NucleotideSQPair;
import com.milaboratory.core.sequence.alignment.KAligner;
import com.milaboratory.core.sequence.alignment.KAlignerParameters;
import com.milaboratory.core.sequence.alignment.KAlignmentHit;
import com.milaboratory.core.sequence.alignment.LocalAlignment;
import com.milaboratory.core.sequence.mutations.MutationType;
import com.milaboratory.core.sequence.mutations.Mutations;
import com.milaboratory.core.sequence.nucleotide.NucleotideAlphabet;
import com.milaboratory.oncomigec.core.align.entity.SAlignmentResult;
import com.milaboratory.oncomigec.core.align.processor.aligners.ExtendedExomeAligner;
import com.milaboratory.oncomigec.core.assemble.entity.SConsensus;
import com.milaboratory.oncomigec.core.assemble.processor.SAssembler;
import com.milaboratory.oncomigec.core.consalign.misc.ConsensusAlignerParameters;
import com.milaboratory.oncomigec.core.genomic.ReferenceLibrary;
import com.milaboratory.oncomigec.core.io.entity.SMig;
import com.milaboratory.oncomigec.util.Basics;
import com.milaboratory.oncomigec.util.Util;
import com.milaboratory.oncomigec.util.testing.generators.GeneratorMutationModel;
import com.milaboratory.oncomigec.util.testing.generators.RandomMigGenerator;
import com.milaboratory.oncomigec.util.testing.generators.RandomReferenceGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class MutationsExtractorTest {
    private final int nRepetiotions1 = 10, nRepetiotions2 = 20, nReferences = 50;
    private final int majorOverlapThreshold = 90, minorOverlapThreshold = 60,
            majorDeltaThreshold = -5, minorDeltaThreshold = -5;

    @Test
    public void randomizedConsistencyIndelExomeTest() {
        SAssembler assembler = new SAssembler();

        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        RandomMigGenerator migGenerator = new RandomMigGenerator();
        for (int i = 0; i < nRepetiotions1; i++) {
            ReferenceLibrary library = referenceGenerator.nextReferenceLibrary(nReferences);
            ExtendedExomeAligner aligner = new ExtendedExomeAligner(library);

            for (int j = 0; j < nRepetiotions2; j++) {
                RandomReferenceGenerator.ParentChildPair parentChildPair =
                        referenceGenerator.nextParentChildPair(library);
                RandomMigGenerator.RandomMigGeneratorResult rndMig =
                        migGenerator.nextMig(parentChildPair.getChildSequence());

                SConsensus consensus = assembler.assemble(rndMig.getMig());
                if (consensus != null) {
                    SAlignmentResult result = aligner.align(consensus.getConsensusSQPair().getSequence());

                    if (result != null) {
                        MutationsExtractor mutationsExtractor = new MutationsExtractor(result.getAlignment(),
                                result.getReference(), consensus,
                                // Do not filter minors and majors, we're going to check them all later
                                ConsensusAlignerParameters.DEFAULT);

                        int[] observedMajors = mutationsExtractor.calculateMajorMutations().getMutationCodes(),
                                expectedMajors = parentChildPair.getMutations();

                        boolean goodMajors = checkMutations(observedMajors);

                        if (!goodMajors) {
                            System.out.println("MAJORS:");
                            String majorStringObs = "OBS", majorStringExp = "EXP";
                            for (int majorObs : observedMajors)
                                majorStringObs += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, majorObs);
                            System.out.println(majorStringObs);
                            for (int majorExp : expectedMajors)
                                majorStringExp += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, majorExp);
                            System.out.println(majorStringExp);
                            System.out.println();
                        }

                        Assert.assertTrue("No bad major mutation codes generated", goodMajors);

                        Set<Integer> observedMinors = mutationsExtractor.calculateMinorMutations().getCodes(),
                                expectedMinors = rndMig.getMinorMutationCounts().keySet();

                        boolean goodMinors = checkMutations(observedMinors);

                        if (!goodMinors) {
                            System.out.println("MINORS");
                            String minorStringObs = "OBS", minorStringExp = "EXP";
                            for (int minorObs : observedMinors)
                                minorStringObs += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, minorObs);
                            System.out.println(minorStringObs);
                            for (int minorExp : expectedMinors)
                                minorStringExp += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, minorExp);
                            System.out.println(minorStringExp);
                            System.out.println();
                        }

                        Assert.assertTrue("No bad minor mutation codes generated", goodMinors);
                    }
                }
            }
        }
    }

    @Test
    public void randomizedNoIndelExomeTest() {
        SAssembler assembler = new SAssembler();

        RandomReferenceGenerator referenceGenerator = new RandomReferenceGenerator();
        RandomMigGenerator migGenerator = new RandomMigGenerator();
        migGenerator.setGeneratorMutationModel(GeneratorMutationModel.NO_INDEL);

        double meanMajorOverlap = 0, meanMinorOverlap = 0,
                meanMajorDelta = 0, meanMinorDelta = 0;

        int failedAlignments = 0;

        for (int i = 0; i < nRepetiotions1; i++) {
            ReferenceLibrary library = referenceGenerator.nextReferenceLibrary(nReferences);
            ExtendedExomeAligner aligner = new ExtendedExomeAligner(library);

            for (int j = 0; j < nRepetiotions2; j++) {
                RandomReferenceGenerator.ParentChildPair parentChildPair =
                        referenceGenerator.nextParentChildPair(library);
                RandomMigGenerator.RandomMigGeneratorResult rndMig =
                        migGenerator.nextMig(parentChildPair.getChildSequence());

                SConsensus consensus = assembler.assemble(rndMig.getMig());
                if (consensus != null) {
                    SAlignmentResult result = aligner.align(consensus.getConsensusSQPair().getSequence());

                    if (result != null) {
                        MutationsExtractor mutationsExtractor = new MutationsExtractor(result.getAlignment(),
                                result.getReference(), consensus,
                                ConsensusAlignerParameters.NO_FILTER);

                        int[] observedMajors = mutationsExtractor.calculateMajorMutations().getMutationCodes(),
                                expectedMajors = parentChildPair.getMutations();

                        boolean goodMajors = checkMutations(observedMajors);

                        if (!goodMajors) {
                            System.out.println("MAJORS:");
                            String majorStringObs = "OBS", majorStringExp = "EXP";
                            for (int majorObs : observedMajors)
                                majorStringObs += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, majorObs);
                            System.out.println(majorStringObs);
                            for (int majorExp : expectedMajors)
                                majorStringExp += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, majorExp);
                            System.out.println(majorStringExp);
                            System.out.println();
                        }

                        Assert.assertTrue("No bad major mutation codes generated", goodMajors);

                        int majorOverlap = overlapSize(observedMajors, expectedMajors);
                        meanMajorOverlap += overlap(majorOverlap, observedMajors.length,
                                expectedMajors.length);
                        meanMajorDelta += delta(observedMajors.length, expectedMajors.length);

                        Set<Integer> observedMinors = mutationsExtractor.calculateMinorMutations().getCodes(),
                                expectedMinors = rndMig.getMinorMutationCounts().keySet();

                        boolean goodMinors = checkMutations(observedMinors);

                        if (!goodMinors) {
                            System.out.println("MINORS");
                            String minorStringObs = "OBS", minorStringExp = "EXP";
                            for (int minorObs : observedMinors)
                                minorStringObs += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, minorObs);
                            System.out.println(minorStringObs);
                            for (int minorExp : expectedMinors)
                                minorStringExp += "\t" + Mutations.toString(NucleotideAlphabet.INSTANCE, minorExp);
                            System.out.println(minorStringExp);
                            System.out.println();
                        }

                        Assert.assertTrue("No bad minor mutation codes generated", goodMinors);

                        int minorOverlap = overlapSize(observedMinors, expectedMinors);
                        meanMinorOverlap += overlap(minorOverlap, observedMinors.size(),
                                expectedMinors.size());
                        meanMinorDelta += delta(observedMinors.size(), expectedMinors.size());
                    } else
                        failedAlignments++;
                }
            }
        }

        int nRepetiotions = nRepetiotions1 * nRepetiotions2 - failedAlignments;
        meanMajorOverlap /= nRepetiotions;
        meanMinorOverlap /= nRepetiotions;
        meanMajorDelta /= nRepetiotions;
        meanMinorDelta /= nRepetiotions;

        System.out.println("Majors: mean overlap=" + Basics.percent(meanMajorOverlap) + "%, mean delta=" +
                Basics.percent(meanMajorDelta) + "%");
        Assert.assertTrue("Good overlap of majors", Basics.percent(meanMajorOverlap) >= majorOverlapThreshold &&
                Basics.percent(meanMajorDelta) >= majorDeltaThreshold);
        System.out.println("Minors: mean overlap=" + Basics.percent(meanMinorOverlap) + "%, mean delta=" +
                Basics.percent(meanMinorDelta) + "%");
        Assert.assertTrue("Good overlap of minors", Basics.percent(meanMinorOverlap) >= minorOverlapThreshold &&
                Basics.percent(meanMinorDelta) >= minorDeltaThreshold);
    }

    private static boolean checkMutations(int[] mutations) {
        for (int i = 0; i < mutations.length; i++) {
            int code = mutations[i];
            MutationType type = Mutations.getType(code);
            if (type == null)
                return false;
            int pos = Mutations.getPosition(code);
            if (pos < 0)
                return false;
        }
        return true;
    }

    private static boolean checkMutations(Set<Integer> mutations) {
        for (int code : mutations) {
            MutationType type = Mutations.getType(code);
            if (type == null)
                return false;
            int pos = Mutations.getPosition(code);
            if (pos < 0)
                return false;
        }
        return true;
    }

    private static double overlap(int overlap, int observed, int expected) {
        double denom = Math.sqrt(observed * expected);
        return denom == 0 ? 1.0 : overlap / denom;
    }

    private static double delta(int observed, int expected) {
        return observed == expected ? 0.0 : 2.0 * (observed - expected) / (double) (observed + expected);
    }

    private static int overlapSize(Set<Integer> mutations1, Set<Integer> mutations2) {
        if (mutations1.size() > mutations2.size()) {
            Set<Integer> tmp = mutations1;
            mutations1 = mutations2;
            mutations2 = tmp;
        }

        int intersection = 0;
        for (int code : mutations1)
            if (mutations2.contains(code))
                intersection++;

        return intersection;
    }

    private static int overlapSize(int[] mutations1, int[] mutations2) {
        if (mutations1.length > mutations2.length) {
            int[] tmp = mutations1;
            mutations1 = mutations2;
            mutations2 = tmp;
        }

        Set<Integer> mutations2Set = new HashSet<>();
        for (int i = 0; i < mutations2.length; i++)
            mutations2Set.add(mutations2[i]);

        int intersection = 0;
        for (int i = 0; i < mutations1.length; i++)
            if (mutations2Set.contains(mutations1[i]))
                intersection++;

        return intersection;
    }

    @Test
    public void filterSubstitutionByQualTestNoIndels() {
        SAssembler assembler = new SAssembler();

        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();
        randomMigGenerator.setGeneratorMutationModel(GeneratorMutationModel.NO_INDEL);
        randomMigGenerator.setMarkMinorMutations(true);

        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();


        int nCleanConsensuses = 0, nGoodConsensuses = 0;
        for (int i = 0; i < nRepetiotions2; i++) {
            SMig mig = randomMigGenerator.nextMig(randomReferenceGenerator).getMig();
            SConsensus consensus = assembler.assemble(mig);

            if (consensus != null) {
                KAligner aligner = new KAligner(KAlignerParameters.getByName("default"));
                aligner.addReference(consensus.getConsensusSQPair().getSequence());

                for (NucleotideSQPair read : consensus.getAssembledReads()) {
                    KAlignmentHit hit = aligner.align(read.getSequence()).getBestHit();

                    if (hit != null) {
                        LocalAlignment alignment = hit.getAlignment();

                        // Filter by ReadQualityPhred threshold
                        int[] rawMutations = MutationsExtractor.filterSubstitutionsByQual(
                                read.getQuality(),
                                alignment, (byte) 20);

                        int nSubstitutions = 0;
                        for (int j = 0; j < rawMutations.length; j++)
                            if (Mutations.isSubstitution(rawMutations[j]))
                                nSubstitutions++;

                        if (nSubstitutions == 0)
                            nCleanConsensuses++;

                        nGoodConsensuses++;
                    }
                }
            }
        }

        System.out.println("Cleaned consensuses(no indels)=" + Basics.percent(nCleanConsensuses, nGoodConsensuses) + "%");
        Assert.assertEquals("All consensuses(no indels) should be cleaned from LQ errors", nGoodConsensuses, nCleanConsensuses);
    }

    private static final int CLEANED_CONS_INDEL_THRESHOLD = 60;

    @Test
    public void filterSubstitutionByQualTest() {
        // Todo: rewrite to single method
        SAssembler assembler = new SAssembler();
        RandomMigGenerator randomMigGenerator = new RandomMigGenerator();
        randomMigGenerator.setMarkMinorMutations(true);
        RandomReferenceGenerator randomReferenceGenerator = new RandomReferenceGenerator();

        int nCleanConsensuses = 0, nGoodConsensuses = 0;
        for (int i = 0; i < nRepetiotions2; i++) {
            SMig mig = randomMigGenerator.nextMig(randomReferenceGenerator).getMig();
            SConsensus consensus = assembler.assemble(mig);

            if (consensus != null) {
                KAligner aligner = new KAligner(KAlignerParameters.getByName("default"));
                aligner.addReference(consensus.getConsensusSQPair().getSequence());

                for (NucleotideSQPair read : consensus.getAssembledReads()) {
                    KAlignmentHit hit = aligner.align(read.getSequence()).getBestHit();

                    if (hit != null) {
                        LocalAlignment alignment = hit.getAlignment();

                        // Filter by ReadQualityPhred threshold
                        int[] rawMutations = MutationsExtractor.filterSubstitutionsByQual(
                                read.getQuality(),
                                alignment, Util.PH33_BAD_QUAL);

                        int nSubstitutions = 0;
                        for (int j = 0; j < rawMutations.length; j++)
                            if (Mutations.isSubstitution(rawMutations[j]))
                                nSubstitutions++;

                        if (nSubstitutions == 0)
                            nCleanConsensuses++;

                        nGoodConsensuses++;
                    }
                }
            }
        }

        int cleanedConsensusesPercent = Basics.percent(nCleanConsensuses, nGoodConsensuses);
        System.out.println("Cleaned consensuses=" + cleanedConsensusesPercent + "%");
        Assert.assertTrue("More than " + CLEANED_CONS_INDEL_THRESHOLD + "% of consensuses " +
                "should be cleaned from LQ errors", cleanedConsensusesPercent > CLEANED_CONS_INDEL_THRESHOLD);
    }
}
