package com.milaboratory.oncomigec.util;

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

    public static int percent(int n, int N) {
        return (int) (100.0 * (n / (double) N));
    }

    public static int percent(double p) {
        return (int) (100.0 * p);
    }
}
