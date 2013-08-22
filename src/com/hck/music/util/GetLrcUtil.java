package com.hck.music.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hck.music.bean.SingLrcBean;

/**
 * 此类用来解析LRC文件 将解析完整的LRC文件放入一个LrcInfo对象中 并且返回这个LrcInfo对象s author:java_mzd
 */
public class GetLrcUtil {
	private SingLrcBean beans; 
	private long currentTime = 0;
	private String currentContent;
	private ArrayList<Long> time;
	private ArrayList<String> musicContent;

	public SingLrcBean parser(String path, boolean isNet,
			InputStream inputStream) throws Exception {
		beans = null;
		beans = new SingLrcBean();
		time = new ArrayList<Long>();
		musicContent = new ArrayList<String>();
		if (!isNet) {
			try {
				File file = new File(path);
				InputStream in = new FileInputStream(file);
				getMusicWordInfo(in,false);
			} catch (Exception e) {
				return null;
			}
		} else {
			getMusicWordInfo(inputStream,true);
		}
		return beans;

	}

	/**
	 * 将输入流中的信息解析，返回一个LrcInfo对象
	 * 
	 * @param inputStream
	 *            输入流
	 * @return 解析好的LrcInfo对象
	 * @throws IOException
	 */
	public void getMusicWordInfo(InputStream inputStream, boolean isNet)
			throws IOException {
		BufferedReader reader;
		if (isNet) {
			reader = new BufferedReader(new InputStreamReader(inputStream,"GBK"));
		} else {
			reader = new BufferedReader(new InputStreamReader(inputStream,
					"UTF-8"));
		}
		// 一行一行的读，每读一行，解析一行
		String line = null;
		while ((line = reader.readLine()) != null) {
			parserLine(line);
		}
		beans.setMusicTimes(time);
		beans.setMusicWords(musicContent);
	}

	public void parserLine(String str) {
		// 取得歌曲名信息
		if (str.startsWith("[ti:")) {
			String title = str.substring(4, str.length() - 1);
			beans.setMusicTitle(title);

		}
		// 取得歌手信息
		else if (str.startsWith("[ar:")) {
			String singer = str.substring(4, str.length() - 1);
			beans.setMusicAuter(singer);
		}
		// 取得专辑信息
		else if (str.startsWith("[al:")) {
			String album = str.substring(4, str.length() - 1);

			beans.setAlbum(album);

		}
		// 通过正则取得歌词的内容和时间
		else {
			// 设置正则规则
			String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";
			// 编译
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(str);

			// 如果存在匹配项，则执行以下操作
			while (matcher.find()) {
				// 得到匹配的所有内容
				// String msg = matcher.group();
				// 得到这个匹配项开始的索引
				// int start = matcher.start();
				// 得到这个匹配项结束的索引
				// int end = matcher.end();
				// 得到这个匹配项中的组数
				int groupCount = matcher.groupCount();
				// 得到每个组中内容
				for (int i = 0; i <= groupCount; i++) {
					String timeStr = matcher.group(i);
					if (i == 1) {
						// 将第二组中的内容设置为当前的一个时间点
						currentTime = strToLong(timeStr);
					}
				}
				// 得到时间点后的内容
				String[] content = pattern.split(str);
				// 输出数组内容
				for (int i = 0; i < content.length; i++) {
					if (i == content.length - 1) {
						// 将内容设置为当前内容
						currentContent = content[i];
					}

				}
				time.add(currentTime);
				musicContent.add(currentContent);
				currentContent = "";

			}
		}
	}

	private long strToLong(String timeStr) {
		// 因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位 // 1:使用：分割 2：使用.分割 //
		// 1:浣块敓鐭綇鎷烽敓琛楅潻鎷�2閿熸枻鎷蜂娇閿熸枻鎷�閿熻闈╂嫹
		String[] s = timeStr.split(":");
		int min = Integer.parseInt(s[0]);
		String[] ss = s[1].split("\\.");
		int sec = Integer.parseInt(ss[0]);
		int mill = Integer.parseInt(ss[1]);
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

}