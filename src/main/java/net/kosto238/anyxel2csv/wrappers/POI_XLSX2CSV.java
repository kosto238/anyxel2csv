/* ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */

package net.kosto238.anyxel2csv.wrappers;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A rudimentary XLSX -> CSV processor modeled on the POI sample program
 * XLS2CSVmra by Nick Burch from the package
 * org.apache.poi.hssf.eventusermodel.examples. Unlike the HSSF version, this
 * one completely ignores missing rows.
 * <p/>
 * Data sheets are read using a SAX parser to keep the memory footprint
 * relatively small, so this should be able to read enormous workbooks. The
 * styles table and the shared-string table must be kept in memory. The standard
 * POI styles table class is used, but a custom (read-only) class is used for
 * the shared string table because the standard POI SharedStringsTable grows
 * very quickly with the number of unique strings.
 * <p/>
 * For a more advanced implementation of SAX event parsing of XLSX files, see
 * {@link XSSFEventBasedExcelExtractor} and {@link XSSFSheetXMLHandler}. Note
 * that some use cases, it may be possible to simply use those with a custom
 * {@link SheetContentsHandler} and no SAX code needed of your own!
 */
public class POI_XLSX2CSV {

    /**
     * The type of the data value is indicated by an attribute on the cell. The
     * value is usually in a "v" element within the cell.
     */
    enum xssfDataType {
        BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
    }

    /**
     * Derived from http://poi.apache.org/spreadsheet/how-to.html#xssf_sax_api
     * <p/>
     * Also see Standard ECMA-376, 1st edition, part 4, pages 1928ff, at
     * http://www.ecma-international.org/publications/standards/Ecma-376.htm
     * <p/>
     * A web-friendly version is http://openiso.org/Ecma/376/Part4
     */
    class MyXSSFSheetHandler extends DefaultHandler {

        /**
         * Table with styles
         */
        private final StylesTable stylesTable;

        /**
         * Table with unique strings
         */
        private final ReadOnlySharedStringsTable sharedStringsTable;

        /**
         * Destination for data
         */
        private final PrintStream output;

        /**
         * Number of columns to read starting with leftmost
         */
        private final int minColumnCount;

        // Set when V start element is seen
        private boolean vIsOpen;

        // Set when cell start element is seen;
        // used when cell close element is seen.
        private xssfDataType nextDataType;

        // Used to format numeric cell values.
        private short formatIndex;
        private String formatString;
        private final DataFormatter formatter;

        private int thisColumn = -1;
        // The last column printed to the output stream
        private int lastColumnNumber = -1;

        // Gathers characters as they are seen.
        private final StringBuffer value;

        /**
         * Accepts objects needed while parsing.
         *
         * @param styles  Table of styles
         * @param strings Table of shared strings
         * @param cols    Minimum number of columns to show
         * @param target  Sink for output
         */
        public MyXSSFSheetHandler(StylesTable styles,
                                  ReadOnlySharedStringsTable strings, int cols, PrintStream target) {
            this.stylesTable = styles;
            this.sharedStringsTable = strings;
            this.minColumnCount = cols;
            this.output = target;
            this.value = new StringBuffer();
            this.nextDataType = xssfDataType.NUMBER;
            this.formatter = new DataFormatter();
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        @SuppressWarnings("unused")
        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {

            if ("inlineStr".equals(name) || "v".equals(name)) {
                vIsOpen = true;
                // Clear contents cache
                value.setLength(0);
            }
            // c => cell
            else if ("c".equals(name)) {
                // Get the cell reference
                String r = attributes.getValue("r");
                int firstDigit = -1;
                for (int c = 0; c < r.length(); ++c) {
                    if (Character.isDigit(r.charAt(c))) {
                        firstDigit = c;
                        break;
                    }
                }
                thisColumn = nameToColumn(r.substring(0, firstDigit));

                // Set up defaults.
                this.nextDataType = xssfDataType.NUMBER;
                this.formatIndex = -1;
                this.formatString = null;
                String cellType = attributes.getValue("t");
                String cellStyleStr = attributes.getValue("s");
                if ("b".equals(cellType))
                    nextDataType = xssfDataType.BOOL;
                else if ("e".equals(cellType))
                    nextDataType = xssfDataType.ERROR;
                else if ("inlineStr".equals(cellType))
                    nextDataType = xssfDataType.INLINESTR;
                else if ("s".equals(cellType))
                    nextDataType = xssfDataType.SSTINDEX;
                else if ("str".equals(cellType))
                    nextDataType = xssfDataType.FORMULA;
                else if (cellStyleStr != null) {
                    // It's a number, but almost certainly one
                    // with a special style or format
                    int styleIndex = Integer.parseInt(cellStyleStr);
                    XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);

                    if (style != null) {
                        this.formatIndex = style.getDataFormat();
                        this.formatString = style.getDataFormatString();
                        if (this.formatString == null)
                            this.formatString = BuiltinFormats
                                .getBuiltinFormat(this.formatIndex);
                    }
                }
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String name)
            throws SAXException {

            String thisStr = null;

            // v => contents of a cell
            if ("v".equals(name)) {
                // Process the value contents as required.
                // Do now, as characters() may be called more than once
                switch (nextDataType) {

                    case BOOL:
                        char first = value.charAt(0);
                        thisStr = first == '0' ? "FALSE" : "TRUE";
                        break;

                    case ERROR:
                        thisStr = "\"ERROR:" + value.toString() + '"';
                        break;

                    case FORMULA:
                        // A formula could result in a string value,
                        // so always add double-quote characters.
                        thisStr = '"' + prepareString(value.toString()) + '"';
                        break;

                    case INLINESTR:
                        XSSFRichTextString rtsi = new XSSFRichTextString(
                            value.toString());
                        thisStr = '"' + prepareString(rtsi.toString()) + '"';
                        break;

                    case SSTINDEX:
                        String sstIndex = value.toString();
                        try {
                            int idx = Integer.parseInt(sstIndex.trim());
                            XSSFRichTextString rtss = new XSSFRichTextString(
                                sharedStringsTable.getEntryAt(idx));
                            thisStr = '"' + prepareString(rtss.toString()) + '"';
                        } catch (NumberFormatException ex) {
                            output.println("Failed to parse SST index '" + sstIndex
                                + "': " + ex.toString());
                        }
                        break;

                    case NUMBER:
                        String n = value.toString();
                        if (this.formatString != null && n.length() > 0)
                            thisStr = '"' + formatter.formatRawCellContents(
                                Double.parseDouble(n), this.formatIndex,
                                this.formatString) + '"';
                        else
                            thisStr = '"' + n + '"';
                        break;

                    default:
                        thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
                        break;
                }

                // Output after we've seen the string contents
                // Emit commas for any fields that were missing on this row
                if (lastColumnNumber == -1) {
                    lastColumnNumber = 0;
                }
                for (int i = lastColumnNumber; i < thisColumn; ++i) {
                    writer.print(',');
                }

                // Might be the empty string.
                writer.print(thisStr);

                // Update column
                if (thisColumn > -1)
                    lastColumnNumber = thisColumn;

            } else if ("row".equals(name)) {

                // Print out any missing commas if needed
                if (minColumns > 0) {
                    // Columns are 0 based
                    if (lastColumnNumber == -1) {
                        lastColumnNumber = 0;
                    }
                    for (int i = lastColumnNumber; i < (this.minColumnCount); i++) {
                        writer.print(',');
                    }
                }

                // We're onto a new row
                writer.println();
                lastColumnNumber = -1;
            }

        }

        /**
         * Captures characters only if a suitable element is open. Originally
         * was just "v"; extended for inlineStr also.
         */
        @Override
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (vIsOpen)
                value.append(ch, start, length);
        }

        /**
         * Converts an Excel column name like "C" to a zero-based index.
         *
         * @param name
         * @return Index corresponding to the specified name
         */
        private int nameToColumn(String name) {
            int column = -1;
            for (int i = 0; i < name.length(); ++i) {
                int c = name.charAt(i);
                column = (column + 1) * 26 + c - 'A';
            }
            return column;
        }

    }

    // /////////////////////////////////////

    private final OPCPackage xlsxPackage;
    private final int minColumns;
    private PrintStream writer;
    private final List<String> outputFileNames = new LinkedList<>();

    /**
     * Creates a new XLSX -> CSV converter
     *
     * @param pkg        The XLSX package to process
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     */
    public POI_XLSX2CSV(OPCPackage pkg, int minColumns) {
        this.xlsxPackage = pkg;
        this.minColumns = minColumns;
    }

    /**
     * Parses and shows the content of one sheet using the specified styles and
     * shared-strings tables.
     *
     * @param styles
     * @param strings
     * @param sheetInputStream
     */
    public void processSheet(StylesTable styles,
                             ReadOnlySharedStringsTable strings, InputStream sheetInputStream)
        throws IOException, ParserConfigurationException, SAXException {

        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new MyXSSFSheetHandler(styles, strings, this.minColumns, this.writer);
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
    }

    /**
     * Initiates the processing of the XLS workbook file to CSV.
     *
     * @throws IOException
     * @throws OpenXML4JException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void process() throws IOException, OpenXML4JException,
        ParserConfigurationException, SAXException {

        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(
            this.xlsxPackage);
        XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
        StylesTable styles = xssfReader.getStylesTable();
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        int index = 0;
        while (iter.hasNext()) {
            InputStream stream = iter.next();
            String sheetName = iter.getSheetName();
            // Output to separate files as well...
            if (writer != null) {
                writer.close();
            }
            String outfile = inputFile + "_" + sheetName + "_sheet-" + index + ".csv";
            outputFileNames.add(outfile);
            writer = new PrintStream(outfile, "UTF-8");

            processSheet(styles, strings, stream);
            stream.close();

            writer.close();
            ++index;
        }
    }

    private static String inputFile;

    /**
     * Creates a new XLS -> CSV converter
     *
     * @param filename   The file to process
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     * @throws InvalidFormatException
     */
    public POI_XLSX2CSV(String filename, int minColumns) throws InvalidFormatException {
        this(OPCPackage.open(filename, PackageAccess.READ), minColumns);
        inputFile = filename;
    }

    public List<String> getOutputFileNames() {
        return outputFileNames;
    }

    String prepareString(String s) {
        return s
            .replace("\"", "")
            .replaceAll("\\\\", "");
    }
}