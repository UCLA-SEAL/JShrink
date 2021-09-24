public class Application {
    public static void main(String[] args){
        Example example = new Example() {
            @Override
            void method() {
                System.out.println("Hello world!");
            }
        };
        example.method();
    }
}
