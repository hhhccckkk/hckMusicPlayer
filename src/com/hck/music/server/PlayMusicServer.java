package com.hck.music.server;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.hck.music.bean.MusicBean;
import com.hck.music.bean.SingLrcBean;
import com.hck.music.ui.PlayMusicActivity;
import com.hck.music.util.GetLrcUtil;
import com.hck.music.util.NotiFicationUtil;
/**
 * 播放音乐的service
 * @author hck
 *
 */
public class PlayMusicServer extends Service implements
		OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
		OnInfoListener, OnErrorListener {
	public static PlayMusicActivity playMusicActivity;   //播放界面activity对象，用于调用实现更新UI
	private static boolean isPlay;   //判断是否在播放中的标记
	private static MediaPlayer player;   //MediaPlayer对象，播放音乐
	private static updateBar updateBar;  //更新seekbar的一个线程对象
	public static SingLrcBean bean;   //歌词实体对象
	private static GetLrcUtil getUtil;   //获取歌词的一个类对象，用于获取歌词
	private static updateSingWords updateWords;  //更新歌词的一个线程
	private static long wordTime;   //当前播放时间，更新歌词用
	private static int flag = 0; 
	public static boolean isPause;   //是否暂停标志
	private static PlayMusicServer playMusicServer;  //当前类对象，用于播放界面调用
	private static Handler handler2 = new Handler();  //更新歌词的handler
	public static List<MusicBean> beans;   //存放歌曲的集合
	public static int playMusicId;   //音乐播放id
	private static String playUrl;   //音乐的播放url
	private static String type;    //是本地还是在线音乐标记
    private boolean isRandom;   //是否随机播放
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		player = new MediaPlayer();   //初始化MediaPlayer对象
		playMusicServer = this;  //初始化playMusicServer对象
	}
	@Override
	public void onStart(Intent intent, int startId) {
		getUtil = new GetLrcUtil();  //初始化获取歌词对象
		init(intent);  //初始化一些数据
		getSingerWords(playMusicId); //通过音乐id获取歌词
		startPlay();  //开始播放视频
		 showImage();
	}
	public static void getSingerWords(int startId) {  //获取歌词的方法
		try {
			if (type.equals("net")) {  //如果是net，则为在线音乐播放
				getLrc(beans.get(playMusicId).getMusicLrcUrl());  //调用getLrc获取歌词
			} else {
				bean = getUtil.parser(beans.get(playMusicId).getMusicPlayUrl()  //本地，调用getUtil.parser获取
						.replace(".mp3", ".lrc"), false, null); 
				if (bean == null) {
					return;
				}
			}
			wordTime = bean.getMusicTimes().get(0);  //获取第一个时间
			playMusicActivity.initView(bean);   //初始化播放界面
			playMusicActivity.refreash(0);     //初始化歌词界面
		} catch (Exception e) {
			e.printStackTrace();
			return;

		}
	}

	static Handler handler3 = new Handler() {  // 线程获取歌词后，更新界面
		public void handleMessage(android.os.Message msg) {
			if (bean.getMusicTimes().size() > 0) {
				wordTime = bean.getMusicTimes().get(0); 
				playMusicActivity.initView(bean); 
				playMusicActivity.refreash(0); 
			}

		};
	};

	private static void getLrc(final String url) { //获取在线歌词数据
		new Thread() {
			@Override
			public void run() {
				super.run();
				HttpURLConnection connection = null;
				try {
					URL url2 = new URL(url);
					connection = (HttpURLConnection) url2.openConnection();
					connection.setConnectTimeout(10000);
					connection.connect();
					InputStream inputStream = connection.getInputStream();
					bean = getUtil.parser(url, true, inputStream); //调用 getUtil.parser把获取到的流转换成我们需要的歌词
					handler3.sendEmptyMessage(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	private void init(Intent intent) { //初始化数据，从intent获取播放界面传过来的数据
		beans=new ArrayList<MusicBean>();
		 Object[] musics = (Object[]) intent.getSerializableExtra("music");    //获取播放界面传过来的音乐数据
	        for (int i = 0; i < musics.length; i++) {    //遍历数组，取出单个音乐对象，保存在集合里面
	            MusicBean bean = (MusicBean) musics[i];   
	            beans.add(bean);   
	        }
		playMusicId = intent.getIntExtra("id", -1);   //获取传过来的播放歌曲id
		updateBar = new updateBar();   //初始化更新seekbar的线程
		updateWords = new updateSingWords();  //初始化更新歌词的线程
		type = intent.getStringExtra("type");  //获取传过来的type数据，用于判断是在线播放，还是本地本地播放。2这获取歌词不一样
		isRandom=intent.getBooleanExtra("isRandom", false);   //获取Playmusicactivity传过来的isRandom，用于是否随机播放
	}
	private static void startPlay() { //开始播放音乐
		try {
			player.reset();   //恢复原始状态
			playUrl = beans.get(playMusicId).getMusicPlayUrl(); //获取当前播放的歌曲的url
			player.setDataSource(playUrl);   //把播放地址丢给播放器
			player.prepareAsync();   //异步初始化
			player.setOnBufferingUpdateListener(playMusicServer); //监听缓冲数据
			player.setOnPreparedListener(playMusicServer);  //监听初始化
			player.setOnCompletionListener(playMusicServer); //监听是否播放完了
			player.setOnErrorListener(playMusicServer);   //监听出错信息
			player.setOnInfoListener(playMusicServer);   //监听播放过程中，返回的信息

		} catch (Exception e) {
             Toast.makeText(playMusicServer, "错误: "+e, Toast.LENGTH_LONG).show(); //出错时候，提示
             Log.e("hck", "PlayMusicserver startPlay: "+e.toString());
		}
	}
	static Handler handler = new Handler() {    //更新seekbar和播放时间的handler
		public void handleMessage(android.os.Message msg) {
			if (playMusicActivity != null && player != null && isPlay) { 
				playMusicActivity.updateSeekBar(player.getCurrentPosition());
				playMusicActivity.updateTime(player.getCurrentPosition());
			}
		};
	};

	class updateBar implements Runnable {  //更新seekbar和时间的线程
		@Override
		public void run() {
			Log.i("hck", "updateBar  run run");
			handler.sendEmptyMessage(1);
			handler.postDelayed(updateBar, 1000);
		}
	}

	class updateSingWords implements Runnable {  //更新歌词的线程
		private int offer;

		@Override
		public void run() {
			if (player == null) { 
				return;
			}
			offer = player.getCurrentPosition(); //获取播放时间
			if (offer >= wordTime)   //播放时间如果大于歌词时间，则需要更新歌词了，显示下一个歌词
			{
				if (flag < bean.getMusicTimes().size()   //处理数组越界判断用
						|| flag < bean.getMusicWords().size())
				{
					if (bean.getMusicWords().get(flag) != null
							&& !bean.getMusicWords().get(flag).equals("")) {
						playMusicActivity.refreash(flag);  //调用该方法更新歌词
					}
					flag++;   //歌词位置加1，指向下一个歌词，以便获取它对应的时间
					if (flag < bean.getMusicTimes().size()) {  
						wordTime = bean.getMusicTimes().get(flag);
					}
				}
			}
			handler2.postDelayed(updateWords, 500); //没500毫秒，进行一次判断，来更新歌词

		}

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {  //播放过程中的一些返回信息
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:  //进入缓冲
			playMusicActivity.showBar();  //显示转圈圈
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:  //结束缓冲
			playMusicActivity.hideBar();  //隐藏圈圈
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {  //初始化播放回调方法
		player.start();  //初始化完成，调用start，开始播放音乐
		isPlay = true;
		isPause = false;
		refreshUI();   //更新PlayMusicActivity界面
		new Thread(updateBar).start();  //启动线程，更新seekbar
		if (bean != null && bean.getMusicWords() != null
				&& !bean.getMusicWords().isEmpty()) {
			handler2.post(updateWords);   //启动更新歌词的线程
		}
		showNotifi();  //在通知栏显示
	}
	public static void refreshUI() { 
		if (player != null && playMusicActivity != null) {
			playMusicActivity.refreshUI(player.getDuration());
			playMusicActivity.setIsplay(true);
		}
	}
	@Override
	public void onCompletion(MediaPlayer mp) { //一首播放完毕，继续下手
		if (isRandom) { //是否为随机播放
			playMusicId = new Random().nextInt(beans.size() - 1);
		}
		playNextMusic();
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {  //获取缓冲进度
		if (playMusicActivity != null && player != null) {
			playMusicActivity.updateSeekCach(mp.getDuration() * percent / 100);  //seekbar第2进度条
		}
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) { //出错时候，会回调这里
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_UNKNOWN :
			break;
		default:
			break;
		}
		return false;
	}

	public static void play() {  //播放
		if (player != null) {
			if (isPlay) { //如果在播放，则暂停
				player.pause();
				isPlay = false;
				isPause = true;
				playMusicActivity.setIsplay(false);
				hidenNotifi(); //应藏通知栏
             
			} else {  //播放
				if (player != null && !player.isPlaying()) {
					player.start();
					playMusicActivity.setIsplay(true);
					isPlay = true;
					isPause = false;
					showNotifi();
				}
			}
		}
	}

	public static void pause() { //暂停播放
		if (player != null) {
			player.pause();
			isPlay = false;
			isPause = true;
			if (playMusicActivity != null) {
				playMusicActivity.setIsplay(false);
			}
			hidenNotifi();
		}
	}

	public static void start() { //开始播放

		if (player != null && !player.isPlaying()) {
			player.start();
			isPlay = true;
			isPause = false;
			if (playMusicActivity != null) {
				playMusicActivity.setIsplay(true);
			}
			showNotifi();
		}
	}

	public static void seekTo(int size) {  //拖动时候，快进，快退

		if (player != null && isPlay) {
			player.seekTo(size);
		}
	}

	public static void setFlag() {  //初始化获取歌词的标志
		flag = 0;
		if (bean != null && bean.getMusicTimes() != null
				&& !bean.getMusicTimes().isEmpty()) {
			wordTime = bean.getMusicTimes().get(0);
		}
	}

	public static void playNextMusic() { //播放下一曲音乐
		if (isPlay) {
			player.stop();
		}
		reset();
		playMusicId += 1; //音乐id+1
		if (playMusicId >= beans.size()) {
			playMusicId = 0;
		}
		getSingerWords(playMusicId); //获取歌词
		startPlay();
		showNotifi();
		playMusicActivity.resetDate();
		 showImage();
	}
	private static void showImage()
	{
		 if ("net".equals(type)) {
			  playMusicActivity.getImage(beans.get(playMusicId).getMusicImage());
		 }
	}
	public static void playFontMusic() { //上一曲歌曲
		
		if (isPlay) {
			player.stop();
		}
		reset();
		playMusicId -= 1;
		if (playMusicId < 0) {
			playMusicId = beans.size() - 1;
		}
		getSingerWords(playMusicId);
		startPlay();
		showNotifi();
		playMusicActivity.resetDate();
		 showImage();
	}

	private static void reset() { //移除相应线程
		isPlay = false;
		handler2.removeCallbacks(updateWords);
		handler.removeCallbacks(updateBar);
		flag = 0;
	}

	private static void showNotifi() {  //通知栏显示
		if (playUrl != null) {
			NotiFicationUtil.showNotification(playMusicServer,
					beans.get(playMusicId).getMusicName());
		} else {
			NotiFicationUtil.showNotification(playMusicServer,
					beans.get(playMusicId).getMusicName());
		}
	}

	private static void hidenNotifi() {
		NotiFicationUtil.clearNotification(playMusicServer);
	}

	@Override
	public void onDestroy() {  //销毁时候的一些数据清理
		super.onDestroy();

		if (isPause || !isPlay) {
			if (player != null) {
				player.stop();
				player.release();
				player = null;
				playMusicId = 0;
				handler2.removeCallbacks(updateWords);
				handler.removeCallbacks(updateBar);
				this.stopSelf();
				flag = 0;
				wordTime = 0;
				beans = null;
				playMusicId = 0;
			}
		}
	}

	public static void stop() { //停止了，移除线程，恢复一些初始数据
		if (player != null) {
			player.stop();
			player.release();
			player = null;
			handler2.removeCallbacks(updateWords);
			handler.removeCallbacks(updateBar);
			flag = 0;
			wordTime = 0;
			playMusicServer.stopSelf();
		}
	}

}
