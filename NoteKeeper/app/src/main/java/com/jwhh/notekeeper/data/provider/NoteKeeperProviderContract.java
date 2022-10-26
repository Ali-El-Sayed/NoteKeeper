package com.jwhh.notekeeper.data.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class NoteKeeperProviderContract {
    private NoteKeeperProviderContract() {
    }

    public static final String AUTHORITY = "com.jwhh.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);


    protected interface CoursesIdColumns {
        public static final String COLUMN_COURSE_ID = "course_id";

    }

    protected interface CoursesColumns {
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }

    protected interface NotesColumns {
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "course_text";
    }

    public static final class Courses implements CoursesColumns, BaseColumns, CoursesIdColumns {
        public static final String PATH = "courses";
        //content://com.jwhh.notekeeper.provider/courses"
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    public static final class Notes implements NotesColumns, CoursesColumns, BaseColumns, CoursesIdColumns {
        public static final String PATH = "notes";
        //content://com.jwhh.notekeeper.provider/notes"
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);

        public static final String PATH_EXPANDED = "notes_expanded";
        //content://com.jwhh.notekeeper.provider/notes_expanded"
        public static final Uri CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED);

    }

}
