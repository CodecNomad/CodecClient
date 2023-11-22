package com.github.codecnomad.codecclient.classes;

public class Counter {
    private int count;

    public boolean countUntil(int threshold) {
        if (count >= threshold) {
            reset();
            return false;
        }
        count++;
        return true;
    }

    public void add(int numberToAdd) {
        count += numberToAdd;
    }
    public int get() { return count; }

    public void reset() {
        count = 0;
    }
}
