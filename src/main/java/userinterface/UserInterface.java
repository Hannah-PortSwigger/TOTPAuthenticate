package userinterface;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import utils.RuleType;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static utils.Utilities.readQRCode;

public class UserInterface
{
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    private static final GoogleAuthenticator AUTHENTICATOR = new GoogleAuthenticator();
    public static final String CONFIG_TEMPLATE = "/^^secretKey:%s,ruleType:%s,parameterName:%s^^/";

    private final JPanel panel;

    public UserInterface(Frame owner)
    {
        GridBagLayout layout = new GridBagLayout();
        panel = new JPanel(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        QRParserPanel qrParserPanel = new QRParserPanel(owner);
        panel.add(qrParserPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;

        ParamInputPanel paramInputPanel = new ParamInputPanel();
        panel.add(paramInputPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;

        panel.add(new ConfigDisplayPanel(qrParserPanel::seed, paramInputPanel::configuredRuleType, paramInputPanel::configuredParam), gbc);
    }

    public JPanel getPanel()
    {
        return panel;
    }

    private static void showError(Frame owner, Exception ex) {
        showError(owner, String.format("%s: %s", ex, ex.getMessage()));
    }

    private static void showError(Frame owner, String error) {
        JOptionPane.showMessageDialog(owner, error, "Error Occurred", JOptionPane.ERROR_MESSAGE);
    }

    private static class QRParserPanel extends JPanel
    {
        private final Frame owner;

        private JLabel qrCodeUrl;

        private String seed = "000000";

        private JLabel currentCode;
        private ScheduledFuture<?> codeUpdateFuture;

        QRParserPanel(Frame owner)
        {
            super(new FlowLayout());
            this.owner = owner;
            initComponents();
        }

        private void initComponents()
        {
            JButton importQr = new JButton("Import QR");
            this.add(importQr);

            qrCodeUrl = new JLabel("otpauth://totp/");
            this.add(qrCodeUrl);

            currentCode = new JLabel("000000");
            this.add(currentCode);

            importQr.addActionListener(e -> {
                JFileChooser jFileChooser = new JFileChooser();

                if (jFileChooser.showDialog(this, "Import") == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile = jFileChooser.getSelectedFile();
                    try {
                        String otpUrl = readQRCode(selectedFile.toString());
                        qrCodeUrl.setText(otpUrl);

                        URI uri = URI.create(otpUrl);
                        if(!"otpauth".equals(uri.getScheme()) || !"totp".equals(uri.getHost()))
                        {
                            showError(owner, "URL must start with otpauth://totp");
                        }
                        else {
                            try {
                                Map<String, String> queryParams = parseQuery(uri);
                                String secret = queryParams.get("secret");
                                if(secret == null) {
                                    showError(owner, "URL does not contain a 'secret' parameter!");
                                } else
                                {
                                    seed = secret;
                                }

                            } catch (UnsupportedEncodingException ex) {
                                showError(owner, ex);
                            }
                        }

                        initializeAuthenticator();
                    } catch (Exception ex) {
                        showError(owner, ex);
                    }
                }
            });
        }

        private void initializeAuthenticator() {
            if(codeUpdateFuture != null)
            {
                codeUpdateFuture.cancel(false);
                codeUpdateFuture = null;

            }
            codeUpdateFuture = EXECUTOR_SERVICE.scheduleAtFixedRate(() -> currentCode.setText(String.valueOf(AUTHENTICATOR.getTotpPassword(seed))), 0, 30, TimeUnit.SECONDS);
        }


        private static Map<String, String> parseQuery(URI uri) throws UnsupportedEncodingException {
            Map<String, String> query = new LinkedHashMap<>();
            String[] pairs = uri.getQuery().split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
            }
            return query;
        }

        String seed()
        {
            return seed;
        }
    }

    private static class ParamInputPanel extends JPanel {

        private JComboBox<RuleType> ruleTypeComboBox;
        private JTextField paramField;

        ParamInputPanel()
        {
            super(new FlowLayout());
            initComponents();
        }

        private void initComponents()
        {
            ruleTypeComboBox = new JComboBox<>(RuleType.values());
            this.add(ruleTypeComboBox);

            JLabel paramLabel = new JLabel("Param Name:");
            this.add(paramLabel);

            paramField = new JTextField("Param name");
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

        RuleType configuredRuleType()
        {
            return (RuleType) ruleTypeComboBox.getSelectedItem();
        }

        String configuredParam()
        {
            return paramField.getText();
        }

    }

    private static class ConfigDisplayPanel extends JPanel
    {
        private final Supplier<String> seedSupplier;
        private final Supplier<RuleType> ruleTypeSupplier;
        private final Supplier<String> paramSupplier;
        private JTextField configField;

        ConfigDisplayPanel(Supplier<String> seedSupplier, Supplier<RuleType> ruleTypeSupplier, Supplier<String> paramSupplier)
        {
            super(new FlowLayout());
            this.seedSupplier = seedSupplier;
            this.ruleTypeSupplier = ruleTypeSupplier;
            this.paramSupplier = paramSupplier;
            initComponents();
        }

        private void initComponents()
        {
            JLabel configLabel = new JLabel("Session handling rule config description:");
            this.add(configLabel);

            JButton generateButton = new JButton("Generate Config");

            generateButton.addActionListener(e -> {
                String seed = seedSupplier.get();
                RuleType ruleType = ruleTypeSupplier.get();
                String paramName = paramSupplier.get();

                if(RuleType.BODY_REGEX.equals(ruleType))
                {
                    paramName = Base64.getEncoder().encodeToString(paramName.getBytes(StandardCharsets.UTF_8));
                }

                configField.setText(String.format(CONFIG_TEMPLATE, seed, ruleType, paramName));

            });

            this.add(generateButton);

            configField = new JTextField(String.format(CONFIG_TEMPLATE, "", "", ""));
            this.add(configField);
        }
    }
}
