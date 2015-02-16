package com.milaboratory.oncomigec.core.align.kmer;

//
// Adapted from KAnalyze package, see
//
// Peter Audano and Fredrik Vannberg.
// KAnalyze: a fast versatile pipelined K-mer toolkit.
// Bioinformatics, Advance Access publication March 18, 2014
// doi:10.1093/bioinformatics/btu152
//
// ---
//
// Copyright (c) 2014 Peter A. Audano III
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 3 of the License or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; see the file COPYING.LESSER.  If not, see
// <http://www.gnu.org/licenses/>

import com.milaboratory.util.IntArrayList;

/**
 * A fast hash-table based implementation of an integer counter. The keys are
 * long integers, and counters associated with each key are integers.
 */
public class KmerMap {

    //
    // Declarations
    //

    // Hashset parameters

    /**
     * Number of elements in this hash set.
     */
    private long size;

    /**
     * Load factor before bucket array is expanded.
     */
    private float maxLoadFactor;

    // Data storage structure

    /**
     * Hash buckets. Values are chained from the elements in this array.
     */
    private KmerData[] buckets;

    // Internal state

    /**
     * Number of buckets.
     */
    private int bucketSize;

    /**
     * Number of elements in hash before bucket array is expanded. This value is
     * calculated from the current bucket size and the load factor.
     */
    private long nextExpandSize;

    /**
     * This mask describes which bits to use when changing from a hash to an
     * address in the bucket array. A mask is used to avoid more expensive
     * modulus operations.
     */
    private int hashMask;

    //
    // Global constants
    //

    /**
     * Default initial size (2 ^ 16)
     */
    public static final int DEFAULT_BUCKET_SIZE = 65536;

    /**
     * Default maximum load factor before bucket array is expanded.
     */
    public static final float DEFAULT_MAX_LOAD_FACTOR = 2.0F;

    /**
     * Maximum bucket size. This is the largest power of 2 that can be
     * represented by an integer.
     */
    public static final int MAX_BUCKET_SIZE = 1073741824;

    /**
     * Bucket array is multiplied by this factor when it is expanded.
     */
    public static final int EXPANSION_FACTOR = 2;

    //
    // Constructors
    //

    /**
     * Create a new long counter.
     * <p/>
     * The bucket size must be a positive integer. If this argument is 0, the
     * default bucket size (65536) is used. If the argument is not 0 and not a
     * power of 2, the first power of 2 greater than this this size is used.
     * Having an even power of 2 makes the hash function more efficient by
     * avoiding modulus operations.
     *
     * @param bucketSize    Number of buckets in the counter hash set. If less than
     *                      or equal to zero, the default bucket size is used (
     *                      <code>DEFAULT_BUCKET_SIZE</code>).
     * @param maxLoadFactor Load factor of this hash set before bucket array is
     *                      expanded. If less than zero, the default value is used
     *                      (MAX_LOAD_FACTOR). If zero, bucket expansion is disabled.
     */
    public KmerMap(int bucketSize, float maxLoadFactor) {

        int power2Size;
        hashMask = 0;

        // Set default arguments
        if (bucketSize <= 0)
            bucketSize = DEFAULT_BUCKET_SIZE;

        if (maxLoadFactor == 0) {
            maxLoadFactor = Float.MAX_VALUE;

        } else if (maxLoadFactor < 0) {
            maxLoadFactor = DEFAULT_MAX_LOAD_FACTOR;
        }

        // Set size to the smallest possible power of 2 greater than or equal to
        // the size requested
        // While checking, build the hash mask (converts hash keys to bucket
        // array indices).
        for (power2Size = 1; power2Size < bucketSize && power2Size <= MAX_BUCKET_SIZE; power2Size *= 2)
            hashMask = (hashMask << 1) | 1;

        bucketSize = power2Size;

        // Set attributes
        this.bucketSize = bucketSize;
        this.maxLoadFactor = maxLoadFactor;
        this.size = 0;

        // Calculate next expansion
        setNextExpansion();

        // Create bucket array
        buckets = new KmerData[bucketSize];
    }

    /**
     * Create a new long counter.
     * <p/>
     * The bucket size must be a positive integer. If this argument is 0, the
     * default bucket size (65536) is used. If the argument is not 0 and not a
     * power of 2, the first power of 2 greater than this this size is used.
     * Having an even power of 2 makes the hash function more efficient by
     * avoiding modulus operations.
     *
     * @param bucketSize Number of buckets in the counter hash set. If less than
     *                   or equal to zero, the default bucket size is used (
     *                   <code>DEFAULT_BUCKET_SIZE</code>).
     */
    public KmerMap(int bucketSize) {
        this(bucketSize, -1);
    }

    /**
     * Create a new long counter.
     */
    public KmerMap() {
        this(0, -1);
    }

    //
    // Public methods
    //

    /**
     * Increment counter for a given key. If the counter is already at its
     * maximum value, <code>Inter.MAX_VALUE</code>, the counter is not
     * incremented.
     *
     * @param kmer Key of the counter to increment.
     */
    public void increment(long kmer, int parentSequenceId) {

        KmerData element;
        int bucketIndex;

        // Hash key
        bucketIndex = (int) (kmer ^ kmer >>> 32) & hashMask;

        // Get bucket
        // bucketIndex = hash (key) & hashMask;
        element = buckets[bucketIndex];

        // If bucket is empty, new element is the first in this bucket
        if (element == null) {
            IntArrayList parentSequenceIdList = new IntArrayList();
            parentSequenceIdList.add(parentSequenceId);
            buckets[bucketIndex] = new KmerData(kmer, 1,
                    parentSequenceIdList);
            ++size;

        } else {

            // Check first element
            if (element.key == kmer) {
                if (element.counter < Integer.MAX_VALUE)
                    ++element.counter;
                element.parentSequenceIds.add(parentSequenceId);

                return;
            }

            // Search chain
            while (element.nextElement != null) {

                if (element.nextElement.key == kmer) {
                    if (element.nextElement.counter < Integer.MAX_VALUE)
                        ++element.nextElement.counter;
                    element.parentSequenceIds.add(parentSequenceId);

                    return;
                }

                element = element.nextElement;
            }

            // Chain exhausted and no value found. Add it.
            IntArrayList parentSequenceIdList = new IntArrayList();
            parentSequenceIdList.add(parentSequenceId);
            element.nextElement = new KmerData(kmer, 1,
                    parentSequenceIdList);
            ++size;
        }

        if (size > nextExpandSize)
            expand();
    }

    public KmerData getData(long kmer) {
        KmerData element;

        // Hash key and get element
        element = buckets[((int) (kmer ^ kmer >>> 32) & hashMask) & hashMask];

        while (element != null) {
            if (element.key == kmer)
                return element;

            element = element.nextElement;
        }

        return null;
    }

    public long getSize() {
        return size;
    }

    //
    // Private utility methods
    //

    /**
     * Set the <code>nextExpandSize</code>.
     */
    private void setNextExpansion() {

        if (bucketSize == MAX_BUCKET_SIZE || maxLoadFactor < 0) {

            nextExpandSize = Long.MAX_VALUE;

            return;
        }

        // Set next expansion size
        nextExpandSize = (long) (bucketSize * maxLoadFactor);

        // Detect and correct overflow
        if (nextExpandSize < 0)
            nextExpandSize = Long.MAX_VALUE;
    }

    /**
     * Expand the bucket array and re-hash.
     */
    private void expand() {

        // Declare new data elements
        int newBucketSize = bucketSize;
        int newHashMask = hashMask;
        KmerData[] newBuckets;

        // Declare variables used during expansion
        int index; // Index of the current bucket in the old hash
        int newIndex; // Index of the bucket in the new hash an element is
        // inserted into

        KmerData thisElement; // Current element
        KmerData nextElement; // Next element to insert
        KmerData insertElement; // Element being inserted

        // Expand size and mask
        for (int count = 1; count < EXPANSION_FACTOR && newBucketSize < MAX_BUCKET_SIZE; count *= 2) {

            // Multiply bucket size
            newBucketSize *= 2;

            // Shift hashMask up and fill in right-most bit
            newHashMask = (hashMask << 1) | 1;
        }

        // Create new bucket list
        newBuckets = new KmerData[newBucketSize];

        // Iterate over old buckets and push elements into the new hash
        for (index = 0; index < bucketSize; ++index) {

            thisElement = buckets[index];

            // Traverse elements in this bucket
            while (thisElement != null) {
                nextElement = thisElement.nextElement; // Store next element
                thisElement.nextElement = null; // Reset next pointer

                // Find bucket in the new hash
                newIndex = (int) (thisElement.key ^ thisElement.key >>> 32) & newHashMask;

                // Get the element at newIndex in the new hash
                insertElement = newBuckets[newIndex];

                // Insert into the new hash
                if (insertElement == null) {
                    // Current bucket is empty
                    newBuckets[newIndex] = thisElement;

                } else {
                    // Walk to the end of this chain
                    while (insertElement.nextElement != null)
                        insertElement = insertElement.nextElement;

                    // Insert at the end of this chain
                    insertElement.nextElement = thisElement;
                }

                // Set the next element from the current chain in the old hash
                thisElement = nextElement;
            }
        }

        // Set object fields
        bucketSize = newBucketSize;
        hashMask = newHashMask;
        buckets = newBuckets;

        // Calculate next expansion
        setNextExpansion();
    }


    //
    // Element Data Structure
    //

    /**
     * An element for storing a key and a count.
     */
    public class KmerData {

        /**
         * Next element in a linked list.
         */
        private KmerData nextElement;

        /**
         * K-mer.
         */
        private long key;

        /**
         * K-mer Counter.
         */
        private int counter;

        private IntArrayList parentSequenceIds;

        /**
         * Create a new counter.
         *
         * @param key     Key for this counter element.
         * @param counter Counter for this element.
         */
        private KmerData(long key, int counter, IntArrayList parentSequenceIds) {

            // Set
            this.key = key;
            this.counter = counter;
            this.parentSequenceIds = parentSequenceIds;
            nextElement = null;
        }

        public int getCounter() {
            return counter;
        }

        public IntArrayList getParentSequenceIds() {
            return parentSequenceIds;
        }
    }
}

