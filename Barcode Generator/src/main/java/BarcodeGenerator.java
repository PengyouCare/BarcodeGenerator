import javax.swing.*;
import java.io.File;

public interface BarcodeGenerator {
    public String getName();
    public void generator(File selectedFile, JLabel url, JComboBox<String> barName, JLabel validBarText, String nameFieldValidation);
}
