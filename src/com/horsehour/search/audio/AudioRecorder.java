package com.horsehour.search.audio;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20150714 AM 12:32:17
 **/
public class AudioRecorder extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1295847831670879156L;

	AudioFormat audioFormat = null;
	TargetDataLine microphone = null;
	SourceDataLine speaker = null;

	// 字节数组输入输出流
	ByteArrayInputStream byteArrayInputStream = null;
	ByteArrayOutputStream byteArrayOutputStream = null;

	// 音频输入流
	AudioInputStream audioInputStream = null;

	boolean isStop = false;
	long timeBeginRecord;

	int bufferLen = 10000;

	JPanel jp1, jp2, jp3;
	JLabel jl1 = null;
	JButton captureBtn, stopBtn, playBtn, saveBtn;

	public AudioRecorder() {
		jp1 = new JPanel();
		jp2 = new JPanel();
		jp3 = new JPanel();

		jl1 = new JLabel("Recording");
		jp1.add(jl1);

		captureBtn = new JButton("Capture");
		captureBtn.addActionListener(this);
		captureBtn.setActionCommand("beginBtn");
		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(this);
		stopBtn.setActionCommand("stopBtn");
		playBtn = new JButton("Playback");
		playBtn.addActionListener(this);
		playBtn.setActionCommand("playBtn");
		saveBtn = new JButton("Save");
		saveBtn.addActionListener(this);
		saveBtn.setActionCommand("saveBtn");

		this.add(jp1, BorderLayout.NORTH);
		this.add(jp2, BorderLayout.CENTER);
		this.add(jp3, BorderLayout.SOUTH);
		jp3.setLayout(null);
		jp3.setLayout(new GridLayout(1, 4, 10, 10));
		jp3.add(captureBtn);
		jp3.add(stopBtn);
		jp3.add(playBtn);
		jp3.add(saveBtn);

		captureBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		playBtn.setEnabled(false);
		saveBtn.setEnabled(false);

		this.setSize(400, 300);
		this.setTitle("AudioRecorder");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("captureBtn")) {
			captureBtn.setEnabled(false);
			stopBtn.setEnabled(true);
			playBtn.setEnabled(false);
			saveBtn.setEnabled(false);

			capture();
			timeBeginRecord = System.currentTimeMillis();

		} else if (e.getActionCommand().equals("stopBtn")) {
			captureBtn.setEnabled(true);
			stopBtn.setEnabled(false);
			playBtn.setEnabled(true);
			saveBtn.setEnabled(true);
			stop();
			long stopPlay = System.currentTimeMillis();
			System.out.println("Play continues for "
			        + (stopPlay - timeBeginRecord));
		} else if (e.getActionCommand().equals("playBtn"))
			playback();
		else if (e.getActionCommand().equals("saveBtn"))
			save();
	}

	/**
	 * capture sound
	 */
	public void capture() {
		try {
			audioFormat = getAudioFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class,
			        audioFormat);
			microphone = (TargetDataLine) (AudioSystem.getLine(info));
			microphone.open(audioFormat);
			microphone.start();

			Thread recordThread = new Thread(new Record());
			recordThread.start();

		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * stop recording/capturing
	 */
	public void stop() {
		isStop = true;
	}

	/**
	 * playback the audio media
	 */
	public void playback() {
		byte[] audioData = byteArrayOutputStream.toByteArray();
		byteArrayInputStream = new ByteArrayInputStream(audioData);

		audioFormat = getAudioFormat();
		audioInputStream = new AudioInputStream(byteArrayInputStream,
		        audioFormat, audioData.length / audioFormat.getFrameSize());

		try {
			DataLine.Info dataLineInfo = new DataLine.Info(
			        SourceDataLine.class, audioFormat);
			speaker = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			speaker.open(audioFormat);
			speaker.start();

			Thread playThread = new Thread(new Play());
			playThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (audioInputStream != null)
					audioInputStream.close();
				if (byteArrayInputStream != null)
					byteArrayInputStream.close();
				if (byteArrayOutputStream != null)
					byteArrayOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * save sound
	 */
	public void save() {
		audioFormat = getAudioFormat();
		byte audioData[] = byteArrayOutputStream.toByteArray();
		byteArrayInputStream = new ByteArrayInputStream(audioData);
		audioInputStream = new AudioInputStream(byteArrayInputStream,
		        audioFormat, audioData.length / audioFormat.getFrameSize());

		File file = null;
		try {
			File srcFile = new File("data/audio/");
			if (!srcFile.exists())
				srcFile.mkdir();

			long time = System.currentTimeMillis();
			file = new File(srcFile + "/" + time + ".wav");
			AudioSystem
			        .write(audioInputStream, AudioFileFormat.Type.WAVE, file);

			String destFile = time + ".mp3";
			Runtime run = null;

			String cmd = "lib/lame.exe -b 16 ";
			cmd += srcFile + "/" + file.getName() + " ";
			cmd += srcFile + "/" + destFile;

			try {
				run = Runtime.getRuntime();
				// 利用解码器lame转换音频文件格式:wav to mp3
				Process p = run.exec(cmd);

				p.getOutputStream().close();
				p.getInputStream().close();
				p.getErrorStream().close();
				p.waitFor();

				if (file.exists())
					file.delete();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				run.freeMemory();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
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
	 * 设置AudioFormat的参数
	 * 
	 * @return 音频格式
	 */
	public AudioFormat getAudioFormat() {
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		// 采样率 - 每秒播放与录制的样本数目, 常用8000, 11025, 16000, 22050, 44100
		float sampleRate = 8000F;
		float frameRate = sampleRate;
		// 每个样本中比特位数, 常用8, 16
		int sampleSizeInBits = 16;
		// 声道:单声道(1)、立体声(2)、多声道....
		int channels = 1;
		// 每一帧字节数
		int frameSize = channels * (sampleSizeInBits) / 8;
		boolean bigEndian = true;
		return new AudioFormat(encoding, sampleRate, sampleSizeInBits,
		        channels, frameSize, frameRate, bigEndian);
	}

	/**
	 * 录音类
	 * 
	 * @author Chunheng Jiang
	 * @version 1.0
	 * @since 20150714
	 */
	protected class Record implements Runnable {
		byte[] buffer = new byte[bufferLen];// 缓存录音的字节数组

		public void run() {
			byteArrayOutputStream = new ByteArrayOutputStream();
			isStop = false;
			try {
				while (isStop == false) {
					int count = microphone.read(buffer, 0, bufferLen);
					if (count > 0)
						byteArrayOutputStream.write(buffer, 0, count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (byteArrayOutputStream != null)
						byteArrayOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					microphone.drain();
					microphone.close();
				}
			}
		}
	}

	/**
	 * 播放类
	 * 
	 * @author Chunheng Jiang
	 * @version 1.0
	 * @since 20150714
	 */
	protected class Play implements Runnable {
		public void run() {
			byte[] buffer = new byte[bufferLen];
			try {
				int count;
				while ((count = audioInputStream.read(buffer, 0, bufferLen)) != -1)
					// 读取数据到缓存数据
					if (count > 0)
						speaker.write(buffer, 0, count);// 将缓存的音频数据写入到混频器

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				speaker.drain();
				speaker.close();
			}
		}
	}

	public static void main(String[] args) {
		new AudioRecorder();
	}
}