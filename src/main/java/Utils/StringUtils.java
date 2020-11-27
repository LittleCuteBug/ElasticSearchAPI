package Utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class StringUtils {
    public static String removeAccents(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static String removeStopWordsSchool(String str) {
        String[] stopWords = {" dai hoc ",
                " dh ",
                " hoc vien ",
                " va ",
                " university ",
                " college ",
                " of ",
                " and ",
            "-","_","~",".",",","(",")","[","]","{","}"};
        str = " "+str+" ";
        for(String parse:stopWords)
        {
            str = str.replace(parse," ");
        }
        return str;
    }

    public static String removeStopWordsWork(String str) {
        String[] stopWords = {" cong ty "," tnhh "," tap doan "," trach nhiem huu han ",
                " of ",
                " and ",
                "-","_","~",".","(",")","[","]","{","}"};
        str = " "+str+" ";
        for(String parse:stopWords)
        {
            str = str.replace(parse," ");
        }
        return str;
    }
}
