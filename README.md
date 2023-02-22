TOTP Authenticate
============================

###### Uses session handling rules to add a TOTP token to outgoing requests.

 ---
For Burp Suite Enterprise Edition

Please note that extensions are written by third party users of Burp, and PortSwigger makes no warranty about their quality or usefulness for any particular purpose.

---
## Usage
1. Adjust the hard-coded values in the `TOTPAuthenticate.java` file and rebuild the extension
2. [Load the extension into Burp Enterprise](https://portswigger.net/burp/documentation/enterprise/working/scans/extensions), and add the extension to your Site Details page
3. Configure your session handling rule in Burp Suite Professional (see details below)
4. [Import the scan configuration into Enterprise](https://portswigger.net/burp/documentation/enterprise/working-with-scans/scan-configurations#importing-scan-configurations) and add the scan configuration to your Site Details page

#### Configuring your session handling rule in Burp Suite Professional/Community
1. Load the extension into `Extensions > Installed > Add`
2. Go to `Settings > Search > Sessions`
3. Under `Session handling rules`, go to `Add > Rule actions > Add > Invoke a Burp extension`, select `TOTP Authenticate` from the dropdown list available and click `OK`
4. Set your `Rule description` appropriately
5. Click across to the `Scope` tab, ensuring that the `Tools scope > Scanner` box is checked
6. Configure your URL scope appropriately
7. Click `OK`
8. Perform any testing in Burp Suite Professional/Community
9. Export the session handling rule by going to `Session handling rules > Cog button > Save settings`

#### Example session handling scan configuration JSON
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
            "description":"Rule 1",
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

### Using Gradle
- If you do not have Gradle already installed, follow the installation instructions [here](https://gradle.org/install/).
- Once Gradle is installed, run `gradle fatJar` from the installation directory using the command line.
- Make sure you are using the latest version of Gradle.

If no changes to the code are required, a prebuilt JAR file is available under `build/libs/`. It is preferable to compile your own JAR file.
