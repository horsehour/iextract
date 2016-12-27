package com.horsehour.util;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Image Utilities
 *
 * @author Chunheng Jiang
 * @version 1.0
 * @since 6:38:55 PM, Jun 8, 2016
 *
 */

public class ImageUtils {
	/**
	 * @param src
	 *            source image
	 * @param width
	 *            width of thumb image
	 * @param height
	 *            height of thumb image
	 * @param scale
	 *            scaled factor
	 * @return thumb image
	 */
	public static BufferedImage getThumb(BufferedImage src, int width, int height, boolean scale) {
		double sx = (double) width / src.getWidth(), sy = (double) height / src.getHeight();

		// 使用最小的比例缩放
		if (scale) {
			if (sx > sy)
				width = (int) (sy * src.getWidth());
			else
				height = (int) (sx * src.getHeight());
		}

		BufferedImage target = null;
		int t = src.getType();

		// 如果没有匹配类型
		if (t == BufferedImage.TYPE_CUSTOM) {
			ColorModel cm = src.getColorModel();

			// 光栅
			WritableRaster raster = null;
			raster = cm.createCompatibleWritableRaster(width, height);

			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
			target = new BufferedImage(cm, raster, alphaPremultiplied, null);

		} else
			target = new BufferedImage(width, height, t);

		Graphics2D graphic = target.createGraphics();

		graphic.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphic.drawRenderedImage(src, AffineTransform.getScaleInstance(sx, sy));
		graphic.dispose();

		return target;
	}

	public static BufferedImage getThumb(String srcFile, int width, int height, boolean scale) {
		BufferedImage image = readImage(srcFile);
		return getThumb(image, width, height, scale);
	}

	public static void getThumb(String src, String dest, int width, int height, boolean scale) {
		BufferedImage image = getThumb(src, width, height, scale);
		writeImage(image, dest);
	}

	/**
	 * Add Water Mark to Image
	 * 
	 * @param imgFile
	 *            source image
	 * @param markFile
	 *            image with water mark
	 * @param x
	 *            x-coordination of water mark in image
	 * @param y
	 *            y-coordination of water mark in image
	 * @param alpha
	 *            transparence [0.1,1.0]
	 */
	public static void watermark(String imgFile, String markFile, int x, int y, float alpha) {
		try {
			// 加载待处理图片文件
			Image img = ImageIO.read(new File(imgFile));

			BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null),
					BufferedImage.TYPE_INT_RGB);

			Graphics2D graphic = image.createGraphics();
			graphic.drawImage(img, 0, 0, null);

			// 加载水印图片文件
			Image mark = ImageIO.read(new File(markFile));
			graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			graphic.drawImage(mark, x, y, null);

			graphic.dispose();

			// 保存处理后的文件
			FileOutputStream out = new FileOutputStream(imgFile);
			ImageIO.write(image, "jpg", out);

			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Add Water Mark to Image
	 * 
	 * @param imgFile
	 *            source image
	 * @param text
	 *            water text
	 * @param font
	 *            font of water mark text
	 * @param color
	 *            colour of water mark text
	 * @param x
	 *            x-coordination of water mark in image
	 * @param y
	 *            y-coordination of water mark in image
	 * @param alpha
	 *            transparence [0.1,1.0]
	 */
	public static void watermark(String imgFile, String text, Font font, Color color, int x, int y, float alpha) {
		try {

			// 默认使用的字体
			if (font == null)
				font = new Font("宋体", 20, 13);

			Image img = ImageIO.read(new File(imgFile));

			BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null),
					BufferedImage.TYPE_INT_RGB);

			Graphics2D graphics = image.createGraphics();

			graphics.drawImage(img, 0, 0, null);

			graphics.setColor(color);
			graphics.setFont(font);
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			graphics.drawString(text, x, y);

			graphics.dispose();

			FileOutputStream out = new FileOutputStream(imgFile);

			ImageIO.write(image, "jpg", out);

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Read Image
	 * 
	 * @param imgFile
	 *            image file
	 * @return image data
	 */
	public static BufferedImage readImage(String imgFile) {
		BufferedImage image = null;
		try {
			InputStream input = new FileInputStream(new File(imgFile));
			image = ImageIO.read(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return image;
	}

	/**
	 * Write Image
	 * 
	 * @param imageData
	 * @param imageFormat
	 * @param destFile
	 */
	public static void writeImage(BufferedImage imageData, String imageFormat, String destFile) {
		try {
			ImageIO.write(imageData, imageFormat, new File(destFile));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void writeImage(BufferedImage image, String format, File destFile) {
		writeImage(image, format, destFile.toString());
	}

	public static void writeImage(BufferedImage image, String destFile) {
		writeImage(image, "jpg", destFile);
	}

	/**
	 * 采集图像核心数据块:RGB像素,每个像素由三部分组成(red, green, blue),每部分使用8位二进制表示(0-255)
	 * 
	 * @param srcFile
	 * @return rgb pixel array
	 */
	public static int[][] getPixel(String srcFile) {
		BufferedImage image = readImage(srcFile);

		int width = image.getWidth();
		int height = image.getHeight();
		int[][] blockRGB = new int[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				blockRGB[i][j] = image.getRGB(i, j) & 0xFFFFFF;// getRGB得到的是ARGB,转化为RGB(0xRRGGBB)
		return blockRGB;
	}

	/**
	 * @param srcFile
	 * @param binFile
	 */
	public static void binarized(String srcFile, String binFile) {
		BufferedImage image = readImage(srcFile);

		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		writeImage(grayImage, binFile);
	}

	/**
	 * @param srcFile
	 * @param grayFile
	 */
	public static void grayscaled(String srcFile, String grayFile) {
		BufferedImage image = readImage(srcFile);

		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		writeImage(grayImage, grayFile);
	}

	/**
	 * @param pixels
	 * @return gray scale value
	 */
	public static int grayscale(int pixels) {
		int red = (pixels >> 16) & 0xFF;
		int green = (pixels >> 8) & 0xFF;
		int blue = (pixels) & 0xFF;

		return (int) (0.3 * red + 0.59 * green + 0.11 * blue);
	}

	/**
	 * 生产图片指纹
	 * <p>
	 * （1）缩小尺寸：8*8个像素
	 * </p>
	 * <p>
	 * （2）简化色彩：将缩小后的图片从rgb转到灰度（64级）
	 * </p>
	 * <p>
	 * （3）计算平均值：计算所有64个像素点的灰度平均值
	 * </p>
	 * <p>
	 * （4）比较像素的灰度：将每个像素的灰度与均值比较，大于或等于均值，记为1，否则为0
	 * </p>
	 * <p>
	 * （5）计算哈希值：组合所有像素灰度比较结果构成64位二进制字串，成为十六进制的图片指纹
	 * </p>
	 * 
	 * @param imgFile
	 * @return 图片指纹
	 */
	public static String produceFingerprint(String imgFile) {
		BufferedImage src = readImage(imgFile);
		if (src == null)// not be a image file
			return null;

		int width = 8;
		int height = 8;

		BufferedImage thumb = getThumb(src, width, height, false);
		// 撷取每个像素点上的灰度值
		int[][] pixels = new int[width][height];
		int gray = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				pixels[x][y] = grayscale(thumb.getRGB(x, y));
				gray += pixels[x][y];
			}
		}

		// 平均灰度值
		float meanGray = gray / (width * height);

		String bin = "";
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if (pixels[x][y] < meanGray)
					bin += 0;
				else
					bin += 1;

		return MathUtils.bin2hex(bin);
	}

	public static void openImage(String imageFile) {
		openImage(new File(imageFile));
	}

	public static void openImage(File file) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(file.getName());
		frame.setBounds(100, 100, 600, 600);
		frame.setVisible(true);
		frame.setLayout(new BorderLayout());
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(file.getPath()));
		frame.add(label);
	}

	/**
	 * 使用Robot类截取当前电脑屏幕
	 * 
	 * @param delay
	 *            线程等待时间（单位秒），准备需要截取的屏幕
	 * @param dest
	 *            存储截图的地址
	 */
	public static void captureScreen(int delay, String dest) {
		Robot robot;
		try {
			robot = new Robot();

			Thread.sleep(Long.valueOf(delay * 1000L));

			// 获取要抓取的区域Dimension,Here是电脑屏幕尺寸
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

			Rectangle rect = new Rectangle(dim);

			BufferedImage image = robot.createScreenCapture(rect);
			ImageIO.write(image, "png", new File(dest));

		} catch (HeadlessException e) {
			e.printStackTrace();
			return;
		} catch (AWTException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 基于MikTex的bmeps命令行图形转换软件将jpeg、png类型的图片转换为eps格式的文件
	 * 
	 * @param src
	 * @param dest
	 */
	public static void png2eps(String src, String dest) {
		try {
			Runtime.getRuntime().exec("bmeps -c -t png " + src + " " + dest);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 不同图片格式之间的相互转换
	 * 
	 * @param src
	 * @param dest
	 * @param format
	 *            dest format:png,jpeg,gif,bmp
	 */
	public static void convert(String src, String dest, String format) {
		BufferedImage buffImage = null;
		try {
			File srcFile = new File(src);
			buffImage = ImageIO.read(srcFile);
			String name = srcFile.getName();
			name = name.substring(0, name.lastIndexOf("."));
			ImageIO.write(buffImage, format, new File(dest + name + "." + format));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Create Image of Pixel Matrix
	 * 
	 * @see http://www.matrix67.com/blog/archives/6039#more-6039
	 * @see http://www.matrix67.com/blog/archives/292
	 * @param pixelMatrix
	 * @param outFile
	 * @param format
	 */
	public static void createImage(byte[][] pixelMatrix, String outFile, String format) {
		int height = pixelMatrix.length;
		int width = pixelMatrix[0].length;

		int size = height * width;
		byte[] pixelBlock = new byte[size];
		int count = 0;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				pixelBlock[count] = pixelMatrix[i][j];
				count++;
			}
		}

		DataBuffer buffer = new DataBufferByte(pixelBlock, size);
		WritableRaster raster = Raster.createInterleavedRaster(buffer, width / 3, height, width, 3,
				new int[] { 0, 1, 2 }, null);

		ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true,
				Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		BufferedImage image = new BufferedImage(cm, raster, true, null);
		try {
			ImageIO.write(image, format, new File(outFile));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Fractal Patterns Popular in Fractal Geometry
	 */
	public static abstract class FractalPattern {
		public int dim = 2 << 10;

		abstract byte[] rgb(int i, int j);

		public byte[][] pixel() {
			byte[][] pixel = new byte[dim][dim * 3];
			byte[] rbg;
			for (int i = 0; i < dim; i++)
				for (int j = 0; j < dim; j++) {
					rbg = rgb(i, j);
					for (int k = 0; k < 3; k++)
						pixel[i][j * 3 + k] = rbg[k];
				}
			return pixel;
		}
	}

	/**
	 * Mandelbrot Set Z(n+1) = Z(n)^2 + C, where Z(n) = zRe(n) + zIm(n) * i, C
	 * is a complex number. If both zRe(0) and zIm(0) are drawn from [-1.5,1.5],
	 * Mandelbrot set must have a upper bound, i.e. |Z(n)| <= 2.
	 */
	public static class MandelbrotSet extends FractalPattern {
		public float zRe, zIm, cRe, cIm;
		public float minRe, minIm, maxRe, maxIm;
		public float deRe, deIm;

		public int nIter = 200;

		public MandelbrotSet() {
			zRe = 0;
			zIm = 0;

			minRe = minIm = -1.5F;
			maxRe = maxIm = 1.5F;

			deRe = (maxRe - minRe) / dim;
			deIm = (maxIm - minIm) / dim;
		}

		public int bound(int i, int j) {
			cRe = minRe + deRe * i;
			cIm = minIm + deIm * j;

			float re = zRe, im = zIm;
			int n = 0;
			for (; n++ < nIter;) {
				float u = re * re * re - 3 * re * im * im + cRe;
				float v = 3 * re * re * im - im * im * im + cIm;
				re = u;
				im = v;

				if (re * re + im * im > 4)
					break;
			}
			return n;
		}

		@Override
		public byte[] rgb(int i, int j) {
			int n = bound(i, j);
			double base = Math.log(n);
			byte[] pixel = new byte[3];// RGB (red, green, blue)
			pixel[0] = (byte) (base * 47);
			pixel[1] = pixel[0];
			pixel[2] = (byte) (128 - base * 23);
			return pixel;
		}
	}

	public static class JuliaSet extends MandelbrotSet {
		public JuliaSet() {
			cRe = -0.8F;
			cIm = 0.152F;
		}

		public int bound(int i, int j) {
			float re = minRe + deRe * i, im = minIm + deIm * j;
			int n = 0;
			for (; n++ < nIter;) {
				float u = re * re - im * im + cRe;
				float v = 2 * re * im + cIm;
				re = u;
				im = v;

				if (re * re + im * im > 4)
					break;
			}
			return n;
		}
	}

	public static class SierpinskiTriangle extends FractalPattern {
		public SierpinskiTriangle() {
		}

		public byte[] rgb(int i, int j) {
			byte[] rgb = new byte[3];
			return rgb;
		}
	}

	public static void main(String[] args) throws IOException {
		TickClock.beginTick();

		float[] d1 = { -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.5100F, -0.2130F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.2920F, 0.7920F, -0.9870F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				0.5600F, 0.9750F, -0.8730F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.7450F, 0.9990F, -0.7480F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, 0.5960F, 1.0000F, -0.6010F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.7140F, 1.0000F,
				-0.5850F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -0.9980F, 0.7140F, 1.0000F, -0.4540F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -0.9970F,
				0.7980F, 1.0000F, -0.5380F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.4020F, 1.0000F, -0.3900F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, 0.3540F, 1.0000F, -0.1420F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.2220F, 1.0000F,
				-0.0600F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.3400F, 1.0000F, -0.2110F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				0.2420F, 1.0000F, -0.1130F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -0.9670F, 0.5940F, 1.0000F, -0.4310F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -0.1400F, 1.0000F, 0.9890F, -0.7050F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -0.6540F, 0.6660F, 0.3010F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F };

		float[] d5 = { -1.0000F, -1.0000F, -1.0000F, -0.8130F, -0.6710F, -0.8090F, -0.8870F, -0.6710F, -0.8530F,
				-1.0000F, -1.0000F, -0.7740F, -0.1800F, 0.0520F, -0.2410F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				0.3920F, 1.0000F, 0.8570F, 0.7270F, 1.0000F, 0.8050F, 0.6130F, 0.6130F, 0.8600F, 1.0000F, 1.0000F,
				0.3960F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -0.5480F, 1.0000F, 1.0000F, 1.0000F, 1.0000F, 1.0000F,
				1.0000F, 1.0000F, 1.0000F, 1.0000F, 1.0000F, 0.8750F, -0.9570F, -1.0000F, -1.0000F, -1.0000F, -0.7860F,
				0.9610F, 1.0000F, 1.0000F, 1.0000F, 0.7270F, 0.4030F, 0.4030F, 0.1710F, -0.3140F, -0.3140F, -0.9400F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -0.2980F, 1.0000F, 1.0000F, 1.0000F, 0.4400F, 0.0560F, -0.7550F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.3660F,
				1.0000F, 1.0000F, 1.0000F, 1.0000F, 1.0000F, 0.8890F, -0.0810F, -0.9200F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -0.3960F, 0.8860F, 0.9740F, 0.8510F, 0.8510F, 0.9500F, 1.0000F,
				1.0000F, 0.5390F, -0.7540F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-0.8860F, -0.5050F, -1.0000F, -1.0000F, -0.6490F, 0.4050F, 1.0000F, 1.0000F, 0.6530F, -0.8380F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -0.5500F, 0.9930F, 1.0000F, 0.6180F, -0.8690F, -1.0000F, -0.9600F, -0.5120F,
				0.1340F, -0.3430F, -0.7960F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -0.4320F,
				0.9940F, 1.0000F, 0.2230F, -1.0000F, 0.4260F, 1.0000F, 1.0000F, 1.0000F, 0.2140F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, 0.2920F, 1.0000F, 0.9670F, -0.8800F, 0.4490F, 1.0000F,
				0.8960F, -0.0940F, -0.7500F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F, -1.0000F,
				-0.6270F, 1.0000F, 1.0000F, 0.1980F, -0.1050F, 1.0000F, 1.0000F, 1.0000F, 0.6390F, -0.1680F, -0.3140F,
				-0.4460F, -1.0000F, -1.0000F, -0.9990F, -0.3370F, 0.1470F, 0.9960F, 1.0000F, 0.6670F, -0.8080F, 0.0650F,
				0.9930F, 1.0000F, 1.0000F, 1.0000F, 1.0000F, 0.9960F, 0.9700F, 0.9700F, 0.9700F, 0.9980F, 1.0000F,
				1.0000F, 1.0000F, 0.1090F, -1.0000F, -1.0000F, -0.8300F, -0.2420F, 0.3500F, 0.8000F, 1.0000F, 1.0000F,
				1.0000F, 1.0000F, 1.0000F, 1.0000F, 1.0000F, 1.0000F, 0.6160F, -0.9300F, -1.0000F, -1.0000F, -1.0000F,
				-1.0000F, -1.0000F, -0.8580F, -0.6710F, -0.6710F, -0.0330F, 0.7610F, 0.7620F, 0.1260F, -0.0950F,
				-0.6710F, -0.8280F, -1.0000F };
		
		int dim = 16;
		byte[][] pixel1 = new byte[dim][dim * 3];
		byte[][] pixel5 = new byte[dim][dim * 3];

		int n = 0;
		for (int i = 0; i < dim; i++)
			for (int j = 0; j < dim; j++, n++) {
				for (int k = 0; k < 3; k++){
					pixel1[i][j * 3 + k] = (byte) (128 * (d1[n] + 1));
					pixel5[i][j * 3 + k] = (byte) (128 * (d5[n] + 1));
				}
			}

		ImageUtils.createImage(pixel1, "data/1.png", "png");
		ImageUtils.createImage(pixel5, "data/5.png", "png");

		ImageUtils.getThumb("data/1.png", "data/1.png", 512, 512, true);
		ImageUtils.getThumb("data/5.png", "data/5.png", 512, 512, true);

		TickClock.stopTick();
	}
}
