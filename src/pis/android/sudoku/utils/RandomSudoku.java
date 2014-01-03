package pis.android.sudoku.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import pis.android.sudoku.db.SudokuColumns;
import pis.android.sudoku.db.SudokuDatabase;
import android.content.Context;
import android.database.Cursor;

public class RandomSudoku {

	public static final int BEGINNER_ID = 1;
	public static final int EASY_ID = 2;
	public static final int MEDIUM_ID = 3;
	public static final int HARD_ID = 4;
	public static final int EXPERT_ID = 5;
	public static final int LEGEND_ID = 6;

	private Cursor mCursor;
	private SudokuDatabase mDatabase;

	private static RandomSudoku sRandomSudoku;

	static Object sLock = new Object();

	public static RandomSudoku getInstance() {
		if (sRandomSudoku == null) {
			synchronized (sLock) {
				if (sRandomSudoku == null) {
					sRandomSudoku = new RandomSudoku();
				}
			}
		}
		return sRandomSudoku;
	}

	public RandomSudoku() {
	}

	public long randomSudokuGame(Context context, long folderId) {
		ArrayList<Long> mListSudokuGame = new ArrayList<Long>();
		// Query ra nhung thang xong roi va dang choi loai khoi mang
		mDatabase = new SudokuDatabase(context);
		mCursor = mDatabase.getSudokuNotStarted(folderId);
		try {
			if (mCursor != null && mCursor.getCount() > 0) {
				mCursor.move(-1);
				while (mCursor.moveToNext()) {
					long sudokuId = mCursor.getLong(mCursor
							.getColumnIndex(SudokuColumns._ID));
					mListSudokuGame.add(sudokuId);
				}
			}
		} finally {
			if (mCursor != null) {
				mCursor.close();
			}
		}
		mDatabase.close();
		int size = mListSudokuGame.size();
		if (size <= 0)
			return -1;
		Random random = new Random();
		int ranId = random.nextInt(size);
		return mListSudokuGame.get(ranId);
	}
}
