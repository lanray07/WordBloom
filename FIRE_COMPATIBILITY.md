# Fire tablet compatibility

Amazon console update: the production APK with this app's public SDK key was uploaded and passed file validation. Amazon selected twelve compatible Fire tablet entries (2018–2024 models), zero Fire TV and zero automotive entries. This establishes manifest compatibility only; the physical-device checks below remain pending.

Compile/target SDK 36, minSdk 26, JDK 17 and core-library desugaring. No mandatory Google Play Services, Firebase, Google billing or account backend. Inspect the final APK's ABI coverage and console device targeting before publication.

| Environment | Result |
|---|---|
| Physical Fire devices | Not tested; none connected |
| API 36 tablet AVD, 1280×800 | Could not boot; hypervisor driver absent |
| Software-only emulator | Failed before boot; Windows exit -1073741819 |
| Phone / portrait runtime | Pending working emulator/device |
| Fire OS with API below 26 | Unsupported by minSdk |
| Fire tablets with API ≥26 | Intended compatibility; not verified |

Above 700 dp the puzzle uses two columns; smaller widths use a scrollable column. Illustrations draw locally as vectors. This does not prove touch accuracy, latency, memory/performance, frame timing or audio behaviour.

Before release test compatible Fire HD 8/10 generations, portrait/landscape, increased font scale, screen reader, reduced motion, contrast, absent/available speech services, microphone denial, interruptions, process recreation, airplane mode and all purchase/restore/refund/account-switch paths. Measure cold startup, memory and frame timing. Results remain pending.
