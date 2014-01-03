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

package pis.android.sudoku.game;

import pis.android.sudoku.game.command.AbstractCommand;
import pis.android.sudoku.game.command.ClearAllNotesCommand;
import pis.android.sudoku.game.command.CommandStack;
import pis.android.sudoku.game.command.EditCellNoteCommand;
import pis.android.sudoku.game.command.FillInNotesCommand;
import pis.android.sudoku.game.command.SetCellValueCommand;
import android.os.Bundle;
import android.os.SystemClock;

public class SudokuGame {

	public static final int GAME_STATE_PLAYING = 0;
	public static final int GAME_STATE_NOT_STARTED = 1;
	public static final int GAME_STATE_COMPLETED = 2;

	private long mId;
	private long mCreated;
	private int mState;
	private long mTime;
	private long mLastPlayed;
	private long mFolderId; // Pis them vao
	private String mNote;
	private CellCollection mCells;

	private OnPuzzleSolvedListener mOnPuzzleSolvedListener;
	private OnUndoListener mOnUndoListener;
	private CommandStack mCommandStack;
	// Time when current activity has become active.
	private long mActiveFromTime = -1;

	public static SudokuGame createEmptyGame() {
		SudokuGame game = new SudokuGame();
		game.setCells(CellCollection.createEmpty());
		// set creation time
		game.setCreated(System.currentTimeMillis());
		return game;
	}

	public SudokuGame() {
		mTime = 0;
		mLastPlayed = 0;
		mCreated = 0;

		mState = GAME_STATE_NOT_STARTED;
	}

	public void saveState(Bundle outState) {
		outState.putLong("id", mId);
		outState.putString("note", mNote);
		outState.putLong("created", mCreated);
		outState.putInt("state", mState);
		outState.putLong("time", mTime);
		outState.putLong("folderId", mFolderId);// Pis them vao
		outState.putLong("lastPlayed", mLastPlayed);
		outState.putString("cells", mCells.serialize());
		mCommandStack.saveState(outState);
	}

	public void restoreState(Bundle inState) {
		mId = inState.getLong("id");
		mNote = inState.getString("note");
		mCreated = inState.getLong("created");
		mState = inState.getInt("state");
		mTime = inState.getLong("time");
		mFolderId = inState.getLong("folderId");// Pis them vao
		mLastPlayed = inState.getLong("lastPlayed");
		mCells = CellCollection.deserialize(inState.getString("cells"));

		mCommandStack = new CommandStack(mCells);
		mCommandStack.restoreState(inState);
		validate();
		if (mOnUndoListener != null) {
			mOnUndoListener.onEnableUndo(!mCommandStack.empty());
		}
	}

	public void setOnPuzzleSolvedListener(OnPuzzleSolvedListener l) {
		mOnPuzzleSolvedListener = l;
	}

	public void setNote(String note) {
		mNote = note;
	}

	public String getNote() {
		return mNote;
	}

	public void setCreated(long created) {
		mCreated = created;
	}

	public long getCreated() {
		return mCreated;
	}

	public void setState(int state) {
		mState = state;
	}

	public int getState() {
		return mState;
	}

	// Pis them vao
	public void setFolderId(long folderId) {
		mFolderId = folderId;
	}

	// Pis them vao
	public long getFolderId() {
		return mFolderId;
	}

	/**
	 * Sets time of play in milliseconds.
	 * 
	 * @param time
	 */
	public void setTime(long time) {
		mTime = time;
	}

	/**
	 * Gets time of game-play in milliseconds.
	 * 
	 * @return
	 */
	public long getTime() {
		if (mActiveFromTime != -1) {
			return mTime + SystemClock.uptimeMillis() - mActiveFromTime;
		} else {
			return mTime;
		}
	}

	public void setLastPlayed(long lastPlayed) {
		mLastPlayed = lastPlayed;
	}

	public long getLastPlayed() {
		return mLastPlayed;
	}

	public void setCells(CellCollection cells) {
		mCells = cells;
		validate();
		mCommandStack = new CommandStack(mCells);
	}

	public CellCollection getCells() {
		return mCells;
	}

	public void setId(long id) {
		mId = id;
	}

	public long getId() {
		return mId;
	}

	/**
	 * Sets value for the given cell. 0 means empty cell.
	 * 
	 * @param cell
	 * @param value
	 */
	public void setCellValue(Cell cell, int value) {
		System.out.println("Trungth - setCellValue start:=" + getCells().toString());
		if (cell == null) {
			throw new IllegalArgumentException("Cell cannot be null.");
		}
		if (value < 0 || value > 9) {
			throw new IllegalArgumentException("Value must be between 0-9.");
		}

		if (cell.isEditable()) {
			executeCommand(new SetCellValueCommand(cell, value));

			validate();
			if (isCompleted()) {
				finish();
				if (mOnPuzzleSolvedListener != null) {
					mOnPuzzleSolvedListener.onPuzzleSolved();
				}
			}
		}
		System.out.println("Trungth - setCellValue end:=" + getCells().toString());
	}

	/**
	 * Sets note attached to the given cell.
	 * 
	 * @param cell
	 * @param note
	 */
	public void setCellNote(Cell cell, CellNote note) {
		if (cell == null) {
			throw new IllegalArgumentException("Cell cannot be null.");
		}
		if (note == null) {
			throw new IllegalArgumentException("Note cannot be null.");
		}

		if (cell.isEditable()) {
			executeCommand(new EditCellNoteCommand(cell, note));
		}
	}

	private void executeCommand(AbstractCommand c) {
		mCommandStack.execute(c);
		// Pis THem vao khi co su thay doi cua cell
		if (mOnUndoListener != null) {
			mOnUndoListener.onEnableUndo(!mCommandStack.empty());
		}
	}

	/**
	 * Undo last command.
	 */
	public void undo() {
		mCommandStack.undo();
		if (mOnUndoListener != null) {
			mOnUndoListener.onEnableUndo(!mCommandStack.empty());
		}
	}

	public boolean hasSomethingToUndo() {
		return mCommandStack.hasSomethingToUndo();
	}

	public void setUndoCheckpoint() {
		mCommandStack.setCheckpoint();
	}

	public void undoToCheckpoint() {
		mCommandStack.undoToCheckpoint();
	}

	public boolean hasUndoCheckpoint() {
		return mCommandStack.hasCheckpoint();
	}

	/**
	 * Start game-play.
	 */
	public void start() {
		mState = GAME_STATE_PLAYING;
		resume();
	}

	public void resume() {
		// reset time we have spent playing so far, so time when activity was
		// not active
		// will not be part of the game play time
		mActiveFromTime = SystemClock.uptimeMillis();
	}

	/**
	 * Pauses game-play (for example if activity pauses).
	 */
	public void pause() {
		// save time we have spent playing so far - it will be reseted after
		// resuming
		mTime += SystemClock.uptimeMillis() - mActiveFromTime;
		mActiveFromTime = -1;

		setLastPlayed(System.currentTimeMillis());
	}

	/**
	 * Finishes game-play. Called when puzzle is solved.
	 */
	private void finish() {
		pause();
		mState = GAME_STATE_COMPLETED;
	}

	/**
	 * Resets game.
	 */
	public void reset() {
		for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
				Cell cell = mCells.getCell(r, c);
				if (cell.isEditable()) {
					cell.setValue(0);
					cell.setNote(new CellNote());
				}
			}
		}
		validate();
		setTime(0);
		setLastPlayed(0);
		mState = GAME_STATE_NOT_STARTED;
	}

	/**
	 * Returns true, if puzzle is solved. In order to know the current state,
	 * you have to call validate first.
	 * 
	 * @return
	 */
	public boolean isCompleted() {
		return mCells.isCompleted();
	}

	public void clearAllNotes() {
		executeCommand(new ClearAllNotesCommand());
	}

	/**
	 * Fills in possible values which can be entered in each cell.
	 */
	public void fillInNotes() {
		executeCommand(new FillInNotesCommand());
	}

	public void validate() {
		mCells.validate();
	}

	public interface OnPuzzleSolvedListener {
		/**
		 * Occurs when puzzle is solved.
		 * 
		 * @return
		 */
		void onPuzzleSolved();
	}

	// ============= Trungth Them vao ===================

	public interface OnUndoListener {
		void onEnableUndo(boolean enable);
	}

	public void setOnUndoListener(OnUndoListener undoListener) {
		mOnUndoListener = undoListener;
	}
}
