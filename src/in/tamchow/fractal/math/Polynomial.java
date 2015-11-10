package in.tamchow.fractal.math;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Represents a polynomial and provides standard methods
 */
public class Polynomial {
    ArrayList<Term> terms;
    ArrayList<String> signs;

    public Polynomial() {
        terms = new ArrayList<>();
        signs = new ArrayList<>();
    }

    public static Polynomial fromString(String polynomial) {
        Polynomial poly = new Polynomial();
        StringTokenizer tokenizer = new StringTokenizer(polynomial.trim(), ";", false);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("+") || token.equals("-")) {
                poly.signs.add(token.trim());
            } else {
                poly.terms.add(Term.fromString(token.trim()));
            }
        }
        if (poly.signs.size() == poly.terms.size() - 1 || (!poly.signs.get(0).equals("-"))) {
            poly.signs.add(0, "+");
        }
        return poly;
    }

    public String derivative() {
        String deriv = "";
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            deriv += " " + signs.get(j) + " " + terms.get(i).derivative();
        }
        return deriv.trim();
    }

    public String toString() {
        String polynomial = "";
        for (int i = 0, j = 0; i < terms.size() && j < signs.size(); i++, j++) {
            polynomial += " " + signs.get(j) + " " + terms.get(i);
        }
        if (polynomial.trim().charAt(0) == '+') {
            return polynomial.trim().substring(1, polynomial.trim().length());
        }
        return polynomial.trim();
    }
}