package thundertactics.aspects;

/*
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import thundertactics.cfg.Config;
import thundertactics.comm.mesg.from.ChatFrom;
import thundertactics.comm.mesg.from.MesgFrom;
import thundertactics.comm.mesg.from.NearChatFrom;
*/
public aspect ChatMessageCensorer {
/*
	private static List<String> fw = new ArrayList<String>();
	
	private static String FORBIDDENWORDS;
	
	static{
		try{
			Scanner s = new Scanner(new FileInputStream(Config.get("forbiddenWordsFile")));
			while(s.hasNextLine()){
				String line = s.nextLine().replaceAll("(?i)[^a-z0-9]+", " ");
				addForbiddenWord(line);
			}
			s.close();
		}catch(Exception ex){
			System.err.println("Failed to load forbidden words");
			ex.printStackTrace();
		}
		updateWordsRegex();
	}
	public static void addForbiddenWord(String word){
		fw.add(word);
	}
	public static void updateWordsRegex(){
		StringBuilder b = new StringBuilder();
		b.append("(?i)");
		for(String word:fw){
			word = word.replaceAll("", "[^a-z0-9]*?");
			b.append(word.substring(11)).append("[a-z]*|");
		}
		FORBIDDENWORDS = fw.isEmpty()? b.toString():b.substring(0, b.length()-1);
	}
	MesgFrom around() : execution(public static MesgFrom read(*)) {
		MesgFrom m = proceed();
		if(m instanceof NearChatFrom){
			((NearChatFrom) m).text = ((NearChatFrom) m).text.replaceAll(FORBIDDENWORDS, "[censored]");
		} else if (m instanceof ChatFrom){
		    ((ChatFrom) m).text = ((ChatFrom) m).text.replaceAll(FORBIDDENWORDS, "[censored]");
		}
		return m;
	}
*/
}
