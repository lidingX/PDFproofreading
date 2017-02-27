package ai.checkpdf.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class Ai_AandB extends Rule {
	@Override
	public String getId() {
		return "Ai_Aandb"; // a unique id that doesn't change over
										// time
	}

	@Override
	public String getDescription() {
		return "A rule that checks A and B"; // shown in the
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
		String shortmessage = StaticRules.externalshortmessage;

		// Let's get all the tokens (i.e. words) of this sentence, but not the
		// spaces:
		// _ A _ B _
		AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
		for (int index = 1; index < tokens.length; index++) {
			if (tokens[index].getToken().equals(",")) {
				if ( index + 1 < tokens.length) {
					boolean flag = false;
					for (AnalyzedToken analyzedToken0 : tokens[index - 1].getReadings()) {
						String tag0 = analyzedToken0.getPOSTag();
						/*if (tag0 == null || tag0.length() < 2) {
							break;
						}*/
						boolean quit = false;
						for(int i = index + 2 ; i < tokens.length; i++){
							if(tokens[i].getToken().equals("and") || tokens[i].getToken().equals(",") || tokens[i].getToken().equals("\"")){
								quit = true;
								break;
							}
						}
						if(quit){
							break;
						}
						for (AnalyzedToken analyzedToken2 : tokens[index + 1].getReadings()) {
							String tag2 = analyzedToken2.getPOSTag();
							/*if (tag2 == null || tag2.length() < 2) {
								quit = true;
								break;
							}*/
							if (StaticRules.leftNP(tag0) && StaticRules.NPright(tag2)) {
								flag = true;
								break;
							} else if (StaticRules.leftAD(tag0) && StaticRules.ADright(tag2)) {
								flag = true;
								break;
							}

						}
						if(quit){
							break;
						}
					}
					if (flag) {
						Pattern p = Pattern.compile("[\"“]");
						String string = new String();
						for(int k = index - 1; k <= index + 1; k++){
							string = string.concat(tokens[k].getToken());
						}
						Matcher m = p.matcher(string);
						if(!m.find()){
						RuleMatch ruleMatch = new RuleMatch(this, tokens[index - 1].getStartPos(),
								tokens[index + 1].getEndPos(),
								"There are only two objects, and they are written as \"A, B\"", shortmessage);
						ruleMatch.setSuggestedReplacement("Wrtie as \"A and B\"");
						ruleMatches.add(ruleMatch);
						}
					}

				}
			}
		}
		return toRuleMatchArray(ruleMatches);
	}
}
