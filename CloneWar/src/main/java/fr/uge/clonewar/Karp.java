package fr.uge.clonewar;

public class Karp {

    public static boolean rabinKarp(String str, String pattern){
        var hashPattern = pattern.hashCode();
        for (int i = 0; i < str.length() - pattern.length() + 1; i++) {
            var subString =  str.substring(i, i + pattern.length());
            var hash = subString.hashCode();
            if(hash == hashPattern && subString.equals(pattern))
                return true;
        }
        return false;
    }

    //pour les tests
    public static void main(String[] args) {
        var s = "ccc";
        var c = "ccc";
        var a = "aac";
        System.out.println(rabinKarp(s, c));
        System.out.println(rabinKarp(s, a));
        System.out.println(rabinKarp(a, "ac"));
        System.out.println(rabinKarp(s, "ab"));
        System.out.println(rabinKarp(s, "aaaa"));
    }
}
