package com.horsehour.search.pdf;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 1st 1 2016 am 11:58:50
 **/
public enum Feature {
	LINENUM(0), PAGEWIDTH(1), PAGEHEIGHT(2), LEFT(3), TOP(4), WIDTH(5), HEIGHT(6), FONTID(7), FONTSIZE(
	        8), MAXMINFONT(9), BOLD(10), ITALIC(11), BEGINCHAR(12), ENDCHAR(13), BEGINWORDICL(14), ENDWORDICL(
	        15), LENGTH(16), ALPHACOUNT(17), UPPERCASECOUNT(18), DIGITCOUNT(19), NONALNUMCOUNT(20), COMMACOUNT(
	        21), PERIODCOUNT(22), WORDCOUNT(23), SENTENCEBREAK(24), TYPE(25), TITLEMINUS(26), AUTHORMINUS(
	        27), AFFILIATION(28), CONTACT(29), ALIGNMENT(30), MARGIN(31), SUBHEAD(32), TYPESCORE(33), TITLESCORE(
	        34), AUTHORSCORE(35), AFFILIATIONSCORE(36), CONTACTSCORE(37), BODYSCORE(38), FIELDID(39);

	private int id;

	Feature(int id) {
		this.id = id;
	}

	public int id(){
		return id;
	}
}
