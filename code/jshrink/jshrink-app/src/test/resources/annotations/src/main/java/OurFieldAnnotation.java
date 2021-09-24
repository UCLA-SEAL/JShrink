import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface OurFieldAnnotation {
    boolean function() default true;
    String id();
    String label() default "";
    String[] labelArray() default {};
}
