package userinterface;

import com.google.zxing.NotFoundException;
import utils.RuleType;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static utils.Utilities.readQRCode;

public class UserInterface
{
    JPanel panel;

    public UserInterface()
    {
        GridBagLayout layout = new GridBagLayout();
        panel = new JPanel(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        panel.add(new QRParserPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;

        panel.add(new ParamInputPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;

        panel.add(new ConfigDisplayPanel(), gbc);
    }

    public JPanel getPanel()
    {
        return panel;
    }

    private static class QRParserPanel extends JPanel
    {
        QRParserPanel()
        {
            super(new FlowLayout());
            initComponents();
        }

        private void initComponents()
        {
            JButton importQr = new JButton("Import QR");
            this.add(importQr);

            JLabel qrCodeUrl = new JLabel("otpauth://totp/");
            this.add(qrCodeUrl);

            importQr.addActionListener(e -> {
                JFileChooser jFileChooser = new JFileChooser();

                if (jFileChooser.showDialog(this, "Import") == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile = jFileChooser.getSelectedFile();
                    try {
                        qrCodeUrl.setText(readQRCode(selectedFile.toString()));
                    } catch (IOException | NotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
    }

    private static class ParamInputPanel extends JPanel {
        ParamInputPanel()
        {
            super(new FlowLayout());
            initComponents();
        }

        private void initComponents()
        {
            JComboBox<RuleType> ruleTypeComboBox = new JComboBox<>(RuleType.values());
            this.add(ruleTypeComboBox);

            JLabel paramLabel = new JLabel("Param Name:");
            this.add(paramLabel);

            JTextField paramField = new JTextField("Param name");
            this.add(paramField);

            ruleTypeComboBox.addItemListener(e -> {
                if(e.getItem() == RuleType.BODY_REGEX)
                {
                    paramLabel.setText("Enter Regex");
                }
                else
                {
                    paramLabel.setText("Param Name");
                }
            });
        }
    }

    private static class ConfigDisplayPanel extends JPanel
    {
        ConfigDisplayPanel()
        {
            super(new FlowLayout());
            initComponents();
        }

        private void initComponents()
        {
            JLabel configLabel = new JLabel("Session handling rule config description:");
            this.add(configLabel);

            JTextField configField = new JTextField("/^^secretKey:,ruleType:BODY_REGEX,parameterName:^^/");
            this.add(configField);
        }
    }
}
