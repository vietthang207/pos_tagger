import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class HiddenMarkovModel {
	private HashMap<String, Integer> nodeIndex;
	private String[] tags;
	private HashSet<String> vocabulary;
	private int[][] transitionCountMatrix;
	private double[][] transitionProbabilityMatrix;
	private ArrayList<HashMap<String, Integer>> emissionCount;
	private ArrayList<HashMap<String, Double>> emissionProbabilities;

	public HiddenMarkovModel() {
		nodeIndex = new HashMap<String, Integer>();
		tags = Util.getTagList();
		vocabulary = new HashSet<String>();
		int count = 0;
		for (String t: Util.getTagList()) {
			nodeIndex.put(t, count);
			count++;
		}
		transitionCountMatrix = new int[count][count];
		transitionProbabilityMatrix = new double[count][count];
		emissionCount = new ArrayList<HashMap<String, Integer>>();
		emissionProbabilities = new ArrayList<HashMap<String, Double>>();
		for (int i = 0; i < count; i++) {
			emissionCount.add(new HashMap<String, Integer>());
			emissionProbabilities.add(new HashMap<String, Double>());
		}
	}

	private int index(String tag) {
		return nodeIndex.get(tag);
	}

	private int transitionCount(int state1, int state2) {
		return transitionCountMatrix[state1][state1];
	}

	private double transitionProbability(int state1, int state2) {
		return transitionProbabilityMatrix[state1][state2];
	}

	private double emissionProbability(int state, String word) {
		if (!emissionProbabilities.get(state).containsKey(word)) {
			// TODO: unknown word
		}
		return emissionProbabilities.get(state).get(word);
	}

	private void addVocabulary(String word) {
		vocabulary.add(word);
	}

	private void addEmissionCount(String tag, String word) {
		int ind = index(tag);
		HashMap<String, Integer> map = emissionCount.get(ind);
		if (!map.containsKey(word)) {
			map.put(word, 1);
		}
		else {
			map.put(word, map.get(word) + 1);
		}
	}

	private void addTransitionCount(String tag1, String tag2) {
		int i1 = index(tag1);
		int i2 = index(tag2);
		transitionCountMatrix[i1][i2]++;
	}

	public void processTrainingSample(String line) {
		TaggedWord[] taggedWords = segmentLineIntoTaggedWord(line);
		addTransitionCount(Constant.TAG_START, taggedWords[0].getTag());
		for (int i=0; i<taggedWords.length; i++) {
			if (i == taggedWords.length-1) {
				addTransitionCount(taggedWords[i].getTag(), Constant.TAG_END);
			}
			else {
				addTransitionCount(taggedWords[i].getTag(), taggedWords[i+1].getTag());
			}
			addVocabulary(taggedWords[i].getWord());
			addEmissionCount(taggedWords[i].getWord);
		}
	}
}

