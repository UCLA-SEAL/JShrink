import java.lang.reflect.Method;

public class StandardStuff {
	private static final String HELLO_WORLD_STRING = "Hello world";
	private static final String GOODBYE_STRING="Goodbye";
	private final int integer;

	public StandardStuff(){
		String temp = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
		this.integer = 6;
		try{
			Method method = StandardStuff.class.getDeclaredMethod("touchedViaReflection");
			method.setAccessible(true);
			Object o = method.invoke(null);
		} catch(Exception e){
			System.out.println("Here reached");
			e.printStackTrace();
		}
	}

	private static void touchedViaReflection(){
                System.out.println("touchedViaReflection touched");
        }

	public String getString(){
		String temp = "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD";
		return getStringStatic(this.integer);
	}

	private static String getStringStatic(int theInteger){
		String temp = "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE";
		System.out.println("getStringStatic touched");
		if(theInteger == 6){
			return HELLO_WORLD_STRING;
		} else if(theInteger == 7){
			return GOODBYE_STRING;
		}

		return "";
	}

	public void publicAndTestedButUntouched(){
		String temp = "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD";
		String temp2 = "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY";
		String temp3 = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		System.out.println("publicAndTestedButUntouched touched");
		publicAndTestedButUntouchedCallee();
	}

	public void publicAndTestedButUntouchedCallee(){
		String temp = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		System.out.println("publicAndTestedButUntouchedCallee touched");
		int i=0;
		i++;
		i=i+10;
	}

	public void publicNotTestedButUntouched(){
		String temp = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
		System.out.println("publicNotTestedButUntouched touched");
		publicNotTestedButUntouchedCallee();
	}

	public void publicNotTestedButUntouchedCallee(){
		String temp = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		System.out.println("publicNotTestedButUntouchedCallee touched");
		int i=0;
		i++;
		i=i+10;
	}

	private int privateAndUntouched(){
		String temp = "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ";
		System.out.println("privateAndUntouched touched");
		int i=0;
		i++;
		i++;
		return i;
	}
}
