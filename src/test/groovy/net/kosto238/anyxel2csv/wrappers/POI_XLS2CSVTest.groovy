package net.kosto238.anyxel2csv.wrappers

import groovy.transform.CompileStatic
import net.kosto238.anyxel2csv.wrappers.POI_XLS2CSV
import org.junit.*

/**
 * Created by kosto238 on 15.05.17.
 */
@CompileStatic
class POI_XLS2CSVTest {
	POI_XLS2CSV instance

	@BeforeClass
	static void setUpClass() {
	}

	@AfterClass
	static void tearDownClass() {
	}

	@Before
	void setUp() {
		File tf = new File('src/test/resources/test-examples/2.xls')
		instance = new POI_XLS2CSV(tf.toPath().toString(), -1)
	}

	@After
	void tearDown() {
	}


	@Test
	void prepareStringXLS(){
		assert instance.prepareString("""ljkghsalkdj"fgasdg\\""") == 'ljkghsalkdjfgasdg'
	}

	@Test @Ignore
	void testXLS_11969(){
		File tf = new File('src/test/resources/test-examples/test.xls')
		instance = new POI_XLS2CSV(tf.toPath().toString(), -1)
		instance.process()
		List<String> fnames = instance.getOutputFileNames()

		String[] lines = new File(fnames[0]).text.split('\n')

//		println lines[-1]
		assert lines.length > 6212
	}

    @Test @Ignore
    void testXLS_2127(){
        File tf = new File('src/test/resources/test-examples/2127.xls')
        instance = new POI_XLS2CSV(tf.toPath().toString(), -1)
        instance.process()
        List<String> fnames = instance.getOutputFileNames()

        String[] lines = new File(fnames[0]).text.split('\n')

//		println lines[-1]
        assert lines.length > 6212
    }

    @Test @Ignore
    void testXLS_2078(){
        File tf = new File('src/test/resources/test-examples/2078.xls')
        instance = new POI_XLS2CSV(tf.toPath().toString(), -1)
        instance.process()
        List<String> fnames = instance.getOutputFileNames()

        String[] lines = new File(fnames[0]).text.split('\n')

//		println lines[-2]
        assert lines.length >= 4179
    }
}