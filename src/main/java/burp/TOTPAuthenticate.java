package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import userinterface.UserInterface;

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
            UserInterface userInterface = new UserInterface();

            api.userInterface().registerSuiteTab(EXTENSION_NAME, userInterface.getPanel());
        }
    }
}