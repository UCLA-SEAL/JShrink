public class SubClass extends SimpleClass {
    String f3;

    public SubClass (String s1, String s2) {
        super(s1);
        this.f3 = s2;
    }

    public void setValue(String s) {
        this.f1 = s;
    }

    public static void main(String[] args) {
        SubClass ab = new SubClass("a", "b");
        System.out.println(ab.f1 + ab.f3);
    }
}