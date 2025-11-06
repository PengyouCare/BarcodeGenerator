import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class Code128 implements BarcodeGenerator {

    @Override
    public String getName() {
        return "Code 128";
    }

    @Override
    public void generator(File file, JLabel statusLabel, JComboBox<String> barName, JLabel validBarText, String nameFieldValidation) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                statusLabel.setText("CSV file is empty.");
                return;
            }

            String[] headers = headerLine.split(",");
            barName.removeAllItems();
            barName.addItem("Select The Field on Bar Name");

            for (String header : headers) {
                barName.addItem(header.trim());
            }

            statusLabel.setText("Select a field and click Generate.");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
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
            List<Integer> selectedIndices = new ArrayList<>();

            for (JCheckBox cb : selectedCheckboxes) {
                if (cb.isSelected()) {
                    String column = cb.getText().trim().toLowerCase();
                    Integer index = headerIndexMap.get(column);
                    if (index != null) {
                        selectedIndices.add(index);
                        selectedColumns.add(column);
                    }
                }
            }

            if (selectedIndices.isEmpty()) {
                statusLabel.setText("No valid columns selected.");
                return;
            }

            File outputDir = new File("C:/Barcodes/Code 128/" + format);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

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

                String name = rowValues.get("name");
                String id = rowValues.get("id");

                for (String selectedColumn : selectedColumns) {
                    String barcodeText = rowValues.get(selectedColumn);
                    if (barcodeText == null || barcodeText.isEmpty()) continue;

                    String label = selectedColumn + "_" + (name != null ? name : "Unknown") + "_" + barcodeText;
                    generateBarcodeForField(barcodeText, label, outputDir, count, withName);
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

    private void generateBarcodeForField(String barcodeText, String label, File outputDir, int count, boolean withName) {
        try {
            // Remove ZXing's built-in quiet zone (no horizontal padding)
            Map<com.google.zxing.EncodeHintType, Object> hints = new HashMap<>();
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 0);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    barcodeText, BarcodeFormat.CODE_128, 270, 100, hints);

            int topPadding = 10;
            int bottomPadding = 10;
            int labelHeight = withName ? 30 : 0;

            BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(matrix, new MatrixToImageConfig());
            int width = barcodeImage.getWidth();
            int height = barcodeImage.getHeight();

            // Keep width same as barcode; add only vertical padding
            BufferedImage finalImage = new BufferedImage(width, height + topPadding + bottomPadding + labelHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = finalImage.createGraphics();

            // White background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

            // Draw barcode without horizontal padding
            g.drawImage(barcodeImage, 0, topPadding, null);

            // Draw label text if enabled
            if (withName) {
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(barcodeText);
                int x = (finalImage.getWidth() - textWidth) / 2; // Centered horizontally
                int y = height + topPadding + (labelHeight + fm.getAscent()) / 2 - 5;
                g.drawString(barcodeText, x, y);
            }

            g.dispose();

            // Save image
            String filename = label + ".png";
            File outputFile = new File(outputDir, filename);
            int counter = 1;
            while (outputFile.exists()) {
                filename = label + "_" + counter + ".png";
                outputFile = new File(outputDir, filename);
                counter++;
            }

            ImageIO.write(finalImage, "PNG", outputFile);
            count++;
        } catch (Exception ex) {
            System.err.println("Failed to generate barcode for field: " + barcodeText);
            ex.printStackTrace();
        }
    }
}
