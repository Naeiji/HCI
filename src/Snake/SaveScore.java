package Snake;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class SaveScore {
	private static int score;
	private static String name;

	public static void setScore(int s, String n){
		score = s;
		name = n;
		
		try {
			if(score > LoadScore.getScore()){
				BufferedWriter writter = new BufferedWriter(new FileWriter("Scores.txt"));
				writter.flush();
				writter.write(Integer.toString(score));
				writter.newLine();
				writter.write(name);
				writter.close();
			}
			
		} catch (Exception e) {
			//Exception
		}
	}
	
	public static void resetScore(){
		
		try {
			if(score > LoadScore.getScore()){
				BufferedWriter writter = new BufferedWriter(new FileWriter("Scores.txt"));
				writter.flush();
				writter.write(0);
				writter.newLine();
				writter.write("Player");
				writter.close();
			}
			
		} catch (Exception e) {
			//Exception
		}
	}
}
