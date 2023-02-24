package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import utils.ConfigurationParser;
import utils.RuleType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.api.montoya.http.message.HttpHeader.httpHeader;
import static burp.api.montoya.http.message.params.HttpParameter.parameter;
import static burp.api.montoya.http.sessions.ActionResult.actionResult;

public class MySessionHandlingAction implements SessionHandlingAction
{
    private String secret;
    private RuleType ruleType;
    private String parameterName;
    private Pattern replacementPattern;

    private final GoogleAuthenticator gAuth;
    private final MontoyaApi api;

    public MySessionHandlingAction(MontoyaApi api)
    {
        this.api = api;
        gAuth = new GoogleAuthenticator();
    }

    @Override
    public String name()
    {
        return "TOTP Authenticate";
    }

    @Override
    public ActionResult performAction(SessionHandlingActionData actionData)
    {
        if (secret == null)
        {
            String input = api.burpSuite().exportProjectOptionsAsJson("project_options.sessions");
            ConfigurationParser configParser = new ConfigurationParser(api, input);

            secret = configParser.getSecret();
            ruleType = configParser.getRuleType();
            parameterName = configParser.getParameterName();
            replacementPattern = configParser.getReplacmentPattern();
        }

        HttpRequest request = actionData.request();

        HttpRequest newRequest = switch (ruleType)
                {
                    case HEADER ->
                            updateOrAddTokenInHeader(request);
                    case URL ->
                            updateOrAddTokenInParameter(request, HttpParameterType.URL);
                    case COOKIE ->
                            updateOrAddTokenInParameter(request, HttpParameterType.COOKIE);
                    case BODY_PARAM ->
                            updateOrAddTokenInParameter(request, HttpParameterType.BODY);
                    case BODY_REGEX -> updateTokenInBody(request);
                };

        return actionResult(newRequest);
    }

    private HttpRequest updateOrAddTokenInHeader(HttpRequest request)
    {
        return isParameterNamePresentInHeaders(request) ? request.withUpdatedHeader(httpHeader(parameterName, String.valueOf(gAuth.getTotpPassword(secret)))) : request.withAddedHeader(httpHeader(parameterName, String.valueOf(gAuth.getTotpPassword(secret))));
    }

    private HttpRequest updateOrAddTokenInParameter(HttpRequest request, HttpParameterType parameterType)
    {
        return isParameterNamePresentInParameters(request, parameterType) ? request.withUpdatedParameters(parameter(parameterName, String.valueOf(gAuth.getTotpPassword(secret)), parameterType)) : request.withAddedParameters(parameter(parameterName, String.valueOf(gAuth.getTotpPassword(secret)), parameterType));
    }

    private HttpRequest updateTokenInBody(HttpRequest request)
    {
        String body = request.bodyToString();
        Matcher matcher = replacementPattern.matcher(body);

        if(matcher.find())
        {
            int start = matcher.start(1);
            int end = matcher.end(1);
            return request.withBody(new StringBuilder(body).replace(start, end, String.valueOf(gAuth.getTotpPassword(secret))).toString());
        }

        return request;
    }


    private boolean isParameterNamePresentInHeaders(HttpRequest request)
    {
        for (HttpHeader h : request.headers())
        {
            if (h.name().equalsIgnoreCase(parameterName))
            {
                return true;
            }
        }

        return false;
    }

    private boolean isParameterNamePresentInParameters(HttpRequest request, HttpParameterType parameterType)
    {
        for (ParsedHttpParameter p : request.parameters())
        {
            if (p.type().equals(parameterType) && p.name().equalsIgnoreCase(parameterName))
            {
                return true;
            }
        }

        return false;
    }
}