package net.kosto238.anyxel2csv

import groovy.transform.CompileStatic
import org.junit.*

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by kosto238 on 22.05.17.
 */
@CompileStatic
class JXLS_XLS2CSVTest {
	JXLS_XLS2CSV instance

	@BeforeClass
	static void setUpClass() {
	}

	@AfterClass
	static void tearDownClass() {
	}

	@Before
	void setUp() {

	}

	@After
	void tearDown() {
		Files.deleteIfExists(Paths.get(instance.outputFileNames[0]))
	}

	@Test
	void file1(){
		File tf = new File('src/test/resources/test-examples/2.xls')
		instance = new JXLS_XLS2CSV(tf)
		instance.process()
		def outs = new FileInputStream(new File(instance.outputFileNames[0]))
/*
		outs.eachLine {
			println it
		}
*/
	}

	@Ignore //JXL doesn/t read this file properly
	@Test
	void file2(){
		File tf = new File('src/test/resources/test-examples/VTR.xls')
		instance = new JXLS_XLS2CSV(tf)
		instance.process()
		def outs = new FileInputStream(new File(instance.outputFileNames[0]))
/*
		outs.eachLine {
			println it
		}
*/
	}

	@Test
	void testXLS_11969(){
		File tf = new File('src/test/resources/test-examples/test.xls')
		instance = new JXLS_XLS2CSV(tf.toPath().toString())
		instance.process()
		List<String> fnames = instance.getOutputFileNames()

		String[] lines = new File(fnames[0]).text.split('\n')

//		println lines[-2]
		assert lines.length > 6212
	}

	@Test @Ignore
	void testXLS_2127(){
		File tf = new File('src/test/resources/test-examples/2127.xls')
		instance = new JXLS_XLS2CSV(tf.toPath().toString())
		instance.process()
		List<String> fnames = instance.getOutputFileNames()

		String[] lines = new File(fnames[0]).text.split('\n')

//		println lines[-2]
		assert lines.length > 6212
	}

	@Test @Ignore
	void testXLS_2078(){
		File tf = new File('src/test/resources/test-examples/2078.xls')
		instance = new JXLS_XLS2CSV(tf.toPath().toString())
		instance.process()
		List<String> fnames = instance.getOutputFileNames()

		String[] lines = new File(fnames[0]).text.split('\n')

//		println lines[-2]
		assert lines.length >= 4179
	}

}