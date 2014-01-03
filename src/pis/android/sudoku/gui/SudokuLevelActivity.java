package pis.android.sudoku.gui;

import pis.android.sudoku.GameActivity;
import pis.android.sudoku.R;
import pis.android.sudoku.db.SudokuDatabase;
import pis.android.sudoku.utils.RandomSudoku;
import pis.android.sudoku.widget.ButtonWithFont;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

public class SudokuLevelActivity extends Activity implements OnClickListener {

	private ButtonWithFont mBeginnerButton;
	private ButtonWithFont mEasyButton;
	private ButtonWithFont mMediumButton;
	private ButtonWithFont mHardButton;
	private ButtonWithFont mExpertButton;
	private ButtonWithFont mLegendButton;
	private SudokuDatabase mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.soduku_level_layout);
		mBeginnerButton = (ButtonWithFont) findViewById(R.id.beginner_level);
		mBeginnerButton.setOnClickListener(this);
		mEasyButton = (ButtonWithFont) findViewById(R.id.easy_level);
		mEasyButton.setOnClickListener(this);
		mMediumButton = (ButtonWithFont) findViewById(R.id.medium_level);
		mMediumButton.setOnClickListener(this);
		mHardButton = (ButtonWithFont) findViewById(R.id.hard_level);
		mHardButton.setOnClickListener(this);
		mExpertButton = (ButtonWithFont) findViewById(R.id.expert_level);
		mExpertButton.setOnClickListener(this);
		mLegendButton = (ButtonWithFont) findViewById(R.id.legend_level);
		mLegendButton.setOnClickListener(this);
		mDatabase = new SudokuDatabase(getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBeginnerButton.setEnabled(mDatabase
				.isLevelClear(RandomSudoku.BEGINNER_ID));
		mEasyButton.setEnabled(mDatabase.isLevelClear(RandomSudoku.EASY_ID));
		mMediumButton
				.setEnabled(mDatabase.isLevelClear(RandomSudoku.MEDIUM_ID));
		mHardButton.setEnabled(mDatabase.isLevelClear(RandomSudoku.HARD_ID));
		mExpertButton
				.setEnabled(mDatabase.isLevelClear(RandomSudoku.EXPERT_ID));
		mLegendButton
				.setEnabled(mDatabase.isLevelClear(RandomSudoku.LEGEND_ID));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.beginner_level:
			newSudoku(RandomSudoku.BEGINNER_ID);
			break;
		case R.id.easy_level:
			newSudoku(RandomSudoku.EASY_ID);
			break;
		case R.id.medium_level:
			newSudoku(RandomSudoku.MEDIUM_ID);
			break;
		case R.id.hard_level:
			newSudoku(RandomSudoku.HARD_ID);
			break;
		case R.id.expert_level:
			newSudoku(RandomSudoku.EXPERT_ID);
			break;
		case R.id.legend_level:
			newSudoku(RandomSudoku.LEGEND_ID);
			break;
		}
	}

	private void newSudoku(long folderId) {
		Intent i = new Intent(SudokuLevelActivity.this,
				SudokuPlayActivity.class);
		i.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, RandomSudoku
				.getInstance().randomSudokuGame(this, folderId));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
	}
}
