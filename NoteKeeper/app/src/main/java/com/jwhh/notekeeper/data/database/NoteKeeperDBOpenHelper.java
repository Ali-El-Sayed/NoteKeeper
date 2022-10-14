package com.jwhh.notekeeper.data.database;

import static com.jwhh.notekeeper.data.database.NoteKeeperDBContract.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NoteKeeperDBOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NoteKeeper.db";
    private static final int DATABASE_VERSION = 2;

    public NoteKeeperDBOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL(CourseInfoTable.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoTable.SQL_CREATE_TABLE);
        db.execSQL(CourseInfoTable.SQL_CREATE_INDEX1);
        db.execSQL(NoteInfoTable.SQL_CREATE_INDEX1);

        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update to Indexes
        if (oldVersion < 2) {
            db.execSQL(NoteInfoTable.SQL_CREATE_INDEX1);
            db.execSQL(CourseInfoTable.SQL_CREATE_INDEX1);
        }

    }
}
