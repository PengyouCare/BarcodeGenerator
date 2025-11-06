
import com.formdev.flatlaf.FlatLightLaf;

import javax.print.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterJob;
import java.io.*;

public class PNRCreation {
    private ZebraPanel zebraPanel;
    private JTextField inchWidthField, inchHeightField;
    private JTextField imgWidthField, imgHeightField;
    private JTextField textField, textSizeField;
    private JScrollPane scrollPane;
    private JFrame frame;
    private final int DPI = Toolkit.getDefaultToolkit().getScreenResolution();


    public void pnr_create() {
        frame = new JFrame("PNR Creator");
        frame.setSize(1300, 750);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            // Set modern FlatLaf theme
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JLabel lblInchWidth = new JLabel("Panel Width (in):");
        lblInchWidth.setBounds(20, 20, 120, 30);
        inchWidthField = new JTextField("");
        inchWidthField.setBounds(140, 20, 60, 30);

        JLabel lblInchHeight = new JLabel("Panel Height (in):");
        lblInchHeight.setBounds(20, 60, 120, 30);
        inchHeightField = new JTextField("");
        inchHeightField.setBounds(140, 60, 60, 30);

        JButton btnGeneratePanel = new JButton("Generate Panel");
        btnGeneratePanel.setBounds(20, 100, 180, 30);

        JButton uploadImageButton = new JButton("Upload Image");
        uploadImageButton.setBounds(20, 150, 180, 30);

        JLabel lblImgW = new JLabel("Image Width (in):");
        lblImgW.setBounds(20, 200, 120, 30);
        imgWidthField = new JTextField("1");
        imgWidthField.setBounds(140, 200, 60, 30);

        JLabel lblImgH = new JLabel("Image Height (in):");
        lblImgH.setBounds(20, 240, 120, 30);
        imgHeightField = new JTextField("1");
        imgHeightField.setBounds(140, 240, 60, 30);

        JButton btnResize = new JButton("Resize Selected");
        btnResize.setBounds(20, 280, 180, 30);

        JLabel lblText = new JLabel("Enter Text:");
        lblText.setBounds(20, 330, 100, 30);
        textField = new JTextField("");
        textField.setBounds(20, 360, 180, 30);

        JLabel lblTextSize = new JLabel("Font Size (in):");
        lblTextSize.setBounds(20, 400, 100, 30);
        textSizeField = new JTextField("0.2");
        textSizeField.setBounds(140, 400, 60, 30);

        JButton btnAddText = new JButton("Add Text");
        btnAddText.setBounds(20, 440, 180, 30);

        JButton btnResizeText = new JButton("Resize Text");
        btnResizeText.setBounds(20, 480, 180, 30);

        JButton btnRemoveSelected = new JButton("Remove Selected");
        btnRemoveSelected.setBounds(20, 520, 180, 30);

        JButton btnResetAll = new JButton("Reset All");
        btnResetAll.setBounds(20, 560, 180, 30);

        JButton btnPrint = new JButton("Print");
        btnPrint.setBounds(20, 600, 180, 30);

        JButton btnSavePrn = new JButton("Save as PRN");
        btnSavePrn.setBounds(20, 640, 180, 30);

        JButton btnView = new JButton("View");
        btnView.setBounds(20, 680, 180, 30);

        btnGeneratePanel.addActionListener(e -> {
            try {
                double wIn = Double.parseDouble(inchWidthField.getText());
                double hIn = Double.parseDouble(inchHeightField.getText());

                // Convert inches → pixels at 203 DPI
                final int DPI = 203;
                int widthPx = (int) Math.round(wIn * DPI);
                int heightPx = (int) Math.round(hIn * DPI);

                if (scrollPane != null) frame.remove(scrollPane);

                // Create ZebraPanel with inch-based logic
                zebraPanel = new ZebraPanel(wIn, hIn);
                zebraPanel.setScreenDPI(DPI); // make sure ZebraPanel uses same DPI

                // Set fixed physical size (no override mismatch)
                zebraPanel.setPreferredSize(new Dimension(widthPx, heightPx));
                zebraPanel.setMinimumSize(new Dimension(widthPx, heightPx));
                zebraPanel.setMaximumSize(new Dimension(widthPx, heightPx));
                zebraPanel.setSize(widthPx, heightPx);

                // Wrap in scroll pane (limit to window size)
                scrollPane = new JScrollPane(zebraPanel);
                scrollPane.setBounds(
                        250, 20,
                        Math.min(widthPx + 20, 1000),
                        Math.min(heightPx + 20, 650)
                );
                frame.add(scrollPane);

                frame.revalidate();
                frame.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid width or height.");
            }
        });



        uploadImageButton.addActionListener(e -> {
            if (zebraPanel == null) return;
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                ImageIcon icon = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
                zebraPanel.addDraggableImage(icon.getImage());
            }
        });


        btnResize.addActionListener(e -> {
            if (zebraPanel == null) return;
            try {
                double w = Double.parseDouble(imgWidthField.getText());
                double h = Double.parseDouble(imgHeightField.getText());
                zebraPanel.resizeSelectedImage(w, h);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid image size.");
            }
        });


        btnAddText.addActionListener(e -> {
            if (zebraPanel != null) zebraPanel.addDraggableText(textField.getText());
        });


        btnResizeText.addActionListener(e -> {
            if (zebraPanel == null) return;
            try {
                zebraPanel.resizeSelectedText(Double.parseDouble(textSizeField.getText()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid font size.");
            }
        });

        btnRemoveSelected.addActionListener(e -> {
            if (zebraPanel != null) zebraPanel.removeSelected();
        });


        btnResetAll.addActionListener(e -> {
            if (zebraPanel != null) {
                zebraPanel.resetAll();
                frame.remove(scrollPane);
                scrollPane = null;
                zebraPanel = null;
                frame.revalidate();
                frame.repaint();
            }
        });


        btnPrint.addActionListener(e -> {
            if (zebraPanel == null) {
                JOptionPane.showMessageDialog(frame, "No panel to print.");
                return;
            }

            String zplCode = generateZPLFromPanel();
            if (zplCode == null) return;


            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService printer = (PrintService) JOptionPane.showInputDialog(
                    frame,
                    "Select printer:",
                    "Print",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    services,
                    services.length > 0 ? services[0] : null
            );

            if (printer == null) return;

            try {
                DocPrintJob job = printer.createPrintJob();
                DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
                Doc doc = new SimpleDoc(zplCode.getBytes("UTF-8"), flavor, null);
                job.print(doc, null);
                JOptionPane.showMessageDialog(frame, "ZPL sent to printer.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error sending to printer.");
            }
        });


        btnSavePrn.addActionListener(e -> {
            if (zebraPanel == null) return;
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("output.prn"));
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File prnFile = chooser.getSelectedFile();
                try (PrintWriter out = new PrintWriter(new FileWriter(prnFile))) {
                    out.print(generateZPLFromPanel());
                    JOptionPane.showMessageDialog(frame, "PRN saved at " + prnFile.getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error saving PRN.");
                }
            }
        });


        btnView.addActionListener(e -> {
            if (zebraPanel == null) {
                JOptionPane.showMessageDialog(frame, "No panel to view.");
                return;
            }

            JFrame viewFrame = new JFrame("View Panel");
            viewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JScrollPane viewScrollPane = new JScrollPane(zebraPanel);
            viewFrame.setSize(800, 600);
            viewFrame.add(viewScrollPane);
            viewFrame.setLocationRelativeTo(null);
            viewFrame.setVisible(true);
        });

        frame.add(lblInchWidth);
        frame.add(inchWidthField);
        frame.add(lblInchHeight);
        frame.add(inchHeightField);
        frame.add(btnGeneratePanel);
        frame.add(uploadImageButton);
        frame.add(lblImgW);
        frame.add(imgWidthField);
        frame.add(lblImgH);
        frame.add(imgHeightField);
        frame.add(btnResize);
        frame.add(lblText);
        frame.add(textField);
        frame.add(lblTextSize);
        frame.add(textSizeField);
        frame.add(btnAddText);
        frame.add(btnResizeText);
        frame.add(btnRemoveSelected);
        frame.add(btnResetAll);
        frame.add(btnPrint);
        frame.add(btnSavePrn);
        frame.add(btnView);

        frame.setVisible(true);
    }


    private String generateZPLFromPanel() {
        if (zebraPanel == null) return null;

        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n");

        int imageCounter = 0;

        for (ZebraPanel.DraggableText txt : zebraPanel.getTexts()) {
            int x = txt.x;
            int y = txt.y;
            int height = txt.fontSize;
            zpl.append(String.format("^FO%d,%d^A0N,%d,%d^FD%s^FS\n", x, y, height, height, txt.text));
        }

        for (ZebraPanel.DraggableImage img : zebraPanel.getImages()) {
            int x = img.x;
            int y = img.y;
            int w = img.width;
            int h = img.height;
            String imageName = "IMG" + (++imageCounter);
            String zplImage = ZebraImageConverter.imageToZPL(img.image, imageName, w, h);
            zpl.append(zplImage);
            zpl.append(String.format("^FO%d,%d^XG%s.GRF,1,1^FS\n", x, y, imageName));
        }

        zpl.append("^XZ\n");
        return zpl.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PNRCreation().pnr_create());
    }
}
