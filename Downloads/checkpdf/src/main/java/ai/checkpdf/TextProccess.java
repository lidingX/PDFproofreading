package ai.checkpdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.languagetool.JLanguageTool;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import ai.checkpdf.rules.*;

public class TextProccess extends PDFTextStripper {
	/*
	 * document to read,pattern to match, outputfilePath to save
	 */
	private PDDocument document;
	private String outputfilePath;
	private ArrayList<StringBuffer> stringbufferofPDF = new ArrayList<StringBuffer>();
	private ArrayList<ArrayList<PDRectangle>> rectsofPDF = new ArrayList<ArrayList<PDRectangle>>();
	private static PDColor color = new PDColor(new float[] { (float) 0, (float) 0, (float) 1 }, PDDeviceRGB.INSTANCE);

	public TextProccess(File file, String outputfilePath) throws IOException {
		try {
			document = PDDocument.load(file);
			setSortByPosition(true);
			setStartPage(0);
			setEndPage(document.getNumberOfPages());
		} catch (Exception ee) {
		}
		this.outputfilePath = outputfilePath;
	}

	/**
	 * Override the default functionality of PDFTextStripper.
	 */
	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
		int i = 0;
		int currentpageno = getCurrentPageNo() - 1;
		PDPage page = document.getPage(currentpageno);
		float ph = page.getMediaBox().getUpperRightY();
		for (TextPosition text : textPositions) {
			String character = (text.getUnicode()).substring(0, 1); // a
																	// character
																	// may
																	// contain
																	// more than
																	// two
																	// character
																	// in fact
			PDRectangle position = new PDRectangle();
			position.setLowerLeftX(text.getXDirAdj());
			position.setLowerLeftY(ph - text.getYDirAdj());
			position.setUpperRightX(text.getXDirAdj() + text.getWidthDirAdj());
			position.setUpperRightY(ph - text.getYDirAdj() + 2 * text.getHeightDir());
			(stringbufferofPDF.get(currentpageno)).append(character);
			for (int j = 0; j < character.length(); j++) {
				(rectsofPDF.get(currentpageno)).add(position);
			}
			if (i == ((textPositions.size() - 1))) {
				if (!character.equals("-")) {
					PDRectangle spaceposition = new PDRectangle();
					spaceposition.setLowerLeftX(text.getXDirAdj() + text.getWidthDirAdj());
					spaceposition.setLowerLeftY(ph - text.getYDirAdj());
					spaceposition.setUpperRightX(text.getXDirAdj() + text.getWidthDirAdj() + text.getWidthOfSpace());
					spaceposition.setUpperRightY(ph - text.getYDirAdj() + 2 * text.getHeightDir());
					(stringbufferofPDF.get(currentpageno)).append(" ");
					(rectsofPDF.get(currentpageno)).add(spaceposition);
				}
			}
			i++;
		}
	}

	/**
	 * This will extract all of the pages and the text that is in them. the
	 * character and its location is saved in stringsofPage and rectsofPage.
	 * 
	 */
	public void extract() {
		int pagenum = document.getNumberOfPages();
		for (int i = 0; i < pagenum; i++) {
			stringbufferofPDF.add(new StringBuffer());
			rectsofPDF.add(new ArrayList<PDRectangle>());
		}
		Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
		try {
			writeText(document, dummy);
		} catch (Exception ee) {
		}
	}

	private PDAnnotationTextMarkup composes(PDRectangle position) {
		return composes(position, null);
	}

	private PDAnnotationTextMarkup composes(PDRectangle position, String content) {
		// Now add the markup annotation, a highlight to PDFBox text
		PDAnnotationTextMarkup txtMark = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
		txtMark.setColor(color);
		txtMark.setConstantOpacity(0.2f); // 20% transparent
		// Set the rectangle containing the markup
		txtMark.setRectangle(position);
		// work out the points forming the four corners of the annotations
		// set out in anti clockwise form (Completely wraps the text)
		// OK, the below doesn't match that description.
		// It's what acrobat 7 does and displays properly!
		float[] quads = new float[8];
		quads[0] = position.getLowerLeftX(); // x1
		quads[1] = position.getUpperRightY() - 2; // y1
		quads[2] = position.getUpperRightX(); // x2
		quads[3] = quads[1]; // y2
		quads[4] = quads[0]; // x3
		quads[5] = position.getLowerLeftY() - 2; // y3
		quads[6] = quads[2]; // x4
		quads[7] = quads[5]; // y4

		txtMark.setQuadPoints(quads);
		txtMark.setContents(content);
		return txtMark;
	}

	/**
	 * This will match a pattern in every page, highlight it.
	 * 
	 * @param pattern
	 *            to match
	 * @param content
	 *            to annotate
	 */
	public void match(Pattern pattern) {
		match(pattern, null);
	}

	public void match(Pattern pattern, String content) {
		int pagenum = 0;
		for (StringBuffer stringbufferofPage : stringbufferofPDF) {
			try {
				PDPage page = document.getPage(pagenum);
				List<PDAnnotation> annotations = page.getAnnotations();
				ArrayList<PDRectangle> rectsofPage = rectsofPDF.get(pagenum);
				String stringsofPage = stringbufferofPage.toString();
				Matcher matcher = pattern.matcher(stringsofPage);
				while (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end() - 1;
					PDRectangle startPosition = rectsofPage.get(start);
					PDRectangle endPosition = rectsofPage.get(end);
					if (Math.abs(startPosition.getLowerLeftY() - endPosition.getLowerLeftY()) < 1e-2) {
						PDRectangle chunkPosition = new PDRectangle();
						chunkPosition.setLowerLeftX(startPosition.getLowerLeftX());
						chunkPosition.setLowerLeftY(startPosition.getLowerLeftY());
						chunkPosition.setUpperRightX(endPosition.getUpperRightX());
						chunkPosition.setUpperRightY(endPosition.getUpperRightY());
						annotations.add(composes(chunkPosition, content));
					} else {
						int chunkstart = start;
						for (int chunkend = start; chunkend < end; chunkend++) {

							if (Math.abs((rectsofPage.get(chunkend)).getLowerLeftY()
									- (rectsofPage.get(chunkend + 1)).getLowerLeftY()) >= 1e-2) {
								PDRectangle chunckPosition = new PDRectangle();
								chunckPosition.setLowerLeftX((rectsofPage.get(chunkstart)).getLowerLeftX());
								chunckPosition.setLowerLeftY((rectsofPage.get(chunkstart)).getLowerLeftY());
								chunckPosition.setUpperRightX((rectsofPage.get(chunkend)).getUpperRightX());
								chunckPosition.setUpperRightY((rectsofPage.get(chunkend)).getUpperRightY());
								annotations.add(composes(chunckPosition, content));
								chunkstart = chunkend + 1;
							}
						}
						PDRectangle chunkPosition = new PDRectangle();
						chunkPosition.setLowerLeftX((rectsofPage.get(chunkstart)).getLowerLeftX());
						chunkPosition.setLowerLeftY((rectsofPage.get(chunkstart)).getLowerLeftY());
						chunkPosition.setUpperRightX((rectsofPage.get(end)).getUpperRightX());
						chunkPosition.setUpperRightY((rectsofPage.get(end)).getUpperRightY());
						annotations.add(composes(chunkPosition, content));
					}
				}
			} catch (Exception ee) {

			}
			pagenum++;
		}

	}

	public void writeto(String outputPath) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputPath);
		} catch (Exception ee) {

		}
		for (StringBuffer stringbufferofPage : stringbufferofPDF) {
			String stringsofPage = stringbufferofPage.toString();
			try {
				fos.write(stringsofPage.getBytes());
			} catch (Exception ee) {

			}
		}
		try {
			fos.close();
		} catch (Exception ee) {
		}
	}

	public void check(String spellingoption) {
		JLanguageTool tool = null;
		tool = new JLanguageTool(new AmericanEnglish());
		tool.addRule(new Ai_AandB());
		tool.addRule(new Ai_PassiveTenseRule());
		tool.addRule(new Ai_ABCetc());
		//tool.addRule(new Ai_ABYZ());
		tool.addRule(new Ai_Not());
		tool.addRule(new Ai_Cannot());
		tool.addRule(new Ai_Respectively());
		tool.addRule(new Ai_Etc());
		int pagenum = 0;
		StaticRules.Hasstring.clear();
		for (StringBuffer stringbufferofPage : stringbufferofPDF) {
			try {
				PDPage page = document.getPage(pagenum);
				List<PDAnnotation> annotations = page.getAnnotations();
				ArrayList<PDRectangle> rectsofPage = rectsofPDF.get(pagenum);
				String stringsofPage = stringbufferofPage.toString();
				int pagelen = stringsofPage.length();
				List<RuleMatch> matches = null;
				List<FilteredMatch> filteredmatches = null;
				try {
					matches = tool.check(stringsofPage, true, ParagraphHandling.ONLYNONPARA);
					filteredmatches = StaticRules.filter(matches, stringsofPage, spellingoption);
					for (FilteredMatch match : filteredmatches) {
						int start = match.getFromPos();
						int end = match.getToPos() - 1;
						String matchmessage = match.getMessage();
						String errorstring = stringsofPage.substring(start, end + 1);
						String content = new String("plain text:" + "(" + errorstring + ")" + "of" + "("
								+ stringsofPage.substring((start - 2) >= 0 ? (start - 2) : 0,
										(end + 3) < pagelen ? (end + 3) : pagelen)
								+ ")" + "\n" + matchmessage);
						PDRectangle startPosition = rectsofPage.get(start);
						PDRectangle endPosition = rectsofPage.get(end);
						if (Math.abs(startPosition.getLowerLeftY() - endPosition.getLowerLeftY()) < 1e-2) {
							PDRectangle chunkPosition = new PDRectangle();
							chunkPosition.setLowerLeftX(startPosition.getLowerLeftX());
							chunkPosition.setLowerLeftY(startPosition.getLowerLeftY());
							chunkPosition.setUpperRightX(endPosition.getUpperRightX());
							chunkPosition.setUpperRightY(endPosition.getUpperRightY());
							annotations.add(composes(chunkPosition, content));
						} else {
							int chunkstart = start;
							for (int chunkend = start; chunkend < end; chunkend++) {

								if (Math.abs((rectsofPage.get(chunkend)).getLowerLeftY()
										- (rectsofPage.get(chunkend + 1)).getLowerLeftY()) >= 1e-2) {
									PDRectangle chunckPosition = new PDRectangle();
									chunckPosition.setLowerLeftX((rectsofPage.get(chunkstart)).getLowerLeftX());
									chunckPosition.setLowerLeftY((rectsofPage.get(chunkstart)).getLowerLeftY());
									chunckPosition.setUpperRightX((rectsofPage.get(chunkend)).getUpperRightX());
									chunckPosition.setUpperRightY((rectsofPage.get(chunkend)).getUpperRightY());
									annotations.add(composes(chunckPosition, content));
									chunkstart = chunkend + 1;
								}
							}
							PDRectangle chunkPosition = new PDRectangle();
							chunkPosition.setLowerLeftX((rectsofPage.get(chunkstart)).getLowerLeftX());
							chunkPosition.setLowerLeftY((rectsofPage.get(chunkstart)).getLowerLeftY());
							chunkPosition.setUpperRightX((rectsofPage.get(end)).getUpperRightX());
							chunkPosition.setUpperRightY((rectsofPage.get(end)).getUpperRightY());
							annotations.add(composes(chunkPosition, content));
						}
					}
				} catch (Exception ee) {
					System.out.println("inner ee");
				}
			} catch (Exception ee) {
				System.out.println("outter ee");
			}
			pagenum++;
		}

	}

	public void saveclose() {
		if (document != null) {
			try {
				document.save(outputfilePath);
				document.close();
			} catch (Exception ee) {
			}
		}
	}

	public void close() {
		if (document != null) {
			try {
				document.close();
			} catch (Exception ee) {
			}
		}
	}
}
