import java.io.IOException;
import java.util.HashSet;

public class Main {
	public static void main(String args[]) throws IOException {
		//getAllTagsFromTrainingSet();
		//System.out.println(Util.getWordAndTag("blabla/BLAM")[0]);
		boolean debug = true;
		// System.out.println(Math.log(0.0) + 10000);
		HiddenMarkovModel model = new HiddenMarkovModel();
		DataReader dataReader = new DataReader("sents.train");
		String line;
		int lineCounter = 0;
		while ( (line = dataReader.nextLine()) != null) {
			model.processTrainingSample(line);
			lineCounter ++ ;
		}
		System.out.println("finish training on " + lineCounter + " lines.");
		model.calculateEmissionProbNaive();
		model.calculateTransitionProbNaive();
		System.out.println("finish calculating emission and transition prob");
		String[] res = model.runViterbi(Util.tokenizeLine("I am all alone , the rooms are getting smaller ."));
		for (String s: res) {
			System.out.print(s + " ");
		}
		String[] words = Util.tokenizeLine("The luxury auto maker last year sold 1,214 cars in the U.S.");
		String[] tags = Util.tokenizeLine("DT NN NN NN JJ NN VBD CD NNS IN DT NNP");
		System.out.println(model.calculateLikelihood(words, tags));
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
		dataReader.close();
	}
}