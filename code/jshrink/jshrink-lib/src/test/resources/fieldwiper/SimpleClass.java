public class SimpleClass {
    String f1;
    String f2; // unused

    public SimpleClass(String s1) {
        f1 = s1;
    }

    public SimpleClass(String s1, String s2) {
        f1 = s1;
        f2 = s2;
    }

    public static void main(String[] args) {
        SimpleClass a = new SimpleClass("A");
        System.out.println(a.f1);
    }
}