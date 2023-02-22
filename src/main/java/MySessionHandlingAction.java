import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import static burp.api.montoya.http.message.HttpHeader.httpHeader;
import static burp.api.montoya.http.message.params.HttpParameter.parameter;
import static burp.api.montoya.http.sessions.ActionResult.actionResult;

public class MySessionHandlingAction implements SessionHandlingAction
{
    private final String secret;
    private final RuleType ruleType;
    private final String parameterName;
    private final GoogleAuthenticator gAuth;

    public MySessionHandlingAction(String secret, RuleType ruleType, String parameterName)
    {
        this.secret = secret;
        this.ruleType = ruleType;
        this.parameterName = parameterName;

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
        HttpRequest request = actionData.request();

        HttpRequest newRequest = switch (ruleType)
                {
                    case HEADER ->
                            updateOrAddTokenInHeader(request);
                    case URL ->
                            updateOrAddTokenInParameter(request, HttpParameterType.URL);
                    case COOKIE ->
                            updateOrAddTokenInParameter(request, HttpParameterType.COOKIE);
                    case BODY ->
                            updateOrAddTokenInParameter(request, HttpParameterType.BODY);
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