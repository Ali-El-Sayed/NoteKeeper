package com.jwhh.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
<<<<<<< HEAD
import androidx.lifecycle.ViewModelProvider;
=======
>>>>>>> 90ef693a4a8770e10c6e16b3a28b90926d609c16

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
<<<<<<< HEAD
    private NoteActivityViewModel mViewModel;
=======
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
>>>>>>> 90ef693a4a8770e10c6e16b3a28b90926d609c16

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

<<<<<<< HEAD
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);


=======
>>>>>>> 90ef693a4a8770e10c6e16b3a28b90926d609c16
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
<<<<<<< HEAD
        if (mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
=======
        if(mIsNewNote)
            return;

        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
>>>>>>> 90ef693a4a8770e10c6e16b3a28b90926d609c16

    }


    private void readDisplayStateValues() {
        /*
        mNote = intent.getParcelableExtra(NOTE_POSITION);
        mIsNewNote = mNote == null;
        * */
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET); // POSITION_NOT_SET  for default value
        mIsNewNote = position == POSITION_NOT_SET;

        if (mIsNewNote) {
            createNewNote();
        } else {
            mNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get(mNotePosition);

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
        // Inflate the menu; this adds items to the action bar if it is present.
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            if (mIsNewNote)
                DataManager.getInstance().removeNote(mNotePosition);
<<<<<<< HEAD
            else {
=======
            else{
>>>>>>> 90ef693a4a8770e10c6e16b3a28b90926d609c16
                storePreviousNoteValues();
            }
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        } else {
            saveNote();
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void storePreviousNoteValues() {
<<<<<<< HEAD
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
=======
          CourseInfo course =   DataManager.getInstance().getCourse(mOriginalNoteCourseId);
          mNote.setCourse(course);
          mNote.setTitle(mOriginalNoteTitle);
          mNote.setText(mOriginalNoteText);
>>>>>>> 90ef693a4a8770e10c6e16b3a28b90926d609c16
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
