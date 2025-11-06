import java.awt.*;
import java.awt.image.BufferedImage;

public class ZebraImageConverter {

    public static String imageToZPL(Image awtImage, String imageName, int targetWidth, int targetHeight) {
        BufferedImage image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(awtImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        int widthBytes = (targetWidth + 7) / 8;
        int totalBytes = widthBytes * targetHeight;
        StringBuilder hexData = new StringBuilder(totalBytes * 2);

        for (int y = 0; y < targetHeight; y++) {
            int byteVal = 0;
            int bit = 0;
            for (int x = 0; x < targetWidth; x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                int pixel = (rgb == 0xFFFFFF) ? 0 : 1;
                byteVal |= (pixel << (7 - bit));
                bit++;
                if (bit == 8) {
                    hexData.append(String.format("%02X", byteVal));
                    bit = 0;
                    byteVal = 0;
                }
            }
            if (bit > 0) {
                hexData.append(String.format("%02X", byteVal));
            }
        }

        return String.format("~DG%s.GRF,%d,%d,%s", imageName, totalBytes, widthBytes, hexData.toString());
    }
}
