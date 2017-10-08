import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class HiddenMarkovModel {
	private HashMap<String, Integer> nodeIndex;
	private String[] tags;
	private int numTags;
	private HashSet<String> vocabulary;
	private int[][] transitionCountMatrix;
	private double[][] transitionProbabilityMatrix;
	private ArrayList<HashMap<String, Integer>> emissionCount;
	private ArrayList<HashMap<String, Double>> emissionProbabilities;
	private boolean debug = true;

	public HiddenMarkovModel() {
		nodeIndex = new HashMap<String, Integer>();
		tags = Util.getTagList();
		vocabulary = new HashSet<String>();
		int count = 0;
		for (String t: Util.getTagList()) {
			nodeIndex.put(t, count);
			count++;
		}
		numTags = count;
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

	private int getTransitionCount(int state1, int state2) {
		return transitionCountMatrix[state1][state1];
	}

	private double getTransitionProbability(int state1, int state2) {
		return transitionProbabilityMatrix[state1][state2];
	}

	private double getEmissionProbability(int state, String word) {
		if (!emissionProbabilities.get(state).containsKey(word)) {
			// TODO: unknown word
			return 0;
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
		TaggedWord[] taggedWords = Util.segmentLineIntoTaggedWord(line);
		addTransitionCount(Constant.TAG_START, taggedWords[0].getTag());
		for (int i=0; i<taggedWords.length; i++) {
			if (i == taggedWords.length-1) {
				addTransitionCount(taggedWords[i].getTag(), Constant.TAG_END);
			}
			else {
				addTransitionCount(taggedWords[i].getTag(), taggedWords[i+1].getTag());
			}
			addVocabulary(taggedWords[i].getWord());
			addEmissionCount(taggedWords[i].getTag(), taggedWords[i].getWord());
		}
	}

	public void calculateEmissionProbNaive() {
		emissionProbabilities = new ArrayList<HashMap<String, Double>>();
		if (debug) {
			System.out.print("tags.length = " + tags.length);
		}
		for (int i=0; i<tags.length; i++) {
			int sum = 0;
			for (String observation: emissionCount.get(i).keySet()) {
				sum += emissionCount.get(i).get(observation);
			}
			for (String observation: emissionCount.get(i).keySet()) {
				int occurence = emissionCount.get(i).get(observation);
				emissionProbabilities.get(i).put(observation, occurence*1.0/sum);
			}
		}
	}

	public void calculateTransitionProbNaive() {
		if (debug) {
			System.out.print("tags.length = " + tags.length);
		}
		for (int i=0; i<tags.length; i++) {
			int sum = 0;
			for (int j=0; j<tags.length; j++) {
				sum += transitionCountMatrix[i][j];
			}
			for (int j=0; j<tags.length; j++) {
				transitionProbabilityMatrix[i][j] = transitionCountMatrix[i][j]*1.0 / sum;
			}
		}
	}

	public String[] runViterbi(String[] words) {
		int n = words.length + 2;
		double[][] dp = new double[n][numTags];
		int[][] trace = new int[n][numTags];
		dp[0][0] = 1;
		for (int i=1; i<n-1; i++) {
			for (int j=0; j<numTags; j++) {
				dp[i][j] = -1;
				trace[i][j] = -1;
				double b = getEmissionProbability(j, words[i-1]);
				for (int k=0; k<numTags; k++) {
					double tmp = dp[i-1][k] * getTransitionProbability(k, j) * b;
					if (tmp > dp[i][j]) {
						dp[i][j] = tmp;
						trace[i][j] = k;
					}
				}
			}
		}
		dp[n-1][numTags-1] = -1;
		trace[n-1][numTags-1] = -1;
		for (int k = 0; k<numTags; k++) {
			double tmp = dp[n-1][k] * getTransitionProbability(k, numTags-1);
			if (tmp > dp[n-1][numTags-1]) {
				dp[n-1][numTags-1] = tmp;
				trace[n-1][numTags-1] = k;
			}
		}
		int[] res = new int[words.length];
		int cur = numTags - 1;
		for (int i=0; i<words.length; i++) {
			res[i] = trace[n-1-i][cur];
			cur = res[i];
		}
		String[] ret = new String[words.length];
		for (int i=0; i<words.length; i++) {
			ret[i] = tags[res[words.length-1-i]];
		}
		return ret;
	}
}

