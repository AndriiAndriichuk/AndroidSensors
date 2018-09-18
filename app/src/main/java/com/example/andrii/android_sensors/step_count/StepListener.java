package com.example.andrii.android_sensors.step_count;

// Will listen to step alerts
public interface StepListener {

    public void step(long timeNs);

}