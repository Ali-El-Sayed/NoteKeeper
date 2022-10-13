package com.jwhh.notekeeper.ui.screens;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jwhh.notekeeper.NoteActivityViewModel;
import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.data.database.NoteKeeperDBContract.CourseInfoTable;
import com.jwhh.notekeeper.data.database.NoteKeeperDBContract.NoteInfoTable;
import com.jwhh.notekeeper.data.database.NoteKeeperDBOpenHelper;
import com.jwhh.notekeeper.data.model.CourseInfo;
import com.jwhh.notekeeper.data.model.DataManager;
import com.jwhh.notekeeper.data.model.NoteInfo;

import org.jetbrains.annotations.NotNull;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NOTE_ID = "com.jwhh.notekeeper.NOTE_INFO";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;

    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private int mNoteId;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;
    private NoteKeeperDBOpenHelper mDBOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCourseQueryFinished;
    private boolean mNoteQueryFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDBOpenHelper = new NoteKeeperDBOpenHelper(this);


        // Configuration changes
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);
        if (mViewModel.mIsNewlyCreated && savedInstanceState != null)
            mViewModel.restoreState(savedInstanceState);

        mViewModel.mIsNewlyCreated = false;
        mSpinnerCourses = findViewById(R.id.spinner_courses);

//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        ArrayAdapter<CourseInfo> adapterCourses =
//                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoTable.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);
//        loadCourseData();
        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
        saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote)
//            loadNoteData();
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start the Call from onCreateLoader
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);

        // Start the Call from onLoaderFinished
//        LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            if (mIsNewNote)
                DataManager.getInstance().removeNote(mNoteId);
            else {
                storePreviousNoteValues();
            }
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        } else {
            saveNote();
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        mDBOpenHelper.close();
        super.onDestroy();
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoTable._ID + " = ? ";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoTable.COLUMN_COURSE_ID,
                NoteInfoTable.COLUMN_NOTE_TITLE,
                NoteInfoTable.COLUMN_NOTE_TEXT
        };

        mNoteCursor = db.query(NoteInfoTable.TABLE_NAME, noteColumns, selection, selectionArgs, null, null, null);

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoTable.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoTable.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoTable.COLUMN_NOTE_TEXT);

        mNoteCursor.moveToNext();
        displayNote();
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoTable.COLUMN_COURSE_TITLE,
                CourseInfoTable.COLUMN_COURSE_ID,
                CourseInfoTable._ID,
        };

        Cursor cursor = db.query(CourseInfoTable.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoTable.COLUMN_COURSE_TITLE);

        mAdapterCourses.changeCursor(cursor);
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();

    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET); // ID_NOT_SET  for default value
        mIsNewNote = mNoteId == ID_NOT_SET;

        if (mIsNewNote)
            createNewNote();
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNoteId = dm.createNewNote();
    }

    private void displayNote() {
        /*
                    Before Database
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(mNote.getTitle());
        mTextNoteText.setText(mNote.getText());
        * */
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
//        int courseIndex = courses.indexOf(course);

        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
        mNoteCursor.close();

    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoTable.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (cursorCourseId.equals(courseId))
                break;
            courseRowIndex++;
            cursor.moveToNext();
        }

        return courseRowIndex;
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();

            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
            return true;
        } else if (id == R.id.action_next) {
            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

//        saveOriginalNoteValues();
        displayNote();
        invalidateMenu();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        //verify the bundle isn't a null
        super.onSaveInstanceState(outState);
        mViewModel.saveState(outState);
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setText(mTextNoteText.getText().toString());
        mNote.setTitle(mTextNoteTitle.getText().toString());
    }


    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out What I learned in the Pluralsight course \""
                + course.getTitle() + ".\"\n" + mTextNoteTitle.getText();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822"); //Standard Email MIME type

        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //when loader is created
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
        CursorLoader loader = null;

        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //when data is finished
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if (loader.getId() == LOADER_COURSES) {
            mAdapterCourses.changeCursor(data);
            mCourseQueryFinished = true;
            displayNoteWhenQueryFinished();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //clean up
        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor != null)
                mNoteCursor.close();
        } else if (loader.getId() == LOADER_COURSES)
            mAdapterCourses.getCursor().close();
    }

    private CursorLoader createLoaderCourses() {
        mCourseQueryFinished = false;

        return new CursorLoader(this) {
            @Nullable
            @Override
            protected Cursor onLoadInBackground() {
                SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoTable.COLUMN_COURSE_TITLE,
                        CourseInfoTable.COLUMN_COURSE_ID,
                        CourseInfoTable._ID,
                };

                return db.query(CourseInfoTable.TABLE_NAME, courseColumns,
                        null, null, null, null, CourseInfoTable.COLUMN_COURSE_TITLE);

            }
        };
    }

    private CursorLoader createLoaderNotes() {
        mNoteQueryFinished = false;
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();

                String courseId = "android_intents";
                String titleStart = "dynamic";

                String selection = NoteInfoTable._ID + " = ? ";

                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                        NoteInfoTable.COLUMN_COURSE_ID,
                        NoteInfoTable.COLUMN_NOTE_TITLE,
                        NoteInfoTable.COLUMN_NOTE_TEXT
                };

                return mNoteCursor = db.query(NoteInfoTable.TABLE_NAME, noteColumns, selection, selectionArgs,
                        null, null, null);

            }
        };
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoTable.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoTable.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoTable.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        mNoteQueryFinished = true;
        displayNoteWhenQueryFinished();
    }

    private void displayNoteWhenQueryFinished() {
        if (mNoteQueryFinished && mCourseQueryFinished)
            displayNote();
    }
}
