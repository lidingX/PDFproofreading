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

import ai.checkpdf.rules.StaticRules;

public class Ai_ABYZ extends Rule {
	@Override
	public String getId() {
		return "Ai_ABYZ"; // a unique id that doesn't change over
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

	@Override
	public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
		List<RuleMatch> ruleMatches = new ArrayList<>();
		String shortmessage = StaticRules.externalshortmessage;

		// Let's get all the tokens (i.e. words) of this sentence, but not the
		// spaces:
		// A,B and C
		AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
		for (int index = 1; index < tokens.length; index++) {
			if(tokens[index].getToken().equals(",")){
			    int iter;
			    boolean find = false;
				boolean quit = false;
				for(iter = index + 2;iter < tokens.length; iter++){
					if(!tokens[iter - 1].getToken().equals(",") && tokens[iter].getToken().equals("and") && iter + 1 < tokens.length ){
						for(AnalyzedToken analyzedToken0 : tokens[index - 1].getReadings()){
							String tag0 = analyzedToken0.getPOSTag();
							/*if (tag0 == null || tag0.length() < 2) {
								quit = true;
								break;
							}*/
							for(AnalyzedToken analyzedToken1 : tokens[index + 1].getReadings()){
								String tag1 = analyzedToken1.getPOSTag();
								/*if (tag1 == null || tag1.length() < 2) {
									quit = true;
									break;
								}*/
								for(AnalyzedToken analyzedToken2 : tokens[iter + 1].getReadings()){
									String tag2 = analyzedToken2.getPOSTag();
									/*if (tag2 == null || tag2.length() < 2) {
										break;
									}*/
									if(StaticRules.leftAD(tag0) && StaticRules.ADright(tag1)&&StaticRules.ADright(tag2)){
										find = true;
										break;
									}
									else if(StaticRules.leftNP(tag0) && StaticRules.NPright(tag1)&&StaticRules.NPright(tag2)){
										find = true;
										break;
									}
								}
								if(find){
									break;
								}
							}
							if(quit){
								break;
							}
							if(find){
								break;
							}
						}
						if(quit){
							break;
						}
						if(find){
							//System.out.println("ABYZ:" + tokens[index].getToken() + tokens[iter].getToken());
							Pattern p = Pattern.compile("[\"“]");
							String string = new String();
							for(int k = index ; k <= iter; k++){
								string = string.concat(tokens[k].getToken());
							}
							Matcher m = p.matcher(string);
							if(!m.find()){
							RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(), tokens[iter].getEndPos(),
									"There are more than two objects, and they are written as \"A, B and C\"", shortmessage);
							ruleMatch.setSuggestedReplacement("Wrtie as \"A, B, and C\"");
							ruleMatches.add(ruleMatch);
							}
						}
					}
				}
			}
		}
		return toRuleMatchArray(ruleMatches);
	}
}
