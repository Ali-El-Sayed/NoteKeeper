package com.jwhh.notekeeper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    public static String ORIGINAL_NOTE_COURSE_ID = "com.jwhh.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static String ORIGINAL_NOTE_TITLE = "com.jwhh.notekeeper.ORIGINAL_NOTE_TITLE";
    public static String ORIGINAL_NOTE_TEXT = "com.jwhh.notekeeper.ORIGINAL_NOTE_TEXT";

    public String mOriginalNoteCourseId;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;
    public boolean mIsNewlyCreated = true;

    public void saveState(@NonNull Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    public void restoreState(@NonNull Bundle inState) {
        mOriginalNoteCourseId = inState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = inState.getString(ORIGINAL_NOTE_TEXT);
    }
}
