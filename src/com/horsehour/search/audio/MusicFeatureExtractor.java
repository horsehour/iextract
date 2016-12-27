package com.horsehour.search.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Extract Music Fingerprint
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20150715 8:11:35
 **/
public class MusicFeatureExtractor implements Callable<Map<String, Long[]>> {
	// error-correction, damping
	private final int FUZ_FACTOR = 2;
	private final int LOWER_LIMIT = 40;
	private final int UPPER_LIMIT = 300;

	// 40-120Hz低音子带(lowpass), 120-180Hz中音子带(bandpass), 180-300Hz高音子带(highpass)
	private final int[] RANGE = new int[]{LOWER_LIMIT, 80, 120, 180, UPPER_LIMIT + 1};
	private final String MUSIC_INDEX_DB = "Data/Audio/";

	private final int bufferedLen = 1024;
	private List<File> musicFiles;

	public MusicFeatureExtractor() {}

	public MusicFeatureExtractor(List<File> musicFiles) {
		this();
		this.musicFiles = musicFiles;
	}

	@Override
	public Map<String, Long[]> call(){
		Map<String, Long[]> fingerprintTable = new HashMap<String, Long[]>();
		byte[] audioData;
		for (File musicFile : musicFiles) {
			audioData = readAudioData(musicFile.getAbsolutePath());
			fingerprintTable.put(musicFile.getAbsolutePath(), getLandmarkFingerprint(audioData));
		}
		return fingerprintTable;
	}

	/**
	 * @return audio format with given parameters
	 */
	public static AudioFormat getAudioFormat(){
		float sampleRate = 44100;// 44.1kHz
		int sampleSizeInBits = 8;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	/**
	 * @param audioFormat
	 * @return decoded audio format
	 */
	public static AudioFormat decodeAudioFormat(AudioFormat audioFormat){
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16, audioFormat.getChannels(),
		        audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);
	}

	/**
	 * @param musicFile
	 * @return audio data of music file
	 */
	public byte[] readAudioData(String musicFile){
		AudioFormat audioFormat = null;
		AudioInputStream audioInputStream = null;

		if (musicFile == null || musicFile.isEmpty())
			recordAudioData();

		try {
			if (musicFile.startsWith("http"))
				audioInputStream = AudioSystem.getAudioInputStream(new URL(musicFile));
			else
				audioInputStream = AudioSystem.getAudioInputStream(new File(musicFile));

			audioFormat = audioInputStream.getFormat();
			audioFormat = decodeAudioFormat(audioFormat);// decode
			audioInputStream = AudioSystem.getAudioInputStream(audioFormat, audioInputStream);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[bufferedLen];
		byte[] audioData = null;

		int n = 1000;
		try {
			while (n > 0) {
				n--;
				int count = 0;
				if ((count = audioInputStream.read(buffer, 0, bufferedLen)) > 0)
					out.write(buffer, 0, count);
			}
			audioData = out.toByteArray();
			out.close();
			audioInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return audioData;
	}

	/**
	 * record audio data according to an microphone
	 * 
	 * @return audio data recorded via an external microphone
	 */
	public byte[] recordAudioData(){
		AudioFormat audioFormat = getAudioFormat();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		TargetDataLine microphone = null;
		try {
			microphone = (TargetDataLine) AudioSystem.getLine(info);
			microphone.open(audioFormat);
			microphone.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[bufferedLen];
		byte[] audioData = null;
		int n = 2000;

		try {
			while (n > 0) {
				n--;
				int count = 0;
				if ((count = microphone.read(buffer, 0, bufferedLen)) > 0)
					out.write(buffer, 0, count);
			}

			audioData = out.toByteArray();
			out.close();
			microphone.close();
		} catch (IOException e) {
			System.err.println("I/O problems: " + e);
			System.exit(-1);
		}
		finally {
			saveAudio(audioData);
		}
		return audioData;
	}

	/**
	 * save audio file recorded from a microphone
	 */
	public void saveAudio(byte[] audioData){
		AudioFormat audioFormat = getAudioFormat();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
		AudioInputStream audioInputStream = null;
		audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioData.length / audioFormat.getFrameSize());

		new File(MUSIC_INDEX_DB).mkdir();
		long time = System.currentTimeMillis();
		File file = new File(MUSIC_INDEX_DB + "/" + time + ".wav");

		try {
			AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (byteArrayInputStream != null)
					byteArrayInputStream.close();
				if (audioInputStream != null)
					audioInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * landmark-based audio fingerprint extraction used in Shazam music matching
	 * service
	 * 
	 * @param audioData
	 * @return fingerprint of audio data
	 */
	public Long[] getLandmarkFingerprint(byte[] audioData){
		Complex[][] ret = musicFFT(audioData);
		int nSlice = ret.length;
		long[][] points = new long[nSlice][5];
		double[][] peakPoints = new double[nSlice][5];

		Long[] fingerprint = new Long[nSlice];
		for (int slice = 0; slice < nSlice; slice++) {
			for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {
				double magnitude = Math.log(ret[slice][freq].abs() + 1);// logarithmic
				                                                        // value
				int index = getIndex(freq);
				if (magnitude > peakPoints[slice][index]) {
					points[slice][index] = freq;
					peakPoints[slice][index] = magnitude;
				}
			}
			// only 4 data point are used for hashing
			long h = hash(points[slice][0], points[slice][1], points[slice][2], points[slice][3]);
			fingerprint[slice] = h;
		}
		return fingerprint;
	}

	/**
	 * @param audioData
	 * @return Fast Fourier Transformation (FFT) of audio data
	 */
	private Complex[][] musicFFT(byte[] audioData){
		int totalSize = audioData.length;
		int sliceSize = 1024 * 4;
		int nSlice = totalSize / sliceSize;

		Complex[][] ret = new Complex[nSlice][];
		Complex[] x = null;
		for (int slice = 0; slice < nSlice; slice++) {
			x = new Complex[sliceSize];
			for (int j = 0; j < sliceSize; j++)
				x[j] = new Complex(audioData[(slice * sliceSize) + j], 0);// imaginary
			                                                              // part
			                                                              // is
			                                                              // zero
			ret[slice] = FFT.fft(x);
		}
		return ret;
	}

	/**
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param p4
	 * @return hash code of music
	 */
	public long hash(long p1, long p2, long p3, long p4){
		return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR)) * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100 + (p1 - (p1 % FUZ_FACTOR));
	}

	/**
	 * @param freq
	 * @return index of given frequency
	 */
	public int getIndex(int freq){
		int i = 0;
		while (RANGE[i] < freq)
			i++;
		return i;
	}
}
