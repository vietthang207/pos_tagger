import java.io.IOException;
import java.util.HashSet;

public class Main {
	public static void main(String args[]) throws IOException {
		//getAllTagsFromTrainingSet();
		//System.out.println(Util.getWordAndTag("blabla/BLAM")[0]);
		// System.out.println(Math.log(0.0) + 10000);
		HiddenMarkovModel model = trainOn("sents.train");
		// model.calculateEmissionProbNaive();
		model.calculateEmissionProbWittenBell();
		// model.calculateTransitionProbNaive();
		model.calculateTransitionProbWittenBell();
		System.out.println("finish calculating emission and transition prob");
		System.out.println("Training accuracy: " + getAccuracyOnFile(model, "sents.train"));
		System.out.println("Accuracy on dev set: " + getAccuracyOnFile(model, "sents.devt"));
		double[][] confusionMat = getConfusionTableOnFile(model, "sents.devt");
		printConfusionMatrix(confusionMat);
		// String[] res = model.runViterbi(Util.tokenizeLine("I am all alone , the rooms are getting smaller ."));
		// for (String s: res) {
		// 	System.out.print(s + " ");
		// }
		// String[] words = Util.tokenizeLine("The luxury auto maker last year sold 1,214 cars in the U.S.");
		// String[] tags = Util.tokenizeLine("DT NN NN NN JJ NN VBD CD NNS IN DT NNP");
		// System.out.println(model.calculateLikelihood(words, tags));
	}

	private static HiddenMarkovModel trainOn(String fileName) throws IOException {
		HiddenMarkovModel model = new HiddenMarkovModel();
		DataReader dataReader = new DataReader("sents.train");
		String line;
		int lineCounter = 0;
		while ( (line = dataReader.nextLine()) != null) {
			model.processTrainingSample(line);
			lineCounter ++ ;
		}
		dataReader.close();
		System.out.println("finish training on " + lineCounter + " lines.");
		return model;
	}

	private static double getAccuracyOnFile(HiddenMarkovModel model, String fileName) throws IOException {
		DataReader dataReader = new DataReader(fileName);
		String line;
		int lineCounter = 0;
		int wordCounter = 0;
		int correctCounter = 0;
		while ( (line = dataReader.nextLine()) != null) {
			lineCounter ++;
			if (lineCounter % 1000 == 0) System.out.println("Evaluate accuracy on line number: " + lineCounter);
			String[] words = Util.getWords(line);
			String[] tags = Util.getTags(line);
			String[] predictedTags = model.runViterbi(words);
			for (int i=0; i<words.length; i++) {
				wordCounter ++;
				if (tags[i].equals(predictedTags[i])) correctCounter ++;
			}
		}
		dataReader.close();
		return correctCounter*100.0/wordCounter;
	}

	private static double[][] getConfusionTableOnFile(HiddenMarkovModel model, String fileName) throws IOException {
		DataReader dataReader = new DataReader(fileName);
		String line;
		int lineCounter = 0;
		int[][] confusionCount = new int[Constant.numTags][Constant.numTags];
		double[][] confusionMatrix = new double[Constant.numTags][Constant.numTags];
		while ( (line = dataReader.nextLine()) != null) {
			lineCounter ++;
			if (lineCounter % 1000 == 0) System.out.println("Evaluate confusion table on line number: " + lineCounter);
			String[] words = Util.getWords(line);
			String[] tags = Util.getTags(line);
			String[] predictedTags = model.runViterbi(words);
			for (int i=0; i<words.length; i++) {
				confusionCount[model.index(tags[i])][model.index(predictedTags[i])] ++;
			}
		}
		dataReader.close();
		for (int i=0; i<Constant.numTags-2; i++) {
			int sum = 0;
			for (int j=0; j<Constant.numTags-2; j++) {
				sum += confusionCount[i][j];
			}
			for (int j=0; j<Constant.numTags-2; j++) {
				if (sum==0) confusionMatrix[i][j] = 0;
				else confusionMatrix[i][j] = confusionCount[i][j]*100.0/sum;
			}
		}
		return confusionMatrix;
	}

	private static void printConfusionMatrix(double[][] mat) {
		String[] tagList = Util.getTagList();
		System.out.print("      ");
		for (int i=0; i<Constant.numTags-1; i++) {
			System.out.print(normalizeTagName(tagList[i]) + "|");
		}
		System.out.println();
		for (int i=0; i<Constant.numTags; i++) {
			System.out.print(normalizeTagName(tagList[i])+"|");
			for (int j=0; j<Constant.numTags; j++) {
				if (i==j) System.out.printf("%.2f|", -1.0);
				else if (mat[i][j]<10) System.out.printf(" %.2f|", mat[i][j]);
				else if (mat[i][j]>=100) System.out.printf("100.0|");
				else System.out.printf("%2.2f|", mat[i][j]);
			}
			System.out.println();
		}
		
	}

	private static String normalizeTagName(String tag) {
		if (tag.equals("START")) tag = "STA";
		if (tag.equals("-LRB-")) tag = "LRB";
		if (tag.equals("-RRB-")) tag = "RRB";
		String res = "";
		for (int i=0; i<5-tag.length(); i++) {
			res += " ";
		}
		res += tag;
		return res;
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