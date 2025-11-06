import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Codabar implements BarcodeGenerator {

    @Override
    public String getName() {
        return "Codabar";
    }

    @Override
    public void generator(File selectedFile, JLabel url, JComboBox<String> barName, JLabel validBarText, String nameFieldValidation) {
        Code128 c = new Code128();
        c.generator(selectedFile, url, barName, validBarText, nameFieldValidation);
    }

    public void generateBarcodes(File file, JLabel statusLabel, List<JCheckBox> selectedCheckboxes, boolean withName) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss a");
        String format = now.format(formatter);

        if (selectedCheckboxes == null || selectedCheckboxes.isEmpty()) {
            statusLabel.setText("No checkboxes selected.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                statusLabel.setText("CSV is empty.");
                return;
            }

            String[] headers = headerLine.split(",");
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndexMap.put(headers[i].trim().toLowerCase(), i);
            }

            List<String> selectedColumns = new ArrayList<>();
            for (JCheckBox cb : selectedCheckboxes) {
                if (cb.isSelected()) {
                    String column = cb.getText().trim().toLowerCase();
                    if (headerIndexMap.containsKey(column)) {
                        selectedColumns.add(column);
                    }
                }
            }

            if (selectedColumns.isEmpty()) {
                statusLabel.setText("No valid columns selected.");
                return;
            }

            File outputDir = new File("C:/Barcodes/CODABAR/" + format);
            if (!outputDir.exists()) outputDir.mkdirs();

            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                Map<String, String> rowValues = new HashMap<>();
                for (int i = 0; i < headers.length && i < fields.length; i++) {
                    String key = headers[i].trim().toLowerCase();
                    String value = fields[i].trim().replaceAll("\\s+", "_");
                    rowValues.put(key, value);
                }

                String name = rowValues.getOrDefault("name", "Unknown");

                for (String selectedColumn : selectedColumns) {
                    String barcodeText = rowValues.get(selectedColumn);
                    if (barcodeText == null || barcodeText.isEmpty()) continue;

                    String label = selectedColumn + "_" + name + "_" + barcodeText;
                    count += generateBarcodeForField(barcodeText, label, outputDir, withName);
                }
            }

            statusLabel.setForeground(new Color(0, 128, 0));
            statusLabel.setText("Generated " + count + " barcodes in: " + outputDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private int generateBarcodeForField(String barcodeText, String label, File outputDir, boolean withName) {
        int successCount = 0;
        try {
            // Dynamic width based on text length
            int dynamicWidth = Math.max(270, barcodeText.length() * 15);
            int height = 100;

            BitMatrix matrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.CODABAR, dynamicWidth, height);
            BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(matrix, new MatrixToImageConfig());

            int labelHeight = withName ? 25 : 0;

            // No horizontal padding
            BufferedImage finalImage = new BufferedImage(barcodeImage.getWidth(), barcodeImage.getHeight() + labelHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = finalImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

            g.drawImage(barcodeImage, 0, 0, null);

            if (withName) {
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(barcodeText);
                int x = (barcodeImage.getWidth() - textWidth) / 2;
                int y = barcodeImage.getHeight() + fm.getAscent();
                g.drawString(barcodeText, x, y);
            }

            g.dispose();

            String filename = label + ".png";
            File outputFile = new File(outputDir, filename);
            int counter = 1;
            while (outputFile.exists()) {
                filename = label + "_" + counter + ".png";
                outputFile = new File(outputDir, filename);
                counter++;
            }

            ImageIO.write(finalImage, "PNG", outputFile);
            successCount++;

        } catch (Exception e) {
            System.err.println("Failed to generate barcode for field: " + barcodeText);
            e.printStackTrace();
        }
        return successCount;
    }
}
