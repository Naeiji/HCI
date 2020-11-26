package snake;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Save {
    public static void setScore(String name, int[] scores, int[] times) {

        try {
            BufferedWriter writter = new BufferedWriter(new FileWriter(name + times[0] + ".txt"));
            writter.write(name);
            writter.newLine();
            for (int i=0; i<5; i++) {
                writter.write(scores[i] + " " + times[i]);
                writter.newLine();
            }
            writter.close();

        } catch (Exception e) {
            //Exception
        }
    }
}
