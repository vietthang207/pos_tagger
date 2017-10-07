import java.io.IOException;
import java.util.HashSet;

public class Main {
	public static void main(String args[]) throws IOException {
		getAllTagsFromTrainingSet();
		//System.out.println(Util.getWordAndTag("blabla/BLAM")[0]);

	}

	private static void getAllTagsFromTrainingSet() throws IOException {
		DataReader dataReader = new DataReader("sents.train");
		String line;
		HashSet<String> tags = new HashSet<String>();
		int lineCounter = 0;
		while ( (line = dataReader.nextLine()) != null) {
			lineCounter ++;
			TaggedWord[] taggedWords = Util.segmentLineIntoTaggedWord(line);
			for (TaggedWord w: taggedWords) {
				tags.add(w.getTag());
			}
		}
		System.out.println(lineCounter);
		System.out.println(tags.size());
		for (String t: tags) {
			System.out.print(t + ", ");
		}
	}
}