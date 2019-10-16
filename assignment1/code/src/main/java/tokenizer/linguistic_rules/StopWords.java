
package tokenizer.linguistic_rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class StopWords implements LinguisticRule {

    private ArrayList<String> stopwords = new ArrayList<>();
    private File file = new File("snowball_stopwords_EN.txt");
    private Scanner sc;
    {
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<String> readStopWords() {
        while (this.sc.hasNextLine()){
            this.stopwords.add(this.sc.nextLine());
        }
        return this.stopwords;
    }

}
