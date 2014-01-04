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

package pis.android.sudoku.gui.inputmethod;

import java.util.HashMap;
import java.util.Map;

import pis.android.sudoku.R;
import pis.android.sudoku.game.Cell;
import pis.android.sudoku.game.CellCollection;
import pis.android.sudoku.game.CellCollection.OnChangeListener;
import pis.android.sudoku.game.CellNote;
import pis.android.sudoku.game.SudokuGame;
import pis.android.sudoku.gui.HintsQueue;
import pis.android.sudoku.gui.SudokuBoardView;
import pis.android.sudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;
import pis.android.sudoku.widget.ButtonWithFont;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class IMNumpad extends InputMethod {

	private boolean moveCellSelectionOnPress = true;
	private boolean mHighlightCompletedValues = true;
	private boolean mShowNumberTotals = false;

	private static final int MODE_EDIT_VALUE = 0;
	private static final int MODE_EDIT_NOTE = 1;

	private Cell mSelectedCell;
	private ButtonWithFont mSwitchNumNoteButton;

	private ButtonWithFont mUndoButton; // Them button undo
	private int mEditMode = MODE_EDIT_VALUE;

	private Map<Integer, ButtonWithFont> mNumberButtons;

	public boolean isMoveCellSelectionOnPress() {
		return moveCellSelectionOnPress;
	}

	public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress) {
		this.moveCellSelectionOnPress = moveCellSelectionOnPress;
	}

	public boolean getHighlightCompletedValues() {
		return mHighlightCompletedValues;
	}

	/**
	 * If set to true, buttons for numbers, which occur in
	 * {@link CellCollection} more than {@link CellCollection#SUDOKU_SIZE}
	 * -times, will be highlighted.
	 * 
	 * @param highlightCompletedValues
	 */
	public void setHighlightCompletedValues(boolean highlightCompletedValues) {
		mHighlightCompletedValues = highlightCompletedValues;
	}

	public boolean getShowNumberTotals() {
		return mShowNumberTotals;
	}

	public void setShowNumberTotals(boolean showNumberTotals) {
		mShowNumberTotals = showNumberTotals;
	}

	@Override
	protected void initialize(Context context, IMControlPanel controlPanel,
			SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
		super.initialize(context, controlPanel, game, board, hintsQueue);

		game.getCells().addOnChangeListener(mOnCellsChangeListener);
	}

	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_numpad, null);

		mNumberButtons = new HashMap<Integer, ButtonWithFont>();
		mNumberButtons.put(1,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_1));
		mNumberButtons.put(2,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_2));
		mNumberButtons.put(3,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_3));
		mNumberButtons.put(4,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_4));
		mNumberButtons.put(5,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_5));
		mNumberButtons.put(6,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_6));
		mNumberButtons.put(7,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_7));
		mNumberButtons.put(8,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_8));
		mNumberButtons.put(9,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_9));
		mNumberButtons.put(0,
				(ButtonWithFont) controlPanel.findViewById(R.id.button_clear));

		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNumberButtonClick);
		}

		mSwitchNumNoteButton = (ButtonWithFont) controlPanel
				.findViewById(R.id.switch_num_note);
		mSwitchNumNoteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE
						: MODE_EDIT_VALUE;
				update();
			}

		});
		mUndoButton = (ButtonWithFont) controlPanel.findViewById(R.id.undo);
		return controlPanel;

	}

	@Override
	public int getNameResID() {
		return R.string.numpad;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_numpad_hint;
	}

	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.numpad_abbr);
	}

	@Override
	protected void onActivated() {
		update();

		mSelectedCell = mBoard.getSelectedCell();
	}

	@Override
	protected void onCellSelected(Cell cell) {
		mSelectedCell = cell;
	}

	private OnClickListener mNumberButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int selNumber = (Integer) v.getTag();
			Cell selCell = mSelectedCell;

			if (selCell != null) {
				if (mOnUpdateStartGame != null) {
					mOnUpdateStartGame.onUpdateStartGame();
				}
				switch (mEditMode) {
				case MODE_EDIT_NOTE:
					if (selNumber == 0) {
						mGame.setCellNote(selCell, CellNote.EMPTY);
						if (mGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
							mGame.start();
						}
					} else if (selNumber > 0 && selNumber <= 9) {
						mGame.setCellNote(selCell, selCell.getNote()
								.toggleNumber(selNumber));
					}
					break;
				case MODE_EDIT_VALUE:
					if (selNumber >= 0 && selNumber <= 9) {
						mGame.setCellValue(selCell, selNumber);
						if (isMoveCellSelectionOnPress()) {
							mBoard.moveCellSelectionRight();
						}
					}
					break;
				}

			}
		}

	};

	private OnChangeListener mOnCellsChangeListener = new OnChangeListener() {

		@Override
		public void onChange() {
			if (mActive) {
				update();
			}
		}
	};

	@SuppressLint("ResourceAsColor")
	private void update() {
		switch (mEditMode) {
		case MODE_EDIT_NOTE:
			mSwitchNumNoteButton.setBackgroundColor(mContext.getResources()
					.getColor(R.color.sub_color));
			break;
		case MODE_EDIT_VALUE:
			mSwitchNumNoteButton.setBackgroundColor(mContext.getResources()
					.getColor(R.color.main_color));
			break;
		}

		Map<Integer, Integer> valuesUseCount = null;
		if (mHighlightCompletedValues || mShowNumberTotals)
			valuesUseCount = mGame.getCells().getValuesUseCount();

		if (mHighlightCompletedValues) {
		}

		if (mShowNumberTotals) {
			for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
				Button b = mNumberButtons.get(entry.getKey());
				b.setText(entry.getKey() + " (" + entry.getValue() + ")");
			}
		}
	}

	@Override
	protected void onSaveState(StateBundle outState) {
		outState.putInt("editMode", mEditMode);
	}

	@Override
	protected void onRestoreState(StateBundle savedState) {
		mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
		if (isInputMethodViewCreated()) {
			update();
		}
	}

	// ================== THem vao =======================
	// Them cac ham lien quan toi undo
	public void setEnableUndo(boolean enable) {
		if (mUndoButton != null) {
			mUndoButton.setEnabled(enable);
		}
	}

	public void setUndoListener(OnClickListener onClickListener) {
		if (mUndoButton != null) {
			mUndoButton.setOnClickListener(onClickListener);
		}
	}

	// thong bao cap nhat update game
	public interface OnUpdateStartGame {
		public void onUpdateStartGame();
	}

	private OnUpdateStartGame mOnUpdateStartGame;

	public void setOnUpdateStartGame(OnUpdateStartGame onUpdateStartGame) {
		mOnUpdateStartGame = onUpdateStartGame;
	}

	public void reInitialize(Context context, IMControlPanel controlPanel,
			SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
		initialize(context, controlPanel, game, board, hintsQueue);
	}
}
