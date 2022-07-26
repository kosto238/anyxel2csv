package net.kosto238.anyxel2csv;

import edu.npu.fastexcel.ExcelException;
import net.kosto238.anyxel2csv.wrappers.FastExcel_XLS2CSV;
import net.kosto238.anyxel2csv.wrappers.JXLS_XLS2CSV;
import net.kosto238.anyxel2csv.wrappers.POI_XLS2CSV2;
import net.kosto238.anyxel2csv.wrappers.POI_XLSX2CSV;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExcelToCsvFileConverter {
    /**
     *
     * @param xls input File
     * @return The result of operation is a file of *.xls.csv creted in the same directory as source
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public File xls2Csv(File xls) throws IllegalArgumentException, IOException {
        if(xls == null || xls.length() == 0)
            throw new IllegalArgumentException("File is null or empty");

        String fname = xls.getName();

        try {
            File outf = File.createTempFile(fname, ".csv");
            POI_XLS2CSV2.xls(xls, outf);
            return outf;

        } catch (Exception e){
            try {
                JXLS_XLS2CSV handle = new JXLS_XLS2CSV(xls);
                handle.process();
                List<String> outFiles = handle.getOutputFileNames();
                if (outFiles != null && !outFiles.isEmpty())
                    return new File(outFiles.get(0));
            } catch (Exception e2) {
                FastExcel_XLS2CSV handle = new FastExcel_XLS2CSV(xls);
                try {
                    handle.process();
                } catch (ExcelException excelException) {
                    excelException.printStackTrace();
                }
                List<String> outFiles = handle.getOutputFileNames();
                if (outFiles != null && !outFiles.isEmpty())
                    return new File(outFiles.get(0));
            }
        } finally {
            //log.info("$fname converted to CSV")
        }

        return null;
    }

    /**
     *
     * @param xlsx input File
     * @return The result of operation is a file of *.xls.csv creted in the same directory as source
     * @throws IOException
     * @throws InvalidFormatException
     */
    public File xlsx2Csv(File xlsx) throws IOException, InvalidFormatException {
        if(xlsx == null || xlsx.length() == 0)
            throw new IllegalArgumentException("File is null or empty");

        POI_XLSX2CSV handle = new POI_XLSX2CSV(xlsx.toPath().toString(), -1);
        try {
            handle.process();
        } catch (OpenXML4JException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        //log.info("$fname converted to CSV")
        List<String> outFiles = handle.getOutputFileNames();

        if (outFiles != null && !outFiles.isEmpty())
            return new File(outFiles.get(0));

        return null;
    }
}