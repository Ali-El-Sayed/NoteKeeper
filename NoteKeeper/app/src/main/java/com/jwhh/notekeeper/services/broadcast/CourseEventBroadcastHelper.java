package com.jwhh.notekeeper.services.broadcast;

import android.content.Context;
import android.content.Intent;

public class CourseEventBroadcastHelper {
    public static final String ACTION_COURSE_ID = "com.jwhh.notekeeper.broadcast";

    public static final String EXTRA_COURSE_ID = "com.jwhh.notekeeper.extra.COURSE_ID";
    public static final String EXTRA_COURSE_MESSAGE = "com.jwhh.notekeeper.services.COURSE_MESSAGE";

    public static void sendEventBroadcast(Context context, String courseId, String message) {
        Intent intent = new Intent(ACTION_COURSE_ID);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_COURSE_MESSAGE, message);
        context.sendBroadcast(intent);
    }

}
