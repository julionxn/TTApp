package me.julionxn.ttapp.excel;

import me.julionxn.ttapp.endpoint.EndpointsManager;
import me.julionxn.ttapp.endpoint.model.Product;
import me.julionxn.ttapp.endpoint.model.Stops;
import me.julionxn.ttapp.endpoint.model.User;
import me.julionxn.ttapp.util.DatesUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelGenerator {

    private Workbook workbook;
    private Sheet mainSheet;
    private Sheet userBigviewSheet;
    private List<User> users;

    public void build(){
        workbook = new XSSFWorkbook();
        mainSheet = workbook.createSheet("Times");
        userBigviewSheet = workbook.createSheet("Products");
        fetchData();
        buildMainHeaders();
        addMainData();
        buildBigviewHeaders();
        addBigviewData();
        flowSheet();
        save();
    }

    private void flowSheet() {
        Sheet flowSheet = workbook.createSheet("Flow");
        int currentRow = 1;
        for (User user : users) {
            Row row = flowSheet.createRow(currentRow);
            Long id = user.getId();
            row.createCell(0).setCellValue(id);
            int currentColumn = 1;
            List<Stops> flow = user.getFlow();
            for (Stops stops : flow) {
                row.createCell(currentColumn).setCellValue(stops.toString());
                currentColumn++;
            }
            currentRow++;
        }
    }

    private void buildBigviewHeaders() {
        Row headerRow = userBigviewSheet.createRow(0);
        String[] headers = {"User", "Product", "Start Epoch", "End Epoch", "Total"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
            userBigviewSheet.setColumnWidth(i, 15 * 256);
        }
    }

    private void addBigviewData(){
        int currentRow = 1;
        for (User user : users) {
            Long userId = user.getId();
            Long startProd = user.getTimeStartProduction();
            Long endGive = user.getTimeEndProductGive();
            Map<Long, Product> products = user.getProducts();
            List<Long> orderedTimes = products.keySet().stream().sorted().toList();
            List<Order> orders = new ArrayList<>();

            for (int i = 0; i < orderedTimes.size(); i++) {
                Long time = orderedTimes.get(i);
                Product product = products.get(time);
                if (i == 0){
                    Long endTime = orderedTimes.size() == 1 ? endGive :
                            orderedTimes.get(i + 1);
                    Order firstOrder = new Order(product, startProd, endTime);
                    orders.add(firstOrder);
                    continue;
                }
                if (i == orderedTimes.size() - 1){
                    Long startTime = orderedTimes.get(i - 1);
                    Order lastOrder = new Order(product, startTime, endGive);
                    orders.add(lastOrder);
                    continue;
                }
                Long startTime = orderedTimes.get(i - 1);
                Order order = new Order(product, startTime, time);
                orders.add(order);
            }

            for (Order order : orders) {
                Row row = userBigviewSheet.createRow(currentRow);
                row.createCell(0).setCellValue(userId);
                row.createCell(1).setCellValue(order.product.toString());
                row.createCell(2).setCellValue(order.start);
                row.createCell(3).setCellValue(order.end);
                CellAddress startAddress = new CellAddress(currentRow, 2);
                CellAddress endAddress = new CellAddress(currentRow, 3);
                row.createCell(4).setCellFormula(endAddress.formatAsString() + "-" + startAddress.formatAsString());
                currentRow++;
            }
        }
    }

    private record Order(Product product, Long start, Long end){}
    
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

    private void addMainData(){
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

    private String formulaEpoch(CellAddress address){
        return  "(" + address.formatAsString() + "/86400) - TIME(6,0,0) + DATE(1970,1,1)";
    }
    
    private void addToTable(List<List<Long>> data){
        int currentRow = 1;
        int currentCol = 0;
        for (List<Long> datum : data) {
            Row row = mainSheet.createRow(currentRow);
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
                String formula = formulaEpoch(prevColumnAddres);
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

    private void buildMainHeaders(){
        Row headerRow = mainSheet.createRow(0);
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
            mainSheet.setColumnWidth(i, 15 * 256);
        }
    }

    private void fetchData(){
        users = EndpointsManager.getInstance()
                .USERS
                .getAllItems(System.out::println)
                .join();
    }

}
