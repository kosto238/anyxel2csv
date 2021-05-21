package net.kosto238.anyxel2csv

import groovy.transform.CompileStatic
import org.junit.*

/**
 * Created by kosto238 on 15.05.17.
 */
@CompileStatic
class POI_XLSX2CSVTest {
	POI_XLSX2CSV instance

	@BeforeClass
	static void setUpClass() {
	}

	@AfterClass
	static void tearDownClass() {
	}

	@Before
	void setUp() {
		File tf = new File('src/test/resources/test-examples/1.xlsx')
		instance = new POI_XLSX2CSV(tf.toPath().toString(), -1)
	}

	@After
	void tearDown() {
	}

	@Test
	void prepareStringXLS(){
		assert instance.prepareString("""ljkghsalkdj"fgasdg\\""") == 'ljkghsalkdjfgasdg'
	}

	@Test
	void test982xlsx(){
		File tf = new File('src/test/resources/test-examples/982.xlsx')

		instance = new POI_XLSX2CSV(tf.toPath().toString(), -1)
		instance.process()
		for(String fn : instance.outputFileNames){
			def outFile = new File(fn)

			String[] lines = outFile.text.split('\n')
//			lines.each {
//				println it
//			}
//			println lines[-1]
			assert lines.length > 0

			outFile.delete()
		}

	}

    @Test
    void test976xlsx(){
        File tf = new File('src/test/resources/test-examples/прада 04.02.2019.xlsx')

        instance = new POI_XLSX2CSV(tf.toPath().toString(), -1)
        instance.process()

        def found = false
        for(String fn : instance.outputFileNames){
            def outFile = new File(fn)

            String[] lines = outFile.text.split('\n')
			lines.each {String it->
				if(it.contains('L5312K18E1')) {
                    found = true
                    println it
                }
			}
            assert lines.length > 0

            outFile.delete()
        }
        assert found

    }
}