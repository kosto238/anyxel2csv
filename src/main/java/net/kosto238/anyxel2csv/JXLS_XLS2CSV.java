package net.kosto238.anyxel2csv;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kosto238 on 22.05.17.
 */
public class JXLS_XLS2CSV {

	private File xlsFile;
    List<String> fileNames = new ArrayList<>();


    public JXLS_XLS2CSV(File file) throws IOException {
		xlsFile = file;
	}

    public JXLS_XLS2CSV(String filename) throws IOException {
		xlsFile = new File(filename);
	}

    public void process(){
		try {
			Workbook book = Workbook.getWorkbook(xlsFile);

			for(Sheet sheet : book.getSheets()){
				File f = new File(xlsFile.getAbsolutePath() +"_"+ sheet.getName() +".csv");
				processSheet(sheet, f);
				fileNames.add(f.getAbsolutePath());
			}

		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processSheet(Sheet sheet, File out){
		try {
			if((sheet.getRows() + sheet.getColumns()) > 0){
                PrintStream ps = new PrintStream(new FileOutputStream(out));
				for(int i = 0; i < sheet.getRows(); i++){
                    StringBuffer line = new StringBuffer();
					boolean hasValues = false;
					for(int j = 0; j < sheet.getColumns(); j++){
						if(! sheet.getCell(j, i).getContents().trim().isEmpty())
							hasValues = true;

						line.append(',').append(format(sheet.getCell(j, i).getContents()));
					}
					if(hasValues)
						ps.println(line.substring(1));
				}
				ps.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

    List<String> getOutputFileNames(){
        return fileNames;
    }

    private String format(String value){
        return "\""+value+"\"";
    }

}