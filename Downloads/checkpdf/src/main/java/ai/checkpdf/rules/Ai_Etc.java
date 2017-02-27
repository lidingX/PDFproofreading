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

public class Ai_Etc extends Rule {

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
			if (tokens[index].getToken().equals("e")) {
					if (index + 4 < tokens.length && tokens[index + 1].getToken().equals(".") && tokens[index + 2].getToken().equals("t")
							&& tokens[index + 3].getToken().equals(".") && tokens[index + 4].getToken().equals("c")) {
						RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(),
								tokens[index + 4].getEndPos(), "Don't write \"e.t.c.\"", shortmessage);
						ruleMatch.setSuggestedReplacement("Write \"etc.\"");
						ruleMatches.add(ruleMatch);
					}
					else if (index + 2 < tokens.length && tokens[index + 1].getToken().equals(".") && tokens[index + 2].getToken().equals("tc")) {
						RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(),
								tokens[index + 2].getEndPos(), "Don't write \"e.tc.\"", shortmessage);
						ruleMatch.setSuggestedReplacement("Write \"etc.\"");
						ruleMatches.add(ruleMatch);
					}
			}
			else if (tokens[index].getToken().equals("et")){
				if (index + 2 < tokens.length && tokens[index + 1].getToken().equals(".") && tokens[index + 2].getToken().equals("c")) {
					RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(),
							tokens[index + 2].getEndPos(), "Don't write \"et.c.\"", shortmessage);
					ruleMatch.setSuggestedReplacement("Write \"etc.\"");
					ruleMatches.add(ruleMatch);
				}
			}
		}
		return toRuleMatchArray(ruleMatches);
	}

}
