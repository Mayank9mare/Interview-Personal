// Companies: PayPal, Walmart, Flipkart, Uber
package com.uber.hashmap;

import java.util.*;

public class CustomHashMap {
    private final double minLF, maxLF;
    private int bucketsCount;
    private List<String[]>[] buckets; // each entry is [key, value]
    private int size;

    @SuppressWarnings("unchecked")
    public CustomHashMap(double minLoadFactor, double maxLoadFactor) {
        this.minLF = round2(minLoadFactor);
        this.maxLF = round2(maxLoadFactor);
        this.bucketsCount = 2;
        this.buckets = new ArrayList[2];
        for (int i = 0; i < 2; i++) buckets[i] = new ArrayList<>();
    }

    private double round2(double x) { return Math.round(x * 100.0) / 100.0; }

    private int hashKey(String key) {
        int len = key.length(), sum = 0;
        for (char c : key.toCharArray()) sum += (c - 'a' + 1);
        return len * len + sum;
    }

    private int bucketIdx(String key, int count) { return hashKey(key) % count; }

    public void put(String key, String value) {
        int idx = bucketIdx(key, bucketsCount);
        for (String[] e : buckets[idx]) {
            if (e[0].equals(key)) { e[1] = value; return; }
        }
        buckets[idx].add(new String[]{key, value});
        size++;
        rehash();
    }

    public String get(String key) {
        for (String[] e : buckets[bucketIdx(key, bucketsCount)])
            if (e[0].equals(key)) return e[1];
        return "";
    }

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

    public List<String> getBucketKeys(int idx) {
        if (idx < 0 || idx >= bucketsCount) return new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (String[] e : buckets[idx]) keys.add(e[0]);
        Collections.sort(keys);
        return keys;
    }

    public int size() { return size; }
    public int bucketsCount() { return bucketsCount; }

    @SuppressWarnings("unchecked")
    private void rehash() {
        double lf = round2((double) size / bucketsCount);
        int newCount = bucketsCount;
        if (lf > maxLF) {
            newCount *= 2;
            while (round2((double) size / newCount) > maxLF) newCount *= 2;
        } else if (lf < minLF && bucketsCount > 2) {
            newCount = Math.max(2, bucketsCount / 2);
            while (newCount > 2 && round2((double) size / newCount) < minLF) newCount /= 2;
        } else return;
        List<String[]>[] newBuckets = new ArrayList[newCount];
        for (int i = 0; i < newCount; i++) newBuckets[i] = new ArrayList<>();
        for (List<String[]> bucket : buckets)
            for (String[] e : bucket) newBuckets[hashKey(e[0]) % newCount].add(e);
        buckets = newBuckets;
        bucketsCount = newCount;
    }
}
