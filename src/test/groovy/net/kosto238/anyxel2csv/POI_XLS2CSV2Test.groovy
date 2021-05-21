package net.kosto238.anyxel2csv

import groovy.transform.CompileStatic
import org.junit.*

import java.text.DecimalFormat

@CompileStatic
class POI_XLS2CSV2Test {
    POI_XLS2CSV2 instance

    @BeforeClass
    static void setUpClass() {
    }

    @AfterClass
    static void tearDownClass() {
    }

    @Before
    void setUp() {
        File tf = new File('src/test/resources/test-examples/2.xls')

    }

    @After
    void tearDown() {
    }
    @Test
    void testXLS_11969(){
        File tf = new File('src/test/resources/test-examples/test.xls')
        File outFile = new File('src/test/resources/test-examples/test.csv')

        POI_XLS2CSV2.xls(tf, outFile)


        String[] lines = outFile.text.split('\n')

//        println lines[-1]
        assert lines.length > 6212

        outFile.delete()
    }

    @Test
    void xtrTest_11969(){
        File tf = new File('src/test/resources/test-examples/11969-xtr.xls')
        File outFile = new File('src/test/resources/test-examples/test.csv')

        POI_XLS2CSV2.xls(tf, outFile)


        String[] lines = outFile.text.split('\n')
//        lines.each {
//            println it
//        }
//        println lines[-1]
        assert lines.length == 55

        outFile.delete()
    }

    @Test
    void t1(){
        def aWrong = "488152213E9", aSrc = "4881522130"
        Double v = 4881522130.0d

        DecimalFormat df = new DecimalFormat("#.##")
        String formatted = df.format(v)
        Long intV = v.longValue()
        if(new Double(intV).equals(v))
            formatted = v.longValue() + ""


        assert formatted == aSrc
    }

//    @Test
//    void alfi(){
//        File tf = new File('src/test/resources/test-examples/alfi.xls')
//        File outFile = new File('src/test/resources/test-examples/test.csv')
//
//        POI_XLS2CSV2.xls(tf, outFile)
//
//
//        String[] lines = outFile.text.split('\n')
//        lines.each { String it->
//            if(it.contains('WW1074'))
//                println it
//        }
////        println lines[-1]
//        assert lines.length == 1347
//
//        outFile.delete()
//    }
}