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

public class Ai_Not extends Rule {

	@Override
	public String getId() {
		return "Ai_Not"; // a unique id that doesn't change over
							// time
	}

	@Override
	public String getDescription() {
		return "A rule that checks abbriviation of not"; // shown in the
															// configuration
															// dialog
	}

	@Override
	public void reset() {
		// if we had some internal state kept in member variables, we would need
		// to reset them here
	}

	@Override
	public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
		List<RuleMatch> ruleMatches = new ArrayList<>();

		// Let's get all the tokens (i.e. words) of this sentence, but not the
		// spaces:
		AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
		String shortmessage = StaticRules.externalshortmessage;
		for (int index = 0; index < tokens.length; index++) {
			if (index + 2 < tokens.length) {
				if(!tokens[index + 1].getToken().equals("'")){
	    			 continue;
	    		 }
				boolean flag2 = false;
				for (AnalyzedToken analyzedToken : tokens[index + 2].getReadings()) {
					if (analyzedToken.getLemma() != null && analyzedToken.getLemma().equals("not")) {
						flag2 = true;
					}
				}
				if (!flag2) {
					continue;
				}
				boolean flag0  = false;
				for (AnalyzedToken analyzedToken : tokens[index].getReadings()) {
					if (analyzedToken.getPOSTag() != null && analyzedToken.getPOSTag().equals("MD")) {
						flag0 = true;
					}
					else if(analyzedToken.getLemma() != null){
						if(analyzedToken.getLemma().equals("be")){
							flag0 = true;
						}
						else if(analyzedToken.getLemma().equals("do")){
							flag0 = true;
						}
					}
				}
				if (!flag0) {
					continue;
				}
				RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(), tokens[index + 2].getEndPos(),
						"Don't write \"" + tokens[index].getToken() + tokens[index+1].getToken() + tokens[index+2].getToken() + "\"",
						shortmessage);
				ruleMatch.setSuggestedReplacement("Separate them");
				ruleMatches.add(ruleMatch);
			}
		}

		return toRuleMatchArray(ruleMatches);
	}

}
