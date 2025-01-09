## Configure Credentials

You will be launching the demo app using GitHub Code Spaces.
Before we are able to do that, we need to configure two secrets in your GitHub account. They will let the app know where to report monitoring data to and how to authenticate.

### 📌 Create Dynatrace API Token

📝 **Reference:**
- [How to create Access Token](https://docs.dynatrace.com/docs/manage/access-control/access-tokens#create-api-token)
- [Token scopes](https://docs.dynatrace.com/docs/shortlink/otel-getstarted-otlpexport#authentication-export-to-activegate)

Select the following token scopes
- logs.ingest
- metrics.ingest
- openTelemetryTrace.ingest
- InstallerDownload

<center>
<img src="../../../assets/images/01_setup_03_create_token.gif" alt="Create API Token" style="width:500px" />
</center>

### 📌 Create Code Space Secrets

Within your GitHub Repository navigate to `Settings` and in there to `Secrets and variables` / `Codespaces`.
In here you need to define two secrets:
* `DT_ENVIRONMENT_URL` needs to contain the URL of the Dynatrace environment for todays session (`https://######.live.dynatrace.com`)
* `DT_API_TOKEN` needs to contain the API Token you have created earlier.
<br>
<br>

<center>
<img src="../../../assets/images/01_setup_04_code_spaces_secrets.gif" alt="Create API Token" style="width:500px; display: block; align: center" />
</center>
