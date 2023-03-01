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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(new QRParserPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(new ParamInputPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
            super(new GridBagLayout());
            initComponents();
        }

        private void initComponents()
        {
            JButton importQr = new JButton("Import QR");
            JLabel qrCodeUrl = new JLabel("otpauth://totp/");

            GridBagConstraints gbc;

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 5;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            this.add(importQr, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            this.add(qrCodeUrl, gbc);


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
            super(new GridBagLayout());
            initComponents();
        }

        private void initComponents()
        {
            GridBagConstraints gbc;

            JComboBox<RuleType> ruleTypeComboBox = new JComboBox<>(RuleType.values());
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            gbc.gridheight = 2;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.add(ruleTypeComboBox, gbc);

            JLabel paramLabel = new JLabel("Param Name");
            gbc = new GridBagConstraints();
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.anchor = GridBagConstraints.WEST;
            this.add(paramLabel, gbc);

            JTextField paramField = new JTextField("Param name");
            gbc = new GridBagConstraints();
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.add(paramField, gbc);

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
            super(new GridBagLayout());
            initComponents();
        }

        private void initComponents()
        {
            GridBagConstraints gbc;

            JLabel configLabel = new JLabel("Session handling rule config description:");
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 5;
            gbc.anchor = GridBagConstraints.WEST;
            this.add(configLabel, gbc);

            JTextField configField = new JTextField("/^^secretKey:,ruleType:BODY_REGEX,parameterName:^^/");
            gbc = new GridBagConstraints();
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.add(configField, gbc);
        }
    }
}
