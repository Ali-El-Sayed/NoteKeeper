package com.jwhh.notekeeper.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.jwhh.notekeeper.services.DATA_URI";
    private NoteUploader noteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        @SuppressLint("StaticFieldLeak") AsyncTask<JobParameters, Void, Void> task =
                new AsyncTask<JobParameters, Void, Void>() {
                    @Override
                    protected Void doInBackground(JobParameters... backgroundParams) {
                        JobParameters jobParams = backgroundParams[0];
                        String stringNotesUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
                        Uri notesUri = Uri.parse(stringNotesUri);
                        noteUploader.doUpload(notesUri);
                        if (!noteUploader.isCanceled())
                            jobFinished(jobParams, false);

                        return null;
                    }
                };
        noteUploader = new NoteUploader(this);
        task.execute(jobParameters);

        //  return true => means running on a background Thread
        //  return false => means running on the main Thread
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        noteUploader.cancel();
        return true;
    }


}