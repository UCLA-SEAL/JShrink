public class Main {
    public static void main(String[] args) {
        SubB subB = new SubB();
        SubA subA = new SubA(subB);
        subA.print();
    }
}