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

public class Ai_Cannot extends Rule {

	@Override
	public String getId() {
		return "Ai_Cannot"; // a unique id that doesn't change over
							// time
	}

	@Override
	public String getDescription() {
		return "A rule that checks can not"; // shown in the
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
			if (index + 1 < tokens.length) {
				if(!tokens[index + 1].getToken().equals("not")){
	    			 continue;
	    		 }
				if(!tokens[index].getToken().equals("can")){
	    			 continue;
	    		 }
				RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(), tokens[index + 1].getEndPos(),
						"Don't write \"" + tokens[index].getToken() + " " + tokens[index+1].getToken() + "\"",
						shortmessage);
				ruleMatch.setSuggestedReplacement("Combine them");
				ruleMatches.add(ruleMatch);
			}
		}

		return toRuleMatchArray(ruleMatches);
	}

}
