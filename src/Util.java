
public class Util {
	public static TaggedWord[] segmentLineIntoTaggedWord(String line) {
		String[] tokens = line.split(" ");
		TaggedWord[] res = new TaggedWord[tokens.length];
		for (int i=0; i<tokens.length; i++) {
			String[] pair = getWordAndTag(tokens[i]);
			res[i] = new TaggedWord(pair[0], pair[1]);
		}
		return res;
	}

	public static String[] getWordAndTag(String str) {
		int ind = str.lastIndexOf("/");
		String[] res = new String[2];
		res[0] = str.substring(0, ind);
		res[1] = str.substring(ind+1, str.length());
		return res;

	}

	public static String[] tokenizeLine(String line) {
		return line.split(" ");
	}
	public static String[] getTagList() {
		String[] tagList = {"START",
							"CC", "CD", "DT", "EX",
							"FW", "IN", "JJ", "JJR",
							"JJS", "LS", "MD", "NN", 
							"NNS", "NNP", "NNPS", "PDT",
							"POS", "PRP", "PRP$", "RB",
							"RBR", "RBS", "RP", "SYM",
							"TO", "UH", "VB", "VBD",
							"VBG", "VBN", "VBP", "VBZ",
							"WDT", "WP", "WP$", "WRB",
							"$", "#", "``", "''", 
							"-LRB-", "-RRB-", ",", ".", ":",
							"END"};
		return tagList;
	}
}