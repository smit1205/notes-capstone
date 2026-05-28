package helperUtils;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;

public class ExcelReader {

    public static Object[][] getTestData(String filePath, String sheetName) {

        Object[][] data = null;

        try {
            FileInputStream file = new FileInputStream(
                    System.getProperty("user.dir") + "/" + filePath
            );

            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }

            int rowCount = sheet.getPhysicalNumberOfRows();
            int colCount = sheet.getRow(0).getLastCellNum();

            data = new Object[rowCount - 1][colCount];

            for (int i = 1; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    XSSFCell cell = sheet.getRow(i).getCell(j);
                    data[i - 1][j] = (cell != null) ? cell.toString() : "";
                }
            }

            workbook.close();
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Excel reading failed");
        }

        return data;
    }
}