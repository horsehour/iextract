package com.horsehour.search.pdf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since July 11 2015 PM 7:15:32
 * @see Accurate Information Extraction from Research Papers using Conditional
 *      Random Fields
 * @see http://csxstatic.ist.psu.edu/about/scholarly-information-extraction
 * @see http://www.dlib.org/dlib/july12/kern/07kern.html
 **/
public class RefExtractor {
	public float searchRange = 0.2F;// 文档尾部20%

	public List<Reference> extract(String xmlFile){
		List<Reference> refList = new ArrayList<Reference>();
		return refList;
	}

	/**
	 * extract one bibliography entry
	 * 
	 * @param line
	 * @return
	 */
	public Reference extractBib(String line){
		return null;
	}

	public int detectBoundary(List<String> lines){

		return -1;
	}

	// //Starts the detection
	// void ReferDetector::run(){
	// for(size_t i = 0;i < srcFiles.size();i++)
	// setDetectRange(srcFiles[i]);
	// }
	//
	// //Sets detection range for efficiency,
	// //default for example,[1/2,1]
	// void ReferDetector::setDetectRange(string &srcFile){
	//
	// ifstream input(srcFile.c_str(),ios_base::in);
	//
	// if(!input){
	// cerr<<"Can't open the file!"<<endl;
	// return;
	// }
	//
	// input.seekg(0,ios_base::end);
	//
	// int fsz = input.tellg();
	// int curr =(int)floor((float)fsz/2);//binary search to the second part
	//
	// input.seekg(curr);//set off
	//
	// searchGuider(input,srcFile,curr);//mark the curr position
	//
	// input.close();
	// }
	//
	// //If fails to search ref header,keeps the srcFile in TodoFile,else stores
	// //results to CitMap
	// void ReferDetector::searchGuider(ifstream &input,string &srcFile,int
	// prePos){
	// bool isRefHead(false);
	// vector<string> entries;
	//
	// isRefHead = detectRefHead(input);
	//
	// cout<<srcFile<<endl;
	//
	// //extract entries
	// if(isRefHead){
	// extractBibli(input,entries);
	// //extractRefEntry(input,entries);
	// }else{
	// if(input.fail()) input.clear();//clear status:fail
	// input.seekg(prePos);
	// //if(detectRefContext(input))//is it necessary?
	//
	// extractBibli(input,entries);
	// //extractRefEntry(input,entries);
	// }
	//
	// //output
	// if(entries.size()>0){
	// ofstream output(citMapFile.c_str(),ios_base::app);
	// output<<srcFile<<endl;
	// appendEntries(entries,output);
	// output.close();
	// }else{
	// ofstream output(todoFile.c_str(),ios_base::app);
	// output<<srcFile<<endl;
	// output.close();
	// }
	// }
	//
	// //Makes use of context keywords to detect the begining line of
	// //bibliography,if there are NO keywords'reference,bibliography'
	// bool ReferDetector::detectRefContext(ifstream &input){
	// bool findHead(false);
	// string line("");
	// while(!findHead && getline(input,line)){
	// if(line[0] != '<'||line.size() < TXT_TAG_LEN) continue;
	//
	// extractTxt(line);
	//
	// if(line.size() >= 30 && (line.find("thank") != string::npos
	// ||line.find(" grate") != string::npos
	// ||line.find("grant") != string::npos
	// ||line.find("acknowle") != string::npos))
	//
	// findHead = true;
	// }
	// return findHead;
	// }
	// /*Detects the top bound of references*/
	// bool ReferDetector::detectRefHead(ifstream &input){
	// bool findHead(false);
	// string line("");
	// while(!findHead && getline(input,line)){
	// if(line[0] != '<'||line.size() < TXT_TAG_LEN) continue;
	// //make sure it's a text line
	// extractTxt(line);
	// if(9 <= line.size()&&line.size() < 30){
	// findHead = headMatcher(line);
	// }
	// }
	// return findHead;
	// }
	// //Matches the keywords in text with
	// "references,bibliography(fia),literatur"
	// bool ReferDetector::headMatcher(string &line){
	// bool ret (false);
	//
	// transform(line.begin(),line.end(),line.begin(),tolower);
	//
	// if(line.find("references") != string::npos
	// ||line.find("bibliogra") != string::npos
	// ||line.find("literatur") != string::npos)
	//
	// ret = true;
	//
	// return ret;
	// }
	//
	// //checkGate for preprocessing,make sure the first line is acurate
	// //return : check Status(0,1,2,3,4)
	// int ReferDetector::checkGate(ifstream &input,string &entry,int &orderId){
	// int status(0);
	// string shortLine("");
	// while(!status && !orderId && !input.eof()){
	// getline(input,entry);
	// extractTxt(entry);
	// polishEntry(entry);//rm noise from entry
	// if(!shortLine.empty()){
	// entry = shortLine + " " + entry;
	// }
	// char ch = entry[0];
	// if(0x61 <= ch) continue;
	// if(entry.size() < 20){
	// shortLine = entry;
	// continue;
	// }else shortLine = "";
	// //finite states
	// //status = 1,2
	// int pos(0) ;
	// if(ch == '[' && (pos = entry.find(']')) != string::npos){
	// string str = entry.substr(1,pos-1);
	// int digi = atoi(str.c_str());
	// if(digi == 1){status = 1;orderId = 1;}
	// else if(!digi&&isInitial(entry.substr(pos))) status = 2;
	// }
	// //status 3/4 must be controled strickly
	// //status = 3
	// if(atoi(entry.c_str()) == 1 && isInitial(entry)){
	// if(countCommaAndPeriod(entry,1) >= 2){
	// status = 3;
	// orderId = 1;
	// }
	// }
	// //status = 4
	// if(0x41 <= ch && ch <= 0x5A ){
	// if(countCommaAndPeriod(entry,1) >= 3 && effectiveComma(entry)
	// &&effectivePeriod(entry)){
	// status = 4;
	// }
	// }//Todo:multi-entries mixed,year is a useful information
	// }
	// return status;
	// }
	//
	// //Extracts references entries from text
	// void ReferDetector::extractRefEntry(ifstream &input,vector<string>
	// &entries){
	// string preLine(""),line("");
	// int preDigit(0),status(0);
	// status = checkGate(input,preLine,preDigit);
	// if(!status||preLine.empty()) return;//if fail to find first entry,exit
	//
	// while(getline(input,line)){
	// extractTxt(line);
	// polishEntry(line);//remove keywords such as fig. table
	// if(line=="") continue;
	// int flag = checkStatus(line,preDigit);
	// if(status == flag){//status={1,2,3,4}
	// entries.push_back(preLine);
	// preLine = line;
	// }else{
	// preLine.append(" ");
	// preLine += line;
	// }
	// }
	//
	// //check if it's abnormal,maybe it have reach to the end
	// //then remove noise from this part
	//
	// preLine = preLine.substr(0,5*NORMAL_LEN);
	// entries.push_back(preLine);
	// }
	//
	// //check the status,like "[1]" at 1; "[borl98]" at 2; "1."or"1 " at 3
	// //"Cottrell," at 4(here,the author's first letter is upper,and
	// //has effective period(.),like A. L.)
	// int ReferDetector::checkStatus(string &line,int &preDigit){
	// size_t len = line.size();
	// if(len > 1 && line[0]== '['){
	// string str = line.substr(1,len-1);
	// int currDigit = atoi(str.c_str());
	// if(currDigit > 0 && isOrderFine(currDigit,preDigit)){
	// return 1;
	// }
	// //make sure the frist alpha out of [] is upper
	// unsigned char ch = (unsigned)str[0];
	// if(isalpha(ch)){
	// size_t pos = str.find(']');
	// if(pos != string::npos){
	// //if(isInitial(str.substr(pos+1)))//improve
	// return 2;
	// }else
	// return 0;
	// }
	// }
	// int currDigit = atoi(line.c_str());
	// if(currDigit > 0 && isOrderFine(currDigit,preDigit)){
	// if(isInitial(line)) return 3;
	// }
	// if(0x41 <= line[0] && line[0] <= 0x5A && line.size() >= 10){
	// size_t pos = line.find(0x20);
	// if(pos != string::npos &&
	// effectivePeriod(line.substr(pos))&&
	// effectiveComma(line))
	// return 4;
	// }
	// return 0;
	// }
	//
	// //Checks the digits order is fine
	// bool ReferDetector::isOrderFine(int currDigit,int &preDigit){
	// if(currDigit - preDigit == 1){
	// preDigit = currDigit;
	// return true;
	// }else
	// return false;
	// }
	//
	// //The period in the line is effective period(.)
	// //if the nearest last alpha from period is upper
	// bool ReferDetector::effectivePeriod(string &line){
	// bool ret(false);
	// size_t len = line.size();
	// for(size_t i = 2;i<len-1;i++){
	//
	// if(line[i] == '.'&&0x41 <= line[i-1]&&line[i-1] <= 0x5A)
	// ret = true;
	//
	// else if(line[i] == '.'&&line[i+1] == '.')
	// break;
	//
	// }
	// return ret;
	// }
	//
	// //If the first commas' last neighour word's head char is upper
	// //then return true
	// bool ReferDetector::effectiveComma(string &line){
	// XMLParser parser;
	// vector<string> subs;
	// size_t pos = line.find(",");
	// if(pos != string::npos){
	// line = line.substr(0,pos);
	// parser.trim(line);
	// parser.split(line,subs);
	// }
	// if(subs.size()>0){
	// string lastSub = subs[subs.size()-1];
	// char ch = lastSub[0];
	// if(0x41 <= ch&&ch <=0x5A) return true;
	// }
	// return false;
	// }
	//
	// //Counts number of period and comma
	// int ReferDetector::countCommaAndPeriod(string &line,int pos){
	// if(pos < 0 || (int)line.size()< pos+1) return 0;
	// int count(0);
	// for(int i = pos;i< (int)line.size();i++){
	// if(line[i]==','||line[i]=='.') count++;
	// }
	// return count;
	// }
	//
	// //Checks the first alpha in line whether an initial
	// bool ReferDetector::isInitial(string &line){
	// bool ret(false);
	// for(size_t i = 0;i<line.size();i++){
	// unsigned char ch = (unsigned)line[i];
	// if(isalpha(ch)){
	// if(isupper(ch)) ret = true;
	// else ret = false;
	//
	// break;
	// }
	// }
	// return ret;
	// }
	//
	// //Some entries may be mixed with terms such as 'fig.','table','appendix'
	// //They are the noise information for references
	// void ReferDetector::polishEntry(string &entry){
	// string line(entry);
	// size_t temp = entry.size();
	// set<size_t> rets;
	// transform(entry.begin(),entry.end(),line.begin(),tolower);
	//
	// if((temp = line.find("appendix")) != string::npos) rets.insert(temp);
	// if((temp = line.find("figure ")) != string::npos) rets.insert(temp);
	// if((temp = line.find("fig.")) != string::npos) rets.insert(temp);
	// if((temp = line.find("table")) != string::npos) rets.insert(temp);
	//
	// //get the smallest one
	// if(rets.size()>0) temp = *rets.begin();
	// entry = entry.substr(0,temp);
	// }
	//
	// //Appends entries to ofstream
	// void ReferDetector::appendEntries(vector<string> &entries,ofstream &out){
	// for(size_t i = 0;i<entries.size();i++){
	//
	// if(entries[i] != "")
	// out << "  " << entries[i] << endl;
	// }
	// }
	//
	// //Extracts text content from line
	// void ReferDetector::extractTxt(string &line){
	// XMLParser parser;
	// line = parser.extractTxt(line,">","</t");//get the text;
	// parser.delimTags(line);
	// parser.trim(line);
	// }
	// /************************************************************************/
	// /* Block features can be used for efficiently extraction */
	// /************************************************************************/
	// /*Extracts bibliographies into entries*/
	// void ReferDetector::extractBibli(ifstream &input,vector<string>
	// &entries){
	// CodeVect candid;
	// Code code;
	// extractEntryCandid(input,candid);//extract entry
	// if(candid.size() == 0){
	// cerr<<"Empty Content..."<<endl;
	// return;
	// }
	// string preLine(""),line("");
	// int preDigit(0),status(0);
	//
	// //If the first entry is correctly detected,every thing may be ok
	// status = checkGate(candid,preLine,preDigit);
	//
	// if(!status || preLine.empty()) return;
	//
	// while(candid.size()>0){
	// code = candid.front();
	// candid.erase(candid.begin());//erase the first element
	// line = code.context;
	// polishEntry(line);
	// if(line == "") continue;
	// int flag = checkStatus(line,preDigit);
	// if(status == flag){//status={1,2,3,4}
	// entries.push_back(preLine);
	// preLine = line;
	// }else{
	// preLine.append(" ");
	// preLine += line;
	// }
	// }
	// preLine = preLine.substr(0,5*NORMAL_LEN);
	//
	// if(entries.size()>0)
	// entries.push_back(preLine);
	// }
	// /*Extracts single reference entry from reference*/
	// void ReferDetector::extractEntryCandid(ifstream &input,CodeVect &candid){
	// string line("");
	// XMLParser parser;
	// Code code;
	//
	// while(getline(input,line)){
	// if(line.find("<text") != string::npos){
	// parser.parseCode(line,code,0);
	// if(code.context.size()>0){
	// candid.push_back(code);
	// }
	// }
	// }
	// parser.mergeLines(candid);
	// }
	// //Overloads checkGate function above,but it deals with the codeVect
	// candid
	// int ReferDetector::checkGate(CodeVect &candid,string &entry,int
	// &orderId){
	// int status(0);
	// Code code;
	// while(!status && !orderId && candid.size()>0){
	// code = candid.front();
	// candid.erase(candid.begin());//erase the first element
	// entry = code.context;
	// polishEntry(entry);
	// char ch = entry[0];
	// if(0x61 <= ch||entry.size()<10) continue;
	// int pos(0) ;
	// //status = 1/2
	// if(ch == '[' && (pos = entry.find(']')) != string::npos){
	// string str = entry.substr(1,pos-1);
	// int digi = atoi(str.c_str());
	// if(digi == 1){status = 1;orderId = 1;}
	// else if(!digi&&isInitial(entry.substr(pos))) status = 2;
	// }
	// //status 3/4 must be controlled strickly
	// //status = 3
	// if(atoi(entry.c_str()) == 1 && isInitial(entry)){
	// if(countCommaAndPeriod(entry,1) >= 2){
	// status = 3;
	// orderId = 1;
	// }
	// }
	// //status = 4
	// if(0x41 <= ch && ch <= 0x5A ){
	// if(countCommaAndPeriod(entry,1) >= 3 && effectiveComma(entry)
	// &&effectivePeriod(entry)){
	// status = 4;
	// }
	// }//Todo:multi-entries mixed,year is a useful information
	// }
	// return status;
	//
	// }

}
