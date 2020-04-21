package com.hjl.anki;

import android.os.Build;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AvoiderIntervalSelector implements IntervalSelector {
    @Override
    public int select(int minDays, int maxDays) {
        double[] weights = getWeights(minDays, maxDays);
        return roulette(weights) + minDays;
    }
    private double[] getWeights(int minDays, int maxDays) {
        // Make an array of the given number of days
        double[] weights = new double[maxDays - minDays + 1];
        // Fill the weights
        LocalDate today = LocalDate.now();
        for(int i = minDays; i <= maxDays; ++i) {
            LocalDate date = today.plusDays(i);
            double w = weight(date);
            weights[i-minDays] = w;
        }
        return weights;
    }
    private int roulette(double[] weights) {
        double sum = 0;
        for(double w: weights) sum += w;
        double x = new Random().nextDouble() * sum;
        for(int i = 0; i < weights.length; ++i) {
            x -= weights[i];
            if(x <= 0) return i;
        }
        return 0;
    }
    public double weight(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        int dom = date.getDayOfMonth();
        Month month = date.getMonth();

        if(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return 0.01;
        if(dom == 30 && month == Month.JULY) return 0.01;
        if(dom == 25 && month == Month.DECEMBER) return 0.01;
        return 1;
    }
}
