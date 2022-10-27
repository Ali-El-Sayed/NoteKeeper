package com.jwhh.notekeeper.data.provider;

import static com.jwhh.notekeeper.data.database.NoteKeeperDBContract.*;
import static com.jwhh.notekeeper.data.provider.NoteKeeperProviderContract.*;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.jwhh.notekeeper.data.database.NoteKeeperDBOpenHelper;

public class NoteKeeperProvider extends ContentProvider {

    private NoteKeeperDBOpenHelper mDBOpenHelper;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int COURSES = 0;
    public static final int NOTES = 1;
    public static final int NOTES_EXPANDED = 2;
    public static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
        int deletedRow = -1;
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case COURSES:
                break;
            case NOTES: {
                deletedRow = db.delete(NoteInfoTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
        }

        return deletedRow;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case NOTES: {
                rowId = db.insert(NoteInfoTable.TABLE_NAME, null, values);
                //content://com.jwhh.notekeeper.provider/notes/rowId
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            }
            case COURSES: {
                rowId = db.insert(CourseInfoTable.TABLE_NAME, null, values);
                //content://com.jwhh.notekeeper.provider/courses/rowId
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            }
            case NOTES_EXPANDED:
                throw new UnsupportedOperationException("Insertion isn't Supported");
        }
        return rowUri;
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
            case NOTES_ROW: {
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoTable._ID + " = ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NoteInfoTable.TABLE_NAME, projection, rowSelection, rowSelectionArgs,
                        null, null, null);
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
        SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        int rows = 0;
        switch (uriMatch) {
            case COURSES:
                break;
            case NOTES:
                 break;
            case NOTES_ROW: {
                rows = db.update(NoteInfoTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
        }
        return rows;
    }
}