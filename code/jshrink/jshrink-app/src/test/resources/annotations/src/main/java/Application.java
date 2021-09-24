public class Application {

    @OurFieldAnnotation(id="STRING_VALUE")
    private final static String STRING_VALUE = "Hello!";

    public static void main(String[] args){
        Application app = new Application();
        app.sayHello();
    }

    @OurMethodAnnotation(id = "sayHello", function=false)
    private void sayHello(){
        System.out.println(STRING_VALUE);
    }

    private void unusedMethod(){
        System.out.println("UnusedMethod");
    }
}
