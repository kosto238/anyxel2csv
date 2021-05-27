package net.kosto238.anyxel2csv.wrappers;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Iterator;

public class POI_XLS2CSV2 {
    public static void xls(File inputFile, File outputFile) {
        // For storing data into CSV files
        StringBuffer data = new StringBuffer();
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);

            // Get the workbook object for XLS file
            HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(inputFile));
            // Get first sheet from the workbook
            HSSFSheet sheet = workbook.getSheetAt(0);
            Cell cell;
            Row row;
            DecimalFormat df = new DecimalFormat("#.##");

            // Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                // For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    cell = cellIterator.next();
                    String formatted;
                    Double v;
                    Long intV;
                    switch (cell.getCellTypeEnum()) {
                        case BOOLEAN:
                            data.append("\""+prepareString(cell.getBooleanCellValue()+"") + "\",");
                            break;

                        case NUMERIC:
                            v = cell.getNumericCellValue();
                            formatted = df.format(v);
                            intV = v.longValue();
                            if(new Double(intV).equals(v))
                                formatted = intV + "";
                            data.append("\""+prepareString(formatted) + "\",");
                            break;

                        case FORMULA:
                            String r = null;
                            switch(cell.getCachedFormulaResultType()) {
                                case NUMERIC:
                                    v = cell.getNumericCellValue();
                                    formatted = df.format(v);
                                    intV = v.longValue();
                                    if(new Double(intV).equals(v))
                                        formatted = intV + "";
                                    data.append("\""+prepareString(formatted) + "\",");
                                    break;
                                case STRING:
                                    data.append("\""+prepareString(cell.getStringCellValue()) + "\",");
                                    break;
                                default:
                                    break;
                            }

                            //data.append("\""+prepareString(cell.getCellFormula()+"") + "\",");
                            break;

                        case STRING:
                            data.append("\""+prepareString(cell.getStringCellValue()) + "\",");
                            break;

                        case BLANK:
                            data.append("\"\",");
                            break;

                        default:
                            data.append("\""+cell + "\",");
                    }
                }
                data.append('\n');
            }

            fos.write(data.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String prepareString(String s){
        return s.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\"", "").replaceAll("\\\\", "");
    }

//    public static void main(String[] args) {
//        File inputFile = new File("C:\test.xls");
//        File outputFile = new File("C:\output.csv");
//        xls(inputFile, outputFile);
//    }

}