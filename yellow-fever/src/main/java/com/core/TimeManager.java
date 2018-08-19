package com.core;

public class TimeManager {
  final int HOURTIME = 60; // minute
  final int DURATION = 24; // 1 day is 12 hour - no need to simulate night time
  final int WEEK_DURATION = 7; // 1 day = 12 hour, 1 week = 7 day = 7 * 12

  public int currentHour(int currentStep) {
    int hour = 1;
    int t = ((int) currentStep) % HOURTIME;
    int m = ((int) currentStep - (t)) / HOURTIME;

    if (m <= DURATION) {
      hour = m;
    } else {
      hour = (int) m % DURATION;
    }
    return hour;
  }

  public int currentDayInWeek(int currentStep) {
    int t = (int) currentStep % (HOURTIME * DURATION);
    int da = ((int) currentStep - (t)) / (HOURTIME * DURATION);
    int w = 0;
    if (da <= WEEK_DURATION) {
      w = da;
    } else {
      w = (int) da % WEEK_DURATION;
    }

    return w;
  }

  public int dayCount(int currentStep) {
    int hour = this.currentHour(currentStep);
    int t = (int) currentStep % (HOURTIME * DURATION);
    int da = ((int) currentStep - (t)) / (HOURTIME * DURATION);

    return da;
  }
}
