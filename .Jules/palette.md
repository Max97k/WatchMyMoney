## 2024-11-20 - Wear OS Touch Targets
**Learning:** Wear OS standardizes touch targets to minimum 48dp, even for auxiliary buttons like 'Edit'. Using smaller targets (e.g. 32dp) breaks accessibility guidelines.
**Action:** Always ensure Compose `Modifier.size()` or minimum bounds for clickable elements are at least 48dp on Wear OS.
