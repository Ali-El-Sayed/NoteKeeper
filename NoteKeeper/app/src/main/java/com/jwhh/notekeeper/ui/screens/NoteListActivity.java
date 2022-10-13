package com.jwhh.notekeeper.ui.screens;

import static com.jwhh.notekeeper.data.database.NoteKeeperDBContract.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jwhh.notekeeper.SettingsActivity;
import com.jwhh.notekeeper.data.database.NoteKeeperDBContract;
import com.jwhh.notekeeper.data.database.NoteKeeperDBOpenHelper;
import com.jwhh.notekeeper.ui.adapters.CourseRecyclerAdapter;
import com.jwhh.notekeeper.ui.adapters.NoteRecyclerAdapter;
import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.data.model.CourseInfo;
import com.jwhh.notekeeper.data.model.DataManager;
import com.jwhh.notekeeper.data.model.NoteInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private ImageButton mNavBarImage;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCourseLayoutManager;
    private NoteKeeperDBOpenHelper mDBOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDBOpenHelper = new NoteKeeperDBOpenHelper(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavBarImage = findViewById(R.id.nav_drawer_image);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mNavBarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });

        initializeDisplayContent();

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        updateNavHeader();
        return true;
    }

    private void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.userName);
        TextView textUserEmail = headerView.findViewById(R.id.userEmail);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = pref.getString("user_display_name", "Note Keeper");
        String userEmail = pref.getString("user_email_address", "name@host.com");

        textUserName.setText(userName);
        textUserEmail.setText(userEmail);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.optionSettings: {

                startActivity(new Intent(NoteListActivity.this, SettingsActivity.class));
                break;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isOpen())
            mDrawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
//        mNoteRecyclerAdapter.notifyDataSetChanged();
    }

    private void loadNotes() {
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
        final String[] NoteColumns = {
                NoteInfoTable.COLUMN_COURSE_ID,
                NoteInfoTable.COLUMN_NOTE_TITLE,
                NoteInfoTable._ID
        };

        final String noteOrderBy = NoteInfoTable.COLUMN_COURSE_ID + " , " +
                NoteInfoTable.COLUMN_NOTE_TITLE;

        Cursor noteCursor = db.query(NoteInfoTable.TABLE_NAME,
                NoteColumns,
                null, null, null, null, noteOrderBy);

        mNoteRecyclerAdapter.changeCursor(noteCursor);
    }

    private void initializeDisplayContent() {
        DataManager.loadFromDatabase(mDBOpenHelper);
        mRecyclerItems = findViewById(R.id.list_notes);

        mNotesLayoutManager = new LinearLayoutManager(this);
        mCourseLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, notes);
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);


        displayNotes();

    }

    @Override
    protected void onDestroy() {
        mDBOpenHelper.close();
        super.onDestroy();

    }

    private void displayCourses() {
        mRecyclerItems.setLayoutManager(mCourseLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);
        selectNavigationMenuItem(R.id.nav_courses);
    }

    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        Menu menu = mNavigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_notes:
                displayNotes();
                break;

            case R.id.nav_courses:
                displayCourses();
                break;
            case R.id.nav_share:
                handleShare();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {
        Snackbar.make(mNavigationView, "Share To - " +
                        PreferenceManager.getDefaultSharedPreferences(this).getString("favourite_social_network", ""),
                Snackbar.LENGTH_SHORT).show();

    }

    private void handleSelection(String message) {
        Snackbar.make(mNavigationView, message, Snackbar.LENGTH_SHORT).show();
    }

}