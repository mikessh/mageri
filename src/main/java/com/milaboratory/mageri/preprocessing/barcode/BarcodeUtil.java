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

package com.milaboratory.mageri.preprocessing.barcode;

public class BarcodeUtil {
    public static final char UMI_MARK = 'N', PROTECTIVE_N = 'n';

    public static char complement(char c) {
        switch (c) {
            case 'A':
                return 'T';
            case 'T':
                return 'A';
            case 'G':
                return 'C';
            case 'C':
                return 'G';
            case 'Y':
                return 'R';
            case 'R':
                return 'Y';
            case 'S':
                return 'S';
            case 'W':
                return 'W';
            case 'K':
                return 'M';
            case 'M':
                return 'K';
            case 'B':
                return 'V';
            case 'D':
                return 'H';
            case 'H':
                return 'D';
            case 'V':
                return 'B';
            case 'N':
                return 'N';
            case 'a':
                return 't';
            case 't':
                return 'a';
            case 'g':
                return 'c';
            case 'c':
                return 'g';
            case 'y':
                return 'r';
            case 'r':
                return 'y';
            case 's':
                return 's';
            case 'w':
                return 'w';
            case 'k':
                return 'm';
            case 'm':
                return 'k';
            case 'b':
                return 'v';
            case 'd':
                return 'h';
            case 'h':
                return 'd';
            case 'v':
                return 'b';
            case 'n':
                return 'n';
            default:
                throw new IllegalArgumentException("Illegar nucleotide character");
        }
    }

    public static String charToRegex(char c) {
        if (!Character.isUpperCase(c))
            return "[ATGC]";

        switch (c) {
            case 'A':
            case 'T':
            case 'G':
            case 'C':
                return Character.toString(c);
            case 'R':
                return "[AG]";
            case 'Y':
                return "[CT]";
            case 'M':
                return "[AC]";
            case 'S':
                return "[GC]";
            case 'W':
                return "[AT]";
            case 'K':
                return "[GT]";
            case 'V':
                return "[ACG]";
            case 'D':
                return "[AGT]";
            case 'H':
                return "[ACT]";
            case 'B':
                return "[CGT]";
            case 'N':
                return "[ATGC]";
            default:
                throw new IllegalArgumentException("Illegal nucleotide character");
        }
    }

    public static boolean compareRedundant(char base, char other) {
        switch (base) {
            case 'A':
            case 'T':
            case 'G':
            case 'C':
                return base == other;
            case 'R':
                return other == 'A' || other == 'G';
            case 'Y':
                return other == 'C' || other == 'T';
            case 'M':
                return other == 'A' || other == 'C';
            case 'S':
                return other == 'C' || other == 'G';
            case 'W':
                return other == 'A' || other == 'T';
            case 'K':
                return other == 'T' || other == 'G';
            case 'V':
                return other != 'T';
            case 'D':
                return other != 'C';
            case 'H':
                return other != 'G';
            case 'B':
                return other != 'A';
            case 'N':
                return true;
            default:
                throw new IllegalArgumentException("Illegar nucleotide character " + base);
        }
    }
}
