TOTP Authenticate
============================

###### Uses session handling rules to add a TOTP token to outgoing requests.

 ---
For Burp Suite Enterprise Edition

Please note that extensions are written by third party users of Burp, and PortSwigger makes no warranty about their quality or usefulness for any particular purpose.

---
## Usage
1. Build the extension
2. [Load the extension into Burp Enterprise](https://portswigger.net/burp/documentation/enterprise/working/scans/extensions), and add the extension to your Site Details page
3. [Configure your session handling rule](#configuring-your-session-handling-rule-in-burp-suite-professionalcommunity) in Burp Suite Professional
4. [Import the scan configuration into Enterprise](https://portswigger.net/burp/documentation/enterprise/working-with-scans/scan-configurations#importing-scan-configurations) and add the scan configuration to your Site Details page

### Configuring your session handling rule in Burp Suite Professional/Community
1. Load the extension into `Extensions > Installed > Add`
2. Go to `Settings > Search > Sessions`
3. Under `Session handling rules`, go to `Add > Rule actions > Add > Invoke a Burp extension`, select `TOTP Authenticate` from the dropdown list available and click `OK`
4. [Set your Rule description](#session-handling-rule-format)
5. Click across to the `Scope` tab, ensuring that the `Tools scope > Scanner` box is checked
6. Configure your URL scope appropriately
7. Click `OK`
8. Go to `Extensions > Installed` and reload the extension (uncheck the TOTP Authenticate "Loaded" checkbox, and click it again)
9. Perform any testing in Burp Suite Professional/Community
10. Export the session handling rule by going to `Session handling rules > Cog button > Save settings`

#### Limitations
- Only the first rule present will be applied
- Any rule changes will require extension reloads when testing in Burp Suite Professional or Community Edition

## Session handling rule format

### Structure

```
/^^secretKey:BASE32,ruleType:TYPE,parameterName:NAME_OF_PARAMETER_VALUE_OR_BASE64_ENCODED_REGEX^^/
```

### Where
- `/^^` delimiter to signify the start of the rule
- `secretKey:` the BASE32 encoded OTP seed
- `ruleType:` describes where to apply the rule
- `parameterName:`
  - for `ruleType` of `BODY_REGEX` it is the BASE64 encoded regular expression that defines where to update
  - for `ruleType` of `HEADER`, `URL`, `COOKIE` or `BODY_PARAM` it is the literal name of the key:value pair to add or update
- `^^/` delimiter to signal the end of the rule

### RuleType
- `HEADER`
- `URL`
- `COOKIE`
- `BODY_PARAM`
- `BODY_REGEX`

## Examples

### Example for a BODY_REGEX regular expression
The following example supplies the regex of `.*\"AdditionalAuthData\":\"(\d*)\".*` as its `parameterName` to identify the match expression and replace `(\d*)` with the generated OTP rolling code
```
/^^secretKey:EQZWG4RTORIDIJBE,ruleType:BODY_REGEX,parameterName:LipcIkFkZGl0aW9uYWxBdXRoRGF0YVwiOlwiKFxkKilcIi4q^^/
```

### Example for a HEADER regular expression
The following example adds or replaces a header named `TEST` with the value of the generated OTP rolling code
```
/^^secretKey:EQZWG4RTORIDIJBE,ruleType:HEADER,parameterName:TEST^^/
```

### Example session handling scan configuration JSON
```json
{
  "project_options":{
    "sessions":{
      "session_handling_rules":{
        "rules":[
          {
            "actions":[
              {
                "action_name":"TOTP Authenticate",
                "enabled":true,
                "type":"invoke_extension"
              }
            ],
            "description":"My OTP rule: /^^secretKey:EQZWG4RTORIDIJBE,ruleType:HEADER,parameterName:TEST^^/",
            "enabled":true,
            "exclude_from_scope":[],
            "include_in_scope":[],
            "named_params":[],
            "restrict_scope_to_named_params":false,
            "tools_scope":[
              "Scanner"
            ],
            "url_scope":"all",
            "url_scope_advanced_mode":false
          }
        ]
      }
    }
  }
}
```

## Troubleshooting
We would recommend testing this extension out in Burp Suite Professional/Community before usage in Burp Suite Enterprise Edition.

To test this extension in Enterprise, you can configure an upstream proxy through Burp to ensure that your token has been added appropriately.

If using the following configuration, please make sure that an instance of Burp Suite Pro/Community is running and you are using a local agent.
```json
{
  "project_options":{
    "connections":{
      "upstream_proxy":{
        "servers":[
          {
            "auth_type":"none",
            "destination_host":"*",
            "proxy_host":"127.0.0.1",
            "proxy_port":8080,
            "enabled":true
          }
        ],
        "use_user_options":false
      }
    }
  }
}
```

## Using Gradle
- If you do not have Gradle already installed, follow the installation instructions [here](https://gradle.org/install/).
- Once Gradle is installed, run `gradle fatJar` from the installation directory using the command line.
- Make sure you are using the latest version of Gradle.

<!-- If no changes to the code are required, a prebuilt JAR file is available under `build/libs/`. It is preferable to compile your own JAR file. -->
