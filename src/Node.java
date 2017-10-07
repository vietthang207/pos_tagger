
public class Node {
	private Tag tag;
	private HashMap<String, Double> emissionProbabilities;

	public Node(Tag tag) {
		this.tag = tag;
		emissionProbabilities = new HashMap<String, Double>();
	}
}

class Token {
	private TokenType type;
	private String word;

	public Token (WordType type, String word) {
		this.type = type;
		this.word = word;
	}

	public TokenType getType() {
		return type;
	}

	public String getWord() {
		return word;
	}

}

enum TokenType {
	NORMAL, UNKNOWN
}

