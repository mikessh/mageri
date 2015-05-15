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

package com.milaboratory.oncomigec.misc;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class Basics {
    private Basics() {
    }

    public static <T, V> Map<T, V> sortMap(Map<T, V> input, Comparator<V> valueComparator) {
        final MapValueComparator<T, V> mapValueComparator = new MapValueComparator<>(input, valueComparator);
        final TreeMap<T, V> sortedMap = new TreeMap<>(mapValueComparator);
        sortedMap.putAll(input);
        return sortedMap;
    }

    private static class MapValueComparator<T, V> implements Comparator<T> {
        private final Map<T, V> base;
        private final Comparator<V> valueComparator;

        public MapValueComparator(Map<T, V> base, Comparator<V> valueComparator) {
            this.base = base;
            this.valueComparator = valueComparator;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(T a, T b) {
            if (valueComparator.compare(base.get(a), base.get(b)) >= 0) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    public static int[] toIntArray(List<Integer> list) {
        int n = list.size();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++)
            arr[i] = list.get(i);
        return arr;
    }

    public static <T> void incrementAICounter(ConcurrentHashMap<T, AtomicInteger> map, T key) {
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger prevCounter = map.putIfAbsent(key, counter);
        counter = prevCounter == null ? counter : prevCounter;
        counter.incrementAndGet();
    }

    public static <T> void incrementAICounter(ConcurrentHashMap<T, AtomicInteger> map, T key, int count) {
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger prevCounter = map.putIfAbsent(key, counter);
        counter = prevCounter == null ? counter : prevCounter;
        counter.addAndGet(count);
    }

    public static <T> void incrementALCounter(ConcurrentHashMap<T, AtomicLong> map, T key) {
        AtomicLong counter = new AtomicLong();
        AtomicLong prevCounter = map.putIfAbsent(key, counter);
        counter = prevCounter == null ? counter : prevCounter;
        counter.incrementAndGet();
    }

    public static <T> void incrementALCounter(ConcurrentHashMap<T, AtomicLong> map, T key, int count) {
        AtomicLong counter = new AtomicLong();
        AtomicLong prevCounter = map.putIfAbsent(key, counter);
        counter = prevCounter == null ? counter : prevCounter;
        counter.addAndGet(count);
    }

    public static double nullToZero(Double value) {
        if (value == null)
            return 0;
        else
            return value;
    }

    public static int nullToZero(Integer value) {
        if (value == null)
            return 0;
        else
            return value;
    }

    public static int nullToZero(AtomicInteger value) {
        if (value == null)
            return 0;
        else
            return value.get();
    }

    public static long nullToZero(Long value) {
        if (value == null)
            return 0;
        else
            return value;
    }

    public static long nullToZero(AtomicLong value) {
        if (value == null)
            return 0;
        else
            return value.get();
    }

    public static void printXml(Element e) throws IOException {
        Document document = new Document(e);
        new XMLOutputter(Format.getPrettyFormat()).output(document, System.out);
    }
}
