package uk.ac.ed.inf.aqmaps;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
//    public static Test suite()
//    {
//    	//创建TestSuite对象pp
//        TestSuite testSuite=new TestSuite("All Test From TestCaseExample");
//        //为TestSuite添加一个测试用例集合，参数为：ClasstestClass
//        //通过参数可以知道，其实该参数就是TestCase的子类
//        testSuite.addTestSuite(App.class);
//        //创建具体的测试用例
//        Test test = TestSuite.createTest(App.class, "testAdd");
//        //添加一个具体的测试用例
//        testSuite.addTest(test);
//        return testSuite;
//        return new TestSuite( AppTest.class );
//    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}

//Sensor A = new Sensor();
//Sensor B = new Sensor();
//A.setLocation("dragon.bottle.crisp");
//Point a = d.Str2W3W("dragon.bottle.crisp").coordinates.getP();
//System.out.print(a.longitude());
//System.out.print(a.latitude());
//A.setBattery(90);
//A.setReading("200");
//
//B.setLocation("valve.elaborate.fortunate");
//Point b = d.Str2W3W("valve.elaborate.fortunate").coordinates.getP();
//System.out.print(b.longitude());
//System.out.print(b.latitude());
//B.setBattery(90);
//B.setReading("200");
//
//List<Sensor> sl = new ArrayList<Sensor>();
//
//sl.add(A);
//sl.add(B);