public class B {
    String lastName;
    String firstName;

    public B(String s1) {
        this.lastName = s1;
    }

    public B(String s1, String s2) {
        this.lastName = s1;
        this.firstName = s2;
    }

    public boolean hasSameLastName(String s) {
        return this.lastName.equals(s);
    }

    public boolean hasSameFirstName(String s) {
        return this.firstName.equals(s);
    }

    public boolean hasSameName(String s1, String s2) {
        return this.lastName.equals(s1) && this.firstName.equals(s2);
    }
}