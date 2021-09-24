
public class References {

	public static String unusedMethodReference(String input){
		return "unusedMethodReference has been called by " + input;
	}

	public static String usedMethodReference(String input){
		return "usedMethodReference has been called by " + input;
	}

	public static String usedInCollectionMethodReference(String input){
		return "usedInJoiningReference has been called by " + input;
	}
}
