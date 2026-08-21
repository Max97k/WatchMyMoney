# AGENTS.md — AI Agent Guidance for WatchMyMoney

> **Repository:** `Max97k/WatchMyMoney`  
> **Default Branch:** `main`  
> **Primary Technology Stack:** Kotlin 2.0.20, Wear OS Jetpack Compose 1.4.0, Jetpack DataStore Preferences, Gradle 8.5.2 (Kotlin DSL), Android SDK (compileSdk 34, minSdk 30, targetSdk 34)  
> **Visibility:** Public  

---

## 1. Project Overview & Architecture

### 1.1 Purpose & Mission
WatchMyMoney is a privacy-first, standalone Wear OS smart complication and application that visualizes real-time earnings throughout the workday at 60fps. Designed specifically for circular and rectangular Wear OS 3.0+ smartwatches (up to Wear OS 5), it enables users to configure annual/hourly salaries and work schedules, providing instant real-time motivation directly on their watch face.

### 1.2 System Architecture & Component Diagram
The application follows modern Android Clean Architecture and reactive MVVM principles:
1. **Wear OS Jetpack Compose UI (`ui/`)**:
   - `OnboardingScreen.kt`: First-launch numpad interface with leading zero prevention, numeric formatting, and live validation.
   - `TickerScreen.kt`: High-performance 60fps real-time counter loop with smooth digit rolling, pause/resume capability, and workday progress indicators.
   - `MainScreen.kt`: Navigation router switching between Onboarding and Ticker screens based on persistent salary state.
2. **Watch Face Complication Service (`complication/`)**:
   - `SalaryComplicationService.kt`: Extends `SuspendingComplicationDataSourceService` from `androidx.wear.watchface`. Generates complication data (Short Text, Ranged Value, Monochromatic Image) to display accrued income directly on third-party watch faces.
3. **Domain & Calculation Engine (`logic/`)**:
   - `SalaryCalculator.kt`: Pure, deterministic calculation engine computing real-time earnings, daily progress percentage, working second rates, and lunch break offsets.
   - `TimeProvider.kt`: Abstracted clock interface enabling deterministic, sub-second time mocking in unit and Robolectric tests.
4. **Data Persistence (`data/`)**:
   - `SalaryRepository.kt`: Asynchronous, reactive data repository backed by Jetpack DataStore Preferences (`androidx.datastore.preferences.core`), providing reactive Kotlin `Flow` streams of salary settings.

### 1.3 Key File & Directory Map
| Path | Purpose / Description |
|---|---|
| `app/build.gradle.kts` | Application module build configuration, SDK levels (minSdk 30, targetSdk 34), and dependencies |
| `gradle/libs.versions.toml` | Gradle Version Catalog managing Kotlin, Compose, Wear OS, and test library versions |
| `app/src/main/AndroidManifest.xml` | Wear OS standalone declaration, complication service provider filters, and launcher intent |
| `app/src/main/java/com/watchmymoney/MainActivity.kt` | Root Activity hosting the Wear OS Compose theme and navigation |
| `app/src/main/java/com/watchmymoney/ui/` | Wear OS Jetpack Compose screens (`MainScreen.kt`, `OnboardingScreen.kt`, `TickerScreen.kt`) |
| `app/src/main/java/com/watchmymoney/complication/` | `SalaryComplicationService.kt` complication data source provider |
| `app/src/main/java/com/watchmymoney/data/` | `SalaryRepository.kt` DataStore persistence layer |
| `app/src/main/java/com/watchmymoney/logic/` | `SalaryCalculator.kt` and `TimeProvider.kt` calculation logic |
| `app/src/test/java/com/watchmymoney/` | Robolectric, JUnit 4, and Compose UI test suite (`SalaryCalculatorTest.kt`, `OnboardingScreenTest.kt`, etc.) |
| `TEST_INFRA.md` | Testing strategy and feature coverage documentation |
| `.github/workflows/deploy-play-store.yml` | Automated Google Play Store deployment CI/CD workflow |

---

## 2. Development, Build & Verification Commands

### 2.1 Prerequisites & Environment Setup
- **JDK**: Java Development Kit 17 (or JDK 21).
- **Android SDK**: Android SDK Platform 34 (Android 14 / Wear OS 5), Build Tools 34.0.0+.
- **Gradle**: Gradle 8.5.2+ (via `./gradlew` wrapper).

### 2.2 Build & Compilation Commands
```bash```
# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Build Release App Bundle (AAB for Google Play Store)
./gradlew bundleRelease
``````

### 2.3 Verification & Testing Suite
```bash```
# Run all unit and Robolectric tests
./gradlew test

# Run specific debug unit tests
./gradlew testDebugUnitTest

# Run Android Lint analysis
./gradlew lint
./gradlew lintDebug
``````

### 2.4 Clean & Reset
```bash```
# Clean Gradle build caches and outputs
./gradlew clean
``````

---

## 3. Coding Standards & Conventions

### 3.1 Code Style & Idioms
- **Language**: Kotlin 2.0+ standard idioms. Explicit return types for public APIs, immutable data structures (`val`, `data class`), and Kotlin Coroutines with `StateFlow` / `SharedFlow`.
- **Compose Best Practices**: Follow Wear OS Compose conventions. Keep Composables stateless by hoisting state to parent composables or repositories. Use `remember` and `derivedStateOf` to prevent unnecessary recompositions on 60fps ticker renders.
- **Testing**: Test coverage must be maintained across business logic, UI state transitions, and complication providers. Follow the test structure outlined in `TEST_INFRA.md`.

### 3.2 File & Module Organization
- Package name: `com.watchmymoney`
- Layer separation: `ui/` for Compose presentation, `complication/` for Wear OS complication services, `logic/` for pure domain calculations, and `data/` for storage.
- Tests are co-located in `app/src/test/java/com/watchmymoney/` mirroring the main source hierarchy.

### 3.3 State Management & Error Handling
- Use `StateFlow` for reactive UI updates from `SalaryRepository`.
- Numpad inputs in `OnboardingScreen` must sanitize input (enforce positive non-zero integers, prevent integer overflow).

---

## 4. Safety, Security & Resource Constraints

### 4.1 Battery & Wearable Lifecycle Constraints
- **Ambient Mode & Wrist Lowering**: The 60fps ticker rendering loop MUST halt immediately when the watch enters ambient mode or when the display turns off to prevent battery drain.
- **Complication Throttling**: Complication data updates should rely on system-scheduled updates (typically ~1 per minute) rather than aggressive continuous polling.
- **Zero WakeLocks**: Never hold background WakeLocks on Wear OS devices.

### 4.2 Privacy & Local Storage
- **100% Offline**: WatchMyMoney operates completely offline. No analytics, tracking SDKs, or network permissions are present. All salary figures remain encrypted and stored locally in Jetpack DataStore.

---

## 5. Git & Branch Workflow

- **Target Default Branch**: `main`
- **Commit Format**: Conventional commits (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`).
