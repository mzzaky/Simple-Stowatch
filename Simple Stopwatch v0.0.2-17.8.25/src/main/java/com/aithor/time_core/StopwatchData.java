package com.aithor.time_core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StopwatchData {
    private long startTime;
    private long pausedTime;
    private long totalPausedDuration;
    private boolean running;
    private final List<Long> laps;

    public StopwatchData() {
        this.startTime = System.currentTimeMillis();
        this.totalPausedDuration = 0;
        this.running = true;
        this.laps = new ArrayList<>();
    }

    public synchronized long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime - totalPausedDuration;
        } else {
            return pausedTime - startTime - totalPausedDuration;
        }
    }

    public synchronized void pause() {
        if (running) {
            pausedTime = System.currentTimeMillis();
            running = false;
        }
    }

    public synchronized void resume() {
        if (!running) {
            totalPausedDuration += System.currentTimeMillis() - pausedTime;
            running = true;
        }
    }

    public synchronized void stop() {
        if (running) {
            pausedTime = System.currentTimeMillis();
        }
        running = false;
    }

    public synchronized void reset() {
        startTime = System.currentTimeMillis();
        totalPausedDuration = 0;
        pausedTime = 0;
        running = true;
        laps.clear();
    }

    public synchronized long recordLap() {
        long lap = getElapsedTime();
        laps.add(lap);
        return lap;
    }

    public synchronized List<Long> getLaps() {
        return Collections.unmodifiableList(new ArrayList<>(laps));
    }

    public synchronized boolean isRunning() {
        return running;
    }
}