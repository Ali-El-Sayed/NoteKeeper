package com.jwhh.notekeeper;

//import junit.framework.TestCase;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest  {

    @Test
    public void createNewNote() {
        DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test Note Title";
        final String noteText = "This is the body of my test note";

        int noteIndex = dm.createNewNote();

        NoteInfo newNote = dm.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        NoteInfo compareNote = dm.getNotes().get(noteIndex);

        // assertSame() isn't a valid test as we want to assure the values are the same not just the index.
       // assertSame(newNote,compareNote);

        assertEquals(course,compareNote.getCourse());
        assertEquals(noteTitle,compareNote.getTitle());
        assertEquals(noteText,compareNote.getText());
    }
}
