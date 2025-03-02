package me.julionxn.ttapp.excel;

import me.julionxn.ttapp.endpoint.EndpointsManager;
import me.julionxn.ttapp.endpoint.model.User;
import me.julionxn.ttapp.util.DatesUtil;
import org.apache.poi.ss.formula.functions.Address;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExcelGenerator {

    private Workbook workbook;
    private Sheet sheet;
    private List<User> users;

    public void build(){
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        buildHeaders();
        fetchData();
        addData();
        save();
    }
    
    private List<List<Long>> fixData(){
        List<List<Long>> data = new ArrayList<>();
        for (User user : users) {
            Long id = user.getId();
            Long arrivalE = user.getTimeArrival();
            Long attentionS = user.getTimeAttendance();
            Long prodS = user.getTimeStartProduction();
            Long giveS = user.getTimeStartProductGive();
            Long giveE = user.getTimeEndProductGive();
            Long prepS = user.getTimeStartPreparation();
            Long prepE = user.getTimeEndPreparation();
            Long cashS = user.getTimeStartCash();
            Long cashE = user.getTimeEndCash();
            Long end = user.getEndTime();
            List<Long> userData = new ArrayList<>();
            userData.add(id);
            userData.add(arrivalE);
            userData.add(attentionS);
            userData.add(prodS);
            userData.add(giveS);
            userData.add(giveE);
            userData.add(prepS);
            userData.add(prepE);
            userData.add(cashS);
            userData.add(cashE);
            userData.add(end);
            data.add(userData);
        }
        return data;
    }

    private void addData(){
        List<List<Long>> data = fixData();
        addToTable(data);
    }

    private void save(){
        Path path = Path.of("").toAbsolutePath();
        path.resolve("Datas").toFile().mkdir();
        Path filePath = path.resolve("Datas").resolve(DatesUtil.now() + ".xlsx");
        try (FileOutputStream fileOut = new FileOutputStream(String.valueOf(filePath))) {
            workbook.write(fileOut);
            System.out.println("Excel file created: " + filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void addToTable(List<List<Long>> data){
        int currentRow = 1;
        int currentCol = 0;
        for (List<Long> datum : data) {
            Row row = sheet.createRow(currentRow);
            row.createCell(currentCol).setCellValue(datum.get(0));
            currentCol++;
            for (int i = 1; i < datum.size(); i++) {
                Long time = datum.get(i);
                if (time != null) {
                    row.createCell(currentCol).setCellValue(time);
                } else {
                    row.createCell(currentCol).setCellValue(0);
                }
                currentCol++;
                Cell cell = row.createCell(currentCol);
                CellAddress address = cell.getAddress();
                CellAddress prevColumnAddres = new CellAddress(address.getRow(), address.getColumn() - 1);
                String formula = "(" + prevColumnAddres.formatAsString() + "/86400) - TIME(6,0,0) + DATE(1970,1,1)";
                cell.setCellFormula(formula);
                currentCol++;
            }

            //Time in queue
            CellAddress arrivalC = new CellAddress(currentRow, 1);
            CellAddress attentionC = new CellAddress(currentRow, 3);
            String queueFormula = attentionC.formatAsString() + "-" + arrivalC.formatAsString();
            row.createCell(currentCol).setCellFormula(queueFormula);
            currentCol++;

            //Time picking
            CellAddress prodStartC = new CellAddress(currentRow, 5);
            String pickingFormula = prodStartC.formatAsString() + "-" + attentionC.formatAsString();
            row.createCell(currentCol).setCellFormula(pickingFormula);
            currentCol++;

            //Time in production
            CellAddress giveEndC = new CellAddress(currentRow, 9);
            String productionFormula = giveEndC.formatAsString() + "-" + prodStartC.formatAsString();
            row.createCell(currentCol).setCellFormula(productionFormula);
            currentCol++;

            //Time in preparation
            CellAddress prepStartC = new CellAddress(currentRow, 11);
            CellAddress prepEndC = new CellAddress(currentRow, 13);
            String preparationFormula = prepEndC.formatAsString() + "-" + prepStartC.formatAsString();
            row.createCell(currentCol).setCellFormula(preparationFormula);
            currentCol++;

            //Time in cash
            CellAddress cashStartC = new CellAddress(currentRow, 15);
            CellAddress cashEndC = new CellAddress(currentRow, 17);
            String cashFormula = cashEndC.formatAsString() + "-" + cashStartC.formatAsString();
            row.createCell(currentCol).setCellFormula(cashFormula);
            currentRow++;
            currentCol = 0;
        }
    }

    private void buildHeaders(){
        Row headerRow = sheet.createRow(0);
        String[] headers = {"User",
                "Arrival Epoch", "Arrival",
                "AttentionStart Epoch", "AttentionStart",
                "StartPreparation Epoch", "StartPreparation",
                "GiveStart Epoch", "GiveStart",
                "GiveEnd Epoch", "GiveEnd",
                "PreparationStart Epoch", "PreparationStart",
                "PreparationEnd Epoch", "PreparationEnd",
                "CashStart Epoch", "CashStart",
                "CashEnd Epoch", "CashEnd",
                "End Epoch", "End",
                "Time Queue",
                "Time Picking",
                "Time Production",
                "Time Preparation",
                "Time cashing"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 15 * 256);
        }
    }

    private void fetchData(){
        users = EndpointsManager.getInstance()
                .USERS
                .getAllItems(System.out::println)
                .join();
    }

}
