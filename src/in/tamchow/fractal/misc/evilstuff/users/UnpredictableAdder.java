package in.tamchow.fractal.misc.evilstuff.users;
import in.tamchow.fractal.misc.evilstuff.EvilStuff;
/**
 * A class which implements a calculator which provides unpredictable additions.
 * <p/>
 * Uses {@link EvilStuff}
 *
 * @author Tamoghna Chowdhury
 * @version 19.03.2016
 */
public class UnpredictableAdder {
    private static void repeatedlyDoEvilStuffToInteger(int waitPeriod) {
        EvilStuff.repeatedlyDoEvilStuff(Integer.class, "cache", "Evil_Thread_", waitPeriod);
    }
    public static void main(String[] args) {
        java.util.Scanner in = new java.util.Scanner(System.in);
        do {
            System.out.println("\nEnter 2 numbers between -64 (-128/2) and 63 (127/2):");
            try {
                int n1 = in.nextInt(), n2 = in.nextInt();
                repeatedlyDoEvilStuffToInteger(Math.max(n1, n2));
                System.out.format("%d + %d = %d , no, really it's %d",
                        new Integer(n1), new Integer(n2), n1 + n2, new Integer(n1 + n2));
            } catch (NumberFormatException | java.util.InputMismatchException nonIntegerException) {
                System.out.println("I won't be more unpredictable if you don't let me :( -> " +
                        "\nI didn't expect you to enter: " +
                        nonIntegerException.getMessage());
                try {
                    EvilStuff.stopDoingEvilStuff();
                } catch (InterruptedException interrupted) {
                    interrupted.printStackTrace();
                } finally {
                    break;
                }
            }
        } while (true);
    }
}