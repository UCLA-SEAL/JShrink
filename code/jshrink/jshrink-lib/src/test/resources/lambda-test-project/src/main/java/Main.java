public class Main {
	public static void main(String[] args){
		NumericTest isEven = (n) -> StandardStuff.isEven(n);
		NumericTest isNegNum = (n) -> {
			if(Main.isNegativeNumber(n)){
				return true;
			}
			return false;
		};

		int num = 10;
		if(isEven.computeTest(num)){
			System.out.println(num + " is an even number");
		} else {
			System.out.println(num + " is not an even number");
		}

		if(isNegNum.computeTest(num)){
			System.out.println(num + " is a negative number");
		} else {
			System.out.println(num + " is not a negative number");
		}
	}


	private static boolean isNegativeNumber(int num){
		return (num < 0);
	}

	private static boolean methodNotUsed(int num){
		return (num < 0);
	}

}
