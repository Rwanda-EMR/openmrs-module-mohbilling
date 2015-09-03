package exercises;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
      
	   A a = new C();
       a.doSomething();
	}

}
class A{
	public void doSomething(){
		System.out.println("A is displaying");
	}
}
class B extends A{
	public void doSomething(){
		
		System.out.println("B is displaying");
	}
}
class C extends B{
	
	public void doSomething(){
		super.doSomething(); // to call doSomething of A
		System.out.println("C is displaying");
	}
}