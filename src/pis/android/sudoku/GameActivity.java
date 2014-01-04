package pis.android.sudoku;

import pis.android.sudoku.db.SudokuDatabase;
import pis.android.sudoku.gui.SudokuHightScore;
import pis.android.sudoku.gui.SudokuLevelActivity;
import pis.android.sudoku.gui.SudokuPlayActivity;
import pis.android.sudoku.gui.SudokuResumeGameActivity;
import pis.android.sudoku.service.MusicService;
import pis.android.sudoku.utils.AndroidUtils;
import pis.android.sudoku.utils.RandomSudoku;
import pis.android.sudoku.widget.ButtonWithFont;
import pis.android.sudoku.widget.TextViewWithFont;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;

public class GameActivity extends Activity implements OnClickListener {
	private ButtonWithFont mResumeGame;
	private ButtonWithFont mNewGame;
	private ButtonWithFont mHighScore;
	private ButtonWithFont mHowto;
	private SudokuDatabase mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game_layout);
		AndroidUtils.setBackGroundActionBar(getApplicationContext(),
				getActionBar());
		mNewGame = (ButtonWithFont) findViewById(R.id.new_game);
		mNewGame.setOnClickListener(this);
		mResumeGame = (ButtonWithFont) findViewById(R.id.resume_game);
		mResumeGame.setOnClickListener(this);
		mHighScore = (ButtonWithFont) findViewById(R.id.high_score);
		mHighScore.setOnClickListener(this);
		mHowto = (ButtonWithFont) findViewById(R.id.howto);
		mHowto.setOnClickListener(this);
		findViewById(R.id.about).setOnClickListener(this);
		mDatabase = new SudokuDatabase(getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		// check trang thai button resume
		mResumeGame.setEnabled(mDatabase.hasResumeGame());
		mHighScore.setEnabled(mDatabase.hasHighScore());
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
					SudokuLevelActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.resume_game: {
			Intent intent = new Intent(GameActivity.this,
					SudokuResumeGameActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.high_score: {
			Intent intent = new Intent(GameActivity.this,
					SudokuHightScore.class);
			startActivity(intent);
			break;
		}
		case R.id.howto: {
			new AlertDialog.Builder(this)
					.setTitle(R.string.how_to)
					.setMessage(getString(R.string.howto))
					.setPositiveButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create().show();
			break;
		}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
	}
}
