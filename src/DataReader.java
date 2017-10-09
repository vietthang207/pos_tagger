import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DataReader {
	FileReader fileReader;
	BufferedReader bufferedReader;

	public DataReader(String fileName) throws IOException {
		File file = new File(fileName);
		fileReader = new FileReader(file);
		bufferedReader = new BufferedReader(fileReader);
	}

	public String nextLine() throws IOException {
		String line = bufferedReader.readLine();
		return line;
	}

	public void close() throws IOException {
		bufferedReader.close();
		fileReader.close();
	}
}