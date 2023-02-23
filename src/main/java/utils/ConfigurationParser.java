package utils;

import burp.api.montoya.MontoyaApi;
import org.apache.commons.codec.binary.Base32;

import java.util.Arrays;

public class ConfigurationParser
{
    String secret;
    RuleType ruleType;
    String parameterName;
    private MontoyaApi api;

    public ConfigurationParser(MontoyaApi api, String input)
    {
        this.api = api;
        boolean isInvalid;

        if (input == null || input.equals(""))
        {
            isInvalid = false;
        }
        else
        {
            isInvalid = input.indexOf("/^^") > input.indexOf("^^/");
        }

        if (isInvalid)
        {
            throw new RuntimeException("Invalid configuration input:\r\n" + input);
        }

        parse(input);
    }

    public String getSecret()
    {
        return secret;
    }

    public RuleType getRuleType()
    {
        return ruleType;
    }

    public String getParameterName()
    {
        return parameterName;
    }

    private void parse(String input)
    {
        int iStart = input.indexOf("/^^");
        int iEnd = input.indexOf("^^/");

        if((iStart == iEnd) || iStart == -1 || iEnd == -1)
        {
            api.logging().logToError("Configuration string not present.");
            return;
        }

        iStart = iStart+3;

        String configurationString = input.substring(iStart,iEnd);

        String[] partsInput = configurationString.split(",");

        for (String s : partsInput)
        {
            String[] partsVariable = s.split("=");

            switch (partsVariable[0].toUpperCase())
            {
                case "SECRETKEY":
                    Base32 base32 = new Base32();
                    byte[] secretKeyDecoded = base32.decode(partsVariable[1]);

                    secret = Arrays.toString(secretKeyDecoded);
                    break;

                case "RULETYPE":
                    String ruleTypeStr = partsVariable[1].toUpperCase();

                    ruleType = switch (ruleTypeStr)
                    {
                        case "HEADER" -> RuleType.HEADER;
                        case "URL" -> RuleType.URL;
                        case "COOKIE" -> RuleType.COOKIE;
                        case "BODY" -> RuleType.BODY;
                        default -> RuleType.HEADER;
                    };

                case "PARAMETERNAME":
                    parameterName = partsVariable[1];
                    break;
                default:
                    break;
            }
        }

        if (secret == null)
        {
            api.logging().logToError("Secret is null based on configuration of " + configurationString);
        }

        if (ruleType == null)
        {
            api.logging().logToError("RuleType is null based on configuration of " + configurationString);
        }

        if (parameterName == null)
        {
            api.logging().logToError("ParameterName is null based on configuration of " + configurationString);
        }


    }
}
