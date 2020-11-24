package Snake;

import java.io.BufferedReader;
import java.io.FileReader;

public class LoadScore {
	private static int bestScore;
	private static String bestName;

	private static void load(){
		try {
			
			BufferedReader buffer = new BufferedReader(new FileReader("Scores.txt"));
			bestScore = Integer.parseInt(buffer.readLine());
			bestName  = buffer.readLine();
			buffer.close();
			
		} catch (Exception e) {
			//Exception
		}
	}

	public static int getScore(){
		load();
		return bestScore;
	}
	
	public static String getName(){
		load();
		return bestName;
	}

	public static void main(String[] args) {

	}
}
