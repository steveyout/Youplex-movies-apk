# CineStream - Movie & TV Show Streaming App

CineStream is a modern Android streaming application built with Kotlin, Jetpack Compose, Material 3, Room local database, and Firebase Analytics.

## 🚀 Features

- **TMDB Catalog & Streaming**: Explore trending movies, top-rated TV shows, and genres.
- **Firebase Analytics**: Real-time event tracking (`AnalyticsManager`) for screen views, search queries, media selection, server switches, and update checks.
- **Mandatory Update System**: Automatic GitHub Release version checking with force update prompt support.
- **Multi-Server Provider Engine**: Switch between multiple streaming mirrors (Auto, FastServer, Mirror HD, Cloud Ultra).
- **AdBlocker Engine**: Built-in popup and ad filtering for clean streaming playback.
- **GitHub Actions CI/CD**: Automated APK builds, version incrementing, and release publishing.

## 🔐 GitHub Actions Environment & Secrets Setup

For detailed step-by-step instructions on setting up Base64 `.env` secrets and `google-services.json` in GitHub Actions, see [GITHUB_ENV_SETUP.md](./GITHUB_ENV_SETUP.md).

Quick summary:
1. Base64 encode your `.env` file: `base64 -w 0 .env`
2. Add a GitHub repository secret named `ENV_BASE64` with the output.
3. Base64 encode `app/google-services.json`: `base64 -w 0 app/google-services.json`
4. Add a secret named `GOOGLE_SERVICES_BASE64`.
