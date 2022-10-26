package com.jwhh.notekeeper.data.provider;

import static com.jwhh.notekeeper.data.database.NoteKeeperDBContract.*;
import static com.jwhh.notekeeper.data.provider.NoteKeeperProviderContract.*;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.jwhh.notekeeper.data.database.NoteKeeperDBOpenHelper;

public class NoteKeeperProvider extends ContentProvider {

    private NoteKeeperDBOpenHelper mDBOpenHelper;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;
    public static final int NOTES = 1;
    public static final int NOTES_EXPANDED = 2;

    static {
        sUriMatcher.addURI(AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        mDBOpenHelper = new NoteKeeperDBOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case COURSES: {
                cursor = db.query(CourseInfoTable.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case NOTES: {
                cursor = db.query(NoteInfoTable.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case NOTES_EXPANDED: {
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            }
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tablesWithJoin = NoteInfoTable.TABLE_NAME + " JOIN " +
                CourseInfoTable.TABLE_NAME + " ON " +
                NoteInfoTable.getQName(NoteInfoTable.COLUMN_COURSE_ID) + " = " +
                CourseInfoTable.getQName(CourseInfoTable.COLUMN_COURSE_ID);

        String[] columns = new String[projection.length];
        for (int idx = 0; idx < projection.length; idx++)
            columns[idx] = projection[idx].equals(BaseColumns._ID) ||
                    projection[idx].equals(NoteInfoTable.COLUMN_COURSE_ID) ?
                    NoteInfoTable.getQName(projection[idx]) : projection[idx];

        return db.query(tablesWithJoin, columns, selection, selectionArgs, null, null, sortOrder);
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}