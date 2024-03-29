import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.Serializable;

public class HiddenMarkovModel implements Serializable {
	private HashMap<String, Integer> nodeIndex;
	private String[] tags;
	private int numTags;
	private HashSet<String> vocabulary;
	private int vocabSize;
	private int[][] transitionCountMatrix;
	private double[][] transitionProbabilityMatrix;
	private ArrayList<HashMap<String, Integer>> emissionCount;
	private int[] emissionSum;
	private ArrayList<HashMap<String, Double>> emissionProbabilities;
	private int[] upperCaseCount;
	private int[] lowerCaseCount;
	private double[] upperCaseProbability;
	private double[] lowerCaseProbability;
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
		for (int i = 0; i < numTags; i++) {
			emissionCount.add(new HashMap<String, Integer>());
			emissionProbabilities.add(new HashMap<String, Double>());
		}
		emissionSum = new int[numTags];
		upperCaseCount = new int[numTags];
		lowerCaseCount = new int[numTags];
		upperCaseProbability = new double[numTags];
		lowerCaseProbability = new double[numTags];
	}

	public int index(String tag) {
		return nodeIndex.get(tag);
	}

	private int getTransitionCount(int state1, int state2) {
		return transitionCountMatrix[state1][state1];
	}

	private double getTransitionProbability(int state1, int state2) {
		return transitionProbabilityMatrix[state1][state2];
	}

	private double getEmissionProbability(int state, String word) {
		if (state==0 || state== numTags-1) return 0;
		if (!emissionProbabilities.get(state).containsKey(word)) {
			// TODO: unknown word
			int T = emissionProbabilities.get(state).size();
			// +1 for unknown word
			int Z = vocabSize - T + 1;
			int C = emissionSum[state];
			return T*1.0/(Z*(C+T));

		}
		return emissionProbabilities.get(state).get(word);
	}

	private double getCapitalProb(int state, String word) {
		if (Character.isLowerCase(word.charAt(0))) return lowerCaseProbability[state];
		return upperCaseProbability[state];
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

	private void addCapitalCount(String tag, String word) {
		int ind = index(tag);
		if (Character.isLowerCase(word.charAt(0))) {
			lowerCaseCount[ind] ++;
		}
		else upperCaseCount[ind] ++;
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
			String word = taggedWords[i].getWord();
			String tag = taggedWords[i].getTag();
			addVocabulary(word);
			addEmissionCount(tag, word);
			addCapitalCount(tag, word);
		}
		vocabSize = vocabulary.size();
	}

	public void calculateEmissionProbNaive() {
		emissionProbabilities = new ArrayList<HashMap<String, Double>>();
		for (int i = 0; i < numTags; i++) {
			emissionProbabilities.add(new HashMap<String, Double>());
		}
		if (debug) {
			System.out.println("tags.length = " + tags.length);
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

	public void calculateEmissionProbWittenBell() {
		emissionProbabilities = new ArrayList<HashMap<String, Double>>();
		for (int i = 0; i < numTags; i++) {
			emissionProbabilities.add(new HashMap<String, Double>());
		}
		for (int i=0; i<tags.length; i++) {
			int sum = 0;
			for (String observation: emissionCount.get(i).keySet()) {
				sum += emissionCount.get(i).get(observation);
			}
			emissionSum[i] = sum;
			for (String observation: emissionCount.get(i).keySet()) {
				int occurence = emissionCount.get(i).get(observation);
				emissionProbabilities.get(i).put(observation, occurence*1.0/(sum + emissionCount.get(i).size()));
			}
		}
	}

	public void calculateTransitionProbNaive() {
		if (debug) {
			System.out.println("tags.length = " + tags.length);
		}

		for (int i=0; i<tags.length; i++) {
			int sum = 0;
			for (int j=0; j<tags.length; j++) {
				sum += transitionCountMatrix[i][j];
			}
			for (int j=0; j<tags.length; j++) {
				if ( j==0 || i==tags.length-1) {
					continue;
				}
				transitionProbabilityMatrix[i][j] = transitionCountMatrix[i][j]*1.0 / sum;
			}
		}
	}

	public void calculateTransitionProbWittenBell() {
		for (int i=0; i<tags.length-1; i++) {
			int sum = 0;
			int T = 0;
			for (int j=0; j<tags.length; j++) {
				sum += transitionCountMatrix[i][j];
				if (transitionCountMatrix[i][j] > 0) T++;
			}
			int Z;
			if (i==0) Z = numTags -2 - T;
			else Z = numTags-1 - T;
			for (int j=0; j<tags.length; j++) {
				if ( j==0 || i==tags.length-1) {
					continue;
				}
				if (transitionCountMatrix[i][j] > 0) {
					transitionProbabilityMatrix[i][j] = transitionCountMatrix[i][j]*1.0 / (sum + T);
				}
				else {
					transitionProbabilityMatrix[i][j] = T*1.0/(Z*(sum+T));
				}
			}
		}
	}

	public void calculateCapitalProbability() {
		for (int i=1; i<numTags-1; i++) {
			lowerCaseProbability[i] = lowerCaseCount[i] * 1.0 / (lowerCaseCount[i] + upperCaseCount[i]);
			upperCaseProbability[i] = upperCaseCount[i] * 1.0 / (lowerCaseCount[i] + upperCaseCount[i]);
		}
	}

	public String[] runViterbi(String[] words) {
		int n = words.length + 2;
		double[][] dp = new double[n][numTags];
		int[][] trace = new int[n][numTags];
		for (int i=0; i<n; i++) {
			for (int j=0; j<numTags; j++) {
				dp[i][j] = Double.NEGATIVE_INFINITY;
			}
		}
		dp[0][0] = 0;
		for (int i=1; i<n-1; i++) {
			for (int j=0; j<numTags; j++) {
				double b = getEmissionProbability(j, words[i-1]) * getCapitalProb(j, words[i-1]);
				for (int k=0; k<numTags; k++) {
					double tmp = dp[i-1][k] + Math.log(getTransitionProbability(k, j) * b);
					if (tmp > dp[i][j]) {
						dp[i][j] = tmp;
						trace[i][j] = k;
					}
				}
			}
		}
		for (int k = 0; k<numTags; k++) {
			double tmp = dp[n-2][k] + Math.log(getTransitionProbability(k, numTags-1));
			if (tmp > dp[n-1][numTags-1]) {
				dp[n-1][numTags-1] = tmp;
				trace[n-1][numTags-1] = k;
			}
		}
		// for (int i=0; i<n; i++) {
		// 	for (int j=0; j<numTags; j++) {
		// 		if (dp[i][j] > Double.NEGATIVE_INFINITY) System.out.printf("%2.0f ", dp[i][j]);
		// 		else System.out.printf("bla ");
		// 	}
		// 	System.out.println();
		// }
		// for (int i=numTags-1; i<numTags; i++) {
		// 	for (int j=0; j<numTags; j++) {
		// 		System.out.print(transitionProbabilityMatrix[i][j] + " ");
		// 	}
		// 	System.out.println();
		// }

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

	public double calculateLikelihood(String[] words, String[] tagList) {
		double res = getTransitionProbability(index(tags[0]), index(tagList[0]));
		res *= getEmissionProbability(index(tagList[0]), words[0]);
		for (int i=1; i<words.length; i++) {
			res *= getEmissionProbability(index(tagList[i]), words[i]);
			res *= getTransitionProbability(index(tagList[i-1]), index(tagList[i]));
		}
		res *= getTransitionProbability(index(tagList[tagList.length-1]), numTags-1);
		return Math.log(res);
	}
}

