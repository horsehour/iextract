package com.horsehour.search.pdf;

/**
 * To extract meta data, there are two primary steps we have to 
 * complete: extract abundant features from text line, and create a
 * well-defined (dis-)similarity metric measure for clustering. It 
 * is common to cluster data into different blocks, however, meta 
 * data in pdf is quite different from general data. Firstly, lines 
 * in pdf have organized structure with clear order. For instance, 
 * most research papers have their title located at the first several
 * lines and be followed by the authorship information with authors'
 * names and their institutions or even contact information(e.g., 
 * phone numbers and email). 
 * <p> Therefore, those lines which are placed closely should be defined
 * as closest neighbors in term of distance measure. The closeness could
 * also been strengthened by other useful signals or features which have
 * been extracted from source text.
 * <p> The density peaks algorithm proposed by Rodriguez, A. and Laio, A
 * is excellent in clustering. An information extraction method will be 
 * designed to parse out the meta data of a scientific articles. The 
 * scope is limited to extract title and authorship information from 
 * academic articles written in English.   
 * <p> The method includes primary four steps as follwing: 
 * <li> Feature Extraction: extract abundant signals of text line to 
 * distinguish text from different fields. The features should be extracted
 * based on both single-line and mutiple-line mode. Our method makes use
 * of the last or the next line to construct the feature space.
 * <li> Dissimilarity Measure Definition: the measure is defined based
 * on the assumption that close lines located closely than those separated.
 * The property is determined by the layout of a research paper. All meta
 * fields are organized in order. Certainly, there is an exception. It is
 * the authorship field, where authors may scattered among other related
 * information, such as affiliation of authors, footnotes, contact informa-
 * tion. The time complexity to build dissimilarity matrix is linear, because
 * we define the distance of two text lines by accumulate the distance values 
 * between all cloest neighbors' among the two text lines.
 * <li> Clustering: the DP algorithm is applied to cluster text lines.
 * <li> Scoring: calculate the scores of fields for each text line to stand
 * out their membership. It is crude however crucial to figure out the field
 * pivotal signals help us to tell their distinct characterisitcs. Therefore,
 * with the result of clustering, they would be extremly helpful to mark the 
 * boundary of each field.
 * @author Chunheng Jiang
 * @version 1.0
 * @since Aug 4th 2011
 */
public class Parameter {
	// directory
	public static final String ARXIV = "./data/arXiv";
	public static final String PDF2HTML = "./lib/pdftohtml.exe";

	// overall page level
	public static final int PAGEWIDTH = 918;
	public static final int PAGEHEIGHT = 1188;
	public static final float TOTALSCORE = 100.0F;

	// overall line level
	public static final int ALLOWED_MIN_TXT_LEN = 5;
	public static final int MAX_DIGITBLOCK_SIZE = 500;
	public static final int MIN_LINENUM_PER_PAGE = 5;
	public static final int MAX_MARGIN = 500;
	public static final int MAX_ABNORMAL_MARGIN = -50;
	public static final int MAX_LEN_MATCH_ABS = 15;// max length to match
	// 'abstract'/'summary'
	public static final int MAX_FONTSIZE = 10;// default max font size can be
	                                          // set
	public static final int THIN_MARGIN = 20;
	public static final int EFFECT_LINENUM_TITLE = 3;// effective line num in
	                                                 // title

	// alpha
	public static final float ALPHA_WIDTH_TITLE = 0.5F;// percentage of width in
	                                                   // title
	public static final float ALPHA_WIDTH_ABSTRACT = 0.6F;// percentage of width
	                                                      // in
	// abstract
	public static final float ALPHA_HEIGHT_ABSTRACT = 0.3F;// percentage of top
	                                                       // to
	// page height
	public static final float ALPHA_PERIOD_LINE = 0.5F;// percentage of periods
	                                                   // in line

	// boost
	public static final int BOOST_PERIOD_AUTHOR = 5;// boost of period weight in
	                                                // author
	public static final int BOOST_COMMA_AUTHOR = 2;// boost of comma weight in
	                                               // author

	// boost of percentage top/pagehegiht in abstract
	public static final int BOOST_TOP_TO_HEIGHT_ABS = 5;

	// boost of percentage top/pageHeight in keywords zone
	public static final int BOOST_TOP_TO_HEIGHT_KEY = 3;
	public static final int BOOST_CONTACT_NEGATIVE = -2;// Negative boost of
	                                                    // contact
	// in abstract
	public static final int BOOST_PERIOD_NEGATIVE = -1;// Negative boost of
	                                                   // period in
	// abstract

	// block digits len
	public static final int CON_DIGIT_TITLE = 1;// Consecutive digits in title
	public static final int CON_DIGIT_AUTHOR = 2;// Consecutive digits in author
	public static final int CON_DIGIT_NOISE = 3;// Consecutive digits in noise
	                                            // zone
	public static final int CON_DIGIT_KEYWORDS = 1;// Consecutive digits in
	                                               // keywords
	// zone

	/************************************************************************/
	/* The score list: lists all the factors which can add/minus scores */
	/************************************************************************/
	public static final float WT_BASELINE = 10.0F;// Baseline weight for all
	                                              // zones
	public static final float WT_ABS_KEYWORDS = 10.0F;// If keywords
	// 'abstract'/'summary' appears
	public static final float WT_COLON_FOLLOW_ABS = 10.0F;// If a colon follows
	// 'abstract'/'summary'
	public static final float WT_POSTFIX_ING = 1.0F;// '-ing' postfix
	public static final float WT_THIN_MARGIN = 2.0F;// If margin less than
	                                                // THIN_MARGIN
	public static final float WT_ALPHA_WIDTH_TITLE = 1.0F;// If title's width
	                                                      // larger
	// than ALPHA*PAGEWIDTH
	public static final float WT_ALPHA_PERIOD = 5.0F;// If the line contains
	                                                 // large
	// period
	public static final float WT_INIT = 5.0F;// Initial contributes weight
	public static final float WT_INIT_FOLLOW_AND = 2.0F;// If initial follows
	                                                    // 'and'
	// Prepostions in PREP_FILTER_AUTHOR bring negative weight
	public static final float WT_PREP_NEGATIVE = -10.0F;
	public static final float WT_CONTACT = 10.0F;// If line contains just
	                                             // contact
	// information
	// If line contains contact information and block digit
	public static final float WT_CONTACT_BLOCKDIGIT = 20.0F;
	// If the position is appreciated:top>alpha_height_abstract*pageHeight
	// and width >alpha_width_abstract*pageWidth
	public static final float WT_POSITION_ABSTRACT = 2.0F;

	// Balance
	public static final float WT_TITLE_AT_STATUS_0 = 5.0F;// If at status
	                                                      // 0,balance
	// title
	public static final float WT_AUTHOR_AT_STATUS_0 = 1.0F;// If at status
	                                                       // 0,balance
	// author
	public static final float WT_TITLE_AT_STATIS_1 = 1.0F;
	public static final float WT_AUTHOR_AT_STATUS_1 = 5.0F;
	public static final float WT_AUTHOR_AT_STATUS_2 = 1.0F;
	public static final float WT_ABS_AT_STATUS_2 = 5.0F;

	// Order id
	public static final float WT_BOOST = 10.0F;
	public static final int SCORE_NUM = 5;// number of score in Score struct
	public static final int POSTION_NUM = 5;// position number the code contains

	public static final String CONTACT_WORDS[] = {"universi", "department", "institut", "laborato",
	        "collaboration", "departament", "division", "center", "college", "mail", "phone:",
	        "tel.", "fax", "@", "www.", "http:", "school", "dipartim", "journal", "dept",
	        "technical report", "corporation", "all rights reserved", "copyright", "microsoft",
	        "research group"};

	public static final String[] BIBBOUNDSIGNALS = {"thank", "grate", "grant", "acknowle"};
	public static final String[] APPENDIXSIGNALS = {"fig.", "figure", "table", "appendix"};
	public static final String[] AFFILIATIONSIGNALS = {"universi", "department", "dipartim",
	        "departament", "dept", "institut", "laborato", "division", "center", "college",
	        "school", "corp"};
	public static final String[] TYPESIGNALS = {"technical", "report", "journal", "conference",
	        "proceedings"};
	public static final String[] CONTACTSIGANALS = {"email", "mail", "phone", "tel", "fax", "www",
	        "http", "edu", "org", "com"};
	public static final String[] TITLEPOISON = {"we", "they", "it", "this", "should", "will",
	        "then", "which", "where", "wish", "hope"};
	public static final String[] AUTHORPOISON = {"of", "in", "for", "the"};

	public static final String[] MONTH = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
	        "Sep", "Oct", "Nov", "Dec"};
}