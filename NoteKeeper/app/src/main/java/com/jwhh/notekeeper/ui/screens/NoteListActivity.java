package com.jwhh.notekeeper.ui.screens;

import static com.jwhh.notekeeper.data.database.NoteKeeperDBContract.*;
import static com.jwhh.notekeeper.data.provider.NoteKeeperProviderContract.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jwhh.notekeeper.BuildConfig;
import com.jwhh.notekeeper.SettingsActivity;
import com.jwhh.notekeeper.data.database.NoteKeeperDBOpenHelper;
import com.jwhh.notekeeper.services.NoteBackup;
import com.jwhh.notekeeper.services.NoteBackupService;
import com.jwhh.notekeeper.ui.adapters.CourseRecyclerAdapter;
import com.jwhh.notekeeper.ui.adapters.NoteRecyclerAdapter;
import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.data.model.CourseInfo;
import com.jwhh.notekeeper.data.model.DataManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_NOTES = 0;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
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
        ImageButton navBarImage = findViewById(R.id.nav_drawer_image);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
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
        });

        navBarImage.setOnClickListener(new View.OnClickListener() {
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
        enableStrictMode();

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
            case R.id.action_option_settings: {

                startActivity(new Intent(NoteListActivity.this, SettingsActivity.class));
                break;
            }
            case R.id.action_backup_notes: {
                backupNotes();
                break;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void backupNotes() {
        Intent intent = new Intent(NoteListActivity.this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
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
//        openDrawer();
//        loadNotes();
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);
//        mNoteRecyclerAdapter.notifyDataSetChanged();
    }

    private void openDrawer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        }, 1000);
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

//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
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

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//        switch (id) {
//            case R.id.nav_notes:
//                displayNotes();
//                break;
//
//            case R.id.nav_courses:
//                displayCourses();
//                break;
//            case R.id.nav_share:
//                handleShare();
//                break;
//        }
//
//        mDrawerLayout.closeDrawer(GravityCompat.START);
//        return true;
//    }

    private void handleShare() {
        Snackbar.make(mNavigationView, "Share To - " +
                        PreferenceManager.getDefaultSharedPreferences(this).getString("favourite_social_network", ""),
                Snackbar.LENGTH_SHORT).show();

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES) {
            final String[] noteColumns = {
                    Notes._ID,
                    Notes.COLUMN_NOTE_TITLE,
                    Courses.COLUMN_COURSE_TITLE
            };
            final String noteOrderBy = Courses.COLUMN_COURSE_TITLE +
                    " , " + Notes.COLUMN_NOTE_TITLE;

            loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI,
                    noteColumns, null, null, noteOrderBy);
        }
        return loader;
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            mNoteRecyclerAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES)
            mNoteRecyclerAdapter.changeCursor(null);
    }

    public void enableStrictMode() {
        if (BuildConfig.DEBUG) {
//                    .detectDiskReads().detectDiskWrites().detectNetwork().build();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }
}