package com.sms.contact_service.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sms.contact_service.entity.Contact;
import com.sms.contact_service.repository.ContactRepository;

@Service
public class CSVService {

    private final ContactRepository repository;

    public CSVService(ContactRepository repository) {
        this.repository = repository;
    }

    // ================== CSV UPLOAD ==================
    public String uploadCSV(MultipartFile file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            int uploadedCount = 0;
            int duplicateCount = 0;
            int invalidCount = 0;

            StringBuilder duplicates = new StringBuilder();
            StringBuilder invalidNumbers = new StringBuilder();

            for (CSVRecord record : records) {
                String name = record.get("name");
                String mobile = record.get("mobile");
                String email = record.get("email");
                String groupName = record.get("groupName");

                // Check 10-digit validation
                if (mobile == null || !mobile.trim().matches("\\d{10}")) {
                    invalidCount++;
                    invalidNumbers.append(mobile).append("\n");
                    continue;
                }

                // Check duplicate check
                if (repository.findByMobile(mobile).isPresent()) {
                    duplicateCount++;
                    duplicates.append(mobile).append("\n");
                    continue;
                }

                Contact contact = new Contact();
                contact.setName(name);
                contact.setMobile(mobile);
                contact.setEmail(email);
                contact.setGroupName(groupName);
                repository.save(contact);
                uploadedCount++;
            }

            return formatResponse("CSV", uploadedCount, duplicateCount, invalidCount, duplicates, invalidNumbers);

        } catch (Exception e) {
            return "CSV Upload Failed : " + e.getMessage();
        }
    }

    // ================== EXCEL UPLOAD ==================
    public String uploadExcel(MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            int uploadedCount = 0;
            int duplicateCount = 0;
            int invalidCount = 0;

            StringBuilder duplicates = new StringBuilder();
            StringBuilder invalidNumbers = new StringBuilder();

            for (Row row : sheet) {
                // Skip header row or empty rows
                if (row.getRowNum() == 0 || row.getCell(0) == null) {
                    continue;
                }

                // Safely convert cells to String values
                String name = getCellValueAsString(row.getCell(0));
                String mobile = getCellValueAsString(row.getCell(1));
                String email = getCellValueAsString(row.getCell(2));
                String groupName = getCellValueAsString(row.getCell(3));

                // Check 10-digit validation
                if (mobile == null || !mobile.trim().matches("\\d{10}")) {
                    invalidCount++;
                    invalidNumbers.append(mobile).append(" (Not 10 Digits)\n");
                    continue;
                }

                // Check duplicate check
                if (repository.findByMobile(mobile).isPresent()) {
                    duplicateCount++;
                    duplicates.append(mobile).append("\n");
                    continue;
                }

                Contact contact = new Contact();
                contact.setName(name);
                contact.setMobile(mobile);
                contact.setEmail(email);
                contact.setGroupName(groupName);
                repository.save(contact);
                uploadedCount++;
            }
            workbook.close();

            return formatResponse("Excel", uploadedCount, duplicateCount, invalidCount, duplicates, invalidNumbers);

        } catch (Exception e) {
            return "Excel Upload Failed : " + e.getMessage();
        }
    }

    // Helper method to safely read any Apache POI cell type as a String
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: 
                // Handles case where numbers might get parsed into scientific notations
                return String.format("%.0f", cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    // Standardized response layout builder
    private String formatResponse(String type, int uploaded, int duplicatesCount, int invalidCount, 
                                  StringBuilder duplicates, StringBuilder invalidNumbers) {
        return type + " Process Completed\n\n"
                + "Uploaded Contacts : " + uploaded + "\n"
                + "Duplicate Contacts Skipped : " + duplicatesCount + "\n"
                + "Invalid Length Contacts Skipped : " + invalidCount + "\n\n"
                + "--- Invalid Mobile Numbers (Not 10 Digits) ---\n"
                + (invalidNumbers.length() == 0 ? "None\n" : invalidNumbers.toString()) + "\n"
                + "--- Duplicate Mobile Numbers ---\n"
                + (duplicates.length() == 0 ? "None\n" : duplicates.toString());
    }
}