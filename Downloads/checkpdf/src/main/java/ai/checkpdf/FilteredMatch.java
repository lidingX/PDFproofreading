package ai.checkpdf;

import java.util.List;

public class FilteredMatch {
	private int frompos;
	private int topos;
	private String message;

	public FilteredMatch(int frompos, int topos, String message, List<String> suggestedreplaces) {
		if(suggestedreplaces != null){
			this.frompos = frompos;
			this.topos = topos;
			this.message = new String("message:" + message + "\n" + "suggestion:" + suggestedreplaces + "\n");}
		else{
			this.frompos = frompos;
			this.topos = topos;
			this.message = new String(message);	
		}
	}
	public int getFromPos() {
		return frompos;
	}

	public int getToPos() {
		return topos;
	}
	public String getMessage() {
		return message;
	}

	public void addMessage(String message) {
			this.message = this.message.concat(message);
	}
}
