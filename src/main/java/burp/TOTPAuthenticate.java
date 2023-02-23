package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import utils.ConfigurationParser;
import utils.RuleType;

public class TOTPAuthenticate implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("TOTP Authenticate");

        api.http().registerSessionHandlingAction(new MySessionHandlingAction(api));
    }
}