# E2E Test Infrastructure Guide

This document outlines the testing strategy, test harness architecture, and feature coverage for **WatchMyMoney**.

## Feature Coverage Map

### 1. Onboarding & Persistence
* **Overview**: Guides the user through entering their annual salary upon first launch using a custom Wear OS numpad. Ensures the salary is successfully saved to Jetpack DataStore and transitions the UI to the ticker screen.
* **Key Scenarios Tested**:
  * First launch state (displays onboarding prompt when salary is not set).
  * Numpad input formatting (concatenating digits).
  * Leading zero prevention (preventing "0" or "0123" input starting with multiple zeros).
  * Backspace behavior (deleting last character, updating state).
  * Confirm button enabled/disabled state based on valid (>0) numeric input.
  * Successful saving of salary and transitioning layout.

### 2. Ticker & Calculations
* **Overview**: Performs calculations of the user's earnings in real-time, based on their annual salary, progress throughout the day, and custom reset hour constraints.
* **Key Scenarios Tested**:
  * Calculation accuracy at different times of day (Noon, Morning, End of Day).
  * Boundary conditions (zero salary, negative salary).
  * Reset hour shifts (e.g., reset hour set to 9 AM, starting the "day" calculation from yesterday 9 AM or today 9 AM depending on the current time).
  * Timezone-specific logic verification.

### 3. Complications
* **Overview**: Exports real-time watch face complications to display the user's earnings progress.
* **Key Scenarios Tested**:
  * Format support: `SHORT_TEXT`, `LONG_TEXT`, and `RANGED_VALUE` complications.
  * Preview data accuracy (verifying standard static preview outputs).
  * Intent launching and action flags (`Intent.FLAG_ACTIVITY_NEW_TASK` and pending intents).

### 4. Customizations
* **Overview**: User customizations (currency symbol, reset hour, and salary updates) must persist and propagate cleanly through the application layers.
* **Key Scenarios Tested**:
  * Update of currency symbol (e.g., transitioning from "$" to "€").
  * Reset hour changes (e.g., transitioning from midnight (0) to 6 AM).
  * DataStore integration verification.

---

## Test Harness Architecture

The WatchMyMoney test harness runs on the JVM using Robolectric to simulate the Android SDK environment, facilitating fast, robust local testing.

### 1. Robolectric on JVM
* Tests are located under `app/src/test/java/`.
* Test classes are annotated with `@RunWith(RobolectricTestRunner::class)`.
* Robolectric simulates core Android framework components (Context, Intents, PendingIntents, System Services) without requiring a physical device or emulator.

### 2. Compose UI Testing
* Leverages `createComposeRule()` (or `createAndroidComposeRule<ComponentActivity>()`) to mount Wear OS Compose UI elements.
* Uses Compose test semantics to locate nodes (`onNodeWithText`, `onNodeWithContentDescription`) and perform actions (`performClick`).
* Controls the frame rendering loop via `composeTestRule.mainClock` to test animations and frame-by-frame updates without causing test timeouts.

### 3. DataStore Isolation & Lifecycle
* Jetpack DataStore saves files locally to the device's storage. To prevent cross-test state pollution and `IllegalStateException` due to multiple active DataStore instances:
  * Each test class isolates its DataStore instances by manually clearing the parent directories of DataStore files before and after every test.
  * DataStore cleanups are executed in `@Before` and `@After` hooks:
    ```kotlin
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.filesDir.parentFile?.resolve("datastore")?.deleteRecursively()
    ```

### 4. Complications Test Harness
* Directly instantiates `SalaryComplicationService` and runs tests by Mocking/Stubbing or retrieving Context via Robolectric.
* Leverages Wear OS complication testing APIs to request updates and inspect the resulting `ComplicationData` structure.
