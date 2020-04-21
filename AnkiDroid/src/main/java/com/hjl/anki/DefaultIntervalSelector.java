package com.hjl.anki;

import java.util.Random;

public class DefaultIntervalSelector implements IntervalSelector {
    @Override
    public int select(int minDays, int maxDays) {
        return (new Random().nextInt(maxDays - minDays + 1)) + minDays;
    }
}
