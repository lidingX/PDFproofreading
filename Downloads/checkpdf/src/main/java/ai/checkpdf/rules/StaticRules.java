package ai.checkpdf.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.rules.RuleMatch;

import ai.checkpdf.FilteredMatch;



public class StaticRules {
	private final static List<String> ligatures =new ArrayList<String>((Arrays.asList("\uFB00","\uFB01","\uFB02","\uFB03","\uFB04","\uFB05")));
	private final static List<String> mathexps = new ArrayList<String>((Arrays.asList("$","ð","¼","½","Þ","")));
	private final static String mms1 =new String("Possible spelling mistake found");
	private final static String mms2 =new String("This sentence does not start with an uppercase letter");
	private final static String mms3 =new String("This phrase is duplicated. You should probably leave only");
	private final static String	mms4 = new String("Possible typo:");
	private final static String mms5 = new String("Don't put a space before the full stop");
	public final static String externalshortmessage = new String("It's an external short message!!!");
	private final static int maxdepth = 4;
	public static Map<String, Integer> Hasstring = new HashMap<String, Integer>();
	public static boolean leftNP(String string){
		if(string == null){
			return true;
		}
		else if(string.length() < 2){
			return false;
		}
		return (string.equals("VBN")||string.equals("VBG") || string.substring(0, 2).equals("NN") || string.equals("FW"));
	}
	public static boolean NPright(String string){
		if(string == null){
			return true;
		}
		else if(string.length() < 2){
			return false;
		}
		return (string.substring(0, 2).equals("JJ") || string.substring(0, 2).equals("NN") || string.equals("VBN") || string.equals("VBG") || string.equals("FW"));	
	}
	public static boolean leftAD(String string){
		if(string == null){
			return true;
		}
		else if(string.length() < 2){
			return false;
		}
		return string.substring(0, 2).equals("JJ");
	}
	public static boolean ADright(String string){
		if(string == null){
			return true;
		}
		else if(string.length() < 2){
			return false;
		}
		return  (string.substring(0, 2).equals("JJ"));
	}
	public static boolean pureChinesecheck(String name,int depth){
		char startchar = name.charAt(0);
		ArrayList<String> syllables = CH.get(startchar - 'a');
		for(String syllable : syllables){
			Pattern p = Pattern.compile("^" + syllable);
			Matcher m = p.matcher(name);
			if(m.find()){
				String subname = name.substring(syllable.length(),name.length());
				if(subname.length() == 0){
					return true;
				}
				if(depth == maxdepth)
				{
					return false;
				}
				if(pureChinesecheck(subname, depth + 1)){
					return true;
				}
			}
		}
		return false;
	}
	public static List<FilteredMatch> filter (List<RuleMatch> matches, String stringsofPage, String spellingoption){
		List<RuleMatch> tempmatches = new ArrayList<RuleMatch>();
		List<FilteredMatch> filteredmatches0 = new ArrayList<FilteredMatch>();
		List<FilteredMatch> filteredmatches = new ArrayList<FilteredMatch>();
		int matchessize = matches.size();
		for(int matchnum = 0; matchnum < matchessize; matchnum++){
			RuleMatch match= matches.get(matchnum);
			int start = match.getFromPos();
			int end = match.getToPos();
			String matchmessage = match.getMessage();
			List<String> suggestedreplaces = match.getSuggestedReplacements();
			String errorstring = stringsofPage.substring(start, end);
			if(matchmessage.equals(mms1)){
				if(spellingoption.equals("Close spelling check")){
					continue;
				}else if(spellingoption.equals("Close spelling check of upperclass word") &&  errorstring.charAt(0) >= 'A' && errorstring.charAt(0) <= 'Z'){
					continue;
				}
			}
			/*for mathexp*/
			if(matchnum > 0 ){
				RuleMatch formermatch = matches.get(matchnum - 1);
				String formerstring = stringsofPage.substring(formermatch.getFromPos(),formermatch.getToPos());
				if(errorstring.length() <= 3 && formerstring.length() <= 3 && start - formermatch.getFromPos() <= 2){
					continue;
				}
			}
			if(matchnum < matchessize - 1){
				RuleMatch lattermatch = matches.get(matchnum + 1);
				String latterstring = stringsofPage.substring(lattermatch.getFromPos(),lattermatch.getToPos());
				if(errorstring.length() <= 3 && latterstring.length() <= 3 && lattermatch.getFromPos() - start<= 2){
					continue;
				}
			}
			if(errorstring.length() < 3 && suggestedreplaces.size() >= 12){
				continue;
			}
			/*words contains upperclass after the first character*/
			{
				Pattern p = Pattern.compile("^[a-zA-Z][a-z]*[A-Z][a-z]*$");
			    Matcher m = p.matcher(errorstring);
			    if(m.find()){
			    	continue;
			    }
			}
			/*for mathexp contains some tag word.*/
			boolean mathexp_flag = false;
			for(String mathexp: mathexps)	{
				if(errorstring.contains(mathexp)){
						mathexp_flag = true;
						break;
					}
			}
			if(mathexp_flag){
				continue;
			}
			/*This phrase is duplicated or Possible typo: you repeated a whitespace, upperclass, abandon it*/
			if(matchmessage.contains(mms3) || matchmessage.contains(mms4) || matchmessage.contains(mms2)){
				continue;
			}
			/* if languagetool can't give a suggestion, abandon it.*/
			if(matchmessage.equals(mms1) && suggestedreplaces.size() == 0){  
				continue;
			}
			/*already add a space after stop, no necessary to check is*/
			if(matchmessage.contains(mms5)){
				continue;
			}
			/*suggest add a space*/
			if(suggestedreplaces.size() == 1 && errorstring.equals(suggestedreplaces.get(0).replaceAll(" ", ""))){
				continue;
			}
			/*if the errorstring contains a ligature and after converting it does not equal its suggestion, it might be right.*/
			boolean ligature_flag = false;
			for(String ligature: ligatures)	{
				if(errorstring.contains(ligature)){
						ligature_flag = true;
						break;
					}
			}
			if(ligature_flag){
				continue;
			}
			/*if the errorstring contains"-" once, it might be right, if converted to  checkstring,and checkstring equals suggestion.*/
			if(errorstring.contains("-")){
				boolean concat_flag = false;
				String checkstring = errorstring.replaceFirst("-", "");
				if(!checkstring.contains("-")){
					for(String replace: suggestedreplaces){
						if(checkstring.equals(replace)){
							concat_flag = true;
							break;
						}
					}
				}
				if(concat_flag){
					continue;
				}
			}
			/*check if it's a name*/
			if((errorstring.charAt(0) >= 'A' && errorstring.charAt(0)<='Z') && matchmessage.equals(mms1))
			{
				String name = errorstring.toLowerCase();
				Pattern p = Pattern.compile("[^a-z]");
				Matcher m = p.matcher(name);
				if(!m.find()){
					if(stringsofPage.charAt((start - 2) >= 0 ? (start - 2 ): 0 )== ',' || stringsofPage.charAt((start - 2) >= 0 ? (start - 2 ): 0 ) == '.'){
						continue;
					}
					if(pureChinesecheck(name, 0)){
						continue;
					}
				}
			}
			/* Ph.D.，M.Sc., B.Sc.*/
			if(errorstring.equals("Ph") && stringsofPage.substring(start - 1, end + 4).equals(" Ph.D. ")){
				continue;
			}
			if(errorstring.equals("Sc")){
				if(stringsofPage.substring(start - 3, end  + 2).equals(" M.Sc. ")){
					continue;
				}
				if(stringsofPage.substring(start - 3, end  + 2).equals(" B.Sc. ")){
					continue;
				}
			}
			/*a spelling error appears more than 5 times*/
			if(matchmessage.equals(mms1)){
				if(!Hasstring.isEmpty() && Hasstring.containsKey(errorstring)){
					int val = Hasstring.get(errorstring);
					if(val >= 5){
						continue;
					}
					else{
						Hasstring.replace(errorstring, val, val + 1);
					}
				}
				else{
					Hasstring.put(errorstring, 1);
				}
			
			}
			tempmatches.add(match);
		}
		for(RuleMatch match: tempmatches){
			filteredmatches0.add(new FilteredMatch(match.getFromPos(), match.getToPos(), match.getMessage(), match.getSuggestedReplacements()));
		}
		Pattern p1 = Pattern.compile("\\u007c\\u007c.{1,16}?\\u007c\\u007c");
		Matcher m1 = p1.matcher(stringsofPage);
		while(m1.find()){
			filteredmatches0.add(new FilteredMatch(m1.start(), m1.end(),"message:For norms, use \"\\|\" instead of \"||\".\n", null));
		}
		Pattern p2 = Pattern.compile("<.{1,16}?>");
		Matcher m2 = p2.matcher(stringsofPage);
		while(m2.find()){
			filteredmatches0.add(new FilteredMatch(m2.start(), m2.end(),"message:For inner product, use \"\\langle\" and \"\\rangle\" instead of \"<\" and \">\"\n", null));
		}
		Collections.sort(filteredmatches0, new Comparator<FilteredMatch>(){public int compare(FilteredMatch arg0, FilteredMatch arg1) {
            return ((Integer)arg0.getFromPos()).compareTo((Integer)arg1.getFromPos());
        }});
		Set<Integer> abandon = new HashSet<Integer>();
		for(int matchnum = 0; matchnum < filteredmatches0.size(); matchnum ++){
			if(!abandon.isEmpty() && abandon.contains(matchnum)){
				abandon.remove(matchnum);
				continue;
			}
			FilteredMatch match= filteredmatches0.get(matchnum);
			int end = match.getToPos();
			int index = matchnum ;
			while(++index < filteredmatches0.size()){
				FilteredMatch _match= filteredmatches0.get(index);
				int _start = _match.getFromPos();
				if(_start >= end){
					break;
				}
				int _end = _match.getToPos();
				if(_end <= end){
					match.addMessage(_match.getMessage());
					abandon.add(index);
				}
			}
			filteredmatches.add(match);
		}
		return filteredmatches;
	}
	
	public final static ArrayList<ArrayList<String>> CH = new ArrayList<ArrayList<String>>();
	static{
		ArrayList<String> CHa = new ArrayList<String>();
		CHa.add("ai");
		CHa.add("ang");
		CHa.add("ao");
		CH.add(CHa);
		ArrayList<String> CHb = new ArrayList<String>();
		CHb.add("ba");
		CHb.add("bai");
		CHb.add("ban");
		CHb.add("bang");
		CHb.add("bao");
		CHb.add("bei");
		CHb.add("ben");
		CHb.add("beng");
		CHb.add("bi");
		CHb.add("bian");
		CHb.add("biao");
		CHb.add("bie");
		CHb.add("bin");
		CHb.add("bing");
		CHb.add("bo");
		CHb.add("bu");
		CH.add(CHb);
		ArrayList<String> CHc = new ArrayList<String>();
		CHc.add("ca");
		CHc.add("cai");
		CHc.add("can");
		CHc.add("cang");
		CHc.add("cao");
		CHc.add("ce");
		CHc.add("cen");
		CHc.add("ceng");
		CHc.add("cha");
		CHc.add("chai");
		CHc.add("chan");
		CHc.add("chang");
		CHc.add("chao");
		CHc.add("chen");
		CHc.add("cheng");
		CHc.add("chi");
		CHc.add("chong");
		CHc.add("chou");
		CHc.add("chu");
		CHc.add("chua");
		CHc.add("chuai");
		CHc.add("chuan");
		CHc.add("chuang");
		CHc.add("chui");
		CHc.add("chun");
		CHc.add("chuo");
		CHc.add("ci");
		CHc.add("cong");
		CHc.add("cu");
		CHc.add("cuan");
		CHc.add("cui");
		CHc.add("cun");
		CHc.add("cuo");
		CH.add(CHc);
		ArrayList<String> CHd = new ArrayList<String>();
		CHd.add("da");
		CHd.add("dai");
		CHd.add("dan");
		CHd.add("dang");
		CHd.add("dao");
		CHd.add("de");
		CHd.add("dei");
		CHd.add("den");
		CHd.add("deng");
		CHd.add("di");
		CHd.add("dia");
		CHd.add("dian");
		CHd.add("diao");
		CHd.add("die");
		CHd.add("ding");
		CHd.add("diu");
		CHd.add("dong");
		CHd.add("dou");
		CHd.add("du");
		CHd.add("duan");
		CHd.add("dui");
		CHd.add("dun");
		CHd.add("duo");
		CH.add(CHd);
		ArrayList<String> CHe = new ArrayList<String>();
		CHe.add("e");
		CHe.add("ei");
		CHe.add("en");
		CHe.add("eng");
		CHe.add("er");
		CH.add(CHe);
		ArrayList<String> CHf = new ArrayList<String>();
		CHf.add("fa");
		CHf.add("fan");
		CHf.add("fang");
		CHf.add("fei");
		CHf.add("fen");
		CHf.add("feng");
		CHf.add("fo");
		CHf.add("fou");
		CHf.add("fu");
		CH.add(CHf);
		ArrayList<String> CHg = new ArrayList<String>();
		CHg.add("ga");
		CHg.add("gai");
		CHg.add("gan");
		CHg.add("gang");
		CHg.add("gao");
		CHg.add("ge");
		CHg.add("gei");
		CHg.add("gen");
		CHg.add("geng");
		CHg.add("gong");
		CHg.add("gou");
		CHg.add("gu");
		CHg.add("gua");
		CHg.add("guai");
		CHg.add("guan");
		CHg.add("guang");
		CHg.add("gui");
		CHg.add("gun");
		CHg.add("guo");
		CH.add(CHg);
		ArrayList<String> CHh = new ArrayList<String>();
		CHh.add("ha");
		CHh.add("hai");
		CHh.add("hang");
		CHh.add("hao");
		CHh.add("he");
		CHh.add("hei");
		CHh.add("hen");
		CHh.add("heng");
		CHh.add("hm");
		CHh.add("hng");
		CHh.add("hong");
		CHh.add("hou");
		CHh.add("hu");
		CHh.add("hua");
		CHh.add("huai");
		CHh.add("huan");
		CHh.add("huang");
		CHh.add("hui");
		CHh.add("hun");
		CHh.add("huo");
		CH.add(CHh);
		ArrayList<String> CHi = new ArrayList<String>();
		CH.add(CHi);
		ArrayList<String> CHj = new ArrayList<String>();
		CHj.add("ji");
		CHj.add("jia");
		CHj.add("jian");
		CHj.add("jiang");
		CHj.add("jiao");
		CHj.add("jie");
		CHj.add("jin");
		CHj.add("jing");
		CHj.add("jiong");
		CHj.add("jiu");
		CHj.add("ju");
		CHj.add("juan");
		CHj.add("jue");
		CHj.add("jun");
		CH.add(CHj);
		ArrayList<String> CHk = new ArrayList<String>();
		CHk.add("ka");
		CHk.add("kai");
		CHk.add("kan");
		CHk.add("kang");
		CHk.add("kao");
		CHk.add("ke");
		CHk.add("kei");
		CHk.add("ken");
		CHk.add("keng");
		CHk.add("kong");
		CHk.add("kou");
		CHk.add("ku");
		CHk.add("kua");
		CHk.add("kuai");
		CHk.add("kuan");
		CHk.add("kuang");
		CHk.add("kui");
		CHk.add("kun");
		CHk.add("kuo");
		CH.add(CHk);
		ArrayList<String> CHl = new ArrayList<String>();
		CHl.add("la");
		CHl.add("lai");
		CHl.add("lan");
		CHl.add("lang");
		CHl.add("lao");
		CHl.add("le");
		CHl.add("lei");
		CHl.add("leng");
		CHl.add("li");
		CHl.add("lia");
		CHl.add("lian");
		CHl.add("liang");
		CHl.add("liao");
		CHl.add("lie");
		CHl.add("lin");
		CHl.add("ling");
		CHl.add("liu");
		CHl.add("lo");
		CHl.add("long");
		CHl.add("lou");
		CHl.add("lv");
		CHl.add("luan");
		CHl.add("lue");
		CHl.add("lun");
		CHl.add("luo");
		CH.add(CHl);
		ArrayList<String> CHm = new ArrayList<String>();
		CHm.add("ma");
		CHm.add("mai");
		CHm.add("man");
		CHm.add("mang");
		CHm.add("mao");
		CHm.add("me");
		CHm.add("mei");
		CHm.add("men");
		CHm.add("meng");
		CHm.add("mi");
		CHm.add("mian");
		CHm.add("miao");
		CHm.add("mie");
		CHm.add("min");
		CHm.add("ming");
		CHm.add("miu");
		CHm.add("mo");
		CHm.add("mou");
		CHm.add("mu");
		CH.add(CHm);
		ArrayList<String> CHn = new ArrayList<String>();
		CHn.add("na");
		CHn.add("nai");
		CHn.add("nan");
		CHn.add("nang");
		CHn.add("nao");
		CHn.add("ne");
		CHn.add("nei");
		CHn.add("nen");
		CHn.add("neng");
		CHn.add("ni");
		CHn.add("nian");
		CHn.add("niang");
		CHn.add("niao");
		CHn.add("nie");
		CHn.add("ning");
		CHn.add("niu");
		CHn.add("nong");
		CHn.add("nu");
		CHn.add("nv");
		CHn.add("nuan");
		CHn.add("nue");
		CHn.add("nuo");
		CH.add(CHn);
		ArrayList<String> CHo = new ArrayList<String>();
		CHo.add("ou");
		CH.add(CHo);
		ArrayList<String> CHp = new ArrayList<String>();
		CHp.add("pa");
		CHp.add("pai");
		CHp.add("pan");
		CHp.add("pang");
		CHp.add("pao");
		CHp.add("pei");
		CHp.add("pen");
		CHp.add("peng");
		CHp.add("pi");
		CHp.add("pian");
		CHp.add("piao");
		CHp.add("pie");
		CHp.add("pin");
		CHp.add("ping");
		CHp.add("po");
		CHp.add("pou");
		CHp.add("pu");
		CH.add(CHp);
		ArrayList<String> CHq = new ArrayList<String>();
		CHq.add("qi");
		CHq.add("qian");
		CHq.add("qiang");
		CHq.add("qiao");
		CHq.add("qie");
		CHq.add("qin");
		CHq.add("qing");
		CHq.add("qiu");
		CHq.add("qu");
		CHq.add("quan");
		CHq.add("que");
		CHq.add("qun");
		CH.add(CHq);
		ArrayList<String> CHr = new ArrayList<String>();
		CHr.add("ran");
		CHr.add("rang");
		CHr.add("rao");
		CHr.add("re");
		CHr.add("ren");
		CHr.add("reng");
		CHr.add("ri");
		CHr.add("rong");
		CHr.add("rou");
		CHr.add("ru");
		CHr.add("ruan");
		CHr.add("rui");
		CHr.add("run");
		CHr.add("ruo");
		CH.add(CHr);
		ArrayList<String> CHs = new ArrayList<String>();
		CHs.add("sa");
		CHs.add("sai");
		CHs.add("san");
		CHs.add("sang");
		CHs.add("sao");
		CHs.add("se");
		CHs.add("sen");
		CHs.add("seng");
		CHs.add("sha");
		CHs.add("shai");
		CHs.add("shan");
		CHs.add("shang");
		CHs.add("shao");
		CHs.add("she");
		CHs.add("shui");
		CHs.add("shen");
		CHs.add("sheng");
		CHs.add("shi");
		CHs.add("shou");
		CHs.add("shu");
		CHs.add("shua");
		CHs.add("shuai");
		CHs.add("shuan");
		CHs.add("shuang");
		CHs.add("shui");
		CHs.add("shun");
		CHs.add("shuo");
		CHs.add("si");
		CHs.add("song");
		CHs.add("sou");
		CHs.add("su");
		CHs.add("suan");
		CHs.add("sui");
		CHs.add("sun");
		CHs.add("suo");
		CH.add(CHs);
		ArrayList<String> CHt = new ArrayList<String>();
		CHt.add("ta");
		CHt.add("tai");
		CHt.add("tan");
		CHt.add("tang");
		CHt.add("tao");
		CHt.add("te");
		CHt.add("teng");
		CHt.add("ti");
		CHt.add("tian");
		CHt.add("tiao");
		CHt.add("tie");
		CHt.add("ting");
		CHt.add("tong");
		CHt.add("tou");
		CHt.add("tu");
		CHt.add("tuan");
		CHt.add("tui");
		CHt.add("tun");
		CHt.add("tuo");
		CH.add(CHt);
		ArrayList<String> CHu = new ArrayList<String>();
		CH.add(CHu);
		ArrayList<String> CHv = new ArrayList<String>();
		CH.add(CHv);
		ArrayList<String> CHw = new ArrayList<String>();
		CHw.add("wa");
		CHw.add("wai");
		CHw.add("wan");
		CHw.add("wang");
		CHw.add("wei");
		CHw.add("weng");
		CHw.add("wo");
		CHw.add("wu");
		CH.add(CHw);
		ArrayList<String> CHx = new ArrayList<String>();
		CHx.add("xi");
		CHx.add("xia");
		CHx.add("xian");
		CHx.add("xiang");
		CHx.add("xiao");
		CHx.add("xie");
		CHx.add("xin");
		CHx.add("xing");
		CHx.add("xiong");
		CHx.add("xiu");
		CHx.add("xuan");
		CHx.add("xue");
		CHx.add("xun");
		CH.add(CHx);
		ArrayList<String> CHy = new ArrayList<String>();
		CHy.add("ya");
		CHy.add("yan");
		CHy.add("yang");
		CHy.add("yao");
		CHy.add("ye");
		CHy.add("yi");
		CHy.add("yin");
		CHy.add("yong");
		CHy.add("you");
		CHy.add("yu");
		CHy.add("yuan");
		CHy.add("yue");
		CHy.add("yun");
		CH.add(CHy);
		ArrayList<String> CHz = new ArrayList<String>();
		CHz.add("za");
		CHz.add("zai");
		CHz.add("zan");
		CHz.add("zang");
		CHz.add("zao");
		CHz.add("ze");
		CHz.add("zei");
		CHz.add("zen");
		CHz.add("zeng");
		CHz.add("zha");
		CHz.add("zhai");
		CHz.add("zhan");
		CHz.add("zhang");
		CHz.add("zhao");
		CHz.add("zhe");
		CHz.add("zhen");
		CHz.add("zheng");
		CHz.add("zhi");
		CHz.add("zhu");
		CHz.add("zhua");
		CHz.add("zhuai");
		CHz.add("zhuan");
		CHz.add("zhuang");
		CHz.add("zhun");
		CHz.add("zhuo");
		CHz.add("zi");
		CHz.add("zong");
		CHz.add("zou");
		CHz.add("zu");
		CHz.add("zuan");
		CHz.add("zui");
		CHz.add("zun");
		CHz.add("zuo");
		CH.add(CHz);
	};
}
