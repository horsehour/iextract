package com.horsehour.search.encoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20110707
 */
public class EncodeDetector extends EncodeTable {
	public int freqGB[][];
	public int freqGBK[][];
	public int freqBig5[][];
	public int freqBig5P[][];
	public int freqEUCTW[][];
	public int freqKR[][];
	public int freqJP[][];

	public EncodeDetector() {
		super();
		freqGB = new int[94][94];
		freqGBK = new int[126][191];
		freqBig5 = new int[94][158];
		freqBig5P = new int[126][191];
		freqEUCTW = new int[94][94];
		freqKR = new int[94][94];
		freqJP = new int[94][94];
		initFrequencyTable();
	}

	public String getWebEncoding(String link) {
		String ret = "UNDEFINED";
		try {
			ret = detectEncoding(new URL(link));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	public String getFileEncoding(String file) {
		return detectEncoding(new File(file));
	}

	public String detectEncoding(File file) {
		byte[] rawtext = new byte[1024];
		int nBytes = 0;
		int offset = 0;
		int guess = UNDEFINED;
		InputStream input;
		try {
			input = new FileInputStream(file);
			while ((nBytes = input.read(rawtext, offset, rawtext.length
			        - offset)) > 0)
				offset += nBytes;

			input.close();
			guess = detectEncoding(rawtext);
		} catch (Exception e) {
			e.printStackTrace();
			return nameList[UNDEFINED];
		}
		return nameList[guess];
	}

	public String detectEncoding(URL url) {
		byte[] rawtext = new byte[1024];
		int nBytes = 0;
		int offset = 0;
		int guess = UNDEFINED;
		InputStream input;
		try {
			URLConnection urlCon = url.openConnection();
			urlCon.setConnectTimeout(6000);
			urlCon.setReadTimeout(6000);
			input = url.openStream();

			while ((nBytes = input.read(rawtext, offset, rawtext.length
			        - offset)) > 0)
				offset += nBytes;

			input.close();
			guess = detectEncoding(rawtext);
		} catch (Exception e) {
			e.printStackTrace();
			return nameList[UNDEFINED];
		}

		return nameList[guess];
	}

	/**
	 * This function looks at the byte array and assigns it a probability score
	 * for each encoding type. The encoding type with the highest probability is
	 * returned
	 * 
	 * @param rawtext
	 * @return One of the encodings from the Encoding enumeration (GB2312, HZ,
	 *         BIG5, EUC_TW, ASCII, or UNDEFINED)
	 */
	private int detectEncoding(byte[] rawtext) {
		int[] scoreList = new int[TOTALTYPES];
		// assign scores
		scoreList[GB2312] = probGB2312(rawtext);
		scoreList[GBK] = probGBK(rawtext);
		scoreList[GB18030] = probGB18030(rawtext);
		scoreList[HZ] = hz_probability(rawtext);
		scoreList[BIG5] = probBIG5(rawtext);
		scoreList[CNS11643] = probEUCTW(rawtext);
		scoreList[ISO2022CN] = probISO2022CN(rawtext);
		scoreList[UTF8] = probUTF8(rawtext);
		scoreList[UNICODE] = probUTF16(rawtext);
		scoreList[EUC_KR] = probEUCKR(rawtext);
		scoreList[CP949] = probCP949(rawtext);
		scoreList[JOHAB] = 0;
		scoreList[ISO2022KR] = probISO2022KR(rawtext);
		scoreList[ASCII] = probASCII(rawtext);
		scoreList[SJIS] = probSJIS(rawtext);
		scoreList[EUC_JP] = probEUCJP(rawtext);
		scoreList[ISO2022JP] = probISO2022JP(rawtext);
		scoreList[UNICODET] = 0;
		scoreList[UNICODES] = 0;
		scoreList[ISO2022CN_GB] = 0;
		scoreList[ISO2022CN_CNS] = 0;
		scoreList[UNDEFINED] = 0;

		int guessId = 0;
		int maxScore = 0;
		for (int i = 0; i < TOTALTYPES; i++) {
			if (scoreList[i] > maxScore) {
				guessId = i;
				maxScore = scoreList[i];
			}
		}

		if (maxScore <= 50)
			guessId = UNDEFINED;

		return guessId;
	}

	public int probGB2312(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, gbchars = 1;
		long gbfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] >= 0) {
			} else {
				dbchars++;
				if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xF7
				        && (byte) 0xA1 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0xFE) {
					gbchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0xA1;
					column = rawtext[i + 1] + 256 - 0xA1;
					if (freqGB[row][column] != 0) {
						gbfreq += freqGB[row][column];
					} else if (15 <= row && row < 55) {
						// In GB high-freq character range
						gbfreq += 200;
					}
				}
				i++;
			}
		}
		rangeval = 50 * ((float) gbchars / (float) dbchars);
		freqval = 50 * ((float) gbfreq / (float) totalfreq);
		return (int) (rangeval + freqval);
	}

	/**
	 * @param rawtext
	 *            pointer to byte array
	 * @return number from 0 to 100 representing probability text in array uses
	 *         GBK encoding
	 */
	public int probGBK(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, gbchars = 1;
		long gbfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] >= 0) {
			} else {
				dbchars++;
				if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xF7
				        && // Original GB range
				        (byte) 0xA1 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0xFE) {
					gbchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0xA1;
					column = rawtext[i + 1] + 256 - 0xA1;
					// System.out.println("original row " + row + " column " +
					// column);
					if (freqGB[row][column] != 0) {
						gbfreq += freqGB[row][column];
					} else if (15 <= row && row < 55) {
						gbfreq += 200;
					}
				} else if ((byte) 0x81 <= rawtext[i]
				        && rawtext[i] <= (byte) 0xFE && // Extended GB range
				        (((byte) 0x80 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE) || ((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E))) {
					gbchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0x81;
					if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
						column = rawtext[i + 1] - 0x40;
					} else {
						column = rawtext[i + 1] + 256 - 0x40;
					}

					if (freqGBK[row][column] != 0) {
						gbfreq += freqGBK[row][column];
					}
				}
				i++;
			}
		}
		rangeval = 50 * ((float) gbchars / (float) dbchars);
		freqval = 50 * ((float) gbfreq / (float) totalfreq);
		// For regular GB files, this would give the same score, so I handicap
		// it slightly
		return (int) (rangeval + freqval) - 1;
	}

	/**
	 * @param rawtext
	 *            pointer to byte array
	 * @return number from 0 to 100 representing probability text in array uses
	 *         GB18030 encoding
	 */
	public int probGB18030(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, gbchars = 1;
		long gbfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			// System.err.println(rawtext[i]);
			if (rawtext[i] >= 0) {
				// asciichars++;
			} else {
				dbchars++;
				if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xF7
				        && // Original GB range
				        i + 1 < rawtextlen && (byte) 0xA1 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0xFE) {
					gbchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0xA1;
					column = rawtext[i + 1] + 256 - 0xA1;
					// System.out.println("original row " + row + " column " +
					// column);
					if (freqGB[row][column] != 0) {
						gbfreq += freqGB[row][column];
					} else if (15 <= row && row < 55) {
						gbfreq += 200;
					}
				} else if ((byte) 0x81 <= rawtext[i]
				        && rawtext[i] <= (byte) 0xFE
				        && // Extended GB range
				        i + 1 < rawtextlen
				        && (((byte) 0x80 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE) || ((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E))) {
					gbchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0x81;
					if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
						column = rawtext[i + 1] - 0x40;
					} else {
						column = rawtext[i + 1] + 256 - 0x40;
					}
					// System.out.println("extended row " + row + " column " +
					// column + " rawtext[i] " + rawtext[i]);
					if (freqGBK[row][column] != 0) {
						gbfreq += freqGBK[row][column];
					}
				} else if ((byte) 0x81 <= rawtext[i]
				        && rawtext[i] <= (byte) 0xFE
				        && // Extended GB range
				        i + 3 < rawtextlen && (byte) 0x30 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0x39
				        && (byte) 0x81 <= rawtext[i + 2]
				        && rawtext[i + 2] <= (byte) 0xFE
				        && (byte) 0x30 <= rawtext[i + 3]
				        && rawtext[i + 3] <= (byte) 0x39) {
					gbchars++;
				}
				i++;
			}
		}
		rangeval = 50 * ((float) gbchars / (float) dbchars);
		freqval = 50 * ((float) gbfreq / (float) totalfreq);
		// For regular GB files, this would give the same score, so I handicap
		// it slightly
		return (int) (rangeval + freqval) - 1;
	}

	/**
	 * @param rawtext
	 *            pointer to byte array
	 * @return number from 0 to 100 representing probability text in array uses
	 *         HZ encoding
	 */
	public int hz_probability(byte[] rawtext) {
		int i, rawtextlen;
		long hzfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int hzstart = 0;
		int row, column;
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen; i++) {
			if (rawtext[i] == '~') {
				if (rawtext[i + 1] == '{') {
					hzstart++;
					i += 2;
					while (i < rawtextlen - 1) {
						if (rawtext[i] == 0x0A || rawtext[i] == 0x0D) {
							break;
						} else if (rawtext[i] == '~' && rawtext[i + 1] == '}') {
							i++;
							break;
						} else if ((0x21 <= rawtext[i] && rawtext[i] <= 0x77)
						        && (0x21 <= rawtext[i + 1] && rawtext[i + 1] <= 0x77)) {
							row = rawtext[i] - 0x21;
							column = rawtext[i + 1] - 0x21;
							totalfreq += 500;
							if (freqGB[row][column] != 0) {
								hzfreq += freqGB[row][column];
							} else if (15 <= row && row < 55) {
								hzfreq += 200;
							}
						} else if ((0xA1 <= rawtext[i] && rawtext[i] <= 0xF7)
						        && (0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= 0xF7)) {
							row = rawtext[i] + 256 - 0xA1;
							column = rawtext[i + 1] + 256 - 0xA1;
							totalfreq += 500;
							if (freqGB[row][column] != 0) {
								hzfreq += freqGB[row][column];
							} else if (15 <= row && row < 55) {
								hzfreq += 200;
							}
						}
						i += 2;
					}
				} else if (rawtext[i + 1] == '}') {
					i++;
				} else if (rawtext[i + 1] == '~') {
					i++;
				}
			}
		}
		if (hzstart > 4) {
			rangeval = 50;
		} else if (hzstart > 1) {
			rangeval = 41;
		} else if (hzstart > 0) { // Only 39 in case the sequence happened to
								  // occur
			rangeval = 39; // in otherwise non-Hz text
		} else {
			rangeval = 0;
		}
		freqval = 50 * ((float) hzfreq / (float) totalfreq);
		return (int) (rangeval + freqval);
	}

	/**
	 * @param rawtext
	 *            pointer to byte array
	 * @return number from 0 to 100 representing probability text in array uses
	 *         Big5 encoding
	 */
	public int probBIG5(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, bfchars = 1;
		float rangeval = 0, freqval = 0;
		long bffreq = 0, totalfreq = 1;
		int row, column;
		// Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] >= 0) {
				// asciichars++;
			} else {
				dbchars++;
				if ((byte) 0xA1 <= rawtext[i]
				        && rawtext[i] <= (byte) 0xF9
				        && (((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E) || ((byte) 0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFE))) {
					bfchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0xA1;
					if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
						column = rawtext[i + 1] - 0x40;
					} else {
						column = rawtext[i + 1] + 256 - 0x61;
					}
					if (freqBig5[row][column] != 0) {
						bffreq += freqBig5[row][column];
					} else if (3 <= row && row <= 37) {
						bffreq += 200;
					}
				}
				i++;
			}
		}
		rangeval = 50 * ((float) bfchars / (float) dbchars);
		freqval = 50 * ((float) bffreq / (float) totalfreq);
		return (int) (rangeval + freqval);
	}

	/**
	 * @param rawtext
	 *            pointer to byte array
	 * @return number from 0 to 100 representing probability text in array uses
	 *         Big5+ encoding
	 */
	public int probBIG5Plus(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, bfchars = 1;
		long bffreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			// System.err.println(rawtext[i]);
			if (rawtext[i] >= 128) {
				// asciichars++;
			} else {
				dbchars++;
				if (0xA1 <= rawtext[i]
				        && rawtext[i] <= 0xF9
				        && // Original Big5 range
				        ((0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) || (0xA1 <= rawtext[i + 1] && rawtext[i + 1] <= 0xFE))) {
					bfchars++;
					totalfreq += 500;
					row = rawtext[i] - 0xA1;
					if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
						column = rawtext[i + 1] - 0x40;
					} else {
						column = rawtext[i + 1] - 0x61;
					}
					// System.out.println("original row " + row + " column " +
					// column);
					if (freqBig5[row][column] != 0) {
						bffreq += freqBig5[row][column];
					} else if (3 <= row && row < 37) {
						bffreq += 200;
					}
				} else if (0x81 <= rawtext[i]
				        && rawtext[i] <= 0xFE
				        && // Extended Big5 range
				        ((0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) || (0x80 <= rawtext[i + 1] && rawtext[i + 1] <= 0xFE))) {
					bfchars++;
					totalfreq += 500;
					row = rawtext[i] - 0x81;
					if (0x40 <= rawtext[i + 1] && rawtext[i + 1] <= 0x7E) {
						column = rawtext[i + 1] - 0x40;
					} else {
						column = rawtext[i + 1] - 0x40;
					}
					// System.out.println("extended row " + row + " column " +
					// column + " rawtext[i] " + rawtext[i]);
					if (freqBig5P[row][column] != 0) {
						bffreq += freqBig5P[row][column];
					}
				}
				i++;
			}
		}
		rangeval = 50 * ((float) bfchars / (float) dbchars);
		freqval = 50 * ((float) bffreq / (float) totalfreq);
		// For regular Big5 files, this would give the same score, so I handicap
		// it slightly
		return (int) (rangeval + freqval) - 1;
	}

	/**
	 * @param rawtext
	 *            pointer to byte array
	 * @return number from 0 to 100 representing probability text in array uses
	 *         EUC-TW (CNS 11643) encoding
	 */
	int probEUCTW(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, cnschars = 1;
		long cnsfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Check to see if characters fit into acceptable ranges
		// and have expected frequency of use
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] >= 0) { // in ASCII range
				// asciichars++;
			} else { // high bit set
				dbchars++;
				if (i + 3 < rawtextlen && (byte) 0x8E == rawtext[i]
				        && (byte) 0xA1 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0xB0
				        && (byte) 0xA1 <= rawtext[i + 2]
				        && rawtext[i + 2] <= (byte) 0xFE
				        && (byte) 0xA1 <= rawtext[i + 3]
				        && rawtext[i + 3] <= (byte) 0xFE) { // Planes 1 - 16
					cnschars++;
					// System.out.println("plane 2 or above CNS char");
					// These are all less frequent chars so just ignore freq
					i += 3;
				} else if ((byte) 0xA1 <= rawtext[i]
				        && rawtext[i] <= (byte) 0xFE
				        && // Plane 1
				        (byte) 0xA1 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0xFE) {
					cnschars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0xA1;
					column = rawtext[i + 1] + 256 - 0xA1;
					if (freqEUCTW[row][column] != 0) {
						cnsfreq += freqEUCTW[row][column];
					} else if (35 <= row && row <= 92) {
						cnsfreq += 150;
					}
					i++;
				}
			}
		}
		rangeval = 50 * ((float) cnschars / (float) dbchars);
		freqval = 50 * ((float) cnsfreq / (float) totalfreq);
		return (int) (rangeval + freqval);
	}

	/**
	 * @param rawtext
	 *            pointer to byte array
	 * @return number from 0 to 100 representing probability text in array uses
	 *         ISO 2022-CN encoding
	 */
	public int probISO2022CN(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, isochars = 1;
		long isofreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Check to see if characters fit into acceptable ranges
		// and have expected frequency of use
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] == (byte) 0x1B && i + 3 < rawtextlen) { // Escape
																   // char ESC
				if (rawtext[i + 1] == (byte) 0x24 && rawtext[i + 2] == 0x29
				        && rawtext[i + 3] == (byte) 0x41) { // GB Escape $ ) A
					i += 4;
					while (rawtext[i] != (byte) 0x1B) {
						dbchars++;
						if ((0x21 <= rawtext[i] && rawtext[i] <= 0x77)
						        && (0x21 <= rawtext[i + 1] && rawtext[i + 1] <= 0x77)) {
							isochars++;
							row = rawtext[i] - 0x21;
							column = rawtext[i + 1] - 0x21;
							totalfreq += 500;
							if (freqGB[row][column] != 0) {
								isofreq += freqGB[row][column];
							} else if (15 <= row && row < 55) {
								isofreq += 200;
							}
							i++;
						}
						i++;
					}
				} else if (i + 3 < rawtextlen && rawtext[i + 1] == (byte) 0x24
				        && rawtext[i + 2] == (byte) 0x29
				        && rawtext[i + 3] == (byte) 0x47) {
					// CNS Escape $ ) G
					i += 4;
					while (rawtext[i] != (byte) 0x1B) {
						dbchars++;
						if ((byte) 0x21 <= rawtext[i]
						        && rawtext[i] <= (byte) 0x7E
						        && (byte) 0x21 <= rawtext[i + 1]
						        && rawtext[i + 1] <= (byte) 0x7E) {
							isochars++;
							totalfreq += 500;
							row = rawtext[i] - 0x21;
							column = rawtext[i + 1] - 0x21;
							if (freqEUCTW[row][column] != 0) {
								isofreq += freqEUCTW[row][column];
							} else if (35 <= row && row <= 92) {
								isofreq += 150;
							}
							i++;
						}
						i++;
					}
				}
				if (rawtext[i] == (byte) 0x1B && i + 2 < rawtextlen
				        && rawtext[i + 1] == (byte) 0x28
				        && rawtext[i + 2] == (byte) 0x42) { // ASCII:
					// ESC
					// ( B
					i += 2;
				}
			}
		}
		rangeval = 50 * ((float) isochars / (float) dbchars);
		freqval = 50 * ((float) isofreq / (float) totalfreq);
		// System.out.println("isochars dbchars isofreq totalfreq " + isochars +
		// " " + dbchars + " " + isofreq + " " + totalfreq + "
		// " + rangeval + " " + freqval);
		return (int) (rangeval + freqval);
		// return 0;
	}

	public int probUTF8(byte[] rawtext) {
		int score = 0;
		int i, rawtextlen = 0;
		int goodbytes = 0, asciibytes = 0;
		// Maybe also use UTF8 Byte Order Mark: EF BB BF
		// Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen; i++) {
			if ((rawtext[i] & (byte) 0x7F) == rawtext[i]) { // One byte
				asciibytes++;
				// Ignore ASCII, can throw off count
			} else if (-64 <= rawtext[i] && rawtext[i] <= -33
			        && // Two bytes
			        i + 1 < rawtextlen && -128 <= rawtext[i + 1]
			        && rawtext[i + 1] <= -65) {
				goodbytes += 2;
				i++;
			} else if (-32 <= rawtext[i]
			        && rawtext[i] <= -17
			        && // Three bytes
			        i + 2 < rawtextlen && -128 <= rawtext[i + 1]
			        && rawtext[i + 1] <= -65 && -128 <= rawtext[i + 2]
			        && rawtext[i + 2] <= -65) {
				goodbytes += 3;
				i += 2;
			}
		}
		if (asciibytes == rawtextlen) {
			return 0;
		}
		score = (int) (100 * ((float) goodbytes / (float) (rawtextlen - asciibytes)));
		// System.out.println("rawtextlen " + rawtextlen + " goodbytes " +
		// goodbytes + " asciibytes " + asciibytes + " score " +
		// score);
		// If not above 98, reduce to zero to prevent coincidental matches
		// Allows for some (few) bad formed sequences
		if (score > 98) {
			return score;
		} else if (score > 95 && goodbytes > 30) {
			return score;
		} else {
			return 0;
		}
	}

	public int probUTF16(byte[] rawtext) {
		if (rawtext.length > 1
		        && ((byte) 0xFE == rawtext[0] && (byte) 0xFF == rawtext[1]) || // Big-endian
		        ((byte) 0xFF == rawtext[0] && (byte) 0xFE == rawtext[1])) { // Little-endian
			return 100;
		}
		return 0;
	}

	public int probASCII(byte[] rawtext) {
		int score = 75;
		int i, rawtextlen;
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen; i++) {
			if (rawtext[i] < 0) {
				score = score - 5;
			} else if (rawtext[i] == (byte) 0x1B) { // ESC (used by ISO 2022)
				score = score - 5;
			}
			if (score <= 0) {
				return 0;
			}
		}
		return score;
	}

	public int probEUCKR(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, krchars = 1;
		long krfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] >= 0) {
			} else {
				dbchars++;
				if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xFE
				        && (byte) 0xA1 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0xFE) {
					krchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0xA1;
					column = rawtext[i + 1] + 256 - 0xA1;
					if (freqKR[row][column] != 0) {
						krfreq += freqKR[row][column];
					} else if (15 <= row && row < 55) {
						krfreq += 0;
					}
				}
				i++;
			}
		}
		rangeval = 50 * ((float) krchars / (float) dbchars);
		freqval = 50 * ((float) krfreq / (float) totalfreq);
		return (int) (rangeval + freqval);
	}

	public int probCP949(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, krchars = 1;
		long krfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] >= 0) {
			} else {
				dbchars++;
				if ((byte) 0x81 <= rawtext[i]
				        && rawtext[i] <= (byte) 0xFE
				        && ((byte) 0x41 <= rawtext[i + 1]
				                && rawtext[i + 1] <= (byte) 0x5A
				                || (byte) 0x61 <= rawtext[i + 1]
				                && rawtext[i + 1] <= (byte) 0x7A || (byte) 0x81 <= rawtext[i + 1]
				                && rawtext[i + 1] <= (byte) 0xFE)) {
					krchars++;
					totalfreq += 500;
					if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xFE
					        && (byte) 0xA1 <= rawtext[i + 1]
					        && rawtext[i + 1] <= (byte) 0xFE) {
						row = rawtext[i] + 256 - 0xA1;
						column = rawtext[i + 1] + 256 - 0xA1;
						if (freqKR[row][column] != 0) {
							krfreq += freqKR[row][column];
						}
					}
				}
				i++;
			}
		}
		rangeval = 50 * ((float) krchars / (float) dbchars);
		freqval = 50 * ((float) krfreq / (float) totalfreq);
		return (int) (rangeval + freqval);
	}

	public int probISO2022KR(byte[] rawtext) {
		int i;
		for (i = 0; i < rawtext.length; i++) {
			if (i + 3 < rawtext.length && rawtext[i] == 0x1b
			        && (char) rawtext[i + 1] == '$'
			        && (char) rawtext[i + 2] == ')'
			        && (char) rawtext[i + 3] == 'C') {
				return 100;
			}
		}
		return 0;
	}

	public int probEUCJP(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, jpchars = 1;
		long jpfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			if (rawtext[i] >= 0) {
			} else {
				dbchars++;
				if ((byte) 0xA1 <= rawtext[i] && rawtext[i] <= (byte) 0xFE
				        && (byte) 0xA1 <= rawtext[i + 1]
				        && rawtext[i + 1] <= (byte) 0xFE) {
					jpchars++;
					totalfreq += 500;
					row = rawtext[i] + 256 - 0xA1;
					column = rawtext[i + 1] + 256 - 0xA1;
					if (freqJP[row][column] != 0) {
						jpfreq += freqJP[row][column];
					} else if (15 <= row && row < 55) {
						jpfreq += 0;
					}
				}
				i++;
			}
		}
		rangeval = 50 * ((float) jpchars / (float) dbchars);
		freqval = 50 * ((float) jpfreq / (float) totalfreq);
		return (int) (rangeval + freqval);
	}

	public int probISO2022JP(byte[] rawtext) {
		int i;
		for (i = 0; i < rawtext.length; i++) {
			if (i + 2 < rawtext.length && rawtext[i] == 0x1b
			        && (char) rawtext[i + 1] == '$'
			        && (char) rawtext[i + 2] == 'B') {
				return 100;
			}
		}
		return 0;
	}

	public int probSJIS(byte[] rawtext) {
		int i, rawtextlen = 0;
		int dbchars = 1, jpchars = 1;
		long jpfreq = 0, totalfreq = 1;
		float rangeval = 0, freqval = 0;
		int row, column, adjust;
		// Stage 1: Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen - 1; i++) {
			// System.err.println(rawtext[i]);
			if (rawtext[i] >= 0) {
				// asciichars++;
			} else {
				dbchars++;
				if (i + 1 < rawtext.length
				        && (((byte) 0x81 <= rawtext[i] && rawtext[i] <= (byte) 0x9F) || ((byte) 0xE0 <= rawtext[i] && rawtext[i] <= (byte) 0xEF))
				        && (((byte) 0x40 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0x7E) || ((byte) 0x80 <= rawtext[i + 1] && rawtext[i + 1] <= (byte) 0xFC))) {
					jpchars++;
					totalfreq += 500;
					row = rawtext[i] + 256;
					column = rawtext[i + 1] + 256;
					if (column < 0x9f) {
						adjust = 1;
						if (column > 0x7f) {
							column -= 0x20;
						} else {
							column -= 0x19;
						}
					} else {
						adjust = 0;
						column -= 0x7e;
					}
					if (row < 0xa0) {
						row = ((row - 0x70) << 1) - adjust;
					} else {
						row = ((row - 0xb0) << 1) - adjust;
					}
					row -= 0x20;
					column = 0x20;
					// System.out.println("original row " + row + " column " +
					// column);
					if (row < freqJP.length && column < freqJP[row].length
					        && freqJP[row][column] != 0) {
						jpfreq += freqJP[row][column];
					}
					i++;
				} else if ((byte) 0xA1 <= rawtext[i]
				        && rawtext[i] <= (byte) 0xDF) {
					// half-width katakana, convert to full-width
				}
			}
		}
		rangeval = 50 * ((float) jpchars / (float) dbchars);
		freqval = 50 * ((float) jpfreq / (float) totalfreq);
		// For regular GB files, this would give the same score, so I handicap
		// it slightly
		return (int) (rangeval + freqval) - 1;
	}

	/**
	 * initialize the frequency table for GB, GBK, Big5, EUC-TW, KR, JP
	 */
	public void initFrequencyTable() {
		int i;
		int j;
		for (i = 0; i < 94; i++)
			for (j = 0; j < 94; j++)
				freqGB[i][j] = 0;

		for (i = 0; i < 126; i++)
			for (j = 0; j < 191; j++)
				freqGBK[i][j] = 0;

		for (i = 0; i < 94; i++)
			for (j = 0; j < 158; j++)
				freqBig5[i][j] = 0;

		for (i = 0; i < 126; i++)
			for (j = 0; j < 191; j++)
				freqBig5P[i][j] = 0;

		for (i = 0; i < 94; i++)
			for (j = 0; j < 94; j++)
				freqEUCTW[i][j] = 0;

		for (i = 0; i < 94; i++)
			for (j = 0; j < 94; j++)
				freqJP[i][j] = 0;

		freqGB[20][35] = 599;
		freqGB[49][26] = 598;
		freqGB[41][38] = 597;
		freqGB[17][26] = 596;
		freqGB[32][42] = 595;
		freqGB[39][42] = 594;
		freqGB[45][49] = 593;
		freqGB[51][57] = 592;
		freqGB[50][47] = 591;
		freqGB[42][90] = 590;
		freqGB[52][65] = 589;
		freqGB[53][47] = 588;
		freqGB[19][82] = 587;
		freqGB[31][19] = 586;
		freqGB[40][46] = 585;
		freqGB[24][89] = 584;
		freqGB[23][85] = 583;
		freqGB[20][28] = 582;
		freqGB[42][20] = 581;
		freqGB[34][38] = 580;
		freqGB[45][9] = 579;
		freqGB[54][50] = 578;
		freqGB[25][44] = 577;
		freqGB[35][66] = 576;
		freqGB[20][55] = 575;
		freqGB[18][85] = 574;
		freqGB[20][31] = 573;
		freqGB[49][17] = 572;
		freqGB[41][16] = 571;
		freqGB[35][73] = 570;
		freqGB[20][34] = 569;
		freqGB[29][44] = 568;
		freqGB[35][38] = 567;
		freqGB[49][9] = 566;
		freqGB[46][33] = 565;
		freqGB[49][51] = 564;
		freqGB[40][89] = 563;
		freqGB[26][64] = 562;
		freqGB[54][51] = 561;
		freqGB[54][36] = 560;
		freqGB[39][4] = 559;
		freqGB[53][13] = 558;
		freqGB[24][92] = 557;
		freqGB[27][49] = 556;
		freqGB[48][6] = 555;
		freqGB[21][51] = 554;
		freqGB[30][40] = 553;
		freqGB[42][92] = 552;
		freqGB[31][78] = 551;
		freqGB[25][82] = 550;
		freqGB[47][0] = 549;
		freqGB[34][19] = 548;
		freqGB[47][35] = 547;
		freqGB[21][63] = 546;
		freqGB[43][75] = 545;
		freqGB[21][87] = 544;
		freqGB[35][59] = 543;
		freqGB[25][34] = 542;
		freqGB[21][27] = 541;
		freqGB[39][26] = 540;
		freqGB[34][26] = 539;
		freqGB[39][52] = 538;
		freqGB[50][57] = 537;
		freqGB[37][79] = 536;
		freqGB[26][24] = 535;
		freqGB[22][1] = 534;
		freqGB[18][40] = 533;
		freqGB[41][33] = 532;
		freqGB[53][26] = 531;
		freqGB[54][86] = 530;
		freqGB[20][16] = 529;
		freqGB[46][74] = 528;
		freqGB[30][19] = 527;
		freqGB[45][35] = 526;
		freqGB[45][61] = 525;
		freqGB[30][9] = 524;
		freqGB[41][53] = 523;
		freqGB[41][13] = 522;
		freqGB[50][34] = 521;
		freqGB[53][86] = 520;
		freqGB[47][47] = 519;
		freqGB[22][28] = 518;
		freqGB[50][53] = 517;
		freqGB[39][70] = 516;
		freqGB[38][15] = 515;
		freqGB[42][88] = 514;
		freqGB[16][29] = 513;
		freqGB[27][90] = 512;
		freqGB[29][12] = 511;
		freqGB[44][22] = 510;
		freqGB[34][69] = 509;
		freqGB[24][10] = 508;
		freqGB[44][11] = 507;
		freqGB[39][92] = 506;
		freqGB[49][48] = 505;
		freqGB[31][46] = 504;
		freqGB[19][50] = 503;
		freqGB[21][14] = 502;
		freqGB[32][28] = 501;
		freqGB[18][3] = 500;
		freqGB[53][9] = 499;
		freqGB[34][80] = 498;
		freqGB[48][88] = 497;
		freqGB[46][53] = 496;
		freqGB[22][53] = 495;
		freqGB[28][10] = 494;
		freqGB[44][65] = 493;
		freqGB[20][10] = 492;
		freqGB[40][76] = 491;
		freqGB[47][8] = 490;
		freqGB[50][74] = 489;
		freqGB[23][62] = 488;
		freqGB[49][65] = 487;
		freqGB[28][87] = 486;
		freqGB[15][48] = 485;
		freqGB[22][7] = 484;
		freqGB[19][42] = 483;
		freqGB[41][20] = 482;
		freqGB[26][55] = 481;
		freqGB[21][93] = 480;
		freqGB[31][76] = 479;
		freqGB[34][31] = 478;
		freqGB[20][66] = 477;
		freqGB[51][33] = 476;
		freqGB[34][86] = 475;
		freqGB[37][67] = 474;
		freqGB[53][53] = 473;
		freqGB[40][88] = 472;
		freqGB[39][10] = 471;
		freqGB[24][3] = 470;
		freqGB[27][25] = 469;
		freqGB[26][15] = 468;
		freqGB[21][88] = 467;
		freqGB[52][62] = 466;
		freqGB[46][81] = 465;
		freqGB[38][72] = 464;
		freqGB[17][30] = 463;
		freqGB[52][92] = 462;
		freqGB[34][90] = 461;
		freqGB[21][7] = 460;
		freqGB[36][13] = 459;
		freqGB[45][41] = 458;
		freqGB[32][5] = 457;
		freqGB[26][89] = 456;
		freqGB[23][87] = 455;
		freqGB[20][39] = 454;
		freqGB[27][23] = 453;
		freqGB[25][59] = 452;
		freqGB[49][20] = 451;
		freqGB[54][77] = 450;
		freqGB[27][67] = 449;
		freqGB[47][33] = 448;
		freqGB[41][17] = 447;
		freqGB[19][81] = 446;
		freqGB[16][66] = 445;
		freqGB[45][26] = 444;
		freqGB[49][81] = 443;
		freqGB[53][55] = 442;
		freqGB[16][26] = 441;
		freqGB[54][62] = 440;
		freqGB[20][70] = 439;
		freqGB[42][35] = 438;
		freqGB[20][57] = 437;
		freqGB[34][36] = 436;
		freqGB[46][63] = 435;
		freqGB[19][45] = 434;
		freqGB[21][10] = 433;
		freqGB[52][93] = 432;
		freqGB[25][2] = 431;
		freqGB[30][57] = 430;
		freqGB[41][24] = 429;
		freqGB[28][43] = 428;
		freqGB[45][86] = 427;
		freqGB[51][56] = 426;
		freqGB[37][28] = 425;
		freqGB[52][69] = 424;
		freqGB[43][92] = 423;
		freqGB[41][31] = 422;
		freqGB[37][87] = 421;
		freqGB[47][36] = 420;
		freqGB[16][16] = 419;
		freqGB[40][56] = 418;
		freqGB[24][55] = 417;
		freqGB[17][1] = 416;
		freqGB[35][57] = 415;
		freqGB[27][50] = 414;
		freqGB[26][14] = 413;
		freqGB[50][40] = 412;
		freqGB[39][19] = 411;
		freqGB[19][89] = 410;
		freqGB[29][91] = 409;
		freqGB[17][89] = 408;
		freqGB[39][74] = 407;
		freqGB[46][39] = 406;
		freqGB[40][28] = 405;
		freqGB[45][68] = 404;
		freqGB[43][10] = 403;
		freqGB[42][13] = 402;
		freqGB[44][81] = 401;
		freqGB[41][47] = 400;
		freqGB[48][58] = 399;
		freqGB[43][68] = 398;
		freqGB[16][79] = 397;
		freqGB[19][5] = 396;
		freqGB[54][59] = 395;
		freqGB[17][36] = 394;
		freqGB[18][0] = 393;
		freqGB[41][5] = 392;
		freqGB[41][72] = 391;
		freqGB[16][39] = 390;
		freqGB[54][0] = 389;
		freqGB[51][16] = 388;
		freqGB[29][36] = 387;
		freqGB[47][5] = 386;
		freqGB[47][51] = 385;
		freqGB[44][7] = 384;
		freqGB[35][30] = 383;
		freqGB[26][9] = 382;
		freqGB[16][7] = 381;
		freqGB[32][1] = 380;
		freqGB[33][76] = 379;
		freqGB[34][91] = 378;
		freqGB[52][36] = 377;
		freqGB[26][77] = 376;
		freqGB[35][48] = 375;
		freqGB[40][80] = 374;
		freqGB[41][92] = 373;
		freqGB[27][93] = 372;
		freqGB[15][17] = 371;
		freqGB[16][76] = 370;
		freqGB[51][12] = 369;
		freqGB[18][20] = 368;
		freqGB[15][54] = 367;
		freqGB[50][5] = 366;
		freqGB[33][22] = 365;
		freqGB[37][57] = 364;
		freqGB[28][47] = 363;
		freqGB[42][31] = 362;
		freqGB[18][2] = 361;
		freqGB[43][64] = 360;
		freqGB[23][47] = 359;
		freqGB[28][79] = 358;
		freqGB[25][45] = 357;
		freqGB[23][91] = 356;
		freqGB[22][19] = 355;
		freqGB[25][46] = 354;
		freqGB[22][36] = 353;
		freqGB[54][85] = 352;
		freqGB[46][20] = 351;
		freqGB[27][37] = 350;
		freqGB[26][81] = 349;
		freqGB[42][29] = 348;
		freqGB[31][90] = 347;
		freqGB[41][59] = 346;
		freqGB[24][65] = 345;
		freqGB[44][84] = 344;
		freqGB[24][90] = 343;
		freqGB[38][54] = 342;
		freqGB[28][70] = 341;
		freqGB[27][15] = 340;
		freqGB[28][80] = 339;
		freqGB[29][8] = 338;
		freqGB[45][80] = 337;
		freqGB[53][37] = 336;
		freqGB[28][65] = 335;
		freqGB[23][86] = 334;
		freqGB[39][45] = 333;
		freqGB[53][32] = 332;
		freqGB[38][68] = 331;
		freqGB[45][78] = 330;
		freqGB[43][7] = 329;
		freqGB[46][82] = 328;
		freqGB[27][38] = 327;
		freqGB[16][62] = 326;
		freqGB[24][17] = 325;
		freqGB[22][70] = 324;
		freqGB[52][28] = 323;
		freqGB[23][40] = 322;
		freqGB[28][50] = 321;
		freqGB[42][91] = 320;
		freqGB[47][76] = 319;
		freqGB[15][42] = 318;
		freqGB[43][55] = 317;
		freqGB[29][84] = 316;
		freqGB[44][90] = 315;
		freqGB[53][16] = 314;
		freqGB[22][93] = 313;
		freqGB[34][10] = 312;
		freqGB[32][53] = 311;
		freqGB[43][65] = 310;
		freqGB[28][7] = 309;
		freqGB[35][46] = 308;
		freqGB[21][39] = 307;
		freqGB[44][18] = 306;
		freqGB[40][10] = 305;
		freqGB[54][53] = 304;
		freqGB[38][74] = 303;
		freqGB[28][26] = 302;
		freqGB[15][13] = 301;
		freqGB[39][34] = 300;
		freqGB[39][46] = 299;
		freqGB[42][66] = 298;
		freqGB[33][58] = 297;
		freqGB[15][56] = 296;
		freqGB[18][51] = 295;
		freqGB[49][68] = 294;
		freqGB[30][37] = 293;
		freqGB[51][84] = 292;
		freqGB[51][9] = 291;
		freqGB[40][70] = 290;
		freqGB[41][84] = 289;
		freqGB[28][64] = 288;
		freqGB[32][88] = 287;
		freqGB[24][5] = 286;
		freqGB[53][23] = 285;
		freqGB[42][27] = 284;
		freqGB[22][38] = 283;
		freqGB[32][86] = 282;
		freqGB[34][30] = 281;
		freqGB[38][63] = 280;
		freqGB[24][59] = 279;
		freqGB[22][81] = 278;
		freqGB[32][11] = 277;
		freqGB[51][21] = 276;
		freqGB[54][41] = 275;
		freqGB[21][50] = 274;
		freqGB[23][89] = 273;
		freqGB[19][87] = 272;
		freqGB[26][7] = 271;
		freqGB[30][75] = 270;
		freqGB[43][84] = 269;
		freqGB[51][25] = 268;
		freqGB[16][67] = 267;
		freqGB[32][9] = 266;
		freqGB[48][51] = 265;
		freqGB[39][7] = 264;
		freqGB[44][88] = 263;
		freqGB[52][24] = 262;
		freqGB[23][34] = 261;
		freqGB[32][75] = 260;
		freqGB[19][10] = 259;
		freqGB[28][91] = 258;
		freqGB[32][83] = 257;
		freqGB[25][75] = 256;
		freqGB[53][45] = 255;
		freqGB[29][85] = 254;
		freqGB[53][59] = 253;
		freqGB[16][2] = 252;
		freqGB[19][78] = 251;
		freqGB[15][75] = 250;
		freqGB[51][42] = 249;
		freqGB[45][67] = 248;
		freqGB[15][74] = 247;
		freqGB[25][81] = 246;
		freqGB[37][62] = 245;
		freqGB[16][55] = 244;
		freqGB[18][38] = 243;
		freqGB[23][23] = 242;
		freqGB[38][30] = 241;
		freqGB[17][28] = 240;
		freqGB[44][73] = 239;
		freqGB[23][78] = 238;
		freqGB[40][77] = 237;
		freqGB[38][87] = 236;
		freqGB[27][19] = 235;
		freqGB[38][82] = 234;
		freqGB[37][22] = 233;
		freqGB[41][30] = 232;
		freqGB[54][9] = 231;
		freqGB[32][30] = 230;
		freqGB[30][52] = 229;
		freqGB[40][84] = 228;
		freqGB[53][57] = 227;
		freqGB[27][27] = 226;
		freqGB[38][64] = 225;
		freqGB[18][43] = 224;
		freqGB[23][69] = 223;
		freqGB[28][12] = 222;
		freqGB[50][78] = 221;
		freqGB[50][1] = 220;
		freqGB[26][88] = 219;
		freqGB[36][40] = 218;
		freqGB[33][89] = 217;
		freqGB[41][28] = 216;
		freqGB[31][77] = 215;
		freqGB[46][1] = 214;
		freqGB[47][19] = 213;
		freqGB[35][55] = 212;
		freqGB[41][21] = 211;
		freqGB[27][10] = 210;
		freqGB[32][77] = 209;
		freqGB[26][37] = 208;
		freqGB[20][33] = 207;
		freqGB[41][52] = 206;
		freqGB[32][18] = 205;
		freqGB[38][13] = 204;
		freqGB[20][18] = 203;
		freqGB[20][24] = 202;
		freqGB[45][19] = 201;
		freqGB[18][53] = 200;

		freqBig5[9][89] = 600;
		freqBig5[11][15] = 599;
		freqBig5[3][66] = 598;
		freqBig5[6][121] = 597;
		freqBig5[3][0] = 596;
		freqBig5[5][82] = 595;
		freqBig5[3][42] = 594;
		freqBig5[5][34] = 593;
		freqBig5[3][8] = 592;
		freqBig5[3][6] = 591;
		freqBig5[3][67] = 590;
		freqBig5[7][139] = 589;
		freqBig5[23][137] = 588;
		freqBig5[12][46] = 587;
		freqBig5[4][8] = 586;
		freqBig5[4][41] = 585;
		freqBig5[18][47] = 584;
		freqBig5[12][114] = 583;
		freqBig5[6][1] = 582;
		freqBig5[22][60] = 581;
		freqBig5[5][46] = 580;
		freqBig5[11][79] = 579;
		freqBig5[3][23] = 578;
		freqBig5[7][114] = 577;
		freqBig5[29][102] = 576;
		freqBig5[19][14] = 575;
		freqBig5[4][133] = 574;
		freqBig5[3][29] = 573;
		freqBig5[4][109] = 572;
		freqBig5[14][127] = 571;
		freqBig5[5][48] = 570;
		freqBig5[13][104] = 569;
		freqBig5[3][132] = 568;
		freqBig5[26][64] = 567;
		freqBig5[7][19] = 566;
		freqBig5[4][12] = 565;
		freqBig5[11][124] = 564;
		freqBig5[7][89] = 563;
		freqBig5[15][124] = 562;
		freqBig5[4][108] = 561;
		freqBig5[19][66] = 560;
		freqBig5[3][21] = 559;
		freqBig5[24][12] = 558;
		freqBig5[28][111] = 557;
		freqBig5[12][107] = 556;
		freqBig5[3][112] = 555;
		freqBig5[8][113] = 554;
		freqBig5[5][40] = 553;
		freqBig5[26][145] = 552;
		freqBig5[3][48] = 551;
		freqBig5[3][70] = 550;
		freqBig5[22][17] = 549;
		freqBig5[16][47] = 548;
		freqBig5[3][53] = 547;
		freqBig5[4][24] = 546;
		freqBig5[32][120] = 545;
		freqBig5[24][49] = 544;
		freqBig5[24][142] = 543;
		freqBig5[18][66] = 542;
		freqBig5[29][150] = 541;
		freqBig5[5][122] = 540;
		freqBig5[5][114] = 539;
		freqBig5[3][44] = 538;
		freqBig5[10][128] = 537;
		freqBig5[15][20] = 536;
		freqBig5[13][33] = 535;
		freqBig5[14][87] = 534;
		freqBig5[3][126] = 533;
		freqBig5[4][53] = 532;
		freqBig5[4][40] = 531;
		freqBig5[9][93] = 530;
		freqBig5[15][137] = 529;
		freqBig5[10][123] = 528;
		freqBig5[4][56] = 527;
		freqBig5[5][71] = 526;
		freqBig5[10][8] = 525;
		freqBig5[5][16] = 524;
		freqBig5[5][146] = 523;
		freqBig5[18][88] = 522;
		freqBig5[24][4] = 521;
		freqBig5[20][47] = 520;
		freqBig5[5][33] = 519;
		freqBig5[9][43] = 518;
		freqBig5[20][12] = 517;
		freqBig5[20][13] = 516;
		freqBig5[5][156] = 515;
		freqBig5[22][140] = 514;
		freqBig5[8][146] = 513;
		freqBig5[21][123] = 512;
		freqBig5[4][90] = 511;
		freqBig5[5][62] = 510;
		freqBig5[17][59] = 509;
		freqBig5[10][37] = 508;
		freqBig5[18][107] = 507;
		freqBig5[14][53] = 506;
		freqBig5[22][51] = 505;
		freqBig5[8][13] = 504;
		freqBig5[5][29] = 503;
		freqBig5[9][7] = 502;
		freqBig5[22][14] = 501;
		freqBig5[8][55] = 500;
		freqBig5[33][9] = 499;
		freqBig5[16][64] = 498;
		freqBig5[7][131] = 497;
		freqBig5[34][4] = 496;
		freqBig5[7][101] = 495;
		freqBig5[11][139] = 494;
		freqBig5[3][135] = 493;
		freqBig5[7][102] = 492;
		freqBig5[17][13] = 491;
		freqBig5[3][20] = 490;
		freqBig5[27][106] = 489;
		freqBig5[5][88] = 488;
		freqBig5[6][33] = 487;
		freqBig5[5][139] = 486;
		freqBig5[6][0] = 485;
		freqBig5[17][58] = 484;
		freqBig5[5][133] = 483;
		freqBig5[9][107] = 482;
		freqBig5[23][39] = 481;
		freqBig5[5][23] = 480;
		freqBig5[3][79] = 479;
		freqBig5[32][97] = 478;
		freqBig5[3][136] = 477;
		freqBig5[4][94] = 476;
		freqBig5[21][61] = 475;
		freqBig5[23][123] = 474;
		freqBig5[26][16] = 473;
		freqBig5[24][137] = 472;
		freqBig5[22][18] = 471;
		freqBig5[5][1] = 470;
		freqBig5[20][119] = 469;
		freqBig5[3][7] = 468;
		freqBig5[10][79] = 467;
		freqBig5[15][105] = 466;
		freqBig5[3][144] = 465;
		freqBig5[12][80] = 464;
		freqBig5[15][73] = 463;
		freqBig5[3][19] = 462;
		freqBig5[8][109] = 461;
		freqBig5[3][15] = 460;
		freqBig5[31][82] = 459;
		freqBig5[3][43] = 458;
		freqBig5[25][119] = 457;
		freqBig5[16][111] = 456;
		freqBig5[7][77] = 455;
		freqBig5[3][95] = 454;
		freqBig5[24][82] = 453;
		freqBig5[7][52] = 452;
		freqBig5[9][151] = 451;
		freqBig5[3][129] = 450;
		freqBig5[5][87] = 449;
		freqBig5[3][55] = 448;
		freqBig5[8][153] = 447;
		freqBig5[4][83] = 446;
		freqBig5[3][114] = 445;
		freqBig5[23][147] = 444;
		freqBig5[15][31] = 443;
		freqBig5[3][54] = 442;
		freqBig5[11][122] = 441;
		freqBig5[4][4] = 440;
		freqBig5[34][149] = 439;
		freqBig5[3][17] = 438;
		freqBig5[21][64] = 437;
		freqBig5[26][144] = 436;
		freqBig5[4][62] = 435;
		freqBig5[8][15] = 434;
		freqBig5[35][80] = 433;
		freqBig5[7][110] = 432;
		freqBig5[23][114] = 431;
		freqBig5[3][108] = 430;
		freqBig5[3][62] = 429;
		freqBig5[21][41] = 428;
		freqBig5[15][99] = 427;
		freqBig5[5][47] = 426;
		freqBig5[4][96] = 425;
		freqBig5[20][122] = 424;
		freqBig5[5][21] = 423;
		freqBig5[4][157] = 422;
		freqBig5[16][14] = 421;
		freqBig5[3][117] = 420;
		freqBig5[7][129] = 419;
		freqBig5[4][27] = 418;
		freqBig5[5][30] = 417;
		freqBig5[22][16] = 416;
		freqBig5[5][64] = 415;
		freqBig5[17][99] = 414;
		freqBig5[17][57] = 413;
		freqBig5[8][105] = 412;
		freqBig5[5][112] = 411;
		freqBig5[20][59] = 410;
		freqBig5[6][129] = 409;
		freqBig5[18][17] = 408;
		freqBig5[3][92] = 407;
		freqBig5[28][118] = 406;
		freqBig5[3][109] = 405;
		freqBig5[31][51] = 404;
		freqBig5[13][116] = 403;
		freqBig5[6][15] = 402;
		freqBig5[36][136] = 401;
		freqBig5[12][74] = 400;
		freqBig5[20][88] = 399;
		freqBig5[36][68] = 398;
		freqBig5[3][147] = 397;
		freqBig5[15][84] = 396;
		freqBig5[16][32] = 395;
		freqBig5[16][58] = 394;
		freqBig5[7][66] = 393;
		freqBig5[23][107] = 392;
		freqBig5[9][6] = 391;
		freqBig5[12][86] = 390;
		freqBig5[23][112] = 389;
		freqBig5[37][23] = 388;
		freqBig5[3][138] = 387;
		freqBig5[20][68] = 386;
		freqBig5[15][116] = 385;
		freqBig5[18][64] = 384;
		freqBig5[12][139] = 383;
		freqBig5[11][155] = 382;
		freqBig5[4][156] = 381;
		freqBig5[12][84] = 380;
		freqBig5[18][49] = 379;
		freqBig5[25][125] = 378;
		freqBig5[25][147] = 377;
		freqBig5[15][110] = 376;
		freqBig5[19][96] = 375;
		freqBig5[30][152] = 374;
		freqBig5[6][31] = 373;
		freqBig5[27][117] = 372;
		freqBig5[3][10] = 371;
		freqBig5[6][131] = 370;
		freqBig5[13][112] = 369;
		freqBig5[36][156] = 368;
		freqBig5[4][60] = 367;
		freqBig5[15][121] = 366;
		freqBig5[4][112] = 365;
		freqBig5[30][142] = 364;
		freqBig5[23][154] = 363;
		freqBig5[27][101] = 362;
		freqBig5[9][140] = 361;
		freqBig5[3][89] = 360;
		freqBig5[18][148] = 359;
		freqBig5[4][69] = 358;
		freqBig5[16][49] = 357;
		freqBig5[6][117] = 356;
		freqBig5[36][55] = 355;
		freqBig5[5][123] = 354;
		freqBig5[4][126] = 353;
		freqBig5[4][119] = 352;
		freqBig5[9][95] = 351;
		freqBig5[5][24] = 350;
		freqBig5[16][133] = 349;
		freqBig5[10][134] = 348;
		freqBig5[26][59] = 347;
		freqBig5[6][41] = 346;
		freqBig5[6][146] = 345;
		freqBig5[19][24] = 344;
		freqBig5[5][113] = 343;
		freqBig5[10][118] = 342;
		freqBig5[34][151] = 341;
		freqBig5[9][72] = 340;
		freqBig5[31][25] = 339;
		freqBig5[18][126] = 338;
		freqBig5[18][28] = 337;
		freqBig5[4][153] = 336;
		freqBig5[3][84] = 335;
		freqBig5[21][18] = 334;
		freqBig5[25][129] = 333;
		freqBig5[6][107] = 332;
		freqBig5[12][25] = 331;
		freqBig5[17][109] = 330;
		freqBig5[7][76] = 329;
		freqBig5[15][15] = 328;
		freqBig5[4][14] = 327;
		freqBig5[23][88] = 326;
		freqBig5[18][2] = 325;
		freqBig5[6][88] = 324;
		freqBig5[16][84] = 323;
		freqBig5[12][48] = 322;
		freqBig5[7][68] = 321;
		freqBig5[5][50] = 320;
		freqBig5[13][54] = 319;
		freqBig5[7][98] = 318;
		freqBig5[11][6] = 317;
		freqBig5[9][80] = 316;
		freqBig5[16][41] = 315;
		freqBig5[7][43] = 314;
		freqBig5[28][117] = 313;
		freqBig5[3][51] = 312;
		freqBig5[7][3] = 311;
		freqBig5[20][81] = 310;
		freqBig5[4][2] = 309;
		freqBig5[11][16] = 308;
		freqBig5[10][4] = 307;
		freqBig5[10][119] = 306;
		freqBig5[6][142] = 305;
		freqBig5[18][51] = 304;
		freqBig5[8][144] = 303;
		freqBig5[10][65] = 302;
		freqBig5[11][64] = 301;
		freqBig5[11][130] = 300;
		freqBig5[9][92] = 299;
		freqBig5[18][29] = 298;
		freqBig5[18][78] = 297;
		freqBig5[18][151] = 296;
		freqBig5[33][127] = 295;
		freqBig5[35][113] = 294;
		freqBig5[10][155] = 293;
		freqBig5[3][76] = 292;
		freqBig5[36][123] = 291;
		freqBig5[13][143] = 290;
		freqBig5[5][135] = 289;
		freqBig5[23][116] = 288;
		freqBig5[6][101] = 287;
		freqBig5[14][74] = 286;
		freqBig5[7][153] = 285;
		freqBig5[3][101] = 284;
		freqBig5[9][74] = 283;
		freqBig5[3][156] = 282;
		freqBig5[4][147] = 281;
		freqBig5[9][12] = 280;
		freqBig5[18][133] = 279;
		freqBig5[4][0] = 278;
		freqBig5[7][155] = 277;
		freqBig5[9][144] = 276;
		freqBig5[23][49] = 275;
		freqBig5[5][89] = 274;
		freqBig5[10][11] = 273;
		freqBig5[3][110] = 272;
		freqBig5[3][40] = 271;
		freqBig5[29][115] = 270;
		freqBig5[9][100] = 269;
		freqBig5[21][67] = 268;
		freqBig5[23][145] = 267;
		freqBig5[10][47] = 266;
		freqBig5[4][31] = 265;
		freqBig5[4][81] = 264;
		freqBig5[22][62] = 263;
		freqBig5[4][28] = 262;
		freqBig5[27][39] = 261;
		freqBig5[27][54] = 260;
		freqBig5[32][46] = 259;
		freqBig5[4][76] = 258;
		freqBig5[26][15] = 257;
		freqBig5[12][154] = 256;
		freqBig5[9][150] = 255;
		freqBig5[15][17] = 254;
		freqBig5[5][129] = 253;
		freqBig5[10][40] = 252;
		freqBig5[13][37] = 251;
		freqBig5[31][104] = 250;
		freqBig5[3][152] = 249;
		freqBig5[5][22] = 248;
		freqBig5[8][48] = 247;
		freqBig5[4][74] = 246;
		freqBig5[6][17] = 245;
		freqBig5[30][82] = 244;
		freqBig5[4][116] = 243;
		freqBig5[16][42] = 242;
		freqBig5[5][55] = 241;
		freqBig5[4][64] = 240;
		freqBig5[14][19] = 239;
		freqBig5[35][82] = 238;
		freqBig5[30][139] = 237;
		freqBig5[26][152] = 236;
		freqBig5[32][32] = 235;
		freqBig5[21][102] = 234;
		freqBig5[10][131] = 233;
		freqBig5[9][128] = 232;
		freqBig5[3][87] = 231;
		freqBig5[4][51] = 230;
		freqBig5[10][15] = 229;
		freqBig5[4][150] = 228;
		freqBig5[7][4] = 227;
		freqBig5[7][51] = 226;
		freqBig5[7][157] = 225;
		freqBig5[4][146] = 224;
		freqBig5[4][91] = 223;
		freqBig5[7][13] = 222;
		freqBig5[17][116] = 221;
		freqBig5[23][21] = 220;
		freqBig5[5][106] = 219;
		freqBig5[14][100] = 218;
		freqBig5[10][152] = 217;
		freqBig5[14][89] = 216;
		freqBig5[6][138] = 215;
		freqBig5[12][157] = 214;
		freqBig5[10][102] = 213;
		freqBig5[19][94] = 212;
		freqBig5[7][74] = 211;
		freqBig5[18][128] = 210;
		freqBig5[27][111] = 209;
		freqBig5[11][57] = 208;
		freqBig5[3][131] = 207;
		freqBig5[30][23] = 206;
		freqBig5[30][126] = 205;
		freqBig5[4][36] = 204;
		freqBig5[26][124] = 203;
		freqBig5[4][19] = 202;
		freqBig5[9][152] = 201;

		freqBig5P[41][122] = 600;
		freqBig5P[35][0] = 599;
		freqBig5P[43][15] = 598;
		freqBig5P[35][99] = 597;
		freqBig5P[35][6] = 596;
		freqBig5P[35][8] = 595;
		freqBig5P[38][154] = 594;
		freqBig5P[37][34] = 593;
		freqBig5P[37][115] = 592;
		freqBig5P[36][12] = 591;
		freqBig5P[18][77] = 590;
		freqBig5P[35][100] = 589;
		freqBig5P[35][42] = 588;
		freqBig5P[120][75] = 587;
		freqBig5P[35][23] = 586;
		freqBig5P[13][72] = 585;
		freqBig5P[0][67] = 584;
		freqBig5P[39][172] = 583;
		freqBig5P[22][182] = 582;
		freqBig5P[15][186] = 581;
		freqBig5P[15][165] = 580;
		freqBig5P[35][44] = 579;
		freqBig5P[40][13] = 578;
		freqBig5P[38][1] = 577;
		freqBig5P[37][33] = 576;
		freqBig5P[36][24] = 575;
		freqBig5P[56][4] = 574;
		freqBig5P[35][29] = 573;
		freqBig5P[9][96] = 572;
		freqBig5P[37][62] = 571;
		freqBig5P[48][47] = 570;
		freqBig5P[51][14] = 569;
		freqBig5P[39][122] = 568;
		freqBig5P[44][46] = 567;
		freqBig5P[35][21] = 566;
		freqBig5P[36][8] = 565;
		freqBig5P[36][141] = 564;
		freqBig5P[3][81] = 563;
		freqBig5P[37][155] = 562;
		freqBig5P[42][84] = 561;
		freqBig5P[36][40] = 560;
		freqBig5P[35][103] = 559;
		freqBig5P[11][84] = 558;
		freqBig5P[45][33] = 557;
		freqBig5P[121][79] = 556;
		freqBig5P[2][77] = 555;
		freqBig5P[36][41] = 554;
		freqBig5P[37][47] = 553;
		freqBig5P[39][125] = 552;
		freqBig5P[37][26] = 551;
		freqBig5P[35][48] = 550;
		freqBig5P[35][28] = 549;
		freqBig5P[35][159] = 548;
		freqBig5P[37][40] = 547;
		freqBig5P[35][145] = 546;
		freqBig5P[37][147] = 545;
		freqBig5P[46][160] = 544;
		freqBig5P[37][46] = 543;
		freqBig5P[50][99] = 542;
		freqBig5P[52][13] = 541;
		freqBig5P[10][82] = 540;
		freqBig5P[35][169] = 539;
		freqBig5P[35][31] = 538;
		freqBig5P[47][31] = 537;
		freqBig5P[18][79] = 536;
		freqBig5P[16][113] = 535;
		freqBig5P[37][104] = 534;
		freqBig5P[39][134] = 533;
		freqBig5P[36][53] = 532;
		freqBig5P[38][0] = 531;
		freqBig5P[4][86] = 530;
		freqBig5P[54][17] = 529;
		freqBig5P[43][157] = 528;
		freqBig5P[35][165] = 527;
		freqBig5P[69][147] = 526;
		freqBig5P[117][95] = 525;
		freqBig5P[35][162] = 524;
		freqBig5P[35][17] = 523;
		freqBig5P[36][142] = 522;
		freqBig5P[36][4] = 521;
		freqBig5P[37][166] = 520;
		freqBig5P[35][168] = 519;
		freqBig5P[35][19] = 518;
		freqBig5P[37][48] = 517;
		freqBig5P[42][37] = 516;
		freqBig5P[40][146] = 515;
		freqBig5P[36][123] = 514;
		freqBig5P[22][41] = 513;
		freqBig5P[20][119] = 512;
		freqBig5P[2][74] = 511;
		freqBig5P[44][113] = 510;
		freqBig5P[35][125] = 509;
		freqBig5P[37][16] = 508;
		freqBig5P[35][20] = 507;
		freqBig5P[35][55] = 506;
		freqBig5P[37][145] = 505;
		freqBig5P[0][88] = 504;
		freqBig5P[3][94] = 503;
		freqBig5P[6][65] = 502;
		freqBig5P[26][15] = 501;
		freqBig5P[41][126] = 500;
		freqBig5P[36][129] = 499;
		freqBig5P[31][75] = 498;
		freqBig5P[19][61] = 497;
		freqBig5P[35][128] = 496;
		freqBig5P[29][79] = 495;
		freqBig5P[36][62] = 494;
		freqBig5P[37][189] = 493;
		freqBig5P[39][109] = 492;
		freqBig5P[39][135] = 491;
		freqBig5P[72][15] = 490;
		freqBig5P[47][106] = 489;
		freqBig5P[54][14] = 488;
		freqBig5P[24][52] = 487;
		freqBig5P[38][162] = 486;
		freqBig5P[41][43] = 485;
		freqBig5P[37][121] = 484;
		freqBig5P[14][66] = 483;
		freqBig5P[37][30] = 482;
		freqBig5P[35][7] = 481;
		freqBig5P[49][58] = 480;
		freqBig5P[43][188] = 479;
		freqBig5P[24][66] = 478;
		freqBig5P[35][171] = 477;
		freqBig5P[40][186] = 476;
		freqBig5P[39][164] = 475;
		freqBig5P[78][186] = 474;
		freqBig5P[8][72] = 473;
		freqBig5P[36][190] = 472;
		freqBig5P[35][53] = 471;
		freqBig5P[35][54] = 470;
		freqBig5P[22][159] = 469;
		freqBig5P[35][9] = 468;
		freqBig5P[41][140] = 467;
		freqBig5P[37][22] = 466;
		freqBig5P[48][97] = 465;
		freqBig5P[50][97] = 464;
		freqBig5P[36][127] = 463;
		freqBig5P[37][23] = 462;
		freqBig5P[40][55] = 461;
		freqBig5P[35][43] = 460;
		freqBig5P[26][22] = 459;
		freqBig5P[35][15] = 458;
		freqBig5P[72][179] = 457;
		freqBig5P[20][129] = 456;
		freqBig5P[52][101] = 455;
		freqBig5P[35][12] = 454;
		freqBig5P[42][156] = 453;
		freqBig5P[15][157] = 452;
		freqBig5P[50][140] = 451;
		freqBig5P[26][28] = 450;
		freqBig5P[54][51] = 449;
		freqBig5P[35][112] = 448;
		freqBig5P[36][116] = 447;
		freqBig5P[42][11] = 446;
		freqBig5P[37][172] = 445;
		freqBig5P[37][29] = 444;
		freqBig5P[44][107] = 443;
		freqBig5P[50][17] = 442;
		freqBig5P[39][107] = 441;
		freqBig5P[19][109] = 440;
		freqBig5P[36][60] = 439;
		freqBig5P[49][132] = 438;
		freqBig5P[26][16] = 437;
		freqBig5P[43][155] = 436;
		freqBig5P[37][120] = 435;
		freqBig5P[15][159] = 434;
		freqBig5P[43][6] = 433;
		freqBig5P[45][188] = 432;
		freqBig5P[35][38] = 431;
		freqBig5P[39][143] = 430;
		freqBig5P[48][144] = 429;
		freqBig5P[37][168] = 428;
		freqBig5P[37][1] = 427;
		freqBig5P[36][109] = 426;
		freqBig5P[46][53] = 425;
		freqBig5P[38][54] = 424;
		freqBig5P[36][0] = 423;
		freqBig5P[72][33] = 422;
		freqBig5P[42][8] = 421;
		freqBig5P[36][31] = 420;
		freqBig5P[35][150] = 419;
		freqBig5P[118][93] = 418;
		freqBig5P[37][61] = 417;
		freqBig5P[0][85] = 416;
		freqBig5P[36][27] = 415;
		freqBig5P[35][134] = 414;
		freqBig5P[36][145] = 413;
		freqBig5P[6][96] = 412;
		freqBig5P[36][14] = 411;
		freqBig5P[16][36] = 410;
		freqBig5P[15][175] = 409;
		freqBig5P[35][10] = 408;
		freqBig5P[36][189] = 407;
		freqBig5P[35][51] = 406;
		freqBig5P[35][109] = 405;
		freqBig5P[35][147] = 404;
		freqBig5P[35][180] = 403;
		freqBig5P[72][5] = 402;
		freqBig5P[36][107] = 401;
		freqBig5P[49][116] = 400;
		freqBig5P[73][30] = 399;
		freqBig5P[6][90] = 398;
		freqBig5P[2][70] = 397;
		freqBig5P[17][141] = 396;
		freqBig5P[35][62] = 395;
		freqBig5P[16][180] = 394;
		freqBig5P[4][91] = 393;
		freqBig5P[15][171] = 392;
		freqBig5P[35][177] = 391;
		freqBig5P[37][173] = 390;
		freqBig5P[16][121] = 389;
		freqBig5P[35][5] = 388;
		freqBig5P[46][122] = 387;
		freqBig5P[40][138] = 386;
		freqBig5P[50][49] = 385;
		freqBig5P[36][152] = 384;
		freqBig5P[13][43] = 383;
		freqBig5P[9][88] = 382;
		freqBig5P[36][159] = 381;
		freqBig5P[27][62] = 380;
		freqBig5P[40][18] = 379;
		freqBig5P[17][129] = 378;
		freqBig5P[43][97] = 377;
		freqBig5P[13][131] = 376;
		freqBig5P[46][107] = 375;
		freqBig5P[60][64] = 374;
		freqBig5P[36][179] = 373;
		freqBig5P[37][55] = 372;
		freqBig5P[41][173] = 371;
		freqBig5P[44][172] = 370;
		freqBig5P[23][187] = 369;
		freqBig5P[36][149] = 368;
		freqBig5P[17][125] = 367;
		freqBig5P[55][180] = 366;
		freqBig5P[51][129] = 365;
		freqBig5P[36][51] = 364;
		freqBig5P[37][122] = 363;
		freqBig5P[48][32] = 362;
		freqBig5P[51][99] = 361;
		freqBig5P[54][16] = 360;
		freqBig5P[41][183] = 359;
		freqBig5P[37][179] = 358;
		freqBig5P[38][179] = 357;
		freqBig5P[35][143] = 356;
		freqBig5P[37][24] = 355;
		freqBig5P[40][177] = 354;
		freqBig5P[47][117] = 353;
		freqBig5P[39][52] = 352;
		freqBig5P[22][99] = 351;
		freqBig5P[40][142] = 350;
		freqBig5P[36][49] = 349;
		freqBig5P[38][17] = 348;
		freqBig5P[39][188] = 347;
		freqBig5P[36][186] = 346;
		freqBig5P[35][189] = 345;
		freqBig5P[41][7] = 344;
		freqBig5P[18][91] = 343;
		freqBig5P[43][137] = 342;
		freqBig5P[35][142] = 341;
		freqBig5P[35][117] = 340;
		freqBig5P[39][138] = 339;
		freqBig5P[16][59] = 338;
		freqBig5P[39][174] = 337;
		freqBig5P[55][145] = 336;
		freqBig5P[37][21] = 335;
		freqBig5P[36][180] = 334;
		freqBig5P[37][156] = 333;
		freqBig5P[49][13] = 332;
		freqBig5P[41][107] = 331;
		freqBig5P[36][56] = 330;
		freqBig5P[53][8] = 329;
		freqBig5P[22][114] = 328;
		freqBig5P[5][95] = 327;
		freqBig5P[37][0] = 326;
		freqBig5P[26][183] = 325;
		freqBig5P[22][66] = 324;
		freqBig5P[35][58] = 323;
		freqBig5P[48][117] = 322;
		freqBig5P[36][102] = 321;
		freqBig5P[22][122] = 320;
		freqBig5P[35][11] = 319;
		freqBig5P[46][19] = 318;
		freqBig5P[22][49] = 317;
		freqBig5P[48][166] = 316;
		freqBig5P[41][125] = 315;
		freqBig5P[41][1] = 314;
		freqBig5P[35][178] = 313;
		freqBig5P[41][12] = 312;
		freqBig5P[26][167] = 311;
		freqBig5P[42][152] = 310;
		freqBig5P[42][46] = 309;
		freqBig5P[42][151] = 308;
		freqBig5P[20][135] = 307;
		freqBig5P[37][162] = 306;
		freqBig5P[37][50] = 305;
		freqBig5P[22][185] = 304;
		freqBig5P[36][166] = 303;
		freqBig5P[19][40] = 302;
		freqBig5P[22][107] = 301;
		freqBig5P[22][102] = 300;
		freqBig5P[57][162] = 299;
		freqBig5P[22][124] = 298;
		freqBig5P[37][138] = 297;
		freqBig5P[37][25] = 296;
		freqBig5P[0][69] = 295;
		freqBig5P[43][172] = 294;
		freqBig5P[42][167] = 293;
		freqBig5P[35][120] = 292;
		freqBig5P[41][128] = 291;
		freqBig5P[2][88] = 290;
		freqBig5P[20][123] = 289;
		freqBig5P[35][123] = 288;
		freqBig5P[36][28] = 287;
		freqBig5P[42][188] = 286;
		freqBig5P[42][164] = 285;
		freqBig5P[42][4] = 284;
		freqBig5P[43][57] = 283;
		freqBig5P[39][3] = 282;
		freqBig5P[42][3] = 281;
		freqBig5P[57][158] = 280;
		freqBig5P[35][146] = 279;
		freqBig5P[24][54] = 278;
		freqBig5P[13][110] = 277;
		freqBig5P[23][132] = 276;
		freqBig5P[26][102] = 275;
		freqBig5P[55][178] = 274;
		freqBig5P[17][117] = 273;
		freqBig5P[41][161] = 272;
		freqBig5P[38][150] = 271;
		freqBig5P[10][71] = 270;
		freqBig5P[47][60] = 269;
		freqBig5P[16][114] = 268;
		freqBig5P[21][47] = 267;
		freqBig5P[39][101] = 266;
		freqBig5P[18][45] = 265;
		freqBig5P[40][121] = 264;
		freqBig5P[45][41] = 263;
		freqBig5P[22][167] = 262;
		freqBig5P[26][149] = 261;
		freqBig5P[15][189] = 260;
		freqBig5P[41][177] = 259;
		freqBig5P[46][36] = 258;
		freqBig5P[20][40] = 257;
		freqBig5P[41][54] = 256;
		freqBig5P[3][87] = 255;
		freqBig5P[40][16] = 254;
		freqBig5P[42][15] = 253;
		freqBig5P[11][83] = 252;
		freqBig5P[0][94] = 251;
		freqBig5P[122][81] = 250;
		freqBig5P[41][26] = 249;
		freqBig5P[36][34] = 248;
		freqBig5P[44][148] = 247;
		freqBig5P[35][3] = 246;
		freqBig5P[36][114] = 245;
		freqBig5P[42][112] = 244;
		freqBig5P[35][183] = 243;
		freqBig5P[49][73] = 242;
		freqBig5P[39][2] = 241;
		freqBig5P[38][121] = 240;
		freqBig5P[44][114] = 239;
		freqBig5P[49][32] = 238;
		freqBig5P[1][65] = 237;
		freqBig5P[38][25] = 236;
		freqBig5P[39][4] = 235;
		freqBig5P[42][62] = 234;
		freqBig5P[35][40] = 233;
		freqBig5P[24][2] = 232;
		freqBig5P[53][49] = 231;
		freqBig5P[41][133] = 230;
		freqBig5P[43][134] = 229;
		freqBig5P[3][83] = 228;
		freqBig5P[38][158] = 227;
		freqBig5P[24][17] = 226;
		freqBig5P[52][59] = 225;
		freqBig5P[38][41] = 224;
		freqBig5P[37][127] = 223;
		freqBig5P[22][175] = 222;
		freqBig5P[44][30] = 221;
		freqBig5P[47][178] = 220;
		freqBig5P[43][99] = 219;
		freqBig5P[19][4] = 218;
		freqBig5P[37][97] = 217;
		freqBig5P[38][181] = 216;
		freqBig5P[45][103] = 215;
		freqBig5P[1][86] = 214;
		freqBig5P[40][15] = 213;
		freqBig5P[22][136] = 212;
		freqBig5P[75][165] = 211;
		freqBig5P[36][15] = 210;
		freqBig5P[46][80] = 209;
		freqBig5P[59][55] = 208;
		freqBig5P[37][108] = 207;
		freqBig5P[21][109] = 206;
		freqBig5P[24][165] = 205;
		freqBig5P[79][158] = 204;
		freqBig5P[44][139] = 203;
		freqBig5P[36][124] = 202;
		freqBig5P[42][185] = 201;
		freqBig5P[39][186] = 200;
		freqBig5P[22][128] = 199;
		freqBig5P[40][44] = 198;
		freqBig5P[41][105] = 197;
		freqBig5P[1][70] = 196;
		freqBig5P[1][68] = 195;
		freqBig5P[53][22] = 194;
		freqBig5P[36][54] = 193;
		freqBig5P[47][147] = 192;
		freqBig5P[35][36] = 191;
		freqBig5P[35][185] = 190;
		freqBig5P[45][37] = 189;
		freqBig5P[43][163] = 188;
		freqBig5P[56][115] = 187;
		freqBig5P[38][164] = 186;
		freqBig5P[35][141] = 185;
		freqBig5P[42][132] = 184;
		freqBig5P[46][120] = 183;
		freqBig5P[69][142] = 182;
		freqBig5P[38][175] = 181;
		freqBig5P[22][112] = 180;
		freqBig5P[38][142] = 179;
		freqBig5P[40][37] = 178;
		freqBig5P[37][109] = 177;
		freqBig5P[40][144] = 176;
		freqBig5P[44][117] = 175;
		freqBig5P[35][181] = 174;
		freqBig5P[26][105] = 173;
		freqBig5P[16][48] = 172;
		freqBig5P[44][122] = 171;
		freqBig5P[12][86] = 170;
		freqBig5P[84][53] = 169;
		freqBig5P[17][44] = 168;
		freqBig5P[59][54] = 167;
		freqBig5P[36][98] = 166;
		freqBig5P[45][115] = 165;
		freqBig5P[73][9] = 164;
		freqBig5P[44][123] = 163;
		freqBig5P[37][188] = 162;
		freqBig5P[51][117] = 161;
		freqBig5P[15][156] = 160;
		freqBig5P[36][155] = 159;
		freqBig5P[44][25] = 158;
		freqBig5P[38][12] = 157;
		freqBig5P[38][140] = 156;
		freqBig5P[23][4] = 155;
		freqBig5P[45][149] = 154;
		freqBig5P[22][189] = 153;
		freqBig5P[38][147] = 152;
		freqBig5P[27][5] = 151;
		freqBig5P[22][42] = 150;
		freqBig5P[3][68] = 149;
		freqBig5P[39][51] = 148;
		freqBig5P[36][29] = 147;
		freqBig5P[20][108] = 146;
		freqBig5P[50][57] = 145;
		freqBig5P[55][104] = 144;
		freqBig5P[22][46] = 143;
		freqBig5P[18][164] = 142;
		freqBig5P[50][159] = 141;
		freqBig5P[85][131] = 140;
		freqBig5P[26][79] = 139;
		freqBig5P[38][100] = 138;
		freqBig5P[53][112] = 137;
		freqBig5P[20][190] = 136;
		freqBig5P[14][69] = 135;
		freqBig5P[23][11] = 134;
		freqBig5P[40][114] = 133;
		freqBig5P[40][148] = 132;
		freqBig5P[53][130] = 131;
		freqBig5P[36][2] = 130;
		freqBig5P[66][82] = 129;
		freqBig5P[45][166] = 128;
		freqBig5P[4][88] = 127;
		freqBig5P[16][57] = 126;
		freqBig5P[22][116] = 125;
		freqBig5P[36][108] = 124;
		freqBig5P[13][48] = 123;
		freqBig5P[54][12] = 122;
		freqBig5P[40][136] = 121;
		freqBig5P[36][128] = 120;
		freqBig5P[23][6] = 119;
		freqBig5P[38][125] = 118;
		freqBig5P[45][154] = 117;
		freqBig5P[51][127] = 116;
		freqBig5P[44][163] = 115;
		freqBig5P[16][173] = 114;
		freqBig5P[43][49] = 113;
		freqBig5P[20][112] = 112;
		freqBig5P[15][168] = 111;
		freqBig5P[35][129] = 110;
		freqBig5P[20][45] = 109;
		freqBig5P[38][10] = 108;
		freqBig5P[57][171] = 107;
		freqBig5P[44][190] = 106;
		freqBig5P[40][56] = 105;
		freqBig5P[36][156] = 104;
		freqBig5P[3][88] = 103;
		freqBig5P[50][122] = 102;
		freqBig5P[36][7] = 101;
		freqBig5P[39][43] = 100;
		freqBig5P[15][166] = 99;
		freqBig5P[42][136] = 98;
		freqBig5P[22][131] = 97;
		freqBig5P[44][23] = 96;
		freqBig5P[54][147] = 95;
		freqBig5P[41][32] = 94;
		freqBig5P[23][121] = 93;
		freqBig5P[39][108] = 92;
		freqBig5P[2][78] = 91;
		freqBig5P[40][155] = 90;
		freqBig5P[55][51] = 89;
		freqBig5P[19][34] = 88;
		freqBig5P[48][128] = 87;
		freqBig5P[48][159] = 86;
		freqBig5P[20][70] = 85;
		freqBig5P[34][71] = 84;
		freqBig5P[16][31] = 83;
		freqBig5P[42][157] = 82;
		freqBig5P[20][44] = 81;
		freqBig5P[11][92] = 80;
		freqBig5P[44][180] = 79;
		freqBig5P[84][33] = 78;
		freqBig5P[16][116] = 77;
		freqBig5P[61][163] = 76;
		freqBig5P[35][164] = 75;
		freqBig5P[36][42] = 74;
		freqBig5P[13][40] = 73;
		freqBig5P[43][176] = 72;
		freqBig5P[2][66] = 71;
		freqBig5P[20][133] = 70;
		freqBig5P[36][65] = 69;
		freqBig5P[38][33] = 68;
		freqBig5P[12][91] = 67;
		freqBig5P[36][26] = 66;
		freqBig5P[15][174] = 65;
		freqBig5P[77][32] = 64;
		freqBig5P[16][1] = 63;
		freqBig5P[25][86] = 62;
		freqBig5P[17][13] = 61;
		freqBig5P[5][75] = 60;
		freqBig5P[36][52] = 59;
		freqBig5P[51][164] = 58;
		freqBig5P[12][85] = 57;
		freqBig5P[39][168] = 56;
		freqBig5P[43][16] = 55;
		freqBig5P[40][69] = 54;
		freqBig5P[26][108] = 53;
		freqBig5P[51][56] = 52;
		freqBig5P[16][37] = 51;
		freqBig5P[40][29] = 50;
		freqBig5P[46][171] = 49;
		freqBig5P[40][128] = 48;
		freqBig5P[72][114] = 47;
		freqBig5P[21][103] = 46;
		freqBig5P[22][44] = 45;
		freqBig5P[40][115] = 44;
		freqBig5P[43][7] = 43;
		freqBig5P[43][153] = 42;
		freqBig5P[17][20] = 41;
		freqBig5P[16][49] = 40;
		freqBig5P[36][57] = 39;
		freqBig5P[18][38] = 38;
		freqBig5P[45][184] = 37;
		freqBig5P[37][167] = 36;
		freqBig5P[26][106] = 35;
		freqBig5P[61][121] = 34;
		freqBig5P[89][140] = 33;
		freqBig5P[46][61] = 32;
		freqBig5P[39][163] = 31;
		freqBig5P[40][62] = 30;
		freqBig5P[38][165] = 29;
		freqBig5P[47][37] = 28;
		freqBig5P[18][155] = 27;
		freqBig5P[20][33] = 26;
		freqBig5P[29][90] = 25;
		freqBig5P[20][103] = 24;
		freqBig5P[37][51] = 23;
		freqBig5P[57][0] = 22;
		freqBig5P[40][31] = 21;
		freqBig5P[45][32] = 20;
		freqBig5P[59][23] = 19;
		freqBig5P[18][47] = 18;
		freqBig5P[45][134] = 17;
		freqBig5P[37][59] = 16;
		freqBig5P[21][128] = 15;
		freqBig5P[36][106] = 14;
		freqBig5P[31][39] = 13;
		freqBig5P[40][182] = 12;
		freqBig5P[52][155] = 11;
		freqBig5P[42][166] = 10;
		freqBig5P[35][27] = 9;
		freqBig5P[38][3] = 8;
		freqBig5P[13][44] = 7;
		freqBig5P[58][157] = 6;
		freqBig5P[47][51] = 5;
		freqBig5P[41][37] = 4;
		freqBig5P[41][172] = 3;
		freqBig5P[51][165] = 2;
		freqBig5P[15][161] = 1;
		freqBig5P[24][181] = 0;
		freqEUCTW[48][49] = 599;
		freqEUCTW[35][65] = 598;
		freqEUCTW[41][27] = 597;
		freqEUCTW[35][0] = 596;
		freqEUCTW[39][19] = 595;
		freqEUCTW[35][42] = 594;
		freqEUCTW[38][66] = 593;
		freqEUCTW[35][8] = 592;
		freqEUCTW[35][6] = 591;
		freqEUCTW[35][66] = 590;
		freqEUCTW[43][14] = 589;
		freqEUCTW[69][80] = 588;
		freqEUCTW[50][48] = 587;
		freqEUCTW[36][71] = 586;
		freqEUCTW[37][10] = 585;
		freqEUCTW[60][52] = 584;
		freqEUCTW[51][21] = 583;
		freqEUCTW[40][2] = 582;
		freqEUCTW[67][35] = 581;
		freqEUCTW[38][78] = 580;
		freqEUCTW[49][18] = 579;
		freqEUCTW[35][23] = 578;
		freqEUCTW[42][83] = 577;
		freqEUCTW[79][47] = 576;
		freqEUCTW[61][82] = 575;
		freqEUCTW[38][7] = 574;
		freqEUCTW[35][29] = 573;
		freqEUCTW[37][77] = 572;
		freqEUCTW[54][67] = 571;
		freqEUCTW[38][80] = 570;
		freqEUCTW[52][74] = 569;
		freqEUCTW[36][37] = 568;
		freqEUCTW[74][8] = 567;
		freqEUCTW[41][83] = 566;
		freqEUCTW[36][75] = 565;
		freqEUCTW[49][63] = 564;
		freqEUCTW[42][58] = 563;
		freqEUCTW[56][33] = 562;
		freqEUCTW[37][76] = 561;
		freqEUCTW[62][39] = 560;
		freqEUCTW[35][21] = 559;
		freqEUCTW[70][19] = 558;
		freqEUCTW[77][88] = 557;
		freqEUCTW[51][14] = 556;
		freqEUCTW[36][17] = 555;
		freqEUCTW[44][51] = 554;
		freqEUCTW[38][72] = 553;
		freqEUCTW[74][90] = 552;
		freqEUCTW[35][48] = 551;
		freqEUCTW[35][69] = 550;
		freqEUCTW[66][86] = 549;
		freqEUCTW[57][20] = 548;
		freqEUCTW[35][53] = 547;
		freqEUCTW[36][87] = 546;
		freqEUCTW[84][67] = 545;
		freqEUCTW[70][56] = 544;
		freqEUCTW[71][54] = 543;
		freqEUCTW[60][70] = 542;
		freqEUCTW[80][1] = 541;
		freqEUCTW[39][59] = 540;
		freqEUCTW[39][51] = 539;
		freqEUCTW[35][44] = 538;
		freqEUCTW[48][4] = 537;
		freqEUCTW[55][24] = 536;
		freqEUCTW[52][4] = 535;
		freqEUCTW[54][26] = 534;
		freqEUCTW[36][31] = 533;
		freqEUCTW[37][22] = 532;
		freqEUCTW[37][9] = 531;
		freqEUCTW[46][0] = 530;
		freqEUCTW[56][46] = 529;
		freqEUCTW[47][93] = 528;
		freqEUCTW[37][25] = 527;
		freqEUCTW[39][8] = 526;
		freqEUCTW[46][73] = 525;
		freqEUCTW[38][48] = 524;
		freqEUCTW[39][83] = 523;
		freqEUCTW[60][92] = 522;
		freqEUCTW[70][11] = 521;
		freqEUCTW[63][84] = 520;
		freqEUCTW[38][65] = 519;
		freqEUCTW[45][45] = 518;
		freqEUCTW[63][49] = 517;
		freqEUCTW[63][50] = 516;
		freqEUCTW[39][93] = 515;
		freqEUCTW[68][20] = 514;
		freqEUCTW[44][84] = 513;
		freqEUCTW[66][34] = 512;
		freqEUCTW[37][58] = 511;
		freqEUCTW[39][0] = 510;
		freqEUCTW[59][1] = 509;
		freqEUCTW[47][8] = 508;
		freqEUCTW[61][17] = 507;
		freqEUCTW[53][87] = 506;
		freqEUCTW[67][26] = 505;
		freqEUCTW[43][46] = 504;
		freqEUCTW[38][61] = 503;
		freqEUCTW[45][9] = 502;
		freqEUCTW[66][83] = 501;
		freqEUCTW[43][88] = 500;
		freqEUCTW[85][20] = 499;
		freqEUCTW[57][36] = 498;
		freqEUCTW[43][6] = 497;
		freqEUCTW[86][77] = 496;
		freqEUCTW[42][70] = 495;
		freqEUCTW[49][78] = 494;
		freqEUCTW[36][40] = 493;
		freqEUCTW[42][71] = 492;
		freqEUCTW[58][49] = 491;
		freqEUCTW[35][20] = 490;
		freqEUCTW[76][20] = 489;
		freqEUCTW[39][25] = 488;
		freqEUCTW[40][34] = 487;
		freqEUCTW[39][76] = 486;
		freqEUCTW[40][1] = 485;
		freqEUCTW[59][0] = 484;
		freqEUCTW[39][70] = 483;
		freqEUCTW[46][14] = 482;
		freqEUCTW[68][77] = 481;
		freqEUCTW[38][55] = 480;
		freqEUCTW[35][78] = 479;
		freqEUCTW[84][44] = 478;
		freqEUCTW[36][41] = 477;
		freqEUCTW[37][62] = 476;
		freqEUCTW[65][67] = 475;
		freqEUCTW[69][66] = 474;
		freqEUCTW[73][55] = 473;
		freqEUCTW[71][49] = 472;
		freqEUCTW[66][87] = 471;
		freqEUCTW[38][33] = 470;
		freqEUCTW[64][61] = 469;
		freqEUCTW[35][7] = 468;
		freqEUCTW[47][49] = 467;
		freqEUCTW[56][14] = 466;
		freqEUCTW[36][49] = 465;
		freqEUCTW[50][81] = 464;
		freqEUCTW[55][76] = 463;
		freqEUCTW[35][19] = 462;
		freqEUCTW[44][47] = 461;
		freqEUCTW[35][15] = 460;
		freqEUCTW[82][59] = 459;
		freqEUCTW[35][43] = 458;
		freqEUCTW[73][0] = 457;
		freqEUCTW[57][83] = 456;
		freqEUCTW[42][46] = 455;
		freqEUCTW[36][0] = 454;
		freqEUCTW[70][88] = 453;
		freqEUCTW[42][22] = 452;
		freqEUCTW[46][58] = 451;
		freqEUCTW[36][34] = 450;
		freqEUCTW[39][24] = 449;
		freqEUCTW[35][55] = 448;
		freqEUCTW[44][91] = 447;
		freqEUCTW[37][51] = 446;
		freqEUCTW[36][19] = 445;
		freqEUCTW[69][90] = 444;
		freqEUCTW[55][35] = 443;
		freqEUCTW[35][54] = 442;
		freqEUCTW[49][61] = 441;
		freqEUCTW[36][67] = 440;
		freqEUCTW[88][34] = 439;
		freqEUCTW[35][17] = 438;
		freqEUCTW[65][69] = 437;
		freqEUCTW[74][89] = 436;
		freqEUCTW[37][31] = 435;
		freqEUCTW[43][48] = 434;
		freqEUCTW[89][27] = 433;
		freqEUCTW[42][79] = 432;
		freqEUCTW[69][57] = 431;
		freqEUCTW[36][13] = 430;
		freqEUCTW[35][62] = 429;
		freqEUCTW[65][47] = 428;
		freqEUCTW[56][8] = 427;
		freqEUCTW[38][79] = 426;
		freqEUCTW[37][64] = 425;
		freqEUCTW[64][64] = 424;
		freqEUCTW[38][53] = 423;
		freqEUCTW[38][31] = 422;
		freqEUCTW[56][81] = 421;
		freqEUCTW[36][22] = 420;
		freqEUCTW[43][4] = 419;
		freqEUCTW[36][90] = 418;
		freqEUCTW[38][62] = 417;
		freqEUCTW[66][85] = 416;
		freqEUCTW[39][1] = 415;
		freqEUCTW[59][40] = 414;
		freqEUCTW[58][93] = 413;
		freqEUCTW[44][43] = 412;
		freqEUCTW[39][49] = 411;
		freqEUCTW[64][2] = 410;
		freqEUCTW[41][35] = 409;
		freqEUCTW[60][22] = 408;
		freqEUCTW[35][91] = 407;
		freqEUCTW[78][1] = 406;
		freqEUCTW[36][14] = 405;
		freqEUCTW[82][29] = 404;
		freqEUCTW[52][86] = 403;
		freqEUCTW[40][16] = 402;
		freqEUCTW[91][52] = 401;
		freqEUCTW[50][75] = 400;
		freqEUCTW[64][30] = 399;
		freqEUCTW[90][78] = 398;
		freqEUCTW[36][52] = 397;
		freqEUCTW[55][87] = 396;
		freqEUCTW[57][5] = 395;
		freqEUCTW[57][31] = 394;
		freqEUCTW[42][35] = 393;
		freqEUCTW[69][50] = 392;
		freqEUCTW[45][8] = 391;
		freqEUCTW[50][87] = 390;
		freqEUCTW[69][55] = 389;
		freqEUCTW[92][3] = 388;
		freqEUCTW[36][43] = 387;
		freqEUCTW[64][10] = 386;
		freqEUCTW[56][25] = 385;
		freqEUCTW[60][68] = 384;
		freqEUCTW[51][46] = 383;
		freqEUCTW[50][0] = 382;
		freqEUCTW[38][30] = 381;
		freqEUCTW[50][85] = 380;
		freqEUCTW[60][54] = 379;
		freqEUCTW[73][6] = 378;
		freqEUCTW[73][28] = 377;
		freqEUCTW[56][19] = 376;
		freqEUCTW[62][69] = 375;
		freqEUCTW[81][66] = 374;
		freqEUCTW[40][32] = 373;
		freqEUCTW[76][31] = 372;
		freqEUCTW[35][10] = 371;
		freqEUCTW[41][37] = 370;
		freqEUCTW[52][82] = 369;
		freqEUCTW[91][72] = 368;
		freqEUCTW[37][29] = 367;
		freqEUCTW[56][30] = 366;
		freqEUCTW[37][80] = 365;
		freqEUCTW[81][56] = 364;
		freqEUCTW[70][3] = 363;
		freqEUCTW[76][15] = 362;
		freqEUCTW[46][47] = 361;
		freqEUCTW[35][88] = 360;
		freqEUCTW[61][58] = 359;
		freqEUCTW[37][37] = 358;
		freqEUCTW[57][22] = 357;
		freqEUCTW[41][23] = 356;
		freqEUCTW[90][66] = 355;
		freqEUCTW[39][60] = 354;
		freqEUCTW[38][0] = 353;
		freqEUCTW[37][87] = 352;
		freqEUCTW[46][2] = 351;
		freqEUCTW[38][56] = 350;
		freqEUCTW[58][11] = 349;
		freqEUCTW[48][10] = 348;
		freqEUCTW[74][4] = 347;
		freqEUCTW[40][42] = 346;
		freqEUCTW[41][52] = 345;
		freqEUCTW[61][92] = 344;
		freqEUCTW[39][50] = 343;
		freqEUCTW[47][88] = 342;
		freqEUCTW[88][36] = 341;
		freqEUCTW[45][73] = 340;
		freqEUCTW[82][3] = 339;
		freqEUCTW[61][36] = 338;
		freqEUCTW[60][33] = 337;
		freqEUCTW[38][27] = 336;
		freqEUCTW[35][83] = 335;
		freqEUCTW[65][24] = 334;
		freqEUCTW[73][10] = 333;
		freqEUCTW[41][13] = 332;
		freqEUCTW[50][27] = 331;
		freqEUCTW[59][50] = 330;
		freqEUCTW[42][45] = 329;
		freqEUCTW[55][19] = 328;
		freqEUCTW[36][77] = 327;
		freqEUCTW[69][31] = 326;
		freqEUCTW[60][7] = 325;
		freqEUCTW[40][88] = 324;
		freqEUCTW[57][56] = 323;
		freqEUCTW[50][50] = 322;
		freqEUCTW[42][37] = 321;
		freqEUCTW[38][82] = 320;
		freqEUCTW[52][25] = 319;
		freqEUCTW[42][67] = 318;
		freqEUCTW[48][40] = 317;
		freqEUCTW[45][81] = 316;
		freqEUCTW[57][14] = 315;
		freqEUCTW[42][13] = 314;
		freqEUCTW[78][0] = 313;
		freqEUCTW[35][51] = 312;
		freqEUCTW[41][67] = 311;
		freqEUCTW[64][23] = 310;
		freqEUCTW[36][65] = 309;
		freqEUCTW[48][50] = 308;
		freqEUCTW[46][69] = 307;
		freqEUCTW[47][89] = 306;
		freqEUCTW[41][48] = 305;
		freqEUCTW[60][56] = 304;
		freqEUCTW[44][82] = 303;
		freqEUCTW[47][35] = 302;
		freqEUCTW[49][3] = 301;
		freqEUCTW[49][69] = 300;
		freqEUCTW[45][93] = 299;
		freqEUCTW[60][34] = 298;
		freqEUCTW[60][82] = 297;
		freqEUCTW[61][61] = 296;
		freqEUCTW[86][42] = 295;
		freqEUCTW[89][60] = 294;
		freqEUCTW[48][31] = 293;
		freqEUCTW[35][75] = 292;
		freqEUCTW[91][39] = 291;
		freqEUCTW[53][19] = 290;
		freqEUCTW[39][72] = 289;
		freqEUCTW[69][59] = 288;
		freqEUCTW[41][7] = 287;
		freqEUCTW[54][13] = 286;
		freqEUCTW[43][28] = 285;
		freqEUCTW[36][6] = 284;
		freqEUCTW[45][75] = 283;
		freqEUCTW[36][61] = 282;
		freqEUCTW[38][21] = 281;
		freqEUCTW[45][14] = 280;
		freqEUCTW[61][43] = 279;
		freqEUCTW[36][63] = 278;
		freqEUCTW[43][30] = 277;
		freqEUCTW[46][51] = 276;
		freqEUCTW[68][87] = 275;
		freqEUCTW[39][26] = 274;
		freqEUCTW[46][76] = 273;
		freqEUCTW[36][15] = 272;
		freqEUCTW[35][40] = 271;
		freqEUCTW[79][60] = 270;
		freqEUCTW[46][7] = 269;
		freqEUCTW[65][72] = 268;
		freqEUCTW[69][88] = 267;
		freqEUCTW[47][18] = 266;
		freqEUCTW[37][0] = 265;
		freqEUCTW[37][49] = 264;
		freqEUCTW[67][37] = 263;
		freqEUCTW[36][91] = 262;
		freqEUCTW[75][48] = 261;
		freqEUCTW[75][63] = 260;
		freqEUCTW[83][87] = 259;
		freqEUCTW[37][44] = 258;
		freqEUCTW[73][54] = 257;
		freqEUCTW[51][61] = 256;
		freqEUCTW[46][57] = 255;
		freqEUCTW[55][21] = 254;
		freqEUCTW[39][66] = 253;
		freqEUCTW[47][11] = 252;
		freqEUCTW[52][8] = 251;
		freqEUCTW[82][81] = 250;
		freqEUCTW[36][57] = 249;
		freqEUCTW[38][54] = 248;
		freqEUCTW[43][81] = 247;
		freqEUCTW[37][42] = 246;
		freqEUCTW[40][18] = 245;
		freqEUCTW[80][90] = 244;
		freqEUCTW[37][84] = 243;
		freqEUCTW[57][15] = 242;
		freqEUCTW[38][87] = 241;
		freqEUCTW[37][32] = 240;
		freqEUCTW[53][53] = 239;
		freqEUCTW[89][29] = 238;
		freqEUCTW[81][53] = 237;
		freqEUCTW[75][3] = 236;
		freqEUCTW[83][73] = 235;
		freqEUCTW[66][13] = 234;
		freqEUCTW[48][7] = 233;
		freqEUCTW[46][35] = 232;
		freqEUCTW[35][86] = 231;
		freqEUCTW[37][20] = 230;
		freqEUCTW[46][80] = 229;
		freqEUCTW[38][24] = 228;
		freqEUCTW[41][68] = 227;
		freqEUCTW[42][21] = 226;
		freqEUCTW[43][32] = 225;
		freqEUCTW[38][20] = 224;
		freqEUCTW[37][59] = 223;
		freqEUCTW[41][77] = 222;
		freqEUCTW[59][57] = 221;
		freqEUCTW[68][59] = 220;
		freqEUCTW[39][43] = 219;
		freqEUCTW[54][39] = 218;
		freqEUCTW[48][28] = 217;
		freqEUCTW[54][28] = 216;
		freqEUCTW[41][44] = 215;
		freqEUCTW[51][64] = 214;
		freqEUCTW[47][72] = 213;
		freqEUCTW[62][67] = 212;
		freqEUCTW[42][43] = 211;
		freqEUCTW[61][38] = 210;
		freqEUCTW[76][25] = 209;
		freqEUCTW[48][91] = 208;
		freqEUCTW[36][36] = 207;
		freqEUCTW[80][32] = 206;
		freqEUCTW[81][40] = 205;
		freqEUCTW[37][5] = 204;
		freqEUCTW[74][69] = 203;
		freqEUCTW[36][82] = 202;
		freqEUCTW[46][59] = 201;

		freqGBK[52][132] = 600;
		freqGBK[73][135] = 599;
		freqGBK[49][123] = 598;
		freqGBK[77][146] = 597;
		freqGBK[81][123] = 596;
		freqGBK[82][144] = 595;
		freqGBK[51][179] = 594;
		freqGBK[83][154] = 593;
		freqGBK[71][139] = 592;
		freqGBK[64][139] = 591;
		freqGBK[85][144] = 590;
		freqGBK[52][125] = 589;
		freqGBK[88][25] = 588;
		freqGBK[81][106] = 587;
		freqGBK[81][148] = 586;
		freqGBK[62][137] = 585;
		freqGBK[94][0] = 584;
		freqGBK[1][64] = 583;
		freqGBK[67][163] = 582;
		freqGBK[20][190] = 581;
		freqGBK[57][131] = 580;
		freqGBK[29][169] = 579;
		freqGBK[72][143] = 578;
		freqGBK[0][173] = 577;
		freqGBK[11][23] = 576;
		freqGBK[61][141] = 575;
		freqGBK[60][123] = 574;
		freqGBK[81][114] = 573;
		freqGBK[82][131] = 572;
		freqGBK[67][156] = 571;
		freqGBK[71][167] = 570;
		freqGBK[20][50] = 569;
		freqGBK[77][132] = 568;
		freqGBK[84][38] = 567;
		freqGBK[26][29] = 566;
		freqGBK[74][187] = 565;
		freqGBK[62][116] = 564;
		freqGBK[67][135] = 563;
		freqGBK[5][86] = 562;
		freqGBK[72][186] = 561;
		freqGBK[75][161] = 560;
		freqGBK[78][130] = 559;
		freqGBK[94][30] = 558;
		freqGBK[84][72] = 557;
		freqGBK[1][67] = 556;
		freqGBK[75][172] = 555;
		freqGBK[74][185] = 554;
		freqGBK[53][160] = 553;
		freqGBK[123][14] = 552;
		freqGBK[79][97] = 551;
		freqGBK[85][110] = 550;
		freqGBK[78][171] = 549;
		freqGBK[52][131] = 548;
		freqGBK[56][100] = 547;
		freqGBK[50][182] = 546;
		freqGBK[94][64] = 545;
		freqGBK[106][74] = 544;
		freqGBK[11][102] = 543;
		freqGBK[53][124] = 542;
		freqGBK[24][3] = 541;
		freqGBK[86][148] = 540;
		freqGBK[53][184] = 539;
		freqGBK[86][147] = 538;
		freqGBK[96][161] = 537;
		freqGBK[82][77] = 536;
		freqGBK[59][146] = 535;
		freqGBK[84][126] = 534;
		freqGBK[79][132] = 533;
		freqGBK[85][123] = 532;
		freqGBK[71][101] = 531;
		freqGBK[85][106] = 530;
		freqGBK[6][184] = 529;
		freqGBK[57][156] = 528;
		freqGBK[75][104] = 527;
		freqGBK[50][137] = 526;
		freqGBK[79][133] = 525;
		freqGBK[76][108] = 524;
		freqGBK[57][142] = 523;
		freqGBK[84][130] = 522;
		freqGBK[52][128] = 521;
		freqGBK[47][44] = 520;
		freqGBK[52][152] = 519;
		freqGBK[54][104] = 518;
		freqGBK[30][47] = 517;
		freqGBK[71][123] = 516;
		freqGBK[52][107] = 515;
		freqGBK[45][84] = 514;
		freqGBK[107][118] = 513;
		freqGBK[5][161] = 512;
		freqGBK[48][126] = 511;
		freqGBK[67][170] = 510;
		freqGBK[43][6] = 509;
		freqGBK[70][112] = 508;
		freqGBK[86][174] = 507;
		freqGBK[84][166] = 506;
		freqGBK[79][130] = 505;
		freqGBK[57][141] = 504;
		freqGBK[81][178] = 503;
		freqGBK[56][187] = 502;
		freqGBK[81][162] = 501;
		freqGBK[53][104] = 500;
		freqGBK[123][35] = 499;
		freqGBK[70][169] = 498;
		freqGBK[69][164] = 497;
		freqGBK[109][61] = 496;
		freqGBK[73][130] = 495;
		freqGBK[62][134] = 494;
		freqGBK[54][125] = 493;
		freqGBK[79][105] = 492;
		freqGBK[70][165] = 491;
		freqGBK[71][189] = 490;
		freqGBK[23][147] = 489;
		freqGBK[51][139] = 488;
		freqGBK[47][137] = 487;
		freqGBK[77][123] = 486;
		freqGBK[86][183] = 485;
		freqGBK[63][173] = 484;
		freqGBK[79][144] = 483;
		freqGBK[84][159] = 482;
		freqGBK[60][91] = 481;
		freqGBK[66][187] = 480;
		freqGBK[73][114] = 479;
		freqGBK[85][56] = 478;
		freqGBK[71][149] = 477;
		freqGBK[84][189] = 476;
		freqGBK[104][31] = 475;
		freqGBK[83][82] = 474;
		freqGBK[68][35] = 473;
		freqGBK[11][77] = 472;
		freqGBK[15][155] = 471;
		freqGBK[83][153] = 470;
		freqGBK[71][1] = 469;
		freqGBK[53][190] = 468;
		freqGBK[50][135] = 467;
		freqGBK[3][147] = 466;
		freqGBK[48][136] = 465;
		freqGBK[66][166] = 464;
		freqGBK[55][159] = 463;
		freqGBK[82][150] = 462;
		freqGBK[58][178] = 461;
		freqGBK[64][102] = 460;
		freqGBK[16][106] = 459;
		freqGBK[68][110] = 458;
		freqGBK[54][14] = 457;
		freqGBK[60][140] = 456;
		freqGBK[91][71] = 455;
		freqGBK[54][150] = 454;
		freqGBK[78][177] = 453;
		freqGBK[78][117] = 452;
		freqGBK[104][12] = 451;
		freqGBK[73][150] = 450;
		freqGBK[51][142] = 449;
		freqGBK[81][145] = 448;
		freqGBK[66][183] = 447;
		freqGBK[51][178] = 446;
		freqGBK[75][107] = 445;
		freqGBK[65][119] = 444;
		freqGBK[69][176] = 443;
		freqGBK[59][122] = 442;
		freqGBK[78][160] = 441;
		freqGBK[85][183] = 440;
		freqGBK[105][16] = 439;
		freqGBK[73][110] = 438;
		freqGBK[104][39] = 437;
		freqGBK[119][16] = 436;
		freqGBK[76][162] = 435;
		freqGBK[67][152] = 434;
		freqGBK[82][24] = 433;
		freqGBK[73][121] = 432;
		freqGBK[83][83] = 431;
		freqGBK[82][145] = 430;
		freqGBK[49][133] = 429;
		freqGBK[94][13] = 428;
		freqGBK[58][139] = 427;
		freqGBK[74][189] = 426;
		freqGBK[66][177] = 425;
		freqGBK[85][184] = 424;
		freqGBK[55][183] = 423;
		freqGBK[71][107] = 422;
		freqGBK[11][98] = 421;
		freqGBK[72][153] = 420;
		freqGBK[2][137] = 419;
		freqGBK[59][147] = 418;
		freqGBK[58][152] = 417;
		freqGBK[55][144] = 416;
		freqGBK[73][125] = 415;
		freqGBK[52][154] = 414;
		freqGBK[70][178] = 413;
		freqGBK[79][148] = 412;
		freqGBK[63][143] = 411;
		freqGBK[50][140] = 410;
		freqGBK[47][145] = 409;
		freqGBK[48][123] = 408;
		freqGBK[56][107] = 407;
		freqGBK[84][83] = 406;
		freqGBK[59][112] = 405;
		freqGBK[124][72] = 404;
		freqGBK[79][99] = 403;
		freqGBK[3][37] = 402;
		freqGBK[114][55] = 401;
		freqGBK[85][152] = 400;
		freqGBK[60][47] = 399;
		freqGBK[65][96] = 398;
		freqGBK[74][110] = 397;
		freqGBK[86][182] = 396;
		freqGBK[50][99] = 395;
		freqGBK[67][186] = 394;
		freqGBK[81][74] = 393;
		freqGBK[80][37] = 392;
		freqGBK[21][60] = 391;
		freqGBK[110][12] = 390;
		freqGBK[60][162] = 389;
		freqGBK[29][115] = 388;
		freqGBK[83][130] = 387;
		freqGBK[52][136] = 386;
		freqGBK[63][114] = 385;
		freqGBK[49][127] = 384;
		freqGBK[83][109] = 383;
		freqGBK[66][128] = 382;
		freqGBK[78][136] = 381;
		freqGBK[81][180] = 380;
		freqGBK[76][104] = 379;
		freqGBK[56][156] = 378;
		freqGBK[61][23] = 377;
		freqGBK[4][30] = 376;
		freqGBK[69][154] = 375;
		freqGBK[100][37] = 374;
		freqGBK[54][177] = 373;
		freqGBK[23][119] = 372;
		freqGBK[71][171] = 371;
		freqGBK[84][146] = 370;
		freqGBK[20][184] = 369;
		freqGBK[86][76] = 368;
		freqGBK[74][132] = 367;
		freqGBK[47][97] = 366;
		freqGBK[82][137] = 365;
		freqGBK[94][56] = 364;
		freqGBK[92][30] = 363;
		freqGBK[19][117] = 362;
		freqGBK[48][173] = 361;
		freqGBK[2][136] = 360;
		freqGBK[7][182] = 359;
		freqGBK[74][188] = 358;
		freqGBK[14][132] = 357;
		freqGBK[62][172] = 356;
		freqGBK[25][39] = 355;
		freqGBK[85][129] = 354;
		freqGBK[64][98] = 353;
		freqGBK[67][127] = 352;
		freqGBK[72][167] = 351;
		freqGBK[57][143] = 350;
		freqGBK[76][187] = 349;
		freqGBK[83][181] = 348;
		freqGBK[84][10] = 347;
		freqGBK[55][166] = 346;
		freqGBK[55][188] = 345;
		freqGBK[13][151] = 344;
		freqGBK[62][124] = 343;
		freqGBK[53][136] = 342;
		freqGBK[106][57] = 341;
		freqGBK[47][166] = 340;
		freqGBK[109][30] = 339;
		freqGBK[78][114] = 338;
		freqGBK[83][19] = 337;
		freqGBK[56][162] = 336;
		freqGBK[60][177] = 335;
		freqGBK[88][9] = 334;
		freqGBK[74][163] = 333;
		freqGBK[52][156] = 332;
		freqGBK[71][180] = 331;
		freqGBK[60][57] = 330;
		freqGBK[72][173] = 329;
		freqGBK[82][91] = 328;
		freqGBK[51][186] = 327;
		freqGBK[75][86] = 326;
		freqGBK[75][78] = 325;
		freqGBK[76][170] = 324;
		freqGBK[60][147] = 323;
		freqGBK[82][75] = 322;
		freqGBK[80][148] = 321;
		freqGBK[86][150] = 320;
		freqGBK[13][95] = 319;
		freqGBK[0][11] = 318;
		freqGBK[84][190] = 317;
		freqGBK[76][166] = 316;
		freqGBK[14][72] = 315;
		freqGBK[67][144] = 314;
		freqGBK[84][44] = 313;
		freqGBK[72][125] = 312;
		freqGBK[66][127] = 311;
		freqGBK[60][25] = 310;
		freqGBK[70][146] = 309;
		freqGBK[79][135] = 308;
		freqGBK[54][135] = 307;
		freqGBK[60][104] = 306;
		freqGBK[55][132] = 305;
		freqGBK[94][2] = 304;
		freqGBK[54][133] = 303;
		freqGBK[56][190] = 302;
		freqGBK[58][174] = 301;
		freqGBK[80][144] = 300;
		freqGBK[85][113] = 299;

		freqKR[31][43] = 600;
		freqKR[19][56] = 599;
		freqKR[38][46] = 598;
		freqKR[3][3] = 597;
		freqKR[29][77] = 596;
		freqKR[19][33] = 595;
		freqKR[30][0] = 594;
		freqKR[29][89] = 593;
		freqKR[31][26] = 592;
		freqKR[31][38] = 591;
		freqKR[32][85] = 590;
		freqKR[15][0] = 589;
		freqKR[16][54] = 588;
		freqKR[15][76] = 587;
		freqKR[31][25] = 586;
		freqKR[23][13] = 585;
		freqKR[28][34] = 584;
		freqKR[18][9] = 583;
		freqKR[29][37] = 582;
		freqKR[22][45] = 581;
		freqKR[19][46] = 580;
		freqKR[16][65] = 579;
		freqKR[23][5] = 578;
		freqKR[26][70] = 577;
		freqKR[31][53] = 576;
		freqKR[27][12] = 575;
		freqKR[30][67] = 574;
		freqKR[31][57] = 573;
		freqKR[20][20] = 572;
		freqKR[30][31] = 571;
		freqKR[20][72] = 570;
		freqKR[15][51] = 569;
		freqKR[3][8] = 568;
		freqKR[32][53] = 567;
		freqKR[27][85] = 566;
		freqKR[25][23] = 565;
		freqKR[15][44] = 564;
		freqKR[32][3] = 563;
		freqKR[31][68] = 562;
		freqKR[30][24] = 561;
		freqKR[29][49] = 560;
		freqKR[27][49] = 559;
		freqKR[23][23] = 558;
		freqKR[31][91] = 557;
		freqKR[31][46] = 556;
		freqKR[19][74] = 555;
		freqKR[27][27] = 554;
		freqKR[3][17] = 553;
		freqKR[20][38] = 552;
		freqKR[21][82] = 551;
		freqKR[28][25] = 550;
		freqKR[32][5] = 549;
		freqKR[31][23] = 548;
		freqKR[25][45] = 547;
		freqKR[32][87] = 546;
		freqKR[18][26] = 545;
		freqKR[24][10] = 544;
		freqKR[26][82] = 543;
		freqKR[15][89] = 542;
		freqKR[28][36] = 541;
		freqKR[28][31] = 540;
		freqKR[16][23] = 539;
		freqKR[16][77] = 538;
		freqKR[19][84] = 537;
		freqKR[23][72] = 536;
		freqKR[38][48] = 535;
		freqKR[23][2] = 534;
		freqKR[30][20] = 533;
		freqKR[38][47] = 532;
		freqKR[39][12] = 531;
		freqKR[23][21] = 530;
		freqKR[18][17] = 529;
		freqKR[30][87] = 528;
		freqKR[29][62] = 527;
		freqKR[29][87] = 526;
		freqKR[34][53] = 525;
		freqKR[32][29] = 524;
		freqKR[35][0] = 523;
		freqKR[24][43] = 522;
		freqKR[36][44] = 521;
		freqKR[20][30] = 520;
		freqKR[39][86] = 519;
		freqKR[22][14] = 518;
		freqKR[29][39] = 517;
		freqKR[28][38] = 516;
		freqKR[23][79] = 515;
		freqKR[24][56] = 514;
		freqKR[29][63] = 513;
		freqKR[31][45] = 512;
		freqKR[23][26] = 511;
		freqKR[15][87] = 510;
		freqKR[30][74] = 509;
		freqKR[24][69] = 508;
		freqKR[20][4] = 507;
		freqKR[27][50] = 506;
		freqKR[30][75] = 505;
		freqKR[24][13] = 504;
		freqKR[30][8] = 503;
		freqKR[31][6] = 502;
		freqKR[25][80] = 501;
		freqKR[36][8] = 500;
		freqKR[15][18] = 499;
		freqKR[39][23] = 498;
		freqKR[16][24] = 497;
		freqKR[31][89] = 496;
		freqKR[15][71] = 495;
		freqKR[15][57] = 494;
		freqKR[30][11] = 493;
		freqKR[15][36] = 492;
		freqKR[16][60] = 491;
		freqKR[24][45] = 490;
		freqKR[37][35] = 489;
		freqKR[24][87] = 488;
		freqKR[20][45] = 487;
		freqKR[31][90] = 486;
		freqKR[32][21] = 485;
		freqKR[19][70] = 484;
		freqKR[24][15] = 483;
		freqKR[26][92] = 482;
		freqKR[37][13] = 481;
		freqKR[39][2] = 480;
		freqKR[23][70] = 479;
		freqKR[27][25] = 478;
		freqKR[15][69] = 477;
		freqKR[19][61] = 476;
		freqKR[31][58] = 475;
		freqKR[24][57] = 474;
		freqKR[36][74] = 473;
		freqKR[21][6] = 472;
		freqKR[30][44] = 471;
		freqKR[15][91] = 470;
		freqKR[27][16] = 469;
		freqKR[29][42] = 468;
		freqKR[33][86] = 467;
		freqKR[29][41] = 466;
		freqKR[20][68] = 465;
		freqKR[25][47] = 464;
		freqKR[22][0] = 463;
		freqKR[18][14] = 462;
		freqKR[31][28] = 461;
		freqKR[15][2] = 460;
		freqKR[23][76] = 459;
		freqKR[38][32] = 458;
		freqKR[29][82] = 457;
		freqKR[21][86] = 456;
		freqKR[24][62] = 455;
		freqKR[31][64] = 454;
		freqKR[38][26] = 453;
		freqKR[32][86] = 452;
		freqKR[22][32] = 451;
		freqKR[19][59] = 450;
		freqKR[34][18] = 449;
		freqKR[18][54] = 448;
		freqKR[38][63] = 447;
		freqKR[36][23] = 446;
		freqKR[35][35] = 445;
		freqKR[32][62] = 444;
		freqKR[28][35] = 443;
		freqKR[27][13] = 442;
		freqKR[31][59] = 441;
		freqKR[29][29] = 440;
		freqKR[15][64] = 439;
		freqKR[26][84] = 438;
		freqKR[21][90] = 437;
		freqKR[20][24] = 436;
		freqKR[16][18] = 435;
		freqKR[22][23] = 434;
		freqKR[31][14] = 433;
		freqKR[15][1] = 432;
		freqKR[18][63] = 431;
		freqKR[19][10] = 430;
		freqKR[25][49] = 429;
		freqKR[36][57] = 428;
		freqKR[20][22] = 427;
		freqKR[15][15] = 426;
		freqKR[31][51] = 425;
		freqKR[24][60] = 424;
		freqKR[31][70] = 423;
		freqKR[15][7] = 422;
		freqKR[28][40] = 421;
		freqKR[18][41] = 420;
		freqKR[15][38] = 419;
		freqKR[32][0] = 418;
		freqKR[19][51] = 417;
		freqKR[34][62] = 416;
		freqKR[16][27] = 415;
		freqKR[20][70] = 414;
		freqKR[22][33] = 413;
		freqKR[26][73] = 412;
		freqKR[20][79] = 411;
		freqKR[23][6] = 410;
		freqKR[24][85] = 409;
		freqKR[38][51] = 408;
		freqKR[29][88] = 407;
		freqKR[38][55] = 406;
		freqKR[32][32] = 405;
		freqKR[27][18] = 404;
		freqKR[23][87] = 403;
		freqKR[35][6] = 402;
		freqKR[34][27] = 401;
		freqKR[39][35] = 400;
		freqKR[30][88] = 399;
		freqKR[32][92] = 398;
		freqKR[32][49] = 397;
		freqKR[24][61] = 396;
		freqKR[18][74] = 395;
		freqKR[23][77] = 394;
		freqKR[23][50] = 393;
		freqKR[23][32] = 392;
		freqKR[23][36] = 391;
		freqKR[38][38] = 390;
		freqKR[29][86] = 389;
		freqKR[36][15] = 388;
		freqKR[31][50] = 387;
		freqKR[15][86] = 386;
		freqKR[39][13] = 385;
		freqKR[34][26] = 384;
		freqKR[19][34] = 383;
		freqKR[16][3] = 382;
		freqKR[26][93] = 381;
		freqKR[19][67] = 380;
		freqKR[24][72] = 379;
		freqKR[29][17] = 378;
		freqKR[23][24] = 377;
		freqKR[25][19] = 376;
		freqKR[18][65] = 375;
		freqKR[30][78] = 374;
		freqKR[27][52] = 373;
		freqKR[22][18] = 372;
		freqKR[16][38] = 371;
		freqKR[21][26] = 370;
		freqKR[34][20] = 369;
		freqKR[15][42] = 368;
		freqKR[16][71] = 367;
		freqKR[17][17] = 366;
		freqKR[24][71] = 365;
		freqKR[18][84] = 364;
		freqKR[15][40] = 363;
		freqKR[31][62] = 362;
		freqKR[15][8] = 361;
		freqKR[16][69] = 360;
		freqKR[29][79] = 359;
		freqKR[38][91] = 358;
		freqKR[31][92] = 357;
		freqKR[20][77] = 356;
		freqKR[3][16] = 355;
		freqKR[27][87] = 354;
		freqKR[16][25] = 353;
		freqKR[36][33] = 352;
		freqKR[37][76] = 351;
		freqKR[30][12] = 350;
		freqKR[26][75] = 349;
		freqKR[25][14] = 348;
		freqKR[32][26] = 347;
		freqKR[23][22] = 346;
		freqKR[20][90] = 345;
		freqKR[19][8] = 344;
		freqKR[38][41] = 343;
		freqKR[34][2] = 342;
		freqKR[39][4] = 341;
		freqKR[27][89] = 340;
		freqKR[28][41] = 339;
		freqKR[28][44] = 338;
		freqKR[24][92] = 337;
		freqKR[34][65] = 336;
		freqKR[39][14] = 335;
		freqKR[21][38] = 334;
		freqKR[19][31] = 333;
		freqKR[37][39] = 332;
		freqKR[33][41] = 331;
		freqKR[38][4] = 330;
		freqKR[23][80] = 329;
		freqKR[25][24] = 328;
		freqKR[37][17] = 327;
		freqKR[22][16] = 326;
		freqKR[22][46] = 325;
		freqKR[33][91] = 324;
		freqKR[24][89] = 323;
		freqKR[30][52] = 322;
		freqKR[29][38] = 321;
		freqKR[38][85] = 320;
		freqKR[15][12] = 319;
		freqKR[27][58] = 318;
		freqKR[29][52] = 317;
		freqKR[37][38] = 316;
		freqKR[34][41] = 315;
		freqKR[31][65] = 314;
		freqKR[29][53] = 313;
		freqKR[22][47] = 312;
		freqKR[22][19] = 311;
		freqKR[26][0] = 310;
		freqKR[37][86] = 309;
		freqKR[35][4] = 308;
		freqKR[36][54] = 307;
		freqKR[20][76] = 306;
		freqKR[30][9] = 305;
		freqKR[30][33] = 304;
		freqKR[23][17] = 303;
		freqKR[23][33] = 302;
		freqKR[38][52] = 301;
		freqKR[15][19] = 300;
		freqKR[28][45] = 299;
		freqKR[29][78] = 298;
		freqKR[23][15] = 297;
		freqKR[33][5] = 296;
		freqKR[17][40] = 295;
		freqKR[30][83] = 294;
		freqKR[18][1] = 293;
		freqKR[30][81] = 292;
		freqKR[19][40] = 291;
		freqKR[24][47] = 290;
		freqKR[17][56] = 289;
		freqKR[39][80] = 288;
		freqKR[30][46] = 287;
		freqKR[16][61] = 286;
		freqKR[26][78] = 285;
		freqKR[26][57] = 284;
		freqKR[20][46] = 283;
		freqKR[25][15] = 282;
		freqKR[25][91] = 281;
		freqKR[21][83] = 280;
		freqKR[30][77] = 279;
		freqKR[35][30] = 278;
		freqKR[30][34] = 277;
		freqKR[20][69] = 276;
		freqKR[35][10] = 275;
		freqKR[29][70] = 274;
		freqKR[22][50] = 273;
		freqKR[18][0] = 272;
		freqKR[22][64] = 271;
		freqKR[38][65] = 270;
		freqKR[22][70] = 269;
		freqKR[24][58] = 268;
		freqKR[19][66] = 267;
		freqKR[30][59] = 266;
		freqKR[37][14] = 265;
		freqKR[16][56] = 264;
		freqKR[29][85] = 263;
		freqKR[31][15] = 262;
		freqKR[36][84] = 261;
		freqKR[39][15] = 260;
		freqKR[39][90] = 259;
		freqKR[18][12] = 258;
		freqKR[21][93] = 257;
		freqKR[24][66] = 256;
		freqKR[27][90] = 255;
		freqKR[25][90] = 254;
		freqKR[22][24] = 253;
		freqKR[36][67] = 252;
		freqKR[33][90] = 251;
		freqKR[15][60] = 250;
		freqKR[23][85] = 249;
		freqKR[34][1] = 248;
		freqKR[39][37] = 247;
		freqKR[21][18] = 246;
		freqKR[34][4] = 245;
		freqKR[28][33] = 244;
		freqKR[15][13] = 243;
		freqKR[32][22] = 242;
		freqKR[30][76] = 241;
		freqKR[20][21] = 240;
		freqKR[38][66] = 239;
		freqKR[32][55] = 238;
		freqKR[32][89] = 237;
		freqKR[25][26] = 236;
		freqKR[16][80] = 235;
		freqKR[15][43] = 234;
		freqKR[38][54] = 233;
		freqKR[39][68] = 232;
		freqKR[22][88] = 231;
		freqKR[21][84] = 230;
		freqKR[21][17] = 229;
		freqKR[20][28] = 228;
		freqKR[32][1] = 227;
		freqKR[33][87] = 226;
		freqKR[38][71] = 225;
		freqKR[37][47] = 224;
		freqKR[18][77] = 223;
		freqKR[37][58] = 222;
		freqKR[34][74] = 221;
		freqKR[32][54] = 220;
		freqKR[27][33] = 219;
		freqKR[32][93] = 218;
		freqKR[23][51] = 217;
		freqKR[20][57] = 216;
		freqKR[22][37] = 215;
		freqKR[39][10] = 214;
		freqKR[39][17] = 213;
		freqKR[33][4] = 212;
		freqKR[32][84] = 211;
		freqKR[34][3] = 210;
		freqKR[28][27] = 209;
		freqKR[15][79] = 208;
		freqKR[34][21] = 207;
		freqKR[34][69] = 206;
		freqKR[21][62] = 205;
		freqKR[36][24] = 204;
		freqKR[16][89] = 203;
		freqKR[18][48] = 202;
		freqKR[38][15] = 201;
		freqKR[36][58] = 200;
		freqKR[21][56] = 199;
		freqKR[34][48] = 198;
		freqKR[21][15] = 197;
		freqKR[39][3] = 196;
		freqKR[16][44] = 195;
		freqKR[18][79] = 194;
		freqKR[25][13] = 193;
		freqKR[29][47] = 192;
		freqKR[38][88] = 191;
		freqKR[20][71] = 190;
		freqKR[16][58] = 189;
		freqKR[35][57] = 188;
		freqKR[29][30] = 187;
		freqKR[29][23] = 186;
		freqKR[34][93] = 185;
		freqKR[30][85] = 184;
		freqKR[15][80] = 183;
		freqKR[32][78] = 182;
		freqKR[37][82] = 181;
		freqKR[22][40] = 180;
		freqKR[21][69] = 179;
		freqKR[26][85] = 178;
		freqKR[31][31] = 177;
		freqKR[28][64] = 176;
		freqKR[38][13] = 175;
		freqKR[25][2] = 174;
		freqKR[22][34] = 173;
		freqKR[28][28] = 172;
		freqKR[24][91] = 171;
		freqKR[33][74] = 170;
		freqKR[29][40] = 169;
		freqKR[15][77] = 168;
		freqKR[32][80] = 167;
		freqKR[30][41] = 166;
		freqKR[23][30] = 165;
		freqKR[24][63] = 164;
		freqKR[30][53] = 163;
		freqKR[39][70] = 162;
		freqKR[23][61] = 161;
		freqKR[37][27] = 160;
		freqKR[16][55] = 159;
		freqKR[22][74] = 158;
		freqKR[26][50] = 157;
		freqKR[16][10] = 156;
		freqKR[34][63] = 155;
		freqKR[35][14] = 154;
		freqKR[17][7] = 153;
		freqKR[15][59] = 152;
		freqKR[27][23] = 151;
		freqKR[18][70] = 150;
		freqKR[32][56] = 149;
		freqKR[37][87] = 148;
		freqKR[17][61] = 147;
		freqKR[18][83] = 146;
		freqKR[23][86] = 145;
		freqKR[17][31] = 144;
		freqKR[23][83] = 143;
		freqKR[35][2] = 142;
		freqKR[18][64] = 141;
		freqKR[27][43] = 140;
		freqKR[32][42] = 139;
		freqKR[25][76] = 138;
		freqKR[19][85] = 137;
		freqKR[37][81] = 136;
		freqKR[38][83] = 135;
		freqKR[35][7] = 134;
		freqKR[16][51] = 133;
		freqKR[27][22] = 132;
		freqKR[16][76] = 131;
		freqKR[22][4] = 130;
		freqKR[38][84] = 129;
		freqKR[17][83] = 128;
		freqKR[24][46] = 127;
		freqKR[33][15] = 126;
		freqKR[20][48] = 125;
		freqKR[17][30] = 124;
		freqKR[30][93] = 123;
		freqKR[28][11] = 122;
		freqKR[28][30] = 121;
		freqKR[15][62] = 120;
		freqKR[17][87] = 119;
		freqKR[32][81] = 118;
		freqKR[23][37] = 117;
		freqKR[30][22] = 116;
		freqKR[32][66] = 115;
		freqKR[33][78] = 114;
		freqKR[21][4] = 113;
		freqKR[31][17] = 112;
		freqKR[39][61] = 111;
		freqKR[18][76] = 110;
		freqKR[15][85] = 109;
		freqKR[31][47] = 108;
		freqKR[19][57] = 107;
		freqKR[23][55] = 106;
		freqKR[27][29] = 105;
		freqKR[29][46] = 104;
		freqKR[33][0] = 103;
		freqKR[16][83] = 102;
		freqKR[39][78] = 101;
		freqKR[32][77] = 100;
		freqKR[36][25] = 99;
		freqKR[34][19] = 98;
		freqKR[38][49] = 97;
		freqKR[19][25] = 96;
		freqKR[23][53] = 95;
		freqKR[28][43] = 94;
		freqKR[31][44] = 93;
		freqKR[36][34] = 92;
		freqKR[16][34] = 91;
		freqKR[35][1] = 90;
		freqKR[19][87] = 89;
		freqKR[18][53] = 88;
		freqKR[29][54] = 87;
		freqKR[22][41] = 86;
		freqKR[38][18] = 85;
		freqKR[22][2] = 84;
		freqKR[20][3] = 83;
		freqKR[39][69] = 82;
		freqKR[30][29] = 81;
		freqKR[28][19] = 80;
		freqKR[29][90] = 79;
		freqKR[17][86] = 78;
		freqKR[15][9] = 77;
		freqKR[39][73] = 76;
		freqKR[15][37] = 75;
		freqKR[35][40] = 74;
		freqKR[33][77] = 73;
		freqKR[27][86] = 72;
		freqKR[36][79] = 71;
		freqKR[23][18] = 70;
		freqKR[34][87] = 69;
		freqKR[39][24] = 68;
		freqKR[26][8] = 67;
		freqKR[33][48] = 66;
		freqKR[39][30] = 65;
		freqKR[33][28] = 64;
		freqKR[16][67] = 63;
		freqKR[31][78] = 62;
		freqKR[32][23] = 61;
		freqKR[24][55] = 60;
		freqKR[30][68] = 59;
		freqKR[18][60] = 58;
		freqKR[15][17] = 57;
		freqKR[23][34] = 56;
		freqKR[20][49] = 55;
		freqKR[15][78] = 54;
		freqKR[24][14] = 53;
		freqKR[19][41] = 52;
		freqKR[31][55] = 51;
		freqKR[21][39] = 50;
		freqKR[35][9] = 49;
		freqKR[30][15] = 48;
		freqKR[20][52] = 47;
		freqKR[35][71] = 46;
		freqKR[20][7] = 45;
		freqKR[29][72] = 44;
		freqKR[37][77] = 43;
		freqKR[22][35] = 42;
		freqKR[20][61] = 41;
		freqKR[31][60] = 40;
		freqKR[20][93] = 39;
		freqKR[27][92] = 38;
		freqKR[28][16] = 37;
		freqKR[36][26] = 36;
		freqKR[18][89] = 35;
		freqKR[21][63] = 34;
		freqKR[22][52] = 33;
		freqKR[24][65] = 32;
		freqKR[31][8] = 31;
		freqKR[31][49] = 30;
		freqKR[33][30] = 29;
		freqKR[37][15] = 28;
		freqKR[18][18] = 27;
		freqKR[25][50] = 26;
		freqKR[29][20] = 25;
		freqKR[35][48] = 24;
		freqKR[38][75] = 23;
		freqKR[26][83] = 22;
		freqKR[21][87] = 21;
		freqKR[27][71] = 20;
		freqKR[32][91] = 19;
		freqKR[25][73] = 18;
		freqKR[16][84] = 17;
		freqKR[25][31] = 16;
		freqKR[17][90] = 15;
		freqKR[18][40] = 14;
		freqKR[17][77] = 13;
		freqKR[17][35] = 12;
		freqKR[23][52] = 11;
		freqKR[23][35] = 10;
		freqKR[16][5] = 9;
		freqKR[23][58] = 8;
		freqKR[19][60] = 7;
		freqKR[30][32] = 6;
		freqKR[38][34] = 5;
		freqKR[23][4] = 4;
		freqKR[23][1] = 3;
		freqKR[27][57] = 2;
		freqKR[39][38] = 1;
		freqKR[32][33] = 0;
		freqJP[3][74] = 600;
		freqJP[3][45] = 599;
		freqJP[3][3] = 598;
		freqJP[3][24] = 597;
		freqJP[3][30] = 596;
		freqJP[3][42] = 595;
		freqJP[3][46] = 594;
		freqJP[3][39] = 593;
		freqJP[3][11] = 592;
		freqJP[3][37] = 591;
		freqJP[3][38] = 590;
		freqJP[3][31] = 589;
		freqJP[3][41] = 588;
		freqJP[3][5] = 587;
		freqJP[3][10] = 586;
		freqJP[3][75] = 585;
		freqJP[3][65] = 584;
		freqJP[3][72] = 583;
		freqJP[37][91] = 582;
		freqJP[0][27] = 581;
		freqJP[3][18] = 580;
		freqJP[3][22] = 579;
		freqJP[3][61] = 578;
		freqJP[3][14] = 577;
		freqJP[24][80] = 576;
		freqJP[4][82] = 575;
		freqJP[17][80] = 574;
		freqJP[30][44] = 573;
		freqJP[3][73] = 572;
		freqJP[3][64] = 571;
		freqJP[38][14] = 570;
		freqJP[33][70] = 569;
		freqJP[3][1] = 568;
		freqJP[3][16] = 567;
		freqJP[3][35] = 566;
		freqJP[3][40] = 565;
		freqJP[4][74] = 564;
		freqJP[4][24] = 563;
		freqJP[42][59] = 562;
		freqJP[3][7] = 561;
		freqJP[3][71] = 560;
		freqJP[3][12] = 559;
		freqJP[15][75] = 558;
		freqJP[3][20] = 557;
		freqJP[4][39] = 556;
		freqJP[34][69] = 555;
		freqJP[3][28] = 554;
		freqJP[35][24] = 553;
		freqJP[3][82] = 552;
		freqJP[28][47] = 551;
		freqJP[3][67] = 550;
		freqJP[37][16] = 549;
		freqJP[26][93] = 548;
		freqJP[4][1] = 547;
		freqJP[26][85] = 546;
		freqJP[31][14] = 545;
		freqJP[4][3] = 544;
		freqJP[4][72] = 543;
		freqJP[24][51] = 542;
		freqJP[27][51] = 541;
		freqJP[27][49] = 540;
		freqJP[22][77] = 539;
		freqJP[27][10] = 538;
		freqJP[29][68] = 537;
		freqJP[20][35] = 536;
		freqJP[41][11] = 535;
		freqJP[24][70] = 534;
		freqJP[36][61] = 533;
		freqJP[31][23] = 532;
		freqJP[43][16] = 531;
		freqJP[23][68] = 530;
		freqJP[32][15] = 529;
		freqJP[3][32] = 528;
		freqJP[19][53] = 527;
		freqJP[40][83] = 526;
		freqJP[4][14] = 525;
		freqJP[36][9] = 524;
		freqJP[4][73] = 523;
		freqJP[23][10] = 522;
		freqJP[3][63] = 521;
		freqJP[39][14] = 520;
		freqJP[3][78] = 519;
		freqJP[33][47] = 518;
		freqJP[21][39] = 517;
		freqJP[34][46] = 516;
		freqJP[36][75] = 515;
		freqJP[41][92] = 514;
		freqJP[37][93] = 513;
		freqJP[4][34] = 512;
		freqJP[15][86] = 511;
		freqJP[46][1] = 510;
		freqJP[37][65] = 509;
		freqJP[3][62] = 508;
		freqJP[32][73] = 507;
		freqJP[21][65] = 506;
		freqJP[29][75] = 505;
		freqJP[26][51] = 504;
		freqJP[3][34] = 503;
		freqJP[4][10] = 502;
		freqJP[30][22] = 501;
		freqJP[35][73] = 500;
		freqJP[17][82] = 499;
		freqJP[45][8] = 498;
		freqJP[27][73] = 497;
		freqJP[18][55] = 496;
		freqJP[25][2] = 495;
		freqJP[3][26] = 494;
		freqJP[45][46] = 493;
		freqJP[4][22] = 492;
		freqJP[4][40] = 491;
		freqJP[18][10] = 490;
		freqJP[32][9] = 489;
		freqJP[26][49] = 488;
		freqJP[3][47] = 487;
		freqJP[24][65] = 486;
		freqJP[4][76] = 485;
		freqJP[43][67] = 484;
		freqJP[3][9] = 483;
		freqJP[41][37] = 482;
		freqJP[33][68] = 481;
		freqJP[43][31] = 480;
		freqJP[19][55] = 479;
		freqJP[4][30] = 478;
		freqJP[27][33] = 477;
		freqJP[16][62] = 476;
		freqJP[36][35] = 475;
		freqJP[37][15] = 474;
		freqJP[27][70] = 473;
		freqJP[22][71] = 472;
		freqJP[33][45] = 471;
		freqJP[31][78] = 470;
		freqJP[43][59] = 469;
		freqJP[32][19] = 468;
		freqJP[17][28] = 467;
		freqJP[40][28] = 466;
		freqJP[20][93] = 465;
		freqJP[18][15] = 464;
		freqJP[4][23] = 463;
		freqJP[3][23] = 462;
		freqJP[26][64] = 461;
		freqJP[44][92] = 460;
		freqJP[17][27] = 459;
		freqJP[3][56] = 458;
		freqJP[25][38] = 457;
		freqJP[23][31] = 456;
		freqJP[35][43] = 455;
		freqJP[4][54] = 454;
		freqJP[35][19] = 453;
		freqJP[22][47] = 452;
		freqJP[42][0] = 451;
		freqJP[23][28] = 450;
		freqJP[46][33] = 449;
		freqJP[36][85] = 448;
		freqJP[31][12] = 447;
		freqJP[3][76] = 446;
		freqJP[4][75] = 445;
		freqJP[36][56] = 444;
		freqJP[4][64] = 443;
		freqJP[25][77] = 442;
		freqJP[15][52] = 441;
		freqJP[33][73] = 440;
		freqJP[3][55] = 439;
		freqJP[43][82] = 438;
		freqJP[27][82] = 437;
		freqJP[20][3] = 436;
		freqJP[40][51] = 435;
		freqJP[3][17] = 434;
		freqJP[27][71] = 433;
		freqJP[4][52] = 432;
		freqJP[44][48] = 431;
		freqJP[27][2] = 430;
		freqJP[17][39] = 429;
		freqJP[31][8] = 428;
		freqJP[44][54] = 427;
		freqJP[43][18] = 426;
		freqJP[43][77] = 425;
		freqJP[4][61] = 424;
		freqJP[19][91] = 423;
		freqJP[31][13] = 422;
		freqJP[44][71] = 421;
		freqJP[20][0] = 420;
		freqJP[23][87] = 419;
		freqJP[21][14] = 418;
		freqJP[29][13] = 417;
		freqJP[3][58] = 416;
		freqJP[26][18] = 415;
		freqJP[4][47] = 414;
		freqJP[4][18] = 413;
		freqJP[3][53] = 412;
		freqJP[26][92] = 411;
		freqJP[21][7] = 410;
		freqJP[4][37] = 409;
		freqJP[4][63] = 408;
		freqJP[36][51] = 407;
		freqJP[4][32] = 406;
		freqJP[28][73] = 405;
		freqJP[4][50] = 404;
		freqJP[41][60] = 403;
		freqJP[23][1] = 402;
		freqJP[36][92] = 401;
		freqJP[15][41] = 400;
		freqJP[21][71] = 399;
		freqJP[41][30] = 398;
		freqJP[32][76] = 397;
		freqJP[17][34] = 396;
		freqJP[26][15] = 395;
		freqJP[26][25] = 394;
		freqJP[31][77] = 393;
		freqJP[31][3] = 392;
		freqJP[46][34] = 391;
		freqJP[27][84] = 390;
		freqJP[23][8] = 389;
		freqJP[16][0] = 388;
		freqJP[28][80] = 387;
		freqJP[26][54] = 386;
		freqJP[33][18] = 385;
		freqJP[31][20] = 384;
		freqJP[31][62] = 383;
		freqJP[30][41] = 382;
		freqJP[33][30] = 381;
		freqJP[45][45] = 380;
		freqJP[37][82] = 379;
		freqJP[15][33] = 378;
		freqJP[20][12] = 377;
		freqJP[18][5] = 376;
		freqJP[28][86] = 375;
		freqJP[30][19] = 374;
		freqJP[42][43] = 373;
		freqJP[36][31] = 372;
		freqJP[17][93] = 371;
		freqJP[4][15] = 370;
		freqJP[21][20] = 369;
		freqJP[23][21] = 368;
		freqJP[28][72] = 367;
		freqJP[4][20] = 366;
		freqJP[26][55] = 365;
		freqJP[21][5] = 364;
		freqJP[19][16] = 363;
		freqJP[23][64] = 362;
		freqJP[40][59] = 361;
		freqJP[37][26] = 360;
		freqJP[26][56] = 359;
		freqJP[4][12] = 358;
		freqJP[33][71] = 357;
		freqJP[32][39] = 356;
		freqJP[38][40] = 355;
		freqJP[22][74] = 354;
		freqJP[3][25] = 353;
		freqJP[15][48] = 352;
		freqJP[41][82] = 351;
		freqJP[41][9] = 350;
		freqJP[25][48] = 349;
		freqJP[31][71] = 348;
		freqJP[43][29] = 347;
		freqJP[26][80] = 346;
		freqJP[4][5] = 345;
		freqJP[18][71] = 344;
		freqJP[29][0] = 343;
		freqJP[43][43] = 342;
		freqJP[23][81] = 341;
		freqJP[4][42] = 340;
		freqJP[44][28] = 339;
		freqJP[23][93] = 338;
		freqJP[17][81] = 337;
		freqJP[25][25] = 336;
		freqJP[41][23] = 335;
		freqJP[34][35] = 334;
		freqJP[4][53] = 333;
		freqJP[28][36] = 332;
		freqJP[4][41] = 331;
		freqJP[25][60] = 330;
		freqJP[23][20] = 329;
		freqJP[3][43] = 328;
		freqJP[24][79] = 327;
		freqJP[29][41] = 326;
		freqJP[30][83] = 325;
		freqJP[3][50] = 324;
		freqJP[22][18] = 323;
		freqJP[18][3] = 322;
		freqJP[39][30] = 321;
		freqJP[4][28] = 320;
		freqJP[21][64] = 319;
		freqJP[4][68] = 318;
		freqJP[17][71] = 317;
		freqJP[27][0] = 316;
		freqJP[39][28] = 315;
		freqJP[30][13] = 314;
		freqJP[36][70] = 313;
		freqJP[20][82] = 312;
		freqJP[33][38] = 311;
		freqJP[44][87] = 310;
		freqJP[34][45] = 309;
		freqJP[4][26] = 308;
		freqJP[24][44] = 307;
		freqJP[38][67] = 306;
		freqJP[38][6] = 305;
		freqJP[30][68] = 304;
		freqJP[15][89] = 303;
		freqJP[24][93] = 302;
		freqJP[40][41] = 301;
		freqJP[38][3] = 300;
		freqJP[28][23] = 299;
		freqJP[26][17] = 298;
		freqJP[4][38] = 297;
		freqJP[22][78] = 296;
		freqJP[15][37] = 295;
		freqJP[25][85] = 294;
		freqJP[4][9] = 293;
		freqJP[4][7] = 292;
		freqJP[27][53] = 291;
		freqJP[39][29] = 290;
		freqJP[41][43] = 289;
		freqJP[25][62] = 288;
		freqJP[4][48] = 287;
		freqJP[28][28] = 286;
		freqJP[21][40] = 285;
		freqJP[36][73] = 284;
		freqJP[26][39] = 283;
		freqJP[22][54] = 282;
		freqJP[33][5] = 281;
		freqJP[19][21] = 280;
		freqJP[46][31] = 279;
		freqJP[20][64] = 278;
		freqJP[26][63] = 277;
		freqJP[22][23] = 276;
		freqJP[25][81] = 275;
		freqJP[4][62] = 274;
		freqJP[37][31] = 273;
		freqJP[40][52] = 272;
		freqJP[29][79] = 271;
		freqJP[41][48] = 270;
		freqJP[31][57] = 269;
		freqJP[32][92] = 268;
		freqJP[36][36] = 267;
		freqJP[27][7] = 266;
		freqJP[35][29] = 265;
		freqJP[37][34] = 264;
		freqJP[34][42] = 263;
		freqJP[27][15] = 262;
		freqJP[33][27] = 261;
		freqJP[31][38] = 260;
		freqJP[19][79] = 259;
		freqJP[4][31] = 258;
		freqJP[4][66] = 257;
		freqJP[17][32] = 256;
		freqJP[26][67] = 255;
		freqJP[16][30] = 254;
		freqJP[26][46] = 253;
		freqJP[24][26] = 252;
		freqJP[35][10] = 251;
		freqJP[18][37] = 250;
		freqJP[3][19] = 249;
		freqJP[33][69] = 248;
		freqJP[31][9] = 247;
		freqJP[45][29] = 246;
		freqJP[3][15] = 245;
		freqJP[18][54] = 244;
		freqJP[3][44] = 243;
		freqJP[31][29] = 242;
		freqJP[18][45] = 241;
		freqJP[38][28] = 240;
		freqJP[24][12] = 239;
		freqJP[35][82] = 238;
		freqJP[17][43] = 237;
		freqJP[28][9] = 236;
		freqJP[23][25] = 235;
		freqJP[44][37] = 234;
		freqJP[23][75] = 233;
		freqJP[23][92] = 232;
		freqJP[0][24] = 231;
		freqJP[19][74] = 230;
		freqJP[45][32] = 229;
		freqJP[16][72] = 228;
		freqJP[16][93] = 227;
		freqJP[45][13] = 226;
		freqJP[24][8] = 225;
		freqJP[25][47] = 224;
		freqJP[28][26] = 223;
		freqJP[43][81] = 222;
		freqJP[32][71] = 221;
		freqJP[18][41] = 220;
		freqJP[26][62] = 219;
		freqJP[41][24] = 218;
		freqJP[40][11] = 217;
		freqJP[43][57] = 216;
		freqJP[34][53] = 215;
		freqJP[20][32] = 214;
		freqJP[34][43] = 213;
		freqJP[41][91] = 212;
		freqJP[29][57] = 211;
		freqJP[15][43] = 210;
		freqJP[22][89] = 209;
		freqJP[33][83] = 208;
		freqJP[43][20] = 207;
		freqJP[25][58] = 206;
		freqJP[30][30] = 205;
		freqJP[4][56] = 204;
		freqJP[17][64] = 203;
		freqJP[23][0] = 202;
		freqJP[44][12] = 201;
		freqJP[25][37] = 200;
		freqJP[35][13] = 199;
		freqJP[20][30] = 198;
		freqJP[21][84] = 197;
		freqJP[29][14] = 196;
		freqJP[30][5] = 195;
		freqJP[37][2] = 194;
		freqJP[4][78] = 193;
		freqJP[29][78] = 192;
		freqJP[29][84] = 191;
		freqJP[32][86] = 190;
		freqJP[20][68] = 189;
		freqJP[30][39] = 188;
		freqJP[15][69] = 187;
		freqJP[4][60] = 186;
		freqJP[20][61] = 185;
		freqJP[41][67] = 184;
		freqJP[16][35] = 183;
		freqJP[36][57] = 182;
		freqJP[39][80] = 181;
		freqJP[4][59] = 180;
		freqJP[4][44] = 179;
		freqJP[40][54] = 178;
		freqJP[30][8] = 177;
		freqJP[44][30] = 176;
		freqJP[31][93] = 175;
		freqJP[31][47] = 174;
		freqJP[16][70] = 173;
		freqJP[21][0] = 172;
		freqJP[17][35] = 171;
		freqJP[21][67] = 170;
		freqJP[44][18] = 169;
		freqJP[36][29] = 168;
		freqJP[18][67] = 167;
		freqJP[24][28] = 166;
		freqJP[36][24] = 165;
		freqJP[23][5] = 164;
		freqJP[31][65] = 163;
		freqJP[26][59] = 162;
		freqJP[28][2] = 161;
		freqJP[39][69] = 160;
		freqJP[42][40] = 159;
		freqJP[37][80] = 158;
		freqJP[15][66] = 157;
		freqJP[34][38] = 156;
		freqJP[28][48] = 155;
		freqJP[37][77] = 154;
		freqJP[29][34] = 153;
		freqJP[33][12] = 152;
		freqJP[4][65] = 151;
		freqJP[30][31] = 150;
		freqJP[27][92] = 149;
		freqJP[4][2] = 148;
		freqJP[4][51] = 147;
		freqJP[23][77] = 146;
		freqJP[4][35] = 145;
		freqJP[3][13] = 144;
		freqJP[26][26] = 143;
		freqJP[44][4] = 142;
		freqJP[39][53] = 141;
		freqJP[20][11] = 140;
		freqJP[40][33] = 139;
		freqJP[45][7] = 138;
		freqJP[4][70] = 137;
		freqJP[3][49] = 136;
		freqJP[20][59] = 135;
		freqJP[21][12] = 134;
		freqJP[33][53] = 133;
		freqJP[20][14] = 132;
		freqJP[37][18] = 131;
		freqJP[18][17] = 130;
		freqJP[36][23] = 129;
		freqJP[18][57] = 128;
		freqJP[26][74] = 127;
		freqJP[35][2] = 126;
		freqJP[38][58] = 125;
		freqJP[34][68] = 124;
		freqJP[29][81] = 123;
		freqJP[20][69] = 122;
		freqJP[39][86] = 121;
		freqJP[4][16] = 120;
		freqJP[16][49] = 119;
		freqJP[15][72] = 118;
		freqJP[26][35] = 117;
		freqJP[32][14] = 116;
		freqJP[40][90] = 115;
		freqJP[33][79] = 114;
		freqJP[35][4] = 113;
		freqJP[23][33] = 112;
		freqJP[19][19] = 111;
		freqJP[31][41] = 110;
		freqJP[44][1] = 109;
		freqJP[22][56] = 108;
		freqJP[31][27] = 107;
		freqJP[32][18] = 106;
		freqJP[27][32] = 105;
		freqJP[37][39] = 104;
		freqJP[42][11] = 103;
		freqJP[29][71] = 102;
		freqJP[32][58] = 101;
		freqJP[46][10] = 100;
		freqJP[17][30] = 99;
		freqJP[38][15] = 98;
		freqJP[29][60] = 97;
		freqJP[4][11] = 96;
		freqJP[38][31] = 95;
		freqJP[40][79] = 94;
		freqJP[28][49] = 93;
		freqJP[28][84] = 92;
		freqJP[26][77] = 91;
		freqJP[22][32] = 90;
		freqJP[33][17] = 89;
		freqJP[23][18] = 88;
		freqJP[32][64] = 87;
		freqJP[4][6] = 86;
		freqJP[33][51] = 85;
		freqJP[44][77] = 84;
		freqJP[29][5] = 83;
		freqJP[46][25] = 82;
		freqJP[19][58] = 81;
		freqJP[4][46] = 80;
		freqJP[15][71] = 79;
		freqJP[18][58] = 78;
		freqJP[26][45] = 77;
		freqJP[45][66] = 76;
		freqJP[34][10] = 75;
		freqJP[19][37] = 74;
		freqJP[33][65] = 73;
		freqJP[44][52] = 72;
		freqJP[16][38] = 71;
		freqJP[36][46] = 70;
		freqJP[20][26] = 69;
		freqJP[30][37] = 68;
		freqJP[4][58] = 67;
		freqJP[43][2] = 66;
		freqJP[30][18] = 65;
		freqJP[19][35] = 64;
		freqJP[15][68] = 63;
		freqJP[3][36] = 62;
		freqJP[35][40] = 61;
		freqJP[36][32] = 60;
		freqJP[37][14] = 59;
		freqJP[17][11] = 58;
		freqJP[19][78] = 57;
		freqJP[37][11] = 56;
		freqJP[28][63] = 55;
		freqJP[29][61] = 54;
		freqJP[33][3] = 53;
		freqJP[41][52] = 52;
		freqJP[33][63] = 51;
		freqJP[22][41] = 50;
		freqJP[4][19] = 49;
		freqJP[32][41] = 48;
		freqJP[24][4] = 47;
		freqJP[31][28] = 46;
		freqJP[43][30] = 45;
		freqJP[17][3] = 44;
		freqJP[43][70] = 43;
		freqJP[34][19] = 42;
		freqJP[20][77] = 41;
		freqJP[18][83] = 40;
		freqJP[17][15] = 39;
		freqJP[23][61] = 38;
		freqJP[40][27] = 37;
		freqJP[16][48] = 36;
		freqJP[39][78] = 35;
		freqJP[41][53] = 34;
		freqJP[40][91] = 33;
		freqJP[40][72] = 32;
		freqJP[18][52] = 31;
		freqJP[35][66] = 30;
		freqJP[39][93] = 29;
		freqJP[19][48] = 28;
		freqJP[26][36] = 27;
		freqJP[27][25] = 26;
		freqJP[42][71] = 25;
		freqJP[42][85] = 24;
		freqJP[26][48] = 23;
		freqJP[28][15] = 22;
		freqJP[3][66] = 21;
		freqJP[25][24] = 20;
		freqJP[27][43] = 19;
		freqJP[27][78] = 18;
		freqJP[45][43] = 17;
		freqJP[27][72] = 16;
		freqJP[40][29] = 15;
		freqJP[41][0] = 14;
		freqJP[19][57] = 13;
		freqJP[15][59] = 12;
		freqJP[29][29] = 11;
		freqJP[4][25] = 10;
		freqJP[21][42] = 9;
		freqJP[23][35] = 8;
		freqJP[33][1] = 7;
		freqJP[4][57] = 6;
		freqJP[17][60] = 5;
		freqJP[25][19] = 4;
		freqJP[22][65] = 3;
		freqJP[42][29] = 2;
		freqJP[27][66] = 1;
		freqJP[26][89] = 0;
	}
}
