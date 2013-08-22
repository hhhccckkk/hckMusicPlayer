package com.hck.music.bean;
import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * 
 * @author hck
 *歌曲实体类
 */
public class MusicBean implements Serializable {   //序列话，以便可以用做一个对象进行传递
	private int id;  //音乐id号
	private String musicName;   //音乐名字
	private String musicPlayUrl;   //音乐播放url地址
	private String musicLrcUrl;    //音乐歌词url地址
	private String musicImage;   //专辑图片地址，或者其他图片地址url
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMusicName() {
		return musicName;
	}
	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getMusicPlayUrl() {
		return musicPlayUrl;
	}

	public void setMusicPlayUrl(String musicPlayUrl) {
		this.musicPlayUrl = musicPlayUrl;
	}

	public String getMusicLrcUrl() {
		return musicLrcUrl;
	}

	public void setMusicLrcUrl(String musicLrcUrl) {
		this.musicLrcUrl = musicLrcUrl;
	}

	public String getMusicImage() {
		return musicImage;
	}

	public void setMusicImage(String musicImage) {
		this.musicImage = musicImage;
	}

	

}
