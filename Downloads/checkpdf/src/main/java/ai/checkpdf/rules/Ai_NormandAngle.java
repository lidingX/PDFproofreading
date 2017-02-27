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

public class Ai_NormandAngle extends Rule {
	@Override
	public String getId() {
		return "Ai_NormandAngle"; // a unique id that doesn't change over
										// time
	}

	@Override
	public String getDescription() {
		return "A rule that checks Norm and Angle"; // shown in the
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
        return toRuleMatchArray(ruleMatches);
	}
}

