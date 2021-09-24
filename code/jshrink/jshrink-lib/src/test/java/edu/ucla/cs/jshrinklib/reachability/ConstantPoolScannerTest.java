package edu.ucla.cs.jshrinklib.reachability;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertTrue;

public class ConstantPoolScannerTest {

	public static File clazz;
	@BeforeClass
	public static void setup(){
		clazz = new File(ConstantPoolScannerTest.class.getClassLoader().getResource("simple-test-project"
				+ File.separator + "target" + File.separator + "classes"+File.separator+"Main.class").getFile());
	}
	@Test(expected=Exception.class)
	public void testjavapScanException() throws IOException, InterruptedException {
		File f = null;
		ConstantPoolScanner.getClassReferences(f);
	}
	@Test(expected=Exception.class)
	public void testjavapScanException2() throws IOException, InterruptedException {
		ConstantPoolScanner.getClassReferences(new File(""));
	}
	@Test
	public void testClassReferenceOutput() throws IOException, InterruptedException{
		Set<String> outputStream = ConstantPoolScanner.getClassReferences(clazz);
		Set<String> references = new HashSet<>();
		references.add("StandardStuff");
		references.add("Main$1");
		references.add("edu.ucla.cs.onr.test.LibraryClass");
		references.add("Main");
		assert(references.size() == outputStream.size());
		assertTrue(outputStream.containsAll(references));
		assertTrue(references.containsAll(outputStream));
	}
	@Test @Ignore
	public void generictestClassReference() throws IOException, InterruptedException{
		Set<String> outputStream = ConstantPoolScanner.getClassReferences("/tmp/junit4_4104879256806578401/target/classes/org/junit/runners/MethodSorters.class");
		System.out.println("".join("\n",outputStream));
	}
	@Test
	public void testClassReferenceOutput2() throws IOException, InterruptedException{
		Set<String> outputStream = ConstantPoolScanner.getClassReferences(ConstantPoolScannerTest.class.getClassLoader().getResource("Jama-1.0.3"
				+ File.separator + "Jama" + File.separator + "Matrix.class").getPath());
		Set<String> references = new HashSet<>();
		references.add("Jama.Matrix");
		references.add("Jama.SingularValueDecomposition");
		references.add("Jama.LUDecomposition");
		references.add("Jama.QRDecomposition");
		references.add("Jama.CholeskyDecomposition");
		references.add("Jama.EigenvalueDecomposition");
		//references.add("D");
		references.add("Jama.util.Maths");

		assert(references.size() == outputStream.size());
		assertTrue(outputStream.containsAll(references));
		assertTrue(references.containsAll(outputStream));
	}
	@Test
	public void testgetConstantPoolOutput() throws IOException, InterruptedException{
		List<String> outputStream = ConstantPoolScanner.getConstantPool(clazz).stream().map(x->x.toString()).map(x->{
			if(x.endsWith(":"))
				return x.substring(0,x.length()-1);
			else
				return x;
		}).collect(toList());
		ArrayList<String> cp = new ArrayList<>();
		cp.add("#1:Methodref:#20.#56:Main.compare:(Ljava.lang.Integer;Ljava.lang.Integer;)I");
		cp.add("#2:Methodref:#21.#57:java.lang.Object.<init>:()V");
		cp.add("#3:String:#58:Lorem ipsum dolor sit amet, prima adipisci et est, mel et purto duis ludus. Vix mollis ancillae te. Eu pro purto soleat consetetur. Vix eripuit reprehendunt id. Audire vidisse aperiri eu sed, incorrupte scripserit signiferumque ad est, omnes platonem ex sea. His libris invenire eu. Falli tractatos qui ea, officiis recusabo convenire ea eos, pri hinc oratio delenit an.Omnesque conceptam appellantur ei vel, an quo possim audiam. Consulatu vituperatoribus nam ea, eos an paulo copiosae. Paulo dolor ei his, eam eu minim partem, saepe putent concludaturque vis ex. Nibh consulatu interpretaris pri id, ut urbanitas delicatissimi mei. Te vim aperiam principes assueverit, ea purto imperdiet dissentiunt eos, ex autem mucius iuvaret quo.In his nibh partiendo ocurreret. Probatus corrumpit molestiae ei ius. In qui dictas doctus atomorum, illum vocent cotidieque no sit, ne mei discere facilis lucilius. Mei efficiendi reformidans theophrastus ea, no vis erat novum laoreet, atqui euripidis mea cu. Ex omnes omnesque cum.An eam prima dicta eligendi, dictas option repudiandae no nam. Nisl vero ei duo. Pericula posidonium eu pri, et ius tale constituam. Rebum veritus in ius. Ut mei dicit repudiandae, vim an primis propriae efficiendi, et quas debitis laboramus eam. Cum elitr principes ei.Prima nulla eligendi ex eum, saperet debitis ullamcorper et cum, ut ius autem denique expetendis. Nobis adversarium an qui, nec no melius iuvaret. Eum mundi tantas eu, novum aperiam pri ei. Eam reprimique neglegentur delicatissimi eu, molestie iudicabit ius ne, ullum dolore animal ei cum. Dolorum nusquam eleifend et pri, in errem mentitum sed.");
		cp.add("#4:Class:#59:StandardStuff");
		cp.add("#5:Methodref:#4.#57:StandardStuff.<init>:()V");
		cp.add("#6:Methodref:#4.#60:StandardStuff.getString:()Ljava.lang.String;");
		cp.add("#7:Fieldref:#4.#61:StandardStuff.f1:Ljava.lang.String;");
		cp.add("#8:Fieldref:#4.#62:StandardStuff.f2:Ljava.lang.String;");
		cp.add("#9:Class:#63:edu.ucla.cs.onr.test.LibraryClass");
		cp.add("#10:Methodref:#9.#57:edu.ucla.cs.onr.test.LibraryClass.<init>:()V");
		cp.add("#11:Methodref:#9.#64:edu.ucla.cs.onr.test.LibraryClass.getNumber:()I");
		cp.add("#12:Class:#65:java.util.ArrayList");
		cp.add("#13:Methodref:#12.#57:java.util.ArrayList.<init>:()V");
		cp.add("#14:Methodref:#66.#67:java.lang.Integer.valueOf:(I)Ljava.lang.Integer;");
		cp.add("#15:InterfaceMethodref:#68.#69:java.util.List.add:(Ljava.lang.Object;)Z");
		cp.add("#16:Class:#70:Main$1");
		cp.add("#17:Methodref:#16.#57:Main$1.<init>:()V");
		cp.add("#18:Methodref:#71.#72:java.util.Collections.sort:(Ljava.util.List;Ljava.util.Comparator;)V");
		cp.add("#19:Methodref:#66.#73:java.lang.Integer.intValue:()I");
		cp.add("#20:Class:#74:Main");
		cp.add("#21:Class:#75:java.lang.Object");
		cp.add("#22:Utf8:InnerClasses");
		cp.add("#23:Utf8:<init>");
		cp.add("#24:Utf8:()V");
		cp.add("#25:Utf8:Code");
		cp.add("#26:Utf8:LineNumberTable");
		cp.add("#27:Utf8:LocalVariableTable");
		cp.add("#28:Utf8:this");
		cp.add("#29:Utf8:LMain;");
		cp.add("#30:Utf8:main");
		cp.add("#31:Utf8:([Ljava.lang.String;)V");
		cp.add("#32:Utf8:args");
		cp.add("#33:Utf8:[Ljava.lang.String;");
		cp.add("#34:Utf8:temp");
		cp.add("#35:Utf8:Ljava.lang.String;");
		cp.add("#36:Utf8:s");
		cp.add("#37:Utf8:LStandardStuff;");
		cp.add("#38:Utf8:testFieldAccess");
		cp.add("#39:Utf8:testStaticFieldAccess");
		cp.add("#40:Utf8:lc");
		cp.add("#41:Utf8:Ledu.ucla.cs.onr.test.LibraryClass;");
		cp.add("#42:Utf8:toSort");
		cp.add("#43:Utf8:Ljava.util.List;");
		cp.add("#44:Utf8:LocalVariableTypeTable");
		cp.add("#45:Utf8:Ljava.util.List<Ljava.lang.Integer;>;");
		cp.add("#46:Utf8:compare");
		cp.add("#47:Utf8:(Ljava.lang.Integer;Ljava.lang.Integer;)I");
		cp.add("#48:Utf8:one");
		cp.add("#49:Utf8:Ljava.lang.Integer;");
		cp.add("#50:Utf8:two");
		cp.add("#51:Utf8:access$000");
		cp.add("#52:Utf8:x0");
		cp.add("#53:Utf8:x1");
		cp.add("#54:Utf8:SourceFile");
		cp.add("#55:Utf8:Main.java");
		cp.add("#56:NameAndType:#46:#47:compare:(Ljava.lang.Integer;Ljava.lang.Integer;)I");
		cp.add("#57:NameAndType:#23:#24:<init>:()V");
		cp.add("#58:Utf8:Lorem ipsum dolor sit amet, prima adipisci et est, mel et purto duis ludus. Vix mollis ancillae te. Eu pro purto soleat consetetur. Vix eripuit reprehendunt id. Audire vidisse aperiri eu sed, incorrupte scripserit signiferumque ad est, omnes platonem ex sea. His libris invenire eu. Falli tractatos qui ea, officiis recusabo convenire ea eos, pri hinc oratio delenit an.Omnesque conceptam appellantur ei vel, an quo possim audiam. Consulatu vituperatoribus nam ea, eos an paulo copiosae. Paulo dolor ei his, eam eu minim partem, saepe putent concludaturque vis ex. Nibh consulatu interpretaris pri id, ut urbanitas delicatissimi mei. Te vim aperiam principes assueverit, ea purto imperdiet dissentiunt eos, ex autem mucius iuvaret quo.In his nibh partiendo ocurreret. Probatus corrumpit molestiae ei ius. In qui dictas doctus atomorum, illum vocent cotidieque no sit, ne mei discere facilis lucilius. Mei efficiendi reformidans theophrastus ea, no vis erat novum laoreet, atqui euripidis mea cu. Ex omnes omnesque cum.An eam prima dicta eligendi, dictas option repudiandae no nam. Nisl vero ei duo. Pericula posidonium eu pri, et ius tale constituam. Rebum veritus in ius. Ut mei dicit repudiandae, vim an primis propriae efficiendi, et quas debitis laboramus eam. Cum elitr principes ei.Prima nulla eligendi ex eum, saperet debitis ullamcorper et cum, ut ius autem denique expetendis. Nobis adversarium an qui, nec no melius iuvaret. Eum mundi tantas eu, novum aperiam pri ei. Eam reprimique neglegentur delicatissimi eu, molestie iudicabit ius ne, ullum dolore animal ei cum. Dolorum nusquam eleifend et pri, in errem mentitum sed.");
		cp.add("#59:Utf8:StandardStuff");
		cp.add("#60:NameAndType:#76:#77:getString:()Ljava.lang.String;");
		cp.add("#61:NameAndType:#78:#35:f1:Ljava.lang.String;");
		cp.add("#62:NameAndType:#79:#35:f2:Ljava.lang.String;");
		cp.add("#63:Utf8:edu.ucla.cs.onr.test.LibraryClass");
		cp.add("#64:NameAndType:#80:#81:getNumber:()I");
		cp.add("#65:Utf8:java.util.ArrayList");
		cp.add("#66:Class:#82:java.lang.Integer");
		cp.add("#67:NameAndType:#83:#84:valueOf:(I)Ljava.lang.Integer;");
		cp.add("#68:Class:#85:java.util.List");
		cp.add("#69:NameAndType:#86:#87:add:(Ljava.lang.Object;)Z");
		cp.add("#70:Utf8:Main$1");
		cp.add("#71:Class:#88:java.util.Collections");
		cp.add("#72:NameAndType:#89:#90:sort:(Ljava.util.List;Ljava.util.Comparator;)V");
		cp.add("#73:NameAndType:#91:#81:intValue:()I");
		cp.add("#74:Utf8:Main");
		cp.add("#75:Utf8:java.lang.Object");
		cp.add("#76:Utf8:getString");
		cp.add("#77:Utf8:()Ljava.lang.String;");
		cp.add("#78:Utf8:f1");
		cp.add("#79:Utf8:f2");
		cp.add("#80:Utf8:getNumber");
		cp.add("#81:Utf8:()I");
		cp.add("#82:Utf8:java.lang.Integer");
		cp.add("#83:Utf8:valueOf");
		cp.add("#84:Utf8:(I)Ljava.lang.Integer;");
		cp.add("#85:Utf8:java.util.List");
		cp.add("#86:Utf8:add");
		cp.add("#87:Utf8:(Ljava.lang.Object;)Z");
		cp.add("#88:Utf8:java.util.Collections");
		cp.add("#89:Utf8:sort");
		cp.add("#90:Utf8:(Ljava.util.List;Ljava.util.Comparator;)V");
		cp.add("#91:Utf8:intValue");

		assert(cp.size() == outputStream.size());
		assertTrue(outputStream.containsAll(cp));
		assertTrue(cp.containsAll(outputStream));

	}
}
