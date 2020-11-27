package alien;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Save {
    public static void setScore(int[] scores, String[] accuracies) {

        try {
            BufferedWriter writter = new BufferedWriter(new FileWriter("file" + scores[0] + ".txt"));
            for (int i=0; i<5; i++) {
                writter.write(scores[i] + " " + accuracies[i]);
                writter.newLine();
            }
            writter.close();

        } catch (Exception e) {
            //Exception
        }
    }
}
