package com.horsehour.search.audio;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FileUtils;

import weka.core.SerializationHelper;

import com.horsehour.util.TickClock;

/**
 * Music Recognizer
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20150714 PM 6:50:14
 * @see An Industrial-Strength Audio Search Algorithm
 * @see http://www.redcode.nl/blog/2010/06/creating-shazam-in-java/
 * @see https://github.com/wsieroci/audiorecognizer
 * @see http://blog.csdn.net/yutianzuijin/article/details/45418035
 * @see http://www.slideshare.net/royvanrijn/what-shazam-doesnt-want-you-to-know
 * @see http://echoprint.me/
 **/
public class MusicRecognizer implements Serializable {
	private static final long serialVersionUID = -1182540974659273187L;

	private final String MUSIC_INDEX_DB = "Data/Audio/";
	private final int bufferedLen = 1024;

	public List<String> musicDB;
	public Map<Long, List<AudioSlice>> musicFingerprintDB;

	public MusicRecognizer() {
		musicDB = new ArrayList<>();
		musicFingerprintDB = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public MusicRecognizer(String dbFile) throws Exception {
		this();
		String mfdbFile = MUSIC_INDEX_DB + "/MusicFingerprintDB.txt";
		String mdbFile = MUSIC_INDEX_DB + "/MusicDB.txt";
		if (new File(mdbFile).exists())
			musicDB = (List<String>) SerializationHelper.read(mdbFile);

		if (new File(mfdbFile).exists()) {
			musicFingerprintDB = (Map<Long, List<AudioSlice>>) SerializationHelper.read(mfdbFile);
		} else
			indexMusicDB(dbFile);
	}

	/**
	 * Audio Slice in Data Base
	 * 
	 * @author Chunheng Jiang
	 * @version 1.0
	 * @since 20150715 14:39
	 */
	public class AudioSlice implements Serializable {
		private static final long serialVersionUID = -7145469145479103488L;
		public int id;
		public int sliceId;// slice id

		public AudioSlice(int id, int sliceId) {
			this.id = id;
			this.sliceId = sliceId;
		}
	}

	/**
	 * index all musics in data ware
	 * 
	 * @param musicware
	 */
	public void indexMusicDB(String musicware){
		FileFilter ff = new FileFilter() {
			@Override
			public boolean accept(File file){
				String name = file.getName();
				if (name.endsWith(".mp3") || name.endsWith(".wav"))
					return true;
				return false;
			}
		};
		File[] fileList = new File(musicware).listFiles(ff);

		int nTask = 2 * Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nTask);// 支持固定大小的线程池
		List<MusicFeatureExtractor> taskList = new ArrayList<>();
		List<Future<Map<String, Long[]>>> futureList = null;

		List<File> musicFiles = null;
		for (int i = 0; i < nTask; i++) {// assign tasks
			musicFiles = new ArrayList<>();
			for (File file : fileList)
				if (file.hashCode() % nTask == i)
					musicFiles.add(file);
			taskList.add(new MusicFeatureExtractor(musicFiles));
		}

		try {
			futureList = exec.invokeAll(taskList);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			exec.shutdown();
		}

		int id = -1;
		List<AudioSlice> audioSlices;
		Map<String, Long[]> fingerprintTable = null;
		Long[] fingerprintList;
		long fingerprint = 0;
		for (Future<Map<String, Long[]>> future : futureList) {
			try {
				fingerprintTable = future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

			for (String musicFile : fingerprintTable.keySet()) {
				fingerprintList = fingerprintTable.get(musicFile);
				musicDB.add(musicFile);
				id++;
				for (int sliceId = 0; sliceId < fingerprintList.length; sliceId++) {
					fingerprint = fingerprintList[sliceId];
					if ((audioSlices = musicFingerprintDB.get(fingerprint)) == null) {
						audioSlices = new ArrayList<AudioSlice>();
						audioSlices.add(new AudioSlice(id, sliceId));
						musicFingerprintDB.put(fingerprint, audioSlices);
					} else
						audioSlices.add(new AudioSlice(id, sliceId));
				}
			}
		}

		try {
			SerializationHelper.write(MUSIC_INDEX_DB + "/MusicFingerprintDB.txt", musicFingerprintDB);
			SerializationHelper.write(MUSIC_INDEX_DB + "/MusicDB.txt", musicDB);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * play the music
	 * 
	 * @param musicFile
	 */
	public void playback(String musicFile){
		playback(musicFile, 3600);// 1 hour enough for most music
	}

	/**
	 * play the music for certain amounts of time
	 * 
	 * @param musicFile
	 * @param seconds
	 */
	public void playback(String musicFile, float seconds){
		if (musicFile == null || musicFile.isEmpty()) {
			System.err.println("Empty File.");
			return;
		}

		AudioInputStream audioInputStream = null;
		try {
			if (musicFile.startsWith("http"))
				audioInputStream = AudioSystem.getAudioInputStream(new URL(musicFile));
			else
				audioInputStream = AudioSystem.getAudioInputStream(new File(musicFile));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		AudioFormat audioFormat = audioInputStream.getFormat();// 音频格式

		// mp3编码格式MPEG1L3强制转换
		if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
			audioFormat = MusicFeatureExtractor.decodeAudioFormat(audioFormat);
			audioInputStream = AudioSystem.getAudioInputStream(audioFormat, audioInputStream);
		}
		playback(audioFormat, audioInputStream, seconds);
	}

	/**
	 * play the audio media for certain amounts of time
	 * 
	 * @param audioFormat
	 * @param audioInputStream
	 * @param seconds
	 */
	private void playback(AudioFormat audioFormat, AudioInputStream audioInputStream, float seconds){
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		SourceDataLine speaker = null;
		try {
			speaker = (SourceDataLine) AudioSystem.getLine(info);
			speaker.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		if (speaker == null)
			return;

		speaker.start();// 写入前启动

		byte[] bufferedData = new byte[bufferedLen];
		try {
			int count = 0;
			long start = System.currentTimeMillis();
			while ((count = audioInputStream.read(bufferedData, 0, bufferedLen)) > 0) {
				speaker.write(bufferedData, 0, count);// 写入数据
				if ((System.currentTimeMillis() - start) > 1000 * seconds)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			speaker.drain();
			speaker.close();
			try {
				if (audioInputStream != null)
					audioInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * retrieval music file from music data base
	 * 
	 * @param queryMusic
	 * @return relevant result
	 */
	public Map<Integer, Map<Integer, Integer>> retrieval(String queryMusic){
		MusicFeatureExtractor mfe = new MusicFeatureExtractor();
		byte[] audioData = mfe.readAudioData(queryMusic);
		Long[] fingerprintList = mfe.getLandmarkFingerprint(audioData);

		List<AudioSlice> audioSlices;
		Long fingerprint;

		// id, diffChunk, count
		Map<Integer, Map<Integer, Integer>> matchTable = null;
		matchTable = new HashMap<Integer, Map<Integer, Integer>>();
		for (int sliceId = 0; sliceId < fingerprintList.length; sliceId++) {
			fingerprint = fingerprintList[sliceId];
			if ((audioSlices = musicFingerprintDB.get(fingerprint)) != null) {
				for (AudioSlice audioSlice : audioSlices) {
					int sliceOffset = audioSlice.sliceId - sliceId;
					Map<Integer, Integer> sliceStat = null;
					if ((sliceStat = matchTable.get(audioSlice.id)) == null) {
						sliceStat = new HashMap<>();
						sliceStat.put(sliceOffset, 1);
						matchTable.put(audioSlice.id, sliceStat);
					} else {
						Integer count = sliceStat.get(sliceOffset);
						if (count == null)
							sliceStat.put(sliceOffset, 1);
						else
							sliceStat.put(sliceOffset, count + 1);
					}
				}
			}
		}
		return matchTable;
	}

	/**
	 * @param matchTable
	 * @param musicFile
	 * @return Most Relevant Music File for Given One
	 * @throws IOException 
	 */
	public String getMostRelevant(Map<Integer, Map<Integer, Integer>> matchTable, String musicFile) throws IOException{
		String destFile = MUSIC_INDEX_DB + "/SearchResult.txt";
		StringBuffer sb = new StringBuffer();
		if (matchTable == null || matchTable.size() == 0) {
			sb.append("Fail to Find A Relevant Music from Database for " + musicFile + ".\r\n");
			FileUtils.write(new File(destFile), sb.toString(),"", false);
			return null;
		}

		sb.append("Music List Associated to File " + musicFile + ":\r\n");
		int bestId = -1;
		int bestCount = 0;

		for (int id : matchTable.keySet()) {
			sb.append(id + " - " + musicDB.get(id) + "\r\n");

			int maxCount = 0;
			for (Map.Entry<Integer, Integer> entry : matchTable.get(id).entrySet()) {
				int count = entry.getValue();
				if (count > maxCount)
					maxCount = count;
				sb.append("\t[" + entry.getKey() + "," + entry.getValue() + "]\r\n");
			}

			if (maxCount > bestCount) {
				bestCount = maxCount;
				bestId = id;
			}
		}

		String highestRelv = musicDB.get(bestId);
		sb.append("Most Relevant Audio File: " + highestRelv);
		FileUtils.write(new File(destFile), sb.toString(), "",false);
		return highestRelv;
	}

	/**
	 * @param matchTable
	 * @param musicFile
	 * @return score of each matched audio file
	 */
	public List<Float> score(Map<Integer, Map<Integer, Integer>> matchTable, String musicFile){
		return null;
	}

	public static void main(String[] args) throws Exception{
		TickClock.beginTick();

		MusicRecognizer mr = new MusicRecognizer();
		String musicDB = "F:/Music/";
		// mr.indexMusicDB(musicDB);
		mr = new MusicRecognizer(musicDB);
		String queryMusic = "Data/Audio/Hole In My Soul-Piece.mp3";// 片段

		mr.playback(queryMusic, 30);
		String mostRelv = mr.getMostRelevant(mr.retrieval(queryMusic), queryMusic);
		mr.playback(mostRelv);

		TickClock.stopTick();
	}
}