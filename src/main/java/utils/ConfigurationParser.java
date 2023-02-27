package utils;

import burp.api.montoya.MontoyaApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.TOTPAuthenticate.EXTENSION_NAME;

public class ConfigurationParser {
    private String secret;
    private RuleType ruleType;
    private String parameterName;

    private final MontoyaApi api;
    private Pattern replacmentPattern;

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

    public Pattern getReplacmentPattern()
    {
        return replacmentPattern;
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
            String[] partsVariable = s.split(":");

            switch (partsVariable[0].toUpperCase())
            {
                case "SECRETKEY" ->
                {
                    secret = partsVariable[1];
                }
                case "RULETYPE" ->
                {
                    String ruleTypeStr = partsVariable[1].toUpperCase();
                    ruleType = switch (ruleTypeStr)
                            {
                                case "HEADER" -> RuleType.HEADER;
                                case "URL" -> RuleType.URL;
                                case "COOKIE" -> RuleType.COOKIE;
                                case "BODY_PARAM" -> RuleType.BODY_PARAM;
                                case "BODY_REGEX" -> RuleType.BODY_REGEX;
                                default -> RuleType.HEADER;
                            };
                }
                case "PARAMETERNAME" ->
                {
                    try
                    {
                        if (ruleType == RuleType.BODY_REGEX)
                        {
                            replacmentPattern = Pattern.compile(api.utilities().base64Utils().decode(partsVariable[1]).toString(), Pattern.DOTALL | Pattern.MULTILINE);
                            Matcher matcher = replacmentPattern.matcher("");
                            if(matcher.groupCount() != 1) {
                                api.logging().logToError("Regex must define exactly one group to insert token into!");
                            }
                        }
                        else
                        {
                            parameterName = partsVariable[1];
                        }

                    }
                    catch (Exception e)
                    {
                        api.logging().logToOutput(partsVariable[0]);
                        api.logging().logToError("Failed to BASE64 decode " + partsVariable[1]);
                    }
                }
                default ->
                {
                }
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

        if (ruleType == RuleType.BODY_REGEX && replacmentPattern == null)
        {
            api.logging().logToError("ParameterName is null based on configuration of " + configurationString);
        }

        if(ruleType == RuleType.BODY_REGEX)
        {
            api.logging().logToOutput(String.format("%s configured to use regex: %s", EXTENSION_NAME, replacmentPattern.pattern()));
        }
        else
        {
            api.logging().logToOutput(String.format("%s configured to use parameter: %s with name: %s", EXTENSION_NAME, ruleType, parameterName));
        }
    }

}
