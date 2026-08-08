# 🔐 Setting up Base64 `.env` and Google Services Secrets in GitHub Actions

This guide explains how to manage `.env` environment variables and Firebase `google-services.json` securely using **GitHub Repository Secrets** in Base64 format.

---

## 1. Convert `.env` to Base64 String

### Linux / macOS Terminal:
Run the following command in your project directory:
```bash
base64 -w 0 .env
```
*(On macOS, if `-w 0` is unsupported, use `base64 -i .env` or `openssl base64 -A -in .env`)*

### Windows PowerShell:
```powershell
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes(".env"))
```

---

## 2. Add `ENV_BASE64` Secret to GitHub Repository

1. Open your GitHub Repository in your web browser:
   `https://github.com/steveyout/Youplex-movies-apk`
2. Click **Settings** (top navigation tab).
3. In the left sidebar, expand **Secrets and variables** -> click **Actions**.
4. Click **New repository secret**.
5. Fill in the fields:
   - **Name**: `ENV_BASE64` (or `ENV_FILE_BASE64`)
   - **Secret**: *[Paste the full Base64 string generated in Step 1]*
6. Click **Add secret**.

---

## 3. Convert and Add `google-services.json` (Firebase)

If you use Firebase Analytics or Firebase services:

### Encode `google-services.json`:
- **Linux / macOS**: `base64 -w 0 app/google-services.json`
- **Windows PowerShell**: `[Convert]::ToBase64String([System.IO.File]::ReadAllBytes("app/google-services.json"))`

### Add Secret on GitHub:
1. Go to **Settings** -> **Secrets and variables** -> **Actions**.
2. Click **New repository secret**.
3. **Name**: `GOOGLE_SERVICES_BASE64`
4. **Secret**: *[Paste the decoded Base64 content of google-services.json]*
5. Click **Add secret**.

---

## 4. Individual Secrets (Alternative / Fallback)

If you prefer adding individual secrets instead of a full `.env` base64 string, you can set:
- `TMDB_API_KEY`: Your TMDB API token or key
- `GEMINI_API_KEY`: Your Gemini API key

The GitHub Actions workflow (`.github/workflows/build.yml`) will automatically decode `ENV_BASE64` if provided, or fall back to individual secret variables!

---

## 📊 Analytics Integration Summary

Firebase Analytics is configured via `AnalyticsManager` (`com.example.cinestream.data.analytics.AnalyticsManager`):

1. **Automatic Initialization**: `AnalyticsManager.initialize(applicationContext)` in `MainActivity`.
2. **Screen Tracking**: Logs `screen_view` events whenever users navigate between screens.
3. **Media Interaction**: Logs `select_item` when users select movies or TV shows.
4. **Search Queries**: Logs `search` when users search in the Explore tab.
5. **Server Provider Swaps**: Logs `change_server_provider` when users switch streaming servers.
6. **Mandatory Update Check**: Logs `check_app_update` on version update checks.
