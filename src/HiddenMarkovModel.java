import java.util.HashMap;
import java.util.ArrayList;

public class HiddenMarkovModel {
	private HashMap<String, Integer> nodeIndex;
	private int numTags;
	private int[][] transitionCountMatrix;
	private double[][] transitionProbabilityMatrix;
	private ArrayList<HashMap<String, Double>> emissionProbabilities;

	public HiddenMarkovModel() {
		nodeIndex = new HashMap<String, Integer>();
		int count = 0;
		for (String t: Util.getTagList()) {
			nodeIndex.put(t, count);
			count++;
		}
		numTags = count;
		transitionCountMatrix = new int[count][count];
		transitionProbabilityMatrix = new double[count][count];
		for (int i = 0; i < count; i++) {
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
}

