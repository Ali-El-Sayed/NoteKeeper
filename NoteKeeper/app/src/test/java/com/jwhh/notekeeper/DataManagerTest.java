package com.jwhh.notekeeper;

//import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.jwhh.notekeeper.data.model.CourseInfo;
import com.jwhh.notekeeper.data.model.DataManager;
import com.jwhh.notekeeper.data.model.NoteInfo;

public class DataManagerTest {
    private static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() {
        sDataManager = DataManager.getInstance();
    }


    @Before
    public void setUp() {
        sDataManager.getNotes().clear();
        sDataManager.initializeExampleNotes();
    }

    @Test
    public void createNewNote() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test Note Title";
        final String noteText = "This is the body of my test note";

        int noteIndex = sDataManager.createNewNote();

        NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
        // assertSame() isn't a valid test as we want to assure the values are the same not just the index.
        // assertSame(newNote,compareNote);

        assertEquals(course, compareNote.getCourse());
        assertEquals(noteTitle, compareNote.getTitle());
        assertEquals(noteText, compareNote.getText());
    }

    @Test
    public void findSimilarNotes() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test Note Title";
        final String noteText2 = "This is the body of my test note";
        final String noteText1 = "This is the body of my second test note";

        int noteIndex1 = sDataManager.createNewNote();
        NoteInfo newNote1 = sDataManager.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(noteTitle);
        newNote1.setText(noteText1);

        int noteIndex2 = sDataManager.createNewNote();
        NoteInfo newNote2 = sDataManager.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(noteTitle);
        newNote2.setText(noteText2);

        int foundIndex1 = sDataManager.findNote(newNote1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = sDataManager.findNote(newNote2);
        assertEquals(foundIndex2, foundIndex2);
    }

    @Test
    public void createNewNoteOneStepCreation() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test note titel";
        final String noteText = "Test note text";

        int noteIndex = sDataManager.createNewNote(course, noteTitle, noteText);

        NoteInfo note = sDataManager.getNotes().get(noteIndex);
        assertEquals(note.getCourse(), course);
        assertEquals(note.getTitle(), noteTitle);
        assertEquals(note.getText(), noteText);

    }
}
