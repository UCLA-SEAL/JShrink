import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface OurMethodAnnotation {
    boolean function() default true;
    String id();
    String label() default "";
    String[] labelArray() default {};
}
