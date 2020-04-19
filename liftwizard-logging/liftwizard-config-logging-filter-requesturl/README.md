

```json
{
  "server": {
    "requestLog": {
      "appenders": [
        {
          "type": "console",
          "filterFactories": [
            {
              "type": "url",
              "bannedUrls": [
                "/manifest.json",
                "/assets-manifest.json",
                "/favicon.ico",
                "/service-worker.js"
              ]
            }
          ]
        }
      ]
    }
  }
}
```
