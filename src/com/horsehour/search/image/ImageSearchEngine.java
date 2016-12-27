package com.horsehour.search.image;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.horsehour.util.ImageUtils;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130516
 */
public class ImageSearchEngine {
	public static void PHADemo(){
		String imageDB = "C:/Users/dell/Pictures/MyPic/HorseshoeBend/";
		String queryImage = imageDB + "latemorning.jpg";

		ImageUtils.openImage(queryImage);// 检索图片

		PerceptualHash pha = new PerceptualHash(imageDB);
		File similarImageFile = pha.searchSimilarImage(queryImage);

		ImageUtils.openImage(similarImageFile);// 相似图片
	}

	public static void otsuDemo(String srcFile){
		BufferedImage srcImage = ImageUtils.readImage(srcFile);

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();

		// Get raw image data
		Raster raster = srcImage.getData();
		DataBuffer buffer = raster.getDataBuffer();

		int type = buffer.getDataType();
		if (type != DataBuffer.TYPE_BYTE) {
			System.err.println("Wrong image data type");
			System.exit(1);
		}

		if (buffer.getNumBanks() != 1) {
			System.err.println("Wrong image data format");
			System.exit(1);
		}

		DataBufferByte byteBuffer = (DataBufferByte) buffer;
		byte[] srcData = byteBuffer.getData(0);

		// Sanity check image
		if (width * height != srcData.length) {
			System.err.println("Unexpected grayscaled image");
			System.exit(1);
		}

		System.out.printf("'%s', w-h:(%d,%d), size:%5.2fk\n", srcFile, width, height, 1.0F
		        * srcData.length / (1 << 10));

		byte[] dstData = new byte[srcData.length];

		// Create Otsu Thresholder
		OtsuMethod thresholder = new OtsuMethod();
		int threshold = thresholder.doThreshold(srcData, dstData);

		System.out.println("Threshold:" + threshold);

		// Create GUI
		GreyFrame srcFrame = new GreyFrame(width, height, srcData);
		GreyFrame dstFrame = new GreyFrame(width, height, dstData);
		GreyFrame histFrame = createHistogramFrame(thresholder);

		JPanel infoPanel = new JPanel();
		infoPanel.add(histFrame);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));
		panel.add(infoPanel, BorderLayout.NORTH);
		panel.add(srcFrame, BorderLayout.WEST);
		panel.add(dstFrame, BorderLayout.EAST);
		panel.add(new JLabel("OtsuMethod", JLabel.CENTER), BorderLayout.SOUTH);

		JFrame frame = new JFrame("OTSU");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);

		// Save Images
		int dotPos = srcFile.lastIndexOf(".");
		String basename = srcFile.substring(0, dotPos);

		ImageUtils.writeImage(dstFrame.getBufferImage(), "png", basename + "-BW.png");
		ImageUtils.writeImage(histFrame.getBufferImage(), "png", basename + "-hist.png");
	}

	private static GreyFrame createHistogramFrame(OtsuMethod otsu){
		int numPixels = 256 * 100;
		byte[] histPlotData = new byte[numPixels];

		int[] histData = otsu.getHistData();
		int max = otsu.getMaxLevelValue();
		int threshold = otsu.getThreshold();

		for (int l = 0; l < 256; l++) {
			int ptr = (numPixels - 256) + l;
			int val = (100 * histData[l]) / max;

			if (l == threshold) {
				for (int i = 0; i < 100; i++, ptr -= 256)
					histPlotData[ptr] = (byte) 128;
			} else {
				for (int i = 0; i < 100; i++, ptr -= 256)
					histPlotData[ptr] = (val < i) ? (byte) 255 : 0;
			}
		}
		return new GreyFrame(256, 100, histPlotData);
	}

	public static void main(String[] args){

		String srcFile = "E:/蒋春恒/Photo/s.jpg";
		String grayFile = "C:/Users/dell/Pictures/MyPic/gray.jpg";
		ImageUtils.grayscaled(srcFile, grayFile);
		otsuDemo(grayFile);
	}
}
