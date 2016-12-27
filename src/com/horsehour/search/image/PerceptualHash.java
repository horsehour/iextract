package com.horsehour.search.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.horsehour.util.MathUtils;
import com.horsehour.util.ImageUtils;

/**
 * 根据感知器哈希算法(Perceptual Hash Algorithm, PHA)从库中检索相似图像
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130516
 */
public class PerceptualHash {
	private final List<File> imageFileDB;
	private final List<String> imageFingerprintDB;
	private int size = 0;

	public PerceptualHash(String imgdb) {
		imageFileDB = new ArrayList<File>();
		imageFingerprintDB = new ArrayList<String>();

		initImageDataBase(imgdb);
		size = imageFileDB.size();
	}

	/**
	 * @param imgdb
	 */
	public void initImageDataBase(String imgdb){
		String fingerprint = "";
		for (File image : FileUtils.listFiles(new File(imgdb), null, false)) {
			fingerprint = ImageUtils.produceFingerprint(image.toString());
			if (fingerprint == null)
				continue;

			imageFileDB.add(image);
			imageFingerprintDB.add(fingerprint);
		}
	}

	/**
	 * @param queryImageFile
	 * @return matched image file according to PHA
	 */
	public File searchSimilarImage(String queryImageFile){
		String queryFP = ImageUtils.produceFingerprint(queryImageFile);
		if (queryFP == null)
			return null;

		int matched = 0;
		String fingerprint = imageFingerprintDB.get(0);
		int mindist = MathUtils.hammingDistance(queryFP, fingerprint);

		for (int i = 1; i < size; i++) {
			int dist = MathUtils.hammingDistance(queryFP, imageFingerprintDB.get(i));
			if (dist < mindist) {
				matched = i;
				mindist = dist;
			}
		}
		return imageFileDB.get(matched);
	}
}
