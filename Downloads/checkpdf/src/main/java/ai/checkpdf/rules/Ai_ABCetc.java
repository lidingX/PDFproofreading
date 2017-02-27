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

public class Ai_ABCetc extends Rule{
	@Override
	public String getId() {
		return "Ai_ABCetc"; // a unique id that doesn't change over
										// time
	}

	@Override
	public String getDescription() {
		return "A rule that checks passive tense using"; // shown in the
														// configuration dialog
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

	    // Let's get all the tokens (i.e. words) of this sentence, but not the spaces:
	    // A, B, and C, etc.
	    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
	    for (int index = 1; index < tokens.length ; index++) {
	    	if(tokens[index].getToken().equals(",")){
			    int iter;
			    int last = index;
			    boolean find = false;
				boolean quit = false;
				for(iter = index + 2;iter < tokens.length; iter++){
					if(tokens[iter].getToken().equals(",") && tokens[iter + 1].getToken().equals("and") && iter + 2 < tokens.length ){
						boolean conti = true;
						for(int i = iter + 3; i < tokens.length; i ++){
							if(tokens[i].getToken().equals("etc")){
								conti = false;
								last = i;
								break;
							}else if(i + 4 < tokens.length &&tokens[i].getToken().equals("e") && tokens[i + 1].getToken().equals(".") && tokens[i + 2].getToken().equals("t") &&
									tokens[i + 3].getToken().equals(".") && tokens[i + 4].getToken().equals("c")){
								conti = false;	
								last = i + 4;
								break;
							}else if( i + 2 < tokens.length && tokens[i].getToken().equals("e") && tokens[i + 1].getToken().equals(".") && tokens[i + 2].getToken().equals("tc")){
								conti = false;
								last = i + 2;
								break;
							}else if(i + 2 < tokens.length && tokens[i].getToken().equals("et") && tokens[i + 1].getToken().equals(".") && tokens[i + 2].getToken().equals("c")){
								conti = false;
								last = i + 2;
								break;
							}
						}
						if(conti){
							continue;
						}
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
								for(AnalyzedToken analyzedToken2 : tokens[iter + 2].getReadings()){
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
							//System.out.println("ABCetc:" + tokens[index].getToken() + tokens[last].getToken());
							Pattern p = Pattern.compile("[\"“]");
							String string = new String();
							for(int k = index ; k <= last; k++){
								string = string.concat(tokens[k].getToken());
							}
							Matcher m = p.matcher(string);
							if(!m.find()){
							RuleMatch ruleMatch = new RuleMatch(this, tokens[index].getStartPos(), tokens[last].getEndPos(),
									"There are more than two objects, and they are written as \"A, B ,and C, etc.\"", shortmessage);
							ruleMatch.setSuggestedReplacement("Wrtie as \"A, B, C, etc.\"");
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
