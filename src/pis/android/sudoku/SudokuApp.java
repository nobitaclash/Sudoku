package pis.android.sudoku;

import pis.android.sudoku.db.SudokuDatabase;
import pis.android.sudoku.utils.RandomSudoku;
import android.app.Application;
import android.preference.PreferenceManager;

public class SudokuApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		boolean isInitalized = PreferenceManager.getDefaultSharedPreferences(
				this).getBoolean("DB_HAS_BEEN_INITIALIZED", false);
		if (!isInitalized) {
			if (SudokuDatabase.createAndInitDB(this)) {
				PreferenceManager.getDefaultSharedPreferences(this).edit()
						.putBoolean("DB_HAS_BEEN_INITIALIZED", true).commit();
			} else {
				return;
			}
		}
	}
}
