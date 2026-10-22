# Release Notes

## Version 1.2 (Latest Release)

🎉 **WatchMyMoney v1.2 is here!**

This major update brings complete Wear OS 3/4/5 cross-compatibility, multi-currency support, custom reset schedules, and full Traditional Chinese localization.

**What's New in v1.2:**
*   **Multi-Currency Support:** Switch effortlessly between `$`, `NT$`, `€`, `£`, `¥`, and `₩` directly in the new Settings screen.
*   **Custom Reset Hour:** Set your workday rollover time (00:00, 06:00, 08:00, 09:00, 12:00, 18:00, or 21:00) to perfectly align with your shift.
*   **Dual Wear OS Complication Engine:** Full support for Wear OS 4+ Dynamic Expressions with graceful fallback to Wear OS 3 static complications, ensuring zero crashes on older watches.
*   **60Hz Zero-Allocation Ticker:** Upgraded to `mutableDoubleStateOf` and `mutableFloatStateOf` to eliminate autoboxing allocations and GC micro-stutters during 60fps rendering.
*   **Traditional Chinese Localization:** Comprehensive `zh-rTW` localization across all screens and complication descriptions.
*   **Modern Splash Screen API:** Integrated `androidx.core:core-splashscreen` for a seamless launch transition.
*   **WearRecents Optimization:** Conforms to Google Wear OS guidelines with `android:taskAffinity=""` for clean system task management.

---

## Version 1.0 (Initial Release)

🎉 **Welcome to WatchMyMoney!**

WatchMyMoney is the ultimate motivation tool for your wrist. Visualize your earnings in real-time with a satisfying, high-frequency ticker.

**Key Features:**
*   **Real-Time Salary Ticker:** Watch your money grow second by second with a smooth 60fps animation.
*   **Watch Face Complication:** See your daily earnings directly on your favorite watch face without opening the app.
*   **Privacy First:** All data is stored locally on your device. We don't track you or your salary.
*   **Battery Efficient:** Optimized for Wear OS to be kind to your battery life.
*   **Custom Input:** Easy-to-use in-app number pad for setting your annual salary.

**Get Started:**
1.  Open the app.
2.  Enter your annual salary.
3.  Enjoy the show!
