package pis.android.sudoku.gui;

import java.util.ArrayList;
import java.util.List;

import pis.android.sudoku.R;
import pis.android.sudoku.db.SudokuColumns;
import pis.android.sudoku.db.SudokuDatabase;
import pis.android.sudoku.widget.ButtonWithFont;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SudokuHightScore extends ListActivity {
	private static final int MAX_LEVEL = 6;
	private Cursor mCursor;
	private SudokuDatabase mDatabase;
	private Context mContext;
	private ArrayList<SudokuHighScoreItem> mHighScoreItems;
	private SudokuHighScoreAdapter mSudokuHighScoreAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.high_score_layout);
		mDatabase = new SudokuDatabase(getApplicationContext());
		mHighScoreItems = new ArrayList<SudokuHightScore.SudokuHighScoreItem>();
		mContext = this;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
		mContext = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateListCompleted();
	}

	private void updateListCompleted() {
		mHighScoreItems.clear();
		if (mCursor != null) {
			stopManagingCursor(mCursor);
		}
		for (int folderId = 1; folderId <= MAX_LEVEL; folderId++) {
			mCursor = mDatabase.getSudokuCompleted(folderId);
			try {
				if (mCursor != null && mCursor.getCount() > 0) {
					SudokuHighScoreItem sudokuHighScoreItem = new SudokuHighScoreItem();
					sudokuHighScoreItem.level = folderId;
					sudokuHighScoreItem.totalCompleted = mCursor.getCount();
					if (mCursor.moveToFirst())
						sudokuHighScoreItem.timeOne = mCursor.getLong(mCursor
								.getColumnIndex(SudokuColumns.TIME));
					if (mCursor.moveToNext())
						sudokuHighScoreItem.timeTwo = mCursor.getLong(mCursor
								.getColumnIndex(SudokuColumns.TIME));
					if (mCursor.moveToNext())
						sudokuHighScoreItem.timeThree = mCursor.getLong(mCursor
								.getColumnIndex(SudokuColumns.TIME));
					mHighScoreItems.add(sudokuHighScoreItem);
				}
			} finally {
				if (mCursor != null) {
					mCursor.close();
				}
			}
		}
		mSudokuHighScoreAdapter = new SudokuHighScoreAdapter(mContext,
				mHighScoreItems);
		getListView().setAdapter(mSudokuHighScoreAdapter);
	}

	private static class SudokuHighScoreItem {
		int level;
		int totalCompleted;
		long timeOne;
		long timeTwo;
		long timeThree;
	}

	private static class SudokuHighScoreAdapter extends
			ArrayAdapter<SudokuHighScoreItem> {
		private LayoutInflater mLayoutInflater;
		private String[] mLevels;
		private ArrayList<SudokuHighScoreItem> mHighScoreItems;
		private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
		private Context mContext;

		public SudokuHighScoreAdapter(Context context,
				ArrayList<SudokuHighScoreItem> highScoreItems) {
			super(context, R.layout.high_score_layout_item, highScoreItems);
			mContext = context;
			mLayoutInflater = LayoutInflater.from(context);
			mHighScoreItems = highScoreItems;
			mLevels = context.getResources().getStringArray(
					R.array.sudoku_level);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SudokuHighScoreHoder sudokuHighScoreHoder;
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(
						R.layout.high_score_layout_item, parent, false);
				sudokuHighScoreHoder = new SudokuHighScoreHoder();
				sudokuHighScoreHoder.level = (TextView) convertView
						.findViewById(R.id.level);
				sudokuHighScoreHoder.totalCompleted = (TextView) convertView
						.findViewById(R.id.total_completed);
				sudokuHighScoreHoder.timeOne = (TextView) convertView
						.findViewById(R.id.one);
				sudokuHighScoreHoder.timeTwo = (TextView) convertView
						.findViewById(R.id.two);
				sudokuHighScoreHoder.timeThree = (TextView) convertView
						.findViewById(R.id.three);
				convertView.setTag(sudokuHighScoreHoder);
			} else {
				sudokuHighScoreHoder = (SudokuHighScoreHoder) convertView
						.getTag();
			}
			SudokuHighScoreItem sudokuHighScoreItem = mHighScoreItems
					.get(position);
			sudokuHighScoreHoder.level
					.setText(mLevels[sudokuHighScoreItem.level - 1]);
			sudokuHighScoreHoder.totalCompleted
					.setText(String.valueOf(sudokuHighScoreItem.totalCompleted));
			if (sudokuHighScoreItem.timeOne != 0) {
				sudokuHighScoreHoder.timeOne.setText(mContext.getResources()
						.getString(
								R.string.time_one,
								mGameTimeFormatter
										.format(sudokuHighScoreItem.timeOne)));
			}
			if (sudokuHighScoreItem.timeTwo != 0) {
				sudokuHighScoreHoder.timeTwo
						.setText(mContext.getResources().getString(
								R.string.time_two,
								mGameTimeFormatter
										.format(sudokuHighScoreItem.timeTwo)));
			}
			if (sudokuHighScoreItem.timeThree != 0) {
				sudokuHighScoreHoder.timeThree
						.setText(mContext.getResources().getString(
								R.string.time_three,
								mGameTimeFormatter
										.format(sudokuHighScoreItem.timeThree)));
			}
			return convertView;
		}

		private static class SudokuHighScoreHoder {
			TextView level;
			TextView totalCompleted;
			TextView timeOne;
			TextView timeTwo;
			TextView timeThree;
		}
	}
}
