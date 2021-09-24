import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class Main {
	public static void main(String[] args){
		Function<String, String> unusedMethod = References::unusedMethodReference;
		Function<String, String> usedMethod = References::usedMethodReference;

		getCollection();

		Function<String, String> innerClassMethod = InnerClass::innerClassMethodReference;

		System.out.println(usedMethod.apply("main"));
	}



	public static Collection getCollection() {
		Set<Function<String, String>> toReturn = new HashSet<Function<String, String>>();
		toReturn.add(References::usedInCollectionMethodReference);
		return toReturn;
	}

	public static class InnerClass{
		private static String innerClassMethodReference(String input){
			return "innerClassMethodReference " + input;
		}
	}

}
