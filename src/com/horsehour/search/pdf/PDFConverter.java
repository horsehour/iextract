package com.horsehour.search.pdf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Convert .pdf to .xml or .html with pdftohtml.exe
 * @author Chunheng Jiang
 * @version 1.0
 * @since 1st July 2011
 */
public class PDFConverter {
	public static void pdf2html(String pdf, String html){
		pdf = pdf.replaceAll(" ", "").trim();
		html = html.replaceAll(" ", "").trim();
		String cmd = Parameter.PDF2HTML + " -c -i -noframes " + pdf + " " + html;

		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void pdf2html(String pdf){
		File src = new File(pdf);
		String name = new File(pdf).getName(), parent = src.getParent(), dest = parent + "/" + name.substring(0, name.length() - ".pdf".length());
		pdf2html(pdf, dest);
	}

	public static void pdf2xml(String pdf, String xml){
		pdf = pdf.replaceAll(" ", "").trim();
		xml = xml.replaceAll(" ", "").trim();
		String cmd = Parameter.PDF2HTML + " -xml " + pdf + " " + xml;
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void pdf2xml(String pdf){
		File src = new File(pdf);
		String name = new File(pdf).getName(), parent = src.getParent(), dest = parent + "/" + name.substring(0, name.length() - ".pdf".length());
		pdf2xml(pdf, dest);
	}

	public static void pdf2xmlBatch(String parent){
		for (File file : FileUtils.listFiles(new File(parent), null, false))
			if (file.getName().endsWith(".pdf"))
				pdf2xml(file.toString());
	}

	public static void pdf2htmlBatch(String parent){
		for (File file : FileUtils.listFiles(new File(parent), null, false))
			if (file.getName().endsWith(".pdf"))
				pdf2html(file.toString());
	}
}