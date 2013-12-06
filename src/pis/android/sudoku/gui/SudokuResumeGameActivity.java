/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package pis.android.sudoku.gui;

import java.text.DateFormat;
import java.util.Date;

import pis.android.sudoku.R;
import pis.android.sudoku.db.SudokuColumns;
import pis.android.sudoku.db.SudokuDatabase;
import pis.android.sudoku.game.CellCollection;
import pis.android.sudoku.game.SudokuGame;
import pis.android.sudoku.utils.AndroidUtils;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

/**
 * List of puzzles in folder.
 * 
 * @author romario
 * 
 */
public class SudokuResumeGameActivity extends ListActivity {

	public static final String EXTRA_FOLDER_ID = "folder_id";

	private static final String FILTER_STATE_NOT_STARTED = "filter"
			+ SudokuGame.GAME_STATE_NOT_STARTED;
	private static final String TAG = "SudokuListActivity";

	private long mDeletePuzzleID;
	private long mResetPuzzleID;
	private long mEditNotePuzzleID;
	private SudokuListFilter mListFilter;

	private SudokuResumeGameAdapter mAdapter;
	private Cursor mCursor;
	private SudokuDatabase mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sudoku_list);
		AndroidUtils.setupAdNetwork(this);

		getListView().setOnCreateContextMenuListener(this);
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		mDatabase = new SudokuDatabase(getApplicationContext());
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mListFilter = new SudokuListFilter(getApplicationContext());
		mListFilter.showStateNotStarted = settings.getBoolean(
				FILTER_STATE_NOT_STARTED, true);
		AndroidUtils.setBackGroundActionBar(getApplicationContext(),
				getActionBar());
		updateList();
		setListAdapter(mAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mDatabase.close();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong("mDeletePuzzleID", mDeletePuzzleID);
		outState.putLong("mResetPuzzleID", mResetPuzzleID);
		outState.putLong("mEditNotePuzzleID", mEditNotePuzzleID);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

		mDeletePuzzleID = state.getLong("mDeletePuzzleID");
		mResetPuzzleID = state.getLong("mResetPuzzleID");
		mEditNotePuzzleID = state.getLong("mEditNotePuzzleID");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// the puzzle list is naturally refreshed when the window
		// regains focus, so we only need to update the title
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		playSudoku(id);
	}

	/**
	 * Updates whole list.
	 */
	private void updateList() {

		if (mCursor != null) {
			stopManagingCursor(mCursor);
		}
		mCursor = mDatabase.getSudokuPlaying();
		startManagingCursor(mCursor);
		mAdapter = new SudokuResumeGameAdapter(this, mCursor);
	}

	private void playSudoku(long sudokuID) {
		Intent i = new Intent(SudokuResumeGameActivity.this,
				SudokuPlayActivity.class);
		i.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, sudokuID);
		startActivity(i);
		finish();
	}

	// ================================ Them vao =============================
	private static class SudokuResumeGameAdapter extends CursorAdapter {

		private Context mContext;
		private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
		private DateFormat mDateTimeFormatter = DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.SHORT);
		private DateFormat mTimeFormatter = DateFormat
				.getTimeInstance(DateFormat.SHORT);
		private LayoutInflater mLayoutInflater;

		public SudokuResumeGameAdapter(Context context, Cursor c) {
			super(context, c);
			mContext = context;
			mLayoutInflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = mLayoutInflater.inflate(R.layout.sudoku_list_item,
					parent, false);
			ResumeGameHoder resumeGameHoder = new ResumeGameHoder();
			resumeGameHoder.sudokuBoardView = (SudokuBoardView) view
					.findViewById(R.id.sudoku_board);
			resumeGameHoder.level = (TextView) view.findViewById(R.id.level);
			resumeGameHoder.time = (TextView) view.findViewById(R.id.time);
			resumeGameHoder.lastplayed = (TextView) view
					.findViewById(R.id.last_played);
			view.setTag(resumeGameHoder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ResumeGameHoder resumeGameHoder = (ResumeGameHoder) view.getTag();
			setSudokuBoardView(resumeGameHoder.sudokuBoardView, cursor);
			setLevel(resumeGameHoder.level, cursor);
			setTime(resumeGameHoder.time, cursor);
			setLastPlay(resumeGameHoder.lastplayed, cursor);
		}

		private void setSudokuBoardView(SudokuBoardView board, Cursor c) {
			String data = c.getString(c.getColumnIndex(SudokuColumns.DATA));
			CellCollection cells = null;
			try {
				cells = CellCollection.deserialize(data);
			} catch (Exception e) {
				long id = c.getLong(c.getColumnIndex(SudokuColumns._ID));
				Log.e(TAG,
						String.format(
								"Exception occurred when deserializing puzzle with id %s.",
								id), e);
			}
			board.setReadOnly(true);
			board.setFocusable(false);
			board.setCells(cells);
		}

		private void setLevel(TextView textView, Cursor c) {
			int folder = c.getInt(c.getColumnIndex(SudokuColumns.FOLDER_ID));
			textView.setText(mContext.getResources().getString(R.string.level,
					"" + folder));
		}

		private void setTime(TextView textView, Cursor c) {
			long time = c.getLong(c.getColumnIndex(SudokuColumns.TIME));
			String timeString = null;
			if (time != 0) {
				timeString = mGameTimeFormatter.format(time);
			}
			textView.setText(mContext.getResources().getString(R.string.time,
					timeString));
		}

		private void setLastPlay(TextView textView, Cursor c) {
			long lastPlayed = c.getLong(c
					.getColumnIndex(SudokuColumns.LAST_PLAYED));
			String lastPlayedString = null;
			if (lastPlayed != 0) {
				lastPlayedString = mContext.getString(R.string.last_played_at,
						getDateAndTimeForHumans(lastPlayed));
				textView.setText(lastPlayedString);
			}
		}

		private String getDateAndTimeForHumans(long datetime) {
			Date date = new Date(datetime);

			Date now = new Date(System.currentTimeMillis());
			Date today = new Date(now.getYear(), now.getMonth(), now.getDate());
			Date yesterday = new Date(System.currentTimeMillis()
					- (1000 * 60 * 60 * 24));

			if (date.after(today)) {
				return mContext.getString(R.string.at_time,
						mTimeFormatter.format(date));
			} else if (date.after(yesterday)) {
				return mContext.getString(R.string.yesterday_at_time,
						mTimeFormatter.format(date));
			} else {
				return mContext.getString(R.string.on_date,
						mDateTimeFormatter.format(date));
			}

		}

		public static class ResumeGameHoder {
			SudokuBoardView sudokuBoardView;
			TextView level;
			TextView time;
			TextView lastplayed;
		}

	}
}
