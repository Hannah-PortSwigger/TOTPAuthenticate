import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class TOTPAuthenticate implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        String secret = "TEST";
        RuleType ruleType = RuleType.HEADER;
        String parameterName = "TEST";

        api.extension().setName("TOTPAuthenticate");

        api.http().registerSessionHandlingAction(new MySessionHandlingAction(secret, ruleType, parameterName));
    }

    private String retrieveInformationFromProjectOptions(MontoyaApi api)
    {
        String jsonConfig = api.burpSuite().exportProjectOptionsAsJson("project_options.sessions");

        //TODO parse JSON retrieve info
        api.logging().logToOutput(jsonConfig);

        return "";
    }
}