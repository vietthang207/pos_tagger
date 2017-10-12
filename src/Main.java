import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

public class Main {
	public static void main(String args[]) throws IOException, ClassNotFoundException {
		if (args.length >= 1) {
			if (args[0].equals("build_tagger") && args.length == 4) {
				buildTagger(args[1], args[2], args[3]);
				return;
			}
			else if (args[0].equals("run_tagger") && args.length == 4) {
				runTagger(args[1], args[2], args[3]);
				return;
			}
			else if (args[0].equals("cross_validation") && args.length == 4) {
				trainWithCrossValidationOn(args[1]);
				return;
			}	
		}
		System.out.println("wrong command");
	}

	private static void buildTagger(String trainingFile, String devFile, String outputModelFile) throws IOException {
		HiddenMarkovModel model = trainOn(trainingFile);
		model.calculateEmissionProbWittenBell();
		model.calculateTransitionProbWittenBell();
		model.calculateCapitalProbability();
		System.out.println("finish calculating emission and transition prob");
		System.out.println("Training accuracy: " + getAccuracyOnFile(model, trainingFile));
		System.out.println("Accuracy on dev set: " + getAccuracyOnFile(model, devFile));
		double[][] confusionMat = getConfusionTableOnFile(model, "sents.devt");
		printConfusionMatrix(confusionMat);
		saveModel(model, outputModelFile);
	}

	private static void runTagger(String inputFile, String modelFile, String outputFile) throws IOException, ClassNotFoundException {
		HiddenMarkovModel model = loadModel(modelFile);
		DataReader dataReader = new DataReader(inputFile);
		PrintWriter writer = new PrintWriter(outputFile);
		String line;
		int lineCounter = 0;
		while ( (line = dataReader.nextLine()) != null) {
			String[] words = line.split(" ");
			String[] tags = model.runViterbi(words);
			String res = "";
			for (int i=0; i<words.length; i++) {
				res += words[i] + "/" + tags[i] + " ";
			}
			writer.println(res);
		}
		writer.close();
		dataReader.close();
	}

	private static void saveModel(HiddenMarkovModel model, String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(model);
	}

	private static HiddenMarkovModel loadModel(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		return (HiddenMarkovModel) ois.readObject();
	}

	private static HiddenMarkovModel trainOn(String fileName) throws IOException {
		HiddenMarkovModel model = new HiddenMarkovModel();
		DataReader dataReader = new DataReader(fileName);
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

	private static void trainWithCrossValidationOn(String fileName) throws IOException {
		ArrayList<Integer> randomArray = new ArrayList<Integer>(Constant.numLines);
		for (int i=0; i<Constant.numLines; i++) {
			randomArray.add(i);
		}
		ArrayList<Double> validationError = new ArrayList<Double>();
		for (int iter=0; iter<10; iter++) {
			System.out.println("Iteration " + (iter + 1));
			HiddenMarkovModel model = new HiddenMarkovModel();
			Collections.shuffle(randomArray);
			// N/10 first element will be put to validation set
			HashSet<Integer> validationIndex = new HashSet<Integer>();
			for (int i=0; i<Constant.numLines/10; i++) {
				validationIndex.add(randomArray.get(i));
			}

			DataReader dataReader = new DataReader(fileName);
			String line;
			int lineCounter = 0;
			while ( (line = dataReader.nextLine()) != null) {
				// skip if line in validation set
				if (validationIndex.contains(lineCounter)) {
					lineCounter++;
					continue;
				}
				model.processTrainingSample(line);
				lineCounter ++ ;
			}
			dataReader.close();
			model.calculateEmissionProbWittenBell();
			model.calculateTransitionProbWittenBell();
			model.calculateCapitalProbability();
			System.out.println("Finish training on " + lineCounter + " lines.");

			dataReader = new DataReader(fileName);
			lineCounter = 0;
			int wordCounter = 0;
			int correctCounter = 0;
			while ( (line = dataReader.nextLine()) != null) {
				if (validationIndex.contains(lineCounter)) {
					lineCounter ++;
					continue;
				}
				lineCounter ++;
				String[] words = Util.getWords(line);
				String[] tags = Util.getTags(line);
				String[] predictedTags = model.runViterbi(words);
				for (int i=0; i<words.length; i++) {
					wordCounter ++;
					if (tags[i].equals(predictedTags[i])) correctCounter ++;
				}
			}
			dataReader.close();
			validationError.add(correctCounter*100.0/wordCounter);
			System.out.println("Validation accuracy " + correctCounter*100.0/wordCounter);
		}
		double cvError = 0;
		for (double d: validationError) {
			cvError += d;
		}
		cvError /= validationError.size();
		System.out.println("Cross validation accuracy " + cvError);
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