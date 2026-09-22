# ToxicTeams

> **Keep your status GREEN when your culture is RED.**  
> An Android app that transforms your device into a physical optical mouse jiggler powered by full-screen display kinetics and continuous hardware haptics.

---

## 🎯 Overview

**ToxicTeams** keeps your enterprise chat presence (Microsoft Teams, Slack, etc.) continuously marked **"Available"** without installing unauthorized background software on corporate laptops or managed machines.

By placing an optical or laser mouse face-down directly on your Android phone screen, ToxicTeams cycles between:

1. **Active Phase (Default: 8s):**
   - Window brightness boosted to **100%** (`1.0f`).
   - High-contrast, high-frequency kinetic animations fill the **entire screen edge-to-edge** (calibrated to excite optical LED/laser mouse cross-correlation sensors).
   - **Continuous hardware haptic vibrations** physically perturbation-jolt the mouse chassis to trigger cursor movement.
2. **Stealth Battery-Saver Phase (Default: 45s):**
   - Screen brightness drops to **1%** (`0.01f`).
   - Deep OLED black background to conserve battery and eliminate screen burn-in.
   - All vibrations completely stop.
   - Minimalist ambient countdown displaying seconds until the next active cycle.

---

## 📱 How to Use

1. **Launch ToxicTeams** on your Android device.
2. **Rest your optical or laser mouse face-down** anywhere on the glass surface.
3. Tap **START JIGGLER**.
4. The app transitions to full-screen kinetic mode:
   - When **Active**: 100% brightness, full-screen optical patterns, and continuous vibration keep your mouse moving.
   - When **Sleeping**: 1% brightness, pitch-black OLED background, zero vibration, and countdown until the next jiggle.
5. Tap **STOP JIGGLER** at any time to return to the dashboard.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose with Material 3 Expressive
- **Architecture:** Clean MVVM with Compose State, `StateFlow`, and Coroutines
- **Target SDK:** 34+ (Android 14+, Android 15, Android 16)
- **Haptics:** Modern `VibratorManager` / `VibrationEffect` with `VibrationAttributes.USAGE_ALARM` and legacy fallbacks
- **Window Management:** Lifecycle-aware brightness control and wake locks via `WindowManager.LayoutParams`

### Build & Install the Debug APK

Run the following in PowerShell from the `toxic-teams` project directory:

```powershell
cd toxic-teams
.\gradlew assembleDebug
.\gradlew installDebug
```

---

## 👨‍💻 Credits

**Designed & Developed by**  
😎 **Kunal Khandelwal**

---

## 📄 License

This project is licensed under the terms of the [LICENSE](LICENSE) file located in the root repository.
