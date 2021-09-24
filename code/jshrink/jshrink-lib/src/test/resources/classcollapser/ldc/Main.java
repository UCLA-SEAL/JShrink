public class Main {
    public static void main(String[] args) {
        B b = new B();
        if (b.getClass().equals(B.class)) {
            System.out.println("correct!");
        } else {
            System.out.println("error occured");
        }
    }
}
