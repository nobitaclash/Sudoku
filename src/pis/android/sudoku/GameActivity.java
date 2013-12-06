package pis.android.sudoku;

import pis.android.sudoku.db.SudokuDatabase;
import pis.android.sudoku.gui.SudokuPlayActivity;
import pis.android.sudoku.gui.SudokuResumeGameActivity;
import pis.android.sudoku.service.MusicService;
import pis.android.sudoku.utils.AndroidUtils;
import pis.android.sudoku.widget.ButtonWithFont;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class GameActivity extends Activity implements OnClickListener {
	private ButtonWithFont mResumeGame;
	private ButtonWithFont mNewGame;
	SudokuDatabase mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_layout);
		boolean isInitalized = PreferenceManager.getDefaultSharedPreferences(
				this).getBoolean("DB_HAS_BEEN_INITIALIZED", false);
		if (!isInitalized) {
			if (SudokuDatabase.createAndInitDB(this)) {
				PreferenceManager.getDefaultSharedPreferences(this).edit()
						.putBoolean("DB_HAS_BEEN_INITIALIZED", true).commit();
			} else {
				finish();
				return;
			}
		}
		AndroidUtils.setBackGroundActionBar(getApplicationContext(), getActionBar());
		mNewGame = (ButtonWithFont) findViewById(R.id.new_game);
		mNewGame.setOnClickListener(this);
		mResumeGame = (ButtonWithFont) findViewById(R.id.resume_game);
		mResumeGame.setOnClickListener(this);
		mResumeGame.setEnabled(new SudokuDatabase(this).hasResumeGame());
	}

	@Override
	protected void onResume() {
		super.onResume();
		startService(new Intent(this, MusicService.class));
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopService(new Intent(this, MusicService.class));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.new_game: {
			Intent intent = new Intent(GameActivity.this,
					SudokuPlayActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.resume_game: {
			Intent intent = new Intent(GameActivity.this,
					SudokuResumeGameActivity.class);
			startActivity(intent);
			break;
		}
		}

	}

}
