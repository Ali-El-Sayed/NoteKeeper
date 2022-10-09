package com.jwhh.notekeeper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;


import com.jwhh.notekeeper.data.model.DataManager;
import com.jwhh.notekeeper.data.model.NoteInfo;
import com.jwhh.notekeeper.ui.screens.NoteListActivity;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class NextThroughNotesTest {
    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityActivityTestRule =
            new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void NextThroughNotes() {

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        int index = 0;

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

        onView(withId(R.id.list_notes))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(index, click()));

        while (index < notes.size() - 1) {
            NoteInfo note = notes.get(index);

            onView(withId(R.id.spinner_courses))
                    .check(matches(withSpinnerText(note.getCourse().getTitle())));
            onView(withId(R.id.text_note_text))
                    .check(matches(withText(note.getText())));
            onView(withId(R.id.text_note_title))
                    .check(matches(withText(note.getTitle())));

            if (index < notes.size())
                onView(allOf(withId(R.id.action_next), isEnabled())).perform(click());
            index++;
        }
        onView(withId(R.id.action_next)).check(matches(not(isEnabled())));
        pressBack();
    }
}

