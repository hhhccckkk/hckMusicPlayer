package com.hck.music.bean;

import java.util.ArrayList;
/**
 * 歌词实体类
 * @author hck
 *
 */
public class SingLrcBean {
	
	public String getMusicTitle() {
		return musicTitle;
	}
	public void setMusicTitle(String musicTitle) {
		this.musicTitle = musicTitle;
	}
	public String getMusicAuter() {
		return musicAuter;
	}
	public void setMusicAuter(String musicAuter) {
		this.musicAuter = musicAuter;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public ArrayList<String> getMusicWords() {
		return musicWords;
	}
	public void setMusicWords(ArrayList<String> musicWords) {
		this.musicWords = musicWords;
	}
	public ArrayList<Long> getMusicTimes() {
		return musicTimes;
	}
	public void setMusicTimes(ArrayList<Long> musicTimes) {
		this.musicTimes = musicTimes;
	}
	private String musicTitle;   //歌曲名字
	private String musicAuter;  //歌曲作者
	private String album;   //专辑名字
	private ArrayList<String> musicWords;  //存放歌词
	private ArrayList<Long> musicTimes;  //歌词对应的时间

}
