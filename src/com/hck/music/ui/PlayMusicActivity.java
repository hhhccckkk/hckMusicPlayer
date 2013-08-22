package com.hck.music.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hck.music.bean.MusicBean;
import com.hck.music.bean.SingLrcBean;
import com.hck.music.server.PlayMusicServer;
import com.hck.music.util.InitImagerLoder;
import com.hck.music.util.ShowSingWordTextView;
import com.hck.music.util.StringUtils;
import com.musicmodel.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PlayMusicActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener {
	private Button fontButton, nextButton;   //上一曲，下一曲，按钮
	private Button playButton;  //播放，暂停按钮
	private SeekBar seekBar;  //拖动条
	private boolean isPlay;  //是否在播放
	private int id;  
	private View bar;   //转圈圈的iew
	private TextView playTime;  //当前播放时间
	private TextView allTime;  //总的播放时间
	private TextView musicName;  //歌曲名字
	private ImageView imageView;  //显示专辑图片的iew
	private ShowSingWordTextView musicWordTextView;  //显示歌词的自定的textview
	private static int nowBarSize;  //当前seekbar进度
	private static int seekBarSize;  //seekbar的总大小
	private List<MusicBean> beans; //放置歌曲的集合
	private Intent intent;
	private String type;  //是本地还是在在线
	private Button mainButton;   
	private boolean isRandom; //是否随机播放标志
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playmusic);
		beans = new ArrayList<MusicBean>();
		init();
		PlayMusicServer.playMusicActivity = this;
		startPlay();
	}
	private void init() {
		InitImagerLoder.Init(this);
		mainButton = (Button) findViewById(R.id.main_bt);
		mainButton.setOnClickListener(this);
		imageView = (ImageView) findViewById(R.id.play_music_image);
		musicName = (TextView) findViewById(R.id.title);
		musicWordTextView = (ShowSingWordTextView) findViewById(R.id.music_word);
		fontButton = (Button) findViewById(R.id.font);
		fontButton.setOnClickListener(this);
		nextButton = (Button) findViewById(R.id.next);
		nextButton.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setEnabled(false);
		playButton = (Button) findViewById(R.id.play);
		bar = findViewById(R.id.pb);
		playTime = (TextView) findViewById(R.id.play_time);
		allTime = (TextView) findViewById(R.id.all_time);
		playButton.setOnClickListener(this);
		Intent intent = getIntent();
		Object[] musics = (Object[]) intent.getSerializableExtra("music");
		if (musics != null) {
			for (int i = 0; i < musics.length; i++) {
				MusicBean bean = (MusicBean) musics[i];
				Log.i("hck", "PlayMusicActivity tupian: "+bean.getMusicImage());
				beans.add(bean);
			}
		}
		isRandom = intent.getBooleanExtra("isRandom", false);
		id = intent.getIntExtra("id", -1);
		type = intent.getStringExtra("type");
		listererTelephony(); //来电话监听
	}
	private void startPlay() {
		if (id != -1) {//id不为-1.则为用于点击歌曲列表进入播放界面，传递相应id过去，播放相应音乐
			startPlay(id);
		} else {  //id为-1，则是从通知栏，进入播放界面，回复播放数据
			this.id = PlayMusicServer.playMusicId;
			this.beans = PlayMusicServer.beans;
			PlayMusicServer.refreshUI();
			PlayMusicServer.getSingerWords(this.id);
			getImage(beans.get(id).getMusicImage());
		}
		if (this.id >= beans.size()) {
			this.id = 0;
		}
		if (this.id < 0) {
			this.id = beans.size() - 1;
		}
		setName(this.id); //显示播放音乐的名字
	}
	private void listererTelephony() {
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new MobliePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
	}
	private class MobliePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				PlayMusicServer.start();
				isPlay = true;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				PlayMusicServer.pause();
				isPlay = false;
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				PlayMusicServer.pause();
				isPlay = false;
				break;
			default:
				break;
			}
		}
	}
	private void startPlay(int id) {
		PlayMusicServer.stop();//播放之前，先停止播放，不管有没有在播放，都先停止
		intent = new Intent();
		intent.putExtra("id", id);
		intent.putExtra("type", type);
		intent.putExtra("music", beans.toArray());
		intent.putExtra("isRandom", isRandom);
		intent.setClass(this, PlayMusicServer.class); //启动service，播放音乐
		startService(intent);
	}
	public void updateSeekBar(int size) {   //更新seekbar的方法
		seekBar.setProgress(size);
	}
	public void updateSeekCach(int size) { //更新seekbar第2进度条的方法
		seekBar.setSecondaryProgress(size);
	}
	public void setIsplay(boolean isPlay) {  //改变当前播放状态数据标志
		if (isPlay) {
			this.isPlay = true;
		} else {
			this.isPlay = false;
		}
	}
	public void refreshUI(int size) {     //音乐初始化 后，调用这里
		allTime.setText(StringUtils.generateTime(size));  //显示播放总时间
		hideBar(); //隐藏圈圈
		seekBar.setMax(size); //设置seekbar的总大小
		playButton.setBackgroundResource(R.drawable.player_pause_highlight);//设置播放按钮的背景
		seekBar.setEnabled(true); //可以拖动seekbar了
	}
	public void hideBar() { //隐藏圈圈
		bar.setVisibility(View.GONE);
	}
	public void showBar() {//显示圈圈
		bar.setVisibility(View.VISIBLE);
	}
	public void updateTime(int nowPlayTime) { //更新播放时间
		playTime.setText(StringUtils.generateTime(nowPlayTime));
	}
	public void refreash(int index) { //跟新歌词
		musicWordTextView.setIndex(index);
		musicWordTextView.invalidate();
	}
	public void initView(SingLrcBean bean) {//刚开始播放时候，初始化歌词，即把歌词显示在界面上
		musicWordTextView.setmLrcList(bean);
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.play) {     //播放按钮
			play();
		} else if (v.getId() == R.id.next) { //下一去
			PlayMusicServer.playNextMusic();
			id += 1;
			if (id >= beans.size()) {
				id = 0;
			}
			setName(id);
		} else if (v.getId() == R.id.font) {//上一曲
			id -= 1;
			PlayMusicServer.playFontMusic();
			if (id < 0) {
				id = beans.size() - 1;
			}
			setName(id);
		}
	}
	public void setName(int id) {
		musicName.setText(beans.get(id).getMusicName());
	}
	private void play() {
		if (isPlay) {
			seekBar.setEnabled(false);
			playButton.setBackgroundResource(R.drawable.player_play_highlight);
		} else {
			seekBar.setEnabled(true);
			playButton.setBackgroundResource(R.drawable.player_pause_highlight);
		}
		PlayMusicServer.play();
	}
	public void getImage(String url) { //显示专辑图片
		if (url != null) {
			ImageLoader.getInstance().displayImage(url, imageView);
		}
	}
	public void back(View view) {
		this.finish();
	}
	public void resetDate() { //下一曲，上一曲，时候，回复界面原始状态
		showBar();
		musicName.setText("");
		playTime.setText("00:00");
		allTime.setText("00:00");
		seekBar.setProgress(0);
		seekBar.setSecondaryProgress(0);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!isPlay && intent != null) {
			stopService(intent);
		}
		if (PlayMusicServer.isPause) {
			PlayMusicServer.stop();
		}
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {   //拖动seekbar时候，调用这里

		if (fromUser) {
			PlayMusicServer.start();
			PlayMusicServer.seekTo(progress);  //播放跳转到拖动位置
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { //开始拖动seekbar
		PlayMusicServer.pause();
		nowBarSize = seekBar.getProgress();
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { //拖动seekbar结束
		seekBarSize = seekBar.getProgress();
		if (nowBarSize >= seekBarSize) {
			PlayMusicServer.setFlag();
		}
	}

}
