## 2024-08-02 - Memoizing ZonedDateTime for SalaryCalculator
**Learning:** `ZonedDateTime` and `Duration` instantiations inside Jetpack Compose `withFrameMillis` loops (running at 60Hz) cause significant allocation and CPU overhead.
**Action:** When a calculation is called on every frame, aggressively memoize expensive logical states that do not change from frame to frame.
