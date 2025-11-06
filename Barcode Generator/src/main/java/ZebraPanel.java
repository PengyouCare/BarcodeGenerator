import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ZebraPanel extends JPanel {

    private final List<DraggableImage> images = new ArrayList<>();
    private final List<DraggableText> texts = new ArrayList<>();
    private DraggableImage selectedImage = null;
    private DraggableText selectedText = null;

    private double panelWidthInches;
    private double panelHeightInches;
    private JLabel sizeLabel;
    private int screenDPI = 203; // not final — adjustable

    public ZebraPanel(double widthInches, double heightInches) {
        this.panelWidthInches = widthInches;
        this.panelHeightInches = heightInches;
        this.setBackground(Color.WHITE);
        setPanelSize();
        initListeners();
    }

    // ------------------- setup methods -------------------
    private void setPanelSize() {
        int panelWidthPx = (int) Math.round(panelWidthInches * screenDPI);
        int panelHeightPx = (int) Math.round(panelHeightInches * screenDPI);

        this.setPreferredSize(new Dimension(panelWidthPx, panelHeightPx));
        this.setMinimumSize(new Dimension(panelWidthPx, panelHeightPx));
        this.setMaximumSize(new Dimension(panelWidthPx, panelHeightPx));
        this.setSize(panelWidthPx, panelHeightPx);
        this.setLayout(null);

        sizeLabel = new JLabel(String.format("Size: %.2f x %.2f in  |  %d x %d px",
                panelWidthInches, panelHeightInches, panelWidthPx, panelHeightPx));
        sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        sizeLabel.setBounds(panelWidthPx - 250, 5, 240, 20);
        this.add(sizeLabel);
    }

    private void initListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                boolean found = false;
                for (DraggableImage img : images) {
                    if (img.contains(e.getPoint())) {
                        selectedImage = img;
                        selectedText = null;
                        found = true;
                        repaint();
                        return;
                    }
                }
                for (DraggableText txt : texts) {
                    if (txt.contains(e.getPoint())) {
                        selectedText = txt;
                        selectedImage = null;
                        found = true;
                        repaint();
                        return;
                    }
                }
                if (!found) {
                    selectedImage = null;
                    selectedText = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (selectedImage != null) {
                    selectedImage.move(e.getPoint());
                    repaint();
                } else if (selectedText != null) {
                    selectedText.move(e.getPoint());
                    repaint();
                }
            }
        });
    }

    // ------------------- public methods -------------------
    public void setScreenDPI(int dpi) {
        this.screenDPI = dpi;
        setPanelSize();
        repaint();
    }

    public void addDraggableImage(Image image) {
        int maxWidth = (int) (panelWidthInches * screenDPI);
        int maxHeight = (int) (panelHeightInches * screenDPI);

        int imgW = image.getWidth(null);
        int imgH = image.getHeight(null);
        if (imgW <= 0 || imgH <= 0) return;

        double scale = Math.min((double) maxWidth / imgW, (double) maxHeight / imgH);
        int newW = (int) (imgW * scale);
        int newH = (int) (imgH * scale);

        DraggableImage newImg = new DraggableImage(image, screenDPI);
        newImg.width = newW;
        newImg.height = newH;
        newImg.x = (maxWidth - newW) / 2;
        newImg.y = (maxHeight - newH) / 2;

        images.add(newImg);
        selectedImage = newImg;
        selectedText = null;
        repaint();
    }

    public void addDraggableText(String text) {
        DraggableText newTxt = new DraggableText(text, screenDPI);
        texts.add(newTxt);
        selectedText = newTxt;
        selectedImage = null;
        repaint();
    }

    public void rotateSelectedImage(double degrees) {
        if (selectedImage != null) {
            selectedImage.rotate(degrees);
            repaint();
        }
    }

    public void rotateSelectedText(double degrees) {
        if (selectedText != null) {
            selectedText.rotate(degrees);
            repaint();
        }
    }

    public void removeSelected() {
        if (selectedImage != null) {
            images.remove(selectedImage);
            selectedImage = null;
        } else if (selectedText != null) {
            texts.remove(selectedText);
            selectedText = null;
        }
        repaint();
    }

    public void resetAll() {
        images.clear();
        texts.clear();
        selectedImage = null;
        selectedText = null;
        repaint();
    }

    // ------------------- paint -------------------
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (DraggableImage img : images) img.draw(g, img == selectedImage);
        for (DraggableText txt : texts) txt.draw(g, txt == selectedText);
    }

    // ------------------- inner classes -------------------
    public static class DraggableImage {
        Image image;
        int x, y, width, height;
        double rotation;
        int screenDPI;

        public DraggableImage(Image image, int screenDPI) {
            this.image = image;
            this.screenDPI = screenDPI;
            this.width = image.getWidth(null);
            this.height = image.getHeight(null);
            this.x = 50;
            this.y = 50;
        }

        public void draw(Graphics g, boolean selected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x + width / 2, y + height / 2);
            g2.rotate(Math.toRadians(rotation));
            g2.drawImage(image, -width / 2, -height / 2, width, height, null);
            if (selected) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(-width / 2, -height / 2, width, height);
            }
            g2.dispose();
        }

        public boolean contains(Point p) {
            return (p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height);
        }

        public void move(Point p) {
            x = p.x - width / 2;
            y = p.y - height / 2;
        }

        public void rotate(double degrees) {
            rotation = (rotation + degrees) % 360;
        }
    }

    public static class DraggableText {
        String text;
        int x, y, fontSize;
        double rotation;
        int screenDPI;

        public DraggableText(String text, int screenDPI) {
            this.text = text;
            this.screenDPI = screenDPI;
            this.fontSize = 20;
            this.x = 100;
            this.y = 100;
        }

        public void draw(Graphics g, boolean selected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            g2.rotate(Math.toRadians(rotation));
            g2.setFont(new Font("Arial", Font.PLAIN, fontSize));
            g2.setColor(Color.BLACK);
            g2.drawString(text, 0, 0);

            if (selected) {
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(text);
                int h = fm.getHeight();
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(0, -fm.getAscent(), w, h);
            }
            g2.dispose();
        }

        public boolean contains(Point p) {
            Font f = new Font("Arial", Font.PLAIN, fontSize);
            FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
            int w = fm.stringWidth(text);
            int h = fm.getHeight();
            return (p.x >= x && p.x <= x + w && p.y >= y - h && p.y <= y);
        }

        public void move(Point p) {
            this.x = p.x;
            this.y = p.y;
        }

        public void rotate(double degrees) {
            rotation = (rotation + degrees) % 360;
        }
    }
    public void resizeSelectedImage(double widthInches, double heightInches) {
        if (selectedImage != null) {
            selectedImage.width = (int) (widthInches * screenDPI);
            selectedImage.height = (int) (heightInches * screenDPI);
            repaint();
        }
    }
    public void resizeSelectedText(double fontSizeInches) {
        if (selectedText != null) {
            selectedText.fontSize = (int) (fontSizeInches * screenDPI);
            repaint();
        }
    }
    public java.util.List<DraggableImage> getImages() {
        return images;
    }

    public java.util.List<DraggableText> getTexts() {
        return texts;
    }


}
