package com.jwhh.notekeeper.ui.screens;


import static com.jwhh.notekeeper.data.provider.NoteKeeperProviderContract.*;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.jwhh.notekeeper.notification.NotificationReceiver;
import com.jwhh.notekeeper.notification.NoteReminderNotification;
import com.jwhh.notekeeper.services.broadcast.CourseEventBroadcastHelper;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NOTE_ID = "com.jwhh.notekeeper.NOTE_INFO";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;

    private ProgressBar mProgressBar;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private long mNoteId;
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
    private Uri mNoteUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        mSpinnerCourses = findViewById(R.id.spinner_courses);
        mProgressBar = findViewById(R.id.progressBar);
        mDBOpenHelper = new NoteKeeperDBOpenHelper(this);


        // Configuration changes
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);
        if (mViewModel.mIsNewlyCreated && savedInstanceState != null)
            mViewModel.restoreState(savedInstanceState);
        mViewModel.mIsNewlyCreated = false;

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

        mNoteId = new Intent().getLongExtra(NOTE_ID, ID_NOT_SET);
        readDisplayStateValues();
        saveOriginalNoteValues();


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
                // DataManager.getInstance().removeNote(mNoteId);
                deleteNoteFromDatabase();
            else {
                storePreviousNoteValues();
            }
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        } else
            saveNote();

    }

    private void deleteNoteFromDatabase() {
        String selection = Notes._ID + " = ?";
        String[] selectionArgs = {Long.toString(mNoteId)};
        getContentResolver().delete(Notes.CONTENT_URI, selection, selectionArgs);

        if (mIsNewNote)
            Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Edit Ignored", Toast.LENGTH_SHORT).show();

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

        String[] selectionArgs = {Long.toString(mNoteId)};

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
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote)
            createNewNote();
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);
    }

    private void createNewNote() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            @Override
            protected void onPreExecute() {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                mProgressBar.setProgress(values[0]);
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                publishProgress(2);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(3);
                ContentValues insertValues = contentValues[0];
                return getContentResolver().insert(Notes.CONTENT_URI, insertValues);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mProgressBar.setVisibility(View.GONE);
                mNoteUri = uri;
            }
        };

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "android_intents");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");
        task.execute(values);
        mNoteId = ContentUris.parseId(mNoteUri);
//        SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
//        mNoteId = (int) db.insert(NoteInfoTable.TABLE_NAME, null, values);
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
//        mNoteCursor.close();
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);

        mNote.setText(noteText);
        mNote.setTitle(noteTitle);

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing a Note");
    }


    private int getIndexOfCourseId(String courseId) {
        if (mIsNewNote)
            return 0;
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoTable.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        while (cursor.moveToNext()) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (cursorCourseId.equals(courseId))
                break;
            courseRowIndex++;
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
        } else if (id == R.id.action_set_reminder) {
            mNoteId = ContentUris.parseId(mNoteUri);
            Random random = new Random();
            int notificationId = random.nextInt();
            NoteReminderNotification.notify(NoteActivity.this,
                    mNote.getTitle(),
                    mNote.getText(),
                    notificationId, mNoteId);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        PendingIntent openActivity, toastMessage;

        Random random = new Random();
        int notificationId = random.nextInt();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel("actionStyleNotificationId", "Action Style Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notification Description");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent openActivityIntent = new Intent(this, NoteActivity.class);
        Intent toastIntent = new Intent(this, NotificationReceiver.class);
//        toastIntent.putExtra("ToastMessage", "This is Message Extra");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            openActivity = PendingIntent.getActivity(this,
                    0,
                    openActivityIntent,
                    PendingIntent.FLAG_MUTABLE);
            toastMessage = PendingIntent.getBroadcast(this,
                    1,
                    toastIntent,
                    PendingIntent.FLAG_MUTABLE);
        } else {
            openActivity = PendingIntent.getActivity(this,
                    0,
                    openActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            toastMessage = PendingIntent.getBroadcast(this,
                    1,
                    toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "actionStyleNotificationId");

        builder.setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(mNote.getTitle())
                .setContentIntent(openActivity)
                .addAction(R.drawable.ic_launcher_foreground, "First Button", toastMessage)
                .addAction(R.drawable.ic_launcher_foreground, "Second Button", toastMessage);

        notificationManager.notify(notificationId, builder.build());

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
        mNote = DataManager.getInstance().getNotes().get((int) mNoteId);

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
//        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        String courseId = selectedCourseId();
        String noteText = mTextNoteText.getText().toString();
        String noteTitle = mTextNoteTitle.getText().toString();
        if (noteText.equals("") && noteTitle.equals("")) {
            deleteNoteFromDatabase();
            return;
        }

        saveNoteToDatabase(courseId, noteTitle, noteText);
        Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
    }

    private String selectedCourseId() {
        // get courseId from Spinner
        int selectedId = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedId);
        int courseTitle = cursor.getColumnIndex(CourseInfoTable.COLUMN_COURSE_ID);

        return cursor.getString(courseTitle);
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        String selection = NoteInfoTable._ID + " = ?";
        String[] selectionArgs = {Long.toString(mNoteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoTable.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoTable.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoTable.COLUMN_NOTE_TEXT, noteText);

//        SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
        //returns the number of effected rows
//        db.update(NoteInfoTable.TABLE_NAME, values, selection, selectionArgs);
        int rows = getContentResolver().update(mNoteUri, values, selection, selectionArgs);

    }


    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out What I learned in the Pluralsight course \""
                + course.getTitle() + ".\"\n" + mTextNoteTitle.getText();

        // Implicit Intent
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
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID,
        };

        return new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);

//        mCourseQueryFinished = false;

//        return new CursorLoader(this) {
//            @Nullable
//            @Override
//            protected Cursor onLoadInBackground() {
//                String[] courseColumns = {
//                        CourseInfoTable.COLUMN_COURSE_TITLE,
//                        CourseInfoTable.COLUMN_COURSE_ID,
//                        CourseInfoTable._ID,
//                };
//                SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
//
//                return db.query(CourseInfoTable.TABLE_NAME, courseColumns,
//                        null, null, null, null, CourseInfoTable.COLUMN_COURSE_TITLE);
//
//            }
//        };
    }

    private CursorLoader createLoaderNotes() {
        mNoteQueryFinished = false;
        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        Uri noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, noteUri, noteColumns,
                null, null, null);
//        return new CursorLoader(this) {
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
//
//                String selection = NoteInfoTable._ID + " = ? ";
//
//                String[] selectionArgs = {Integer.toString(mNoteId)};
//
//                String[] noteColumns = {
//                        NoteInfoTable.COLUMN_COURSE_ID,
//                        NoteInfoTable.COLUMN_NOTE_TITLE,
//                        NoteInfoTable.COLUMN_NOTE_TEXT
//                };
//
//                return mNoteCursor = db.query(NoteInfoTable.TABLE_NAME, noteColumns, selection, selectionArgs,
//                        null, null, null);
//
//            }
//        };
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
