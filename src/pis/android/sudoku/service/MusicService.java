package pis.android.sudoku.service;

import pis.android.sudoku.R;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
	private MediaPlayer mMediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate();
		// startMusic();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopMusic();
	}

	public void startMusic() {
		mMediaPlayer = MediaPlayer.create(getApplicationContext(),
				R.raw.main_music);
		mMediaPlayer.start();
	}

	public void stopMusic() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
	}

}
