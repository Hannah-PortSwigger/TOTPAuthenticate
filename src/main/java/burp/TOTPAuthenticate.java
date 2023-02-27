package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import utils.RuleType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static burp.api.montoya.core.BurpSuiteEdition.ENTERPRISE_EDITION;

public class TOTPAuthenticate implements BurpExtension {
    public static final String EXTENSION_NAME = "TOTP Authenticate";

    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName(EXTENSION_NAME);

        api.http().registerSessionHandlingAction(new MySessionHandlingAction(api));

        if (!ENTERPRISE_EDITION.equals(api.burpSuite().version().edition()))
        {
            GridLayout layout = new GridLayout(3, 1);
            JPanel panel = new JPanel(layout);
            panel.add(new QRParserPanel());
            panel.add(new ParamInputPanel());
            panel.add(new ConfigDisplayPanel());

            api.userInterface().registerSuiteTab(EXTENSION_NAME, panel);
        }
    }

    private static class QRParserPanel extends JPanel
    {
        QRParserPanel()
        {
            super(new GridLayout(1,2));
            initComponents();
        }

        private void initComponents()
        {
            JButton importQr = new JButton("Import QR");
            JLabel qrCodeUrl = new JLabel("otpauth://totp/");

            importQr.addActionListener(e -> {
                JFileChooser jFileChooser = new JFileChooser();
                if(jFileChooser.showDialog(this, "Import") == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile = jFileChooser.getSelectedFile();
                    try {
                        qrCodeUrl.setText(readQRCode(selectedFile.toString()));
                    } catch (IOException | NotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            });

            add(importQr);
            add(qrCodeUrl);
        }

        public static String readQRCode(String filePath)
                throws IOException, NotFoundException {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(
                            ImageIO.read(new FileInputStream(filePath)))));
            Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
            return qrCodeResult.getText();
        }
    }

    private static class ParamInputPanel extends JPanel {
        ParamInputPanel()
        {
            super(new GridLayout(1,3));
            initComponents();
        }

        private void initComponents()
        {
            JComboBox<RuleType> regexLabel = new JComboBox<>(RuleType.values());
            JLabel paramLabel = new JLabel("Param Name");
            JTextField paramName = new JTextField("Param Name");
            regexLabel.addItemListener(e -> {
                if(e.getItem() == RuleType.BODY_REGEX)
                {
                    paramLabel.setText("Enter Regex");
                }
                else
                {
                    paramLabel.setText("Param Name");
                }
            });

            add(regexLabel);
            add(paramLabel);
            add(paramName);
        }
    }

    private static class ConfigDisplayPanel extends JPanel
    {
        ConfigDisplayPanel()
        {
            super(new GridLayout(1,2));
            initComponents();
        }

        private void initComponents()
        {
            JLabel configLabel = new JLabel("Session handling rule config description: ");
            JTextField configText = new JTextField("/^^secretKey:,ruleType:BODY_REGEX,parameterName:^^/");

            add(configLabel);
            add(configText);
        }
    }
}