package ai.checkpdf.user;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.DemoRule;
import org.languagetool.rules.RuleMatch;

import ai.checkpdf.TextProccess;
import ai.checkpdf.rules.*;


public class UserInterface extends Basic {
	/**
	 * This will convert a inputFilename to its outputFilepath
	 * 
	 * @param name
	 *            The name of inputFile
	 * @return filePath of the outpuFile
	 */
	private String convertPDFname(String name) {
		StringBuilder builder = new StringBuilder(name);
		return (builder.insert(name.length() - 4, "-out")).toString();
	}
	
	private String convertTextname(String name) {
		StringBuilder builder = new StringBuilder(name.substring(0, name.length() - 4));
		return (builder.append(".txt")).toString();
	}
	/*
	 * This will proofread a text according to its style;
	 */
	private void text_proccess(String spellingoption, String text)
	{
		JLanguageTool tool = null;
		tool = new JLanguageTool(new AmericanEnglish());
		tool.addRule(new DemoRule());
		tool.addRule(new Ai_AandB());
		tool.addRule(new Ai_PassiveTenseRule());
		tool.addRule(new Ai_ABCetc());
		//tool.addRule(new Ai_ABYZ());
		tool.addRule(new Ai_Not());
		tool.addRule(new Ai_Cannot());
		tool.addRule(new Ai_Respectively());
		tool.addRule(new Ai_Etc());
		List<RuleMatch> matches = null;
		try{
			matches = tool.check(text, true, ParagraphHandling.NORMAL);
			for(RuleMatch match: matches)
			{
				System.out.println(match.getRule().getId()+ ":" +match.getFromPos() + "," + match.getToPos() + match.getMessage() + ";" + match.getSuggestedReplacements());
			}
		}
		
		catch(Exception ee){
		}
	}
	/*
	 * This will extract the strings and their locations in pdf, and match the
	 *highlight and add annotations, or proofread the pdf , highlight and add annotations .
	 */
	private void pdf_proccess(String inputfilePath, String spellingoption)
	{
		File inputfile = new File(inputfilePath);
		String outputPDFfilename = convertPDFname(inputfile.getName());
		String outputPDFfilePath = getOutputPath(outputPDFfilename);
		try {
			TextProccess proccessor = new TextProccess(inputfile, outputPDFfilePath);
			proccessor.extract();
			proccessor.check(spellingoption);
			proccessor.saveclose();
		} catch (Exception ee) {
		}
	}
	private void extract_proccess(String inputfilePath)
	{
		File inputfile = new File(inputfilePath);
		String outputTextfilename = convertTextname(inputfile.getName());
		String outputTextfilePath = getOutputPath(outputTextfilename);
		try {
			TextProccess proccessor = new TextProccess(inputfile, null);
			proccessor.extract();
			proccessor.writeto(outputTextfilePath);
			proccessor.close();
		} catch (Exception ee) {
		}
	}
	private void regex_proccess(String inputfilePath,Pattern pattern) {
		File inputfile = new File(inputfilePath);
		String outputPDFfilename = convertPDFname(inputfile.getName());
		String outputPDFfilePath = getOutputPath(outputPDFfilename);
		try {
			TextProccess proccessor = new TextProccess(inputfile,outputPDFfilePath);
			proccessor.extract();
			proccessor.match(pattern);
			proccessor.saveclose();
		} catch (Exception ee) {
		}
	}

	public void run() {
		prompt("checkpdf: read pdfs in .\\pdf file , and output results in .\\result file");
		Map<String, String> options = new HashMap<String, String>();
		options.put("0", "Mode0: regex search engine");
		options.put("1", "Mode1: proofreading of a text");
		options.put("2", "Mode2: proofreading of a pdf");
		options.put("3", "Mode3: extract plain text");
		options.put("4", "Exit");
		Map<String, String> modifier_options = new HashMap<String, String>();
		modifier_options.put("0", "default");
		modifier_options.put("1", "CANON_EQ: Enables canonical equivalence.");
		modifier_options.put("2", "CASE_INSENSITIVE: Enables case-insensitive matching.");
		modifier_options.put("3", "COMMENTS: Permits whitespace and comments in pattern.");
		modifier_options.put("4", "DOTALL: Enables dotall mode.");
		modifier_options.put("5", "LITERAL: Enables literal parsing of the pattern.");
		modifier_options.put("6", "MULTILINE: Enables multiline mode.");
		modifier_options.put("7", "UNICODE_CASE: Enables Unicode-aware case folding.");
		modifier_options.put("8", "UNICODE_CHARACTER_CLASS: Enables the Unicode version of Predefined character classes and POSIX character classes.");
		modifier_options.put("9", "UNIX_LINES: Enables Unix lines mode.");
		Map<String, Integer> modifier_optionstoint = new HashMap<String, Integer>();
		modifier_optionstoint.put("1", Pattern.CANON_EQ);
		modifier_optionstoint.put("2", Pattern.CASE_INSENSITIVE);
		modifier_optionstoint.put("3", Pattern.COMMENTS);
		modifier_optionstoint.put("4", Pattern.DOTALL);
		modifier_optionstoint.put("5", Pattern.LITERAL);
		modifier_optionstoint.put("6", Pattern.MULTILINE);
		modifier_optionstoint.put("7", Pattern.UNICODE_CASE);
		modifier_optionstoint.put("8", Pattern.UNICODE_CHARACTER_CLASS);
		modifier_optionstoint.put("9", Pattern.UNIX_LINES);
		Map<String, String> spellingoptions = new HashMap<String, String>();
		spellingoptions.put("0", "Close spelling check of upperclass word");
		spellingoptions.put("1", "Close spelling check");
		spellingoptions.put("2", "Open spelling check");
		while (true) {
			String mode = promptChoice(options, "Please select:");
			if (mode.equals("0")) {
				String inputfilePath = promptFileChoice("Please select a PDF file");
				String patterntype = promptChoice(modifier_options, "Please select a modifier type:");
				String stringpattern = promptChoice("Please input a regular expression:");
				Pattern pattern = null;
				if (patterntype.equals("0")) {
					pattern = Pattern.compile(stringpattern);
				} else {
					pattern = Pattern.compile(stringpattern, modifier_optionstoint.get(patterntype));
				}
				regex_proccess(inputfilePath, pattern);
			}else if(mode.equals("1")){
				String spellingoption = promptChoice(spellingoptions, "Please select spelling check mode:");
				String text = promptChoice("Please input a text:");
				text_proccess(spellingoptions.get(spellingoption), text);
			}
			else if (mode.equals("2")) {
				String inputfilePath = promptFileChoice("Please select a PDF file");
				String spellingoption = promptChoice(spellingoptions, "Please select spelling check mode:");
				pdf_proccess(inputfilePath, spellingoptions.get(spellingoption));
			} 
			else if(mode.equals("3")){
				String inputfilePath = promptFileChoice("Please select a PDF file");
				extract_proccess(inputfilePath);
			}
			else {
				break;
			}
		}
	}

	public static void main(String[] args) {
		UserInterface userinterface = new UserInterface();
		userinterface.initialize("pdf", "result");
		userinterface.run();
	}
}

