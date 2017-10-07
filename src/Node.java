
public class Node {
	private Tag tag;
	private HashMap<Token, Double> emissionProbabilities;

	public Node(Tag tag) {
		this.tag = tag;
		emissionProbabilities = new HashMap<Token, Double>();
	}
}

class Token {
	private TokenType type;
	private String word;

	public Word(WordType type, String word) {
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

enum Tag {
	PRP, RB
}