// Companies: PayPal, Walmart, Flipkart, Uber
package com.uber.hashmap;

import java.util.*;

/**
 * A custom open-addressing-free hash map backed by an array of chained buckets (separate chaining).
 *
 * <p>Key behaviours:
 * <ul>
 *   <li>Keys and values are both {@code String}.</li>
 *   <li>The bucket array is resized (rehashed) whenever the load factor falls outside the
 *       [{@code minLF}, {@code maxLF}] window after a {@code put} or {@code remove}.</li>
 *   <li>Bucket count never drops below 2.</li>
 * </ul>
 *
 * <p>Hash function: {@code h(key) = len² + Σ(c - 'a' + 1)} — deliberately simple for interview
 * traceability; not suitable for production use (poor distribution, case-sensitive only for
 * lowercase letters).
 *
 * <p>Load factor: {@code lf = size / bucketsCount}, rounded to 2 decimal places before comparison
 * to avoid floating-point noise (e.g., 0.300000004 > 0.30).
 */
public class CustomHashMap {

    /** Lower bound on load factor; triggers shrink when {@code lf < minLF} (and buckets > 2). */
    private final double minLF;

    /** Upper bound on load factor; triggers grow when {@code lf > maxLF}. */
    private final double maxLF;

    /** Current number of buckets in the backing array. Always >= 2. */
    private int bucketsCount;

    /**
     * The backing array. Each slot holds a list of {@code [key, value]} string pairs.
     * Collisions are resolved by chaining within the same bucket list.
     */
    private List<String[]>[] buckets; // each entry is [key, value]

    /** Total number of key-value pairs stored across all buckets. */
    private int size;

    /**
     * Constructs a new {@code CustomHashMap} with the given load-factor bounds.
     *
     * <p>Starts with 2 empty buckets. The first rehash that pushes {@code lf > maxLF} will
     * double the bucket count.
     *
     * @param minLoadFactor lower bound; shrink is triggered when load factor drops below this
     * @param maxLoadFactor upper bound; grow is triggered when load factor exceeds this
     */
    @SuppressWarnings("unchecked")
    public CustomHashMap(double minLoadFactor, double maxLoadFactor) {
        this.minLF = round2(minLoadFactor);
        this.maxLF = round2(maxLoadFactor);
        this.bucketsCount = 2;
        this.buckets = new ArrayList[2];
        for (int i = 0; i < 2; i++) buckets[i] = new ArrayList<>();
    }

    /**
     * Rounds {@code x} to 2 decimal places.
     *
     * <p>Used before every load-factor comparison to suppress floating-point rounding noise
     * (e.g., {@code 3/10.0 = 0.30000000000000004} should compare equal to {@code 0.30}).
     */
    private double round2(double x) { return Math.round(x * 100.0) / 100.0; }

    /**
     * Custom hash function: {@code h(key) = len² + Σ(c - 'a' + 1)}.
     *
     * <p>Intentionally simple so the bucket an element lands in can be traced by hand during
     * an interview. Not a general-purpose hash — assumes lowercase ASCII letters and produces
     * many collisions for keys of the same length and character-sum.
     *
     * @param key the key to hash
     * @return a non-negative integer hash code (may be large; callers take {@code % bucketsCount})
     */
    private int hashKey(String key) {
        int len = key.length(), sum = 0;
        for (char c : key.toCharArray()) sum += (c - 'a' + 1);
        return len * len + sum;
    }

    /**
     * Returns the bucket index for {@code key} given a bucket array of size {@code count}.
     *
     * <p>Passing {@code count} explicitly (rather than using {@code bucketsCount}) lets
     * {@link #rehash()} call this during migration when the new count differs from the old one.
     *
     * @param key   the key to locate
     * @param count the total number of buckets in the target array
     * @return index in {@code [0, count)}
     */
    private int bucketIdx(String key, int count) { return hashKey(key) % count; }

    /**
     * Inserts or updates the mapping for {@code key}.
     *
     * <p>If {@code key} already exists in its bucket chain, the value is updated in place and
     * size does not change. Otherwise the entry is appended and size is incremented. A rehash
     * is attempted after every call (no-op when load factor stays in bounds).
     *
     * @param key   the key (must be non-null)
     * @param value the value to associate with the key
     */
    public void put(String key, String value) {
        int idx = bucketIdx(key, bucketsCount);
        for (String[] e : buckets[idx]) {
            if (e[0].equals(key)) { e[1] = value; return; } // update existing — no size change
        }
        buckets[idx].add(new String[]{key, value});
        size++;
        rehash();
    }

    /**
     * Returns the value mapped to {@code key}, or {@code ""} if the key is absent.
     *
     * @param key the key to look up
     * @return the associated value, or {@code ""} if not found
     */
    public String get(String key) {
        for (String[] e : buckets[bucketIdx(key, bucketsCount)])
            if (e[0].equals(key)) return e[1];
        return "";
    }

    /**
     * Removes the mapping for {@code key} and returns its value.
     *
     * <p>After removal, a rehash is attempted; the bucket array may shrink if the load factor
     * drops below {@code minLF}.
     *
     * @param key the key to remove
     * @return the value that was mapped to {@code key}, or {@code ""} if the key was absent
     */
    public String remove(String key) {
        List<String[]> bucket = buckets[bucketIdx(key, bucketsCount)];
        for (int i = 0; i < bucket.size(); i++) {
            if (bucket.get(i)[0].equals(key)) {
                String val = bucket.get(i)[1];
                bucket.remove(i);
                size--;
                rehash();
                return val;
            }
        }
        return "";
    }

    /**
     * Returns a sorted list of all keys that currently reside in bucket {@code idx}.
     *
     * <p>Useful for white-box testing: callers can verify that specific keys hash to the
     * expected bucket after a put or rehash.
     *
     * @param idx the 0-based bucket index
     * @return sorted list of keys in that bucket, or an empty list if {@code idx} is out of range
     */
    public List<String> getBucketKeys(int idx) {
        if (idx < 0 || idx >= bucketsCount) return new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (String[] e : buckets[idx]) keys.add(e[0]);
        Collections.sort(keys);
        return keys;
    }

    /** @return the total number of key-value pairs stored in this map */
    public int size() { return size; }

    /** @return the current number of buckets in the backing array */
    public int bucketsCount() { return bucketsCount; }

    /**
     * Resizes the bucket array if the current load factor is outside [{@code minLF}, {@code maxLF}].
     *
     * <p>Growth: doubles {@code bucketsCount} until {@code lf <= maxLF}.<br>
     * Shrink: halves {@code bucketsCount} until {@code lf >= minLF} or floor of 2 is reached.
     *
     * <p>All existing entries are re-distributed into the new array using the same
     * {@link #hashKey(String)} function modulo the new count.
     *
     * <p>No-op when the load factor is already within bounds.
     */
    @SuppressWarnings("unchecked")
    private void rehash() {
        double lf = round2((double) size / bucketsCount);
        int newCount = bucketsCount;

        if (lf > maxLF) {
            // Grow: keep doubling until load factor is within the upper bound.
            newCount *= 2;
            while (round2((double) size / newCount) > maxLF) newCount *= 2;
        } else if (lf < minLF && bucketsCount > 2) {
            // Shrink: keep halving until load factor is within the lower bound (floor: 2 buckets).
            newCount = Math.max(2, bucketsCount / 2);
            while (newCount > 2 && round2((double) size / newCount) < minLF) newCount /= 2;
        } else {
            return; // load factor is already in bounds — nothing to do
        }

        // Migrate all entries into the new bucket array.
        List<String[]>[] newBuckets = new ArrayList[newCount];
        for (int i = 0; i < newCount; i++) newBuckets[i] = new ArrayList<>();
        for (List<String[]> bucket : buckets)
            for (String[] e : bucket) newBuckets[hashKey(e[0]) % newCount].add(e);

        buckets = newBuckets;
        bucketsCount = newCount;
    }
}
