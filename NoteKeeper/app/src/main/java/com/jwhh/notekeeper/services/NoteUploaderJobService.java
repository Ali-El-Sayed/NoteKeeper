package com.jwhh.notekeeper.services;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.IBinder;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.jwhh.notekeeper.services.DATA_URI";

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


}