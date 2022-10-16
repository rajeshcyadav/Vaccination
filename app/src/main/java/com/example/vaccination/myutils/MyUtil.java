package com.example.vaccination.myutils;

import android.animation.ValueAnimator;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MyUtil {
    public static String getDayAndDate() {
        LocalDate date = getDate(System.currentTimeMillis());
        String day = getDay(date);
        return day + "," + date.toString();
    }

    public static String getDay(LocalDate date) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEE");
        return fmt.print(date);
    }

    public static LocalDate getDate(long milli) {
        return new DateTime(milli).toLocalDate();
    }

    public static void animateTextView(int initialValue, int finalValue, final TextView textview) {

        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.setDuration(1500);

        valueAnimator.addUpdateListener(valueAnimator1 -> textview.setText(valueAnimator1.getAnimatedValue().toString()));
        valueAnimator.start();

    }


}
