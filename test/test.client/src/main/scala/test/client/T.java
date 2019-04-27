package test.client;

/**
 * Created by Night on 2019/4/26.
 */
public class T {


    public static void main(String[] args) {
        X x = new X();
//        String name ="123";
//        x.u(name);
//        System.out.println(name);

        Integer a = new Integer(1300);
       // x.uInt(a);
        System.out.println(a.intValue());
    }
}

class X {
    public void u(String name) {
        name = "456";
        System.out.println("function name"+name);
    }

    public void uInt(Integer value) {

        new Exception()
        value = new Integer(1500);
        System.out.println("function uInt"+value);
    }
}