import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BarcodeUi {

    public static void main(String[] args) {
        try {
            // Set modern FlatLaf theme
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("Barcode Generator");
        frame.setSize(1000, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        final File[] selectedFile = new File[1];
        List<JCheckBox> checkBoxes = new ArrayList<>();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);
        frame.setContentPane(mainPanel);

        JLabel heading = new JLabel("Barcode Generator", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setBounds(300, 30, 400, 40);
        mainPanel.add(heading);

        // CSV File Selection
        JLabel pathLabel = new JLabel("Source CSV File:");
        pathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        pathLabel.setBounds(200, 100, 200, 30);
        mainPanel.add(pathLabel);

        JLabel filePathLabel = new JLabel("No File Selected");
        filePathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        filePathLabel.setBounds(380, 100, 400, 30);
        filePathLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        filePathLabel.setOpaque(true);
        filePathLabel.setBackground(Color.WHITE);
        filePathLabel.setForeground(Color.GRAY);
        mainPanel.add(filePathLabel);

        JButton browseButton = new JButton("Browse");
        browseButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        browseButton.setBounds(800, 100, 120, 32);
        mainPanel.add(browseButton);

        // Barcode Type Selection
        JLabel barcodeTypeLabel = new JLabel("Barcode Type:");
        barcodeTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        barcodeTypeLabel.setBounds(200, 150, 200, 30);
        mainPanel.add(barcodeTypeLabel);

        JComboBox<String> barcodeSelect = new JComboBox<>();
        barcodeSelect.setBounds(380, 150, 400, 30);
        barcodeSelect.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        barcodeSelect.addItem("Select Barcode Type");
        barcodeSelect.addItem("Code 128");
        barcodeSelect.addItem("Code 39");
        barcodeSelect.addItem("QR Code");
        barcodeSelect.addItem("Data Matrix");
        barcodeSelect.addItem("Code 93");
        barcodeSelect.addItem("PDF 417");
        barcodeSelect.addItem("Codabar");
        mainPanel.add(barcodeSelect);

        // CSV Column Checkboxes
        JPanel display = new JPanel();
        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));
        display.setBackground(Color.WHITE);
        display.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(display);
        scrollPane.setBounds(200, 200, 700, 200);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        mainPanel.add(scrollPane);

        // Status Label
        JLabel statusLabel = new JLabel("");
        statusLabel.setBounds(200, 420, 700, 25);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(statusLabel);

        // Radio Button for 'With Name' or 'Without Name'
        JLabel optionText = new JLabel("Do You Want With Name Or Without Name");
        optionText.setBounds(200, 430, 400, 30);
        optionText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        frame.add(optionText);

        JRadioButton name = new JRadioButton("With Name");
        name.setBounds(200, 460, 150, 30);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        name.setBackground(Color.WHITE);
        frame.add(name);

        JRadioButton withoutName = new JRadioButton("Without Name");
        withoutName.setBounds(350, 460, 150, 30);
        withoutName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        withoutName.setBackground(Color.WHITE);
        frame.add(withoutName);

        ButtonGroup group = new ButtonGroup();
        group.add(name);
        group.add(withoutName);

        // Generate Barcode Button
        JButton generateButton = new JButton("Generate Barcode");
        generateButton.setBounds(200, 560, 250, 35);
        generateButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(generateButton);

        // Go To PRN File Button
        JButton prFile = new JButton("Go To PRN File");
        prFile.setBounds(200, 610, 250, 35);
        prFile.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(prFile);

        // View CSV Data Button
        JButton viewCsvDataButton = new JButton("View CSV Data");
        viewCsvDataButton.setBounds(200, 660, 250, 35);
        viewCsvDataButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(viewCsvDataButton);


        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                filePathLabel.setText(selectedFile[0].getAbsolutePath());
                filePathLabel.setForeground(Color.BLACK);
                statusLabel.setText("");
            }
        });

        // Barcode Type Selection Action Listener
        barcodeSelect.addActionListener(e -> {
            if (barcodeSelect.getSelectedIndex() <= 0) return;

            if (selectedFile[0] == null) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please select a file first.");
                barcodeSelect.setSelectedIndex(0);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile[0]))) {
                String headerLine = reader.readLine();
                if (headerLine != null) {
                    checkBoxes.clear();
                    display.removeAll();

                    String[] headers = headerLine.split(",");

                    for (String header : headers) {
                        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        row.setBackground(Color.WHITE);
                        JLabel label = new JLabel(header.trim());
                        label.setPreferredSize(new Dimension(250, 25));

                        JCheckBox cb = new JCheckBox(header.trim());
                        cb.setBackground(Color.WHITE);
                        checkBoxes.add(cb);
                        row.add(cb);
                        display.add(row);
                    }

                    display.revalidate();
                    display.repaint();
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Select columns and click Generate.");
                }
            } catch (IOException ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Error reading CSV: " + ex.getMessage());
            }
        });

        // Generate Barcode Button Action Listener
        generateButton.addActionListener(e -> {
            if (barcodeSelect.getSelectedIndex() <= 0) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please select a barcode type.");
                return;
            }

            if (selectedFile[0] == null) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please select a CSV file.");
                return;
            }

            boolean anySelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
            if (!anySelected) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please select at least one field.");
                return;
            }


            boolean withName = name.isSelected();
            boolean withoutNameSelected = withoutName.isSelected();

            if (!withName && !withoutNameSelected) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please select 'With Name' or 'Without Name'.");
                return;
            }


            if (barcodeSelect.getSelectedIndex() == 1) {
                BarcodeGenerator generator = new Code128();
                try {
                    // Pass 'withName' as parameter for barcode generation logic
                    ((Code128) generator).generateBarcodes(selectedFile[0], statusLabel, checkBoxes, withName);
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Barcodes generated successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error generating barcode: " + ex.getMessage());
                }
            }
            else if(barcodeSelect.getSelectedIndex() == 2)
            {
                Code39 code39 = new Code39();
                try{
                    ((Code39)code39 ).generateBarcodes(selectedFile[0],statusLabel,checkBoxes,withName);
                    statusLabel.setForeground(new Color(0,128,0));
                    statusLabel.setText("Barcodes Generated Sucessfully");
                }catch(Exception ex)
                {
                    ex.printStackTrace();
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error Generating barcode:"+ex.getMessage());
                }
            }
            else if(barcodeSelect.getSelectedIndex() == 3)
            {
                QRCode qrCode = new QRCode();
                try{
                    ((QRCode)qrCode).generateBarcodes(selectedFile[0],statusLabel,checkBoxes,withName);
                    statusLabel.setForeground(new Color(0,128,0));
                    statusLabel.setText("Barcodes Generated Sucessfully");
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error Generating barcode:"+ex.getMessage());
                }
            }
            else if(barcodeSelect.getSelectedIndex() == 4)
            {
                DataMatrix dm = new DataMatrix();
                try{
                    ((DataMatrix)dm).generateBarcodes(selectedFile[0],statusLabel,checkBoxes,withName);
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error Generating barcode:"+ex.getMessage());
                }
            }
            else if(barcodeSelect.getSelectedIndex() == 5){
                Code93 code93 = new Code93();
                try{
                    ((Code93)code93).generateBarcodes(selectedFile[0],statusLabel,checkBoxes,withName);
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error Generating barcode:"+ex.getMessage());
                }
            } else if (barcodeSelect.getSelectedIndex() ==6 ) {
                PDF417 pdf417 = new PDF417();
                try{
                    ((PDF417)pdf417).generateBarcodes(selectedFile[0],statusLabel,checkBoxes,withName);
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error Generating barcode:"+ ex.getMessage());
                }
            } else if (barcodeSelect.getSelectedIndex() == 7) {
                Codabar codabar = new Codabar();
                try{
                    ((Codabar)codabar).generateBarcodes(selectedFile[0],statusLabel,checkBoxes,withName);
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error Generating barcode:"+ ex.getMessage());
                }
            }
        });



        prFile.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PNRCreation pnr_create = new PNRCreation();
                pnr_create.pnr_create();
            }
        });


        viewCsvDataButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Please select a CSV file first.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile[0]))) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    JOptionPane.showMessageDialog(frame, "CSV file is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] headers = headerLine.split(",");
                List<String[]> rows = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] row = line.split(",", -1);
                    rows.add(row);
                }

                String[][] data = new String[rows.size()][headers.length];
                for (int i = 0; i < rows.size(); i++) {
                    data[i] = rows.get(i);
                }

                JTable table = new JTable(data, headers);
                JScrollPane tableScrollPane = new JScrollPane(table);
                tableScrollPane.setPreferredSize(new Dimension(800, 400));

                JOptionPane.showMessageDialog(frame, tableScrollPane, "CSV Data", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error reading CSV file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Show the frame
        frame.setVisible(true);
    }
}
