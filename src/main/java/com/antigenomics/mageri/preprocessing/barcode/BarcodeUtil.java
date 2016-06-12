/*
 * Copyright 2014-2016 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antigenomics.mageri.preprocessing.barcode;

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
