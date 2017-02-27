package ai.checkpdf.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import ai.checkpdf.rules.StaticRules;

public class Ai_PassiveTenseRule extends Rule {

	@Override
	public String getId() {
		return "Ai_PassiveTenseRule"; // a unique id that doesn't change over
										// time
	}

	@Override
	public String getDescription() {
		return "A rule that checks passive tense using"; // shown in the
															// configuration
															// dialog
	}

	@Override
	public void reset() {
		// if we had some internal state kept in member variables, we would need
		// to reset them here
	}

	// This is the method with the error detection logic that you need to
	// implement:
	@Override
	public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
		List<RuleMatch> ruleMatches = new ArrayList<>();
		// Let's get all the tokens (i.e. words) of this sentence, but not the
		// spaces:
		AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

		// No let's iterate over those - note that the first token will
		// be a special token that indicates the start of a sentence:
		String shortmessage = StaticRules.externalshortmessage;
		int index = 0;
		for (AnalyzedTokenReadings token : tokens) {
			boolean VBNflag = false;
			boolean ADJflag = false;
			// A word can have more than one reading, e.g. 'dance' can be a verb
			// or a noun,
			// so we iterate over the readings:
			for (AnalyzedToken analyzedToken : token.getReadings()) {
				if (analyzedToken.getPOSTag() == null) {
					break;
				}
				if (analyzedToken.getPOSTag().equals("VBN")) {
					VBNflag = true;
				}
				if (analyzedToken.getPOSTag().equals("JJ")) {
					ADJflag = true;
				}
			}
			if (VBNflag && !ADJflag) {
				boolean detectedflag = false;
				AnalyzedTokenReadings formertoken = tokens[index - 1];
				for (AnalyzedToken formeranalyzedToken : formertoken.getReadings()) {
					if (formeranalyzedToken.getLemma() == null || formeranalyzedToken.equals(",")) {
						break;
					}
					if (formeranalyzedToken.getLemma().equals("be")) {
						detectedflag = true;
						break;
					}
				}
				if (detectedflag) {
					RuleMatch ruleMatch = new RuleMatch(this, token.getStartPos(), token.getEndPos(),
							"Used passive tense", shortmessage);
					ruleMatch.setSuggestedReplacement("Change it");
					ruleMatches.add(ruleMatch);
				}
			}
			index++;
		}
		return toRuleMatchArray(ruleMatches);
	}
}
