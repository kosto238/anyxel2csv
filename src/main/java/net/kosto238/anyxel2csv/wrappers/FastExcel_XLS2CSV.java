package net.kosto238.anyxel2csv.wrappers;

import edu.npu.fastexcel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by kosto238 on 22.05.17.
 */
public class FastExcel_XLS2CSV {
	private File xlsFile;
	List<String> fileNames = new ArrayList<>();


    public FastExcel_XLS2CSV(File file) throws IOException {
		xlsFile = file;
	}

    public FastExcel_XLS2CSV(String filename) throws IOException {
		xlsFile = new File(filename);
	}

	public void process() throws FileNotFoundException, ExcelException {
		Workbook workBook;
		workBook = FastExcel.createReadableWorkbook(xlsFile);

		workBook.setSSTType(BIFFSetting.SST_TYPE_DEFAULT);//memory storage
		workBook.open();
		Sheet s = workBook.getSheet(0);

		File f = new File(xlsFile.getAbsolutePath() +"_"+ s.getName() +".csv");
		fileNames.add(f.getAbsolutePath());
        PrintStream ps = new PrintStream(new FileOutputStream(f));

		for (int i = s.getFirstRow(); i < s.getLastRow(); i++) {
            StringBuffer line = new StringBuffer();

			for (int j = s.getFirstColumn(); j < s.getLastColumn(); j++)
				line.append(',').append(format(s.getCell(i, j)));

			ps.println(line.substring(1));
		}

		workBook.close();
	}

	public List<String> getOutputFileNames(){
		return fileNames;
	}

	private String format(String value){
		return "\""+value+"\"";
	}
}