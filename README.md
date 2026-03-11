# Lottie Player

A cross-platform Lottie animation player built with [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) and [Compottie](https://github.com/alexzhirkevich/compottie).

Supports **Android**, **iOS**, **Desktop (JVM)**, and **Web (Wasm/JS)**.

## Features

- Load Lottie animations from URL (`.json` or `.lottie`)
- Open local files (`.json`, `.lottie`, `.zip`)
- Auto-detects ZIP/dotLottie format via magic bytes
- Playback controls: loop toggle, speed slider (0.1x - 3.0x)
- Dark theme UI

## Live Demo

Available on [GitHub Pages](https://github.com/nickel-cat/CompottieSample) after enabling Pages in repository settings.

## Building

### Web (Wasm)
```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Desktop
```shell
./gradlew :composeApp:run
```

### Android
```shell
./gradlew :composeApp:assembleDebug
```

### iOS
Open `iosApp/` in Xcode and run.

## Publishing

### GitHub Pages
Pushes to `main` automatically deploy the Wasm web app via GitHub Actions. Enable **Settings > Pages > Source: GitHub Actions** in your repository.

### GitHub Releases
Tag a version to create a release with platform artifacts:
```shell
git tag v1.0.0
git push origin v1.0.0
```

This builds and uploads: `.deb` (Linux), `.dmg` (macOS), `.msi` (Windows), `.aab` (Android), and `web-wasm.zip`.
