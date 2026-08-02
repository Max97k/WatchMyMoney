## 2024-08-02 - Over-privileged manifest
**Vulnerability:** Found `WAKE_LOCK` permission in `AndroidManifest.xml` that wasn't used anywhere in the codebase.
**Learning:** Requesting unneeded permissions violates the principle of least privilege and unnecessarily increases the attack surface or exposes the app to extra scrutiny, and may raise user suspicion.
**Prevention:** Periodically audit requested permissions in `AndroidManifest.xml` against actual usages in the codebase.
