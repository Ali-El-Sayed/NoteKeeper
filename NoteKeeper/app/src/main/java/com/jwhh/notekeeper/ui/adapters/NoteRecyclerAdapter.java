package com.jwhh.notekeeper.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.data.database.NoteKeeperDBContract.NoteInfoTable;
import com.jwhh.notekeeper.ui.screens.NoteActivity;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.DataHolder> {
    private static Context mContext;
    //    private final List<NoteInfo> mNotes;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;

    //     List<NoteInfo> notes,
    //     mNotes = notes;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mCursor = cursor;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnsPosition();
    }

    private void populateColumnsPosition() {
        if (mCursor == null)
            return;
        // Get Column Indexes from mCursor

        mCoursePos = mCursor.getColumnIndex(NoteInfoTable.COLUMN_COURSE_ID);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoTable.COLUMN_NOTE_TITLE);
        mIdPos = mCursor.getColumnIndex(NoteInfoTable._ID);
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null)
            mCursor.close();
        mCursor = cursor;
        populateColumnsPosition();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);
        return new DataHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DataHolder holder, int position) {
        mCursor.moveToPosition(position);
        String courseTitle = mCursor.getString(mCoursePos);
        String noteTitle = mCursor.getString(mNoteTitlePos);
        int noteId = mCursor.getInt(mIdPos);

        holder.mTextCourse.setText(courseTitle);
        holder.mTextTitle.setText(noteTitle);
        holder.mId = noteId;

    }

    @Override
    public int getItemCount() {
//        return mNotes.size();
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public class DataHolder extends RecyclerView.ViewHolder {


        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mId;

        public DataHolder(View itemView) {
            super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(intent);
                }
            });

        }
    }
}
