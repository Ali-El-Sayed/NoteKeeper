package com.jwhh.notekeeper.ui.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jwhh.notekeeper.NoteActivityViewModel;
import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.data.model.CourseInfo;
import com.jwhh.notekeeper.data.model.DataManager;
import com.jwhh.notekeeper.data.model.NoteInfo;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

    public static final String NOTE_POSITION = "com.jwhh.notekeeper.NOTE_INFO";
    public static final int POSITION_NOT_SET = -1;

    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private int mNotePosition;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuration changes
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if (mViewModel.mIsNewlyCreated && savedInstanceState != null)
            mViewModel.restoreState(savedInstanceState);

        mViewModel.mIsNewlyCreated = false;

        mSpinnerCourses = findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        if (!mIsNewNote) displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

    }



    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();

    }


    private void readDisplayStateValues() {
        /*
        mNote = intent.getParcelableExtra(NOTE_POSITION);
        mIsNewNote = mNote == null;
        * */
        Intent intent = getIntent();
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET); // POSITION_NOT_SET  for default value
        mIsNewNote = mNotePosition == POSITION_NOT_SET;

        if (mIsNewNote)
            createNewNote();

        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
    }

    private void displayNote(Spinner spinner, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());

        spinner.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        item.setEnabled(mNotePosition < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

//        saveOriginalNoteValues();
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        invalidateMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            if (mIsNewNote)
                DataManager.getInstance().removeNote(mNotePosition);
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
    protected void onSaveInstanceState(Bundle outState) {//verify the bundle isn't a null
        super.onSaveInstanceState(outState);

        if (outState != null)
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
}