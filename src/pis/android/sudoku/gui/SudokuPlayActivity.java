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

import pis.android.sudoku.R;
import pis.android.sudoku.db.SudokuDatabase;
import pis.android.sudoku.game.SudokuGame;
import pis.android.sudoku.game.SudokuGame.OnPuzzleSolvedListener;
import pis.android.sudoku.game.SudokuGame.OnUndoListener;
import pis.android.sudoku.gui.inputmethod.IMControlPanel;
import pis.android.sudoku.gui.inputmethod.IMControlPanelStatePersister;
import pis.android.sudoku.gui.inputmethod.IMNumpad;
import pis.android.sudoku.gui.inputmethod.IMNumpad.OnUpdateStartGame;
import pis.android.sudoku.utils.AndroidUtils;
import pis.android.sudoku.utils.RandomSudoku;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

/*
 */
public class SudokuPlayActivity extends Activity {

	public static final String EXTRA_SUDOKU_ID = "sudoku_id";
	public static final String EXTRA_FODER_ID = "forder_id";
	public static final int MENU_ITEM_RESTART = Menu.FIRST;
	public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
	public static final int MENU_ITEM_HELP = Menu.FIRST + 2;
	public static final int MENU_ITEM_REFRESH = Menu.FIRST + 3;

	private static final int DIALOG_RESTART = 1;
	private static final int DIALOG_WELL_DONE = 2;
	private static final int DIALOG_CLEAR_NOTES = 3;
	private static final int DIALOG_UNDO_TO_CHECKPOINT = 4;

	private static final int REQUEST_SETTINGS = 1;

	private long mSudokuGameID;
	private long mForderId;
	private SudokuGame mSudokuGame;

	private SudokuDatabase mDatabase;

	private ViewGroup mRootLayout;
	private SudokuBoardView mSudokuBoard;
	private TextView mTimeLabel;

	private IMControlPanel mIMControlPanel;
	private IMControlPanelStatePersister mIMControlPanelStatePersister;
	private IMNumpad mIMNumpad;

	private boolean mShowTime = true;
	private GameTimer mGameTimer;
	private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();

	private HintsQueue mHintsQueue;
	private String[] mLevels;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// go fullscreen for devices with QVGA screen (only way I found
		// how to fit UI on the screen)
		setContentView(R.layout.sudoku_play);
		AndroidUtils.setupAdNetwork(this);

		mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
		mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);
		mTimeLabel = (TextView) findViewById(R.id.time_label);

		mDatabase = new SudokuDatabase(getApplicationContext());
		mHintsQueue = new HintsQueue(this);
		mGameTimer = new GameTimer();
		mLevels = getResources().getStringArray(R.array.sudoku_level);
		// create sudoku game instance
		if (savedInstanceState == null) {
			// activity runs for the first time, read game from database
			mSudokuGameID = getIntent().getLongExtra(
					SudokuPlayActivity.EXTRA_SUDOKU_ID, -1);
			mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
		} else {
			// activity has been running before, restore its state
			mSudokuGame = new SudokuGame();
			mSudokuGame.restoreState(savedInstanceState);
			mGameTimer.restoreState(savedInstanceState);
		}

		mForderId = mSudokuGame.getFolderId();
		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
			// Khong lam gi de nguoi dung co the refresh game khacs
			// mSudokuGame.start();
		} else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();
		}

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
			mSudokuBoard.setReadOnly(true);
		}
		mHintsQueue.showOneTimeHint("welcome", R.string.welcome,
				R.string.first_run_hint);

		mSudokuBoard.setGame(mSudokuGame);
		mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);
		mSudokuGame.setOnUndoListener(mOnUndoListener);

		mIMControlPanel = (IMControlPanel) findViewById(R.id.input_methods);
		mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);

		mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

		mIMNumpad = mIMControlPanel
				.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
		if (Build.VERSION.SDK_INT > 10) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(getResources()
					.getColor(R.color.main_color)));
			bar.setTitle(mLevels[(int) mForderId - 1]);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// read game settings
		SharedPreferences gameSettings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		int screenPadding = gameSettings.getInt("screen_border_size", 0);
		mRootLayout.setPadding(screenPadding, screenPadding, screenPadding,
				screenPadding);

		mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean(
				"highlight_wrong_values", true));
		mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean(
				"highlight_touched_cell", true));

		mShowTime = gameSettings.getBoolean("show_time", true);
		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();

			if (mShowTime) {
				mGameTimer.start();
			}
		} else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
			mIMNumpad.setOnUpdateStartGame(mOnUpdateStartGame);
		}
		mTimeLabel.setVisibility(View.GONE);

		mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
		mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean(
				"im_numpad_move_right", false));
		mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean(
				"highlight_completed_values", true));
		mIMNumpad.setShowNumberTotals(gameSettings.getBoolean(
				"show_number_totals", false));

		mIMControlPanel.activateFirstInputMethod(); // make sure that some input
													// method is activated
		mIMControlPanelStatePersister.restoreState(mIMControlPanel);
		mIMNumpad.setUndoListener(mUndoButtonClickListener);
		mIMNumpad.setEnableUndo(mSudokuGame.hasSomethingToUndo());
		updateTime();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// we will save game to the database as we might not be able to get back
		mDatabase.updateSudoku(mSudokuGame);

		mGameTimer.stop();
		mIMControlPanel.pause();
		mIMControlPanelStatePersister.saveState(mIMControlPanel);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mGameTimer.stop();
		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.pause();
		}

		mSudokuGame.saveState(outState);
		mGameTimer.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sudoku_play_menu, menu);
		mRefreshGame = menu.findItem(R.id.refresh);
		mNewGame = menu.findItem(R.id.newGame);
		mRestart = menu.findItem(R.id.restart);
		mClearAllNote = menu.findItem(R.id.clear_all_notes);
		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, SudokuPlayActivity.class), null,
				intent, 0, null);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// Pis Game chua start thi hien thi button refresh de doi sang game khac
		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
			enableMenuRefreshGame();
		} else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			disableMenuRefreshGame();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.restart:
			showDialog(DIALOG_RESTART);
			return true;
		case R.id.clear_all_notes:
			// showDialog(DIALOG_CLEAR_NOTES);
			mSudokuGame.fillInNotes();
			return true;
		case R.id.refresh:
			// Chuyen sang game moi
			createNewSudoku();
			return true;
		case R.id.newGame:
			// Tao 1 game moi
			createNewSudoku();
			enableMenuRefreshGame();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SETTINGS:
			restartActivity();
			break;
		}
	}

	/**
	 * Restarts whole activity.
	 */
	private void restartActivity() {
		startActivity(getIntent());
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WELL_DONE:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.well_done)
					.setMessage(
							getString(R.string.congrats, mGameTimeFormatter
									.format(mSudokuGame.getTime())))
					.setPositiveButton(R.string.new_game,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									createNewSudoku();
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create();

		case DIALOG_RESTART:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_menu_rotate)
					.setTitle(R.string.app_name)
					.setMessage(R.string.restart_confirm)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Restart game
									mSudokuGame.reset();
									mSudokuGame.start();
									mSudokuBoard.setReadOnly(false);
									if (mShowTime) {
										mGameTimer.start();
									}
								}
							}).setNegativeButton(android.R.string.no, null)
					.create();
		case DIALOG_CLEAR_NOTES:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_menu_delete)
					.setTitle(R.string.app_name)
					.setMessage(R.string.clear_all_notes_confirm)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mSudokuGame.clearAllNotes();
								}
							}).setNegativeButton(android.R.string.no, null)
					.create();
		case DIALOG_UNDO_TO_CHECKPOINT:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_menu_delete)
					.setTitle(R.string.app_name)
					.setMessage(R.string.undo_to_checkpoint_confirm)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mSudokuGame.undoToCheckpoint();
								}
							}).setNegativeButton(android.R.string.no, null)
					.create();

		}
		return null;
	}

	/**
	 * Occurs when puzzle is solved.
	 */
	private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {

		@Override
		public void onPuzzleSolved() {
			mSudokuBoard.setReadOnly(true);
			// cap nhat database co sudoku da xong
			mDatabase.updateSudoku(mSudokuGame);
			showDialog(DIALOG_WELL_DONE);
		}

	};

	/**
	 * Update the time of game-play.
	 */
	void updateTime() {
		if (mShowTime) {
			// Trungth - sua time
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar bar = getActionBar();
				bar.setSubtitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
			} else {
				setTitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
			}

		} else {
			setTitle(R.string.app_name);
		}

	}

	// This class implements the game clock. All it does is update the
	// status each tick.
	private final class GameTimer extends Timer {

		GameTimer() {
			super(1000);
		}

		@Override
		protected boolean step(int count, long time) {
			updateTime();

			// Run until explicitly stopped.
			return false;
		}

	}

	// ============= THEM VAO ===============
	private MenuItem mRefreshGame;
	private MenuItem mNewGame;
	private MenuItem mRestart;
	private MenuItem mClearAllNote;
	private OnClickListener mUndoButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mSudokuGame.undo();
		}
	};
	private OnUndoListener mOnUndoListener = new OnUndoListener() {

		@Override
		public void onEnableUndo(boolean enable) {
			if (mIMNumpad != null) {
				mIMNumpad.setEnableUndo(enable);
			}
		}
	};
	private OnUpdateStartGame mOnUpdateStartGame = new OnUpdateStartGame() {

		@Override
		public void onUpdateStartGame() {
			if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
				mSudokuGame.start();
				if (mShowTime) {
					mGameTimer.start();
				}
				mRefreshGame.setVisible(false);
			}
		}
	};

	private void createNewSudoku() {
		// Save game cu lai
		mDatabase.updateSudoku(mSudokuGame);
		mSudokuGameID = RandomSudoku.getInstance().randomSudokuGame(this,
				mForderId);
		mSudokuGame = SudokuGame.createEmptyGame();
		mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
		mSudokuBoard.setReadOnly(false);
		mSudokuBoard.setGame(mSudokuGame);
		mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);
		mSudokuGame.setOnUndoListener(mOnUndoListener);
		mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);
		mIMNumpad.reInitialize(this, mIMControlPanel, mSudokuGame,
				mSudokuBoard, mHintsQueue);
		mIMNumpad.setOnUpdateStartGame(mOnUpdateStartGame);
		mIMNumpad.setUndoListener(mUndoButtonClickListener);
		mIMNumpad.setEnableUndo(mSudokuGame.hasSomethingToUndo());
	}

	private void enableMenuRefreshGame() {
		mRefreshGame.setVisible(true);
		mNewGame.setEnabled(false);
		mRestart.setEnabled(false);
		mClearAllNote.setEnabled(false);
	}

	private void disableMenuRefreshGame() {
		mRefreshGame.setVisible(false);
		mNewGame.setEnabled(true);
		mRestart.setEnabled(true);
		mClearAllNote.setEnabled(true);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void finish() {
		Bundle bundle = new Bundle();
		onSaveInstanceState(bundle);
		super.finish();
	}

}
