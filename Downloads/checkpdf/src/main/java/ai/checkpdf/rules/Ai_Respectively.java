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

public class  Ai_Respectively extends Rule {

	@Override
	public String getId() {
		return " Ai_Respectively"; // a unique id that doesn't change over
							// time
	}

	@Override
	public String getDescription() {
		return "A rule that checks if a comma before respectively"; // shown in the
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
		for (int index = 1; index < tokens.length ; index++) {
	
					if(tokens[index].getToken().equals("respectively") && !tokens[index - 1].getToken().equals(",")){
				RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(), tokens[index].getEndPos(),
						"There is no comma before \"respectively\"",
						shortmessage);
				ruleMatch.setSuggestedReplacement("Put a comma before \"respectively\"");
				ruleMatches.add(ruleMatch);
					}
			
		}

		return toRuleMatchArray(ruleMatches);
	}

}
