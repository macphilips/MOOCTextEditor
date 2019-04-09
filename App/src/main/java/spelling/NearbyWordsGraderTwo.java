package spelling;

import java.io.PrintWriter;
import java.util.List;

public class NearbyWordsGraderTwo {
    public static void main(String args[]) {
        int tests = 0;
        int incorrect = 0;
        String feedback = "";
        PrintWriter out;

        try {
            out = new PrintWriter("grader_output/module5.part2.out");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            Dictionary d = new DictionaryHashSet();
            DictionaryLoader.loadDictionary(d, "test_cases/dict2.txt");
            NearbyWords nw = new NearbyWords(d);

            feedback += "** TextStyleInfo 1: 2 suggestions... ";
            List<String> d1 = nw.suggestions("dag", 4);
            feedback += "" + d1.size() + " suggestions returned.\n";

            feedback += "** TextStyleInfo 2: Checking suggestion correctness... ";
            feedback += "Suggestions: ";
            for (String i : d1) {
                feedback += i + ", ";
            }

            feedback += "\n** TextStyleInfo 3: 3 suggestions... ";
            d1 = nw.suggestions("fare", 3);
            feedback += "" + d1.size() + " suggestions returned.\n";

            feedback += "** TextStyleInfo 4: Checking suggestion correctness... ";
            feedback += "Suggestions: ";
            for (String i : d1) {
                feedback += i + ", ";
            }
            feedback += "\n";
            
        } catch (Exception e) {
            out.println(feedback + "Runtime error: " + e);
            return;
        }

        out.println(feedback += "Tests complete. Make sure everything looks right.");
        out.close();
    }
}
