# Hướng Dẫn Kỹ Thuật Dành Cho AI Coding Agents (AGENTS.md)

Tài liệu này định nghĩa các nguyên tắc thiết kế, quy ước kiến trúc và hướng dẫn phát triển cho các AI coding agent khi làm việc trên kho mã nguồn **HealthKit Android**.

---

## 1. Bản Chất Dự Án & Nguyên Tắc Cốt Lõi

- **Loại dự án**: Ứng dụng Android Native viết bằng **Kotlin** và **Jetpack Compose**.
- **Quy mô**: Dự án phát triển độc lập (Solo Developer) nhưng theo chuẩn **Production-Ready / Store-Ready**.
- **Triết lý kiến trúc**:
  1. **Thực dụng & Rõ ràng**: Áp dụng MVVM + Dagger Hilt + Navigation Compose. Tránh over-engineering với quá nhiều tầng abstractions (không cần chia nhiều module con hay viết UseCases/Mappers rườm rà nếu chưa cần thiết).
  2. **Single Source of Truth**: Sử dụng **Health Connect** (`androidx.health.connect:connect-client`) làm kho dữ liệu sức khỏe duy nhất trên thiết bị. **KHÔNG** tạo thêm Room DB để cache dữ liệu sức khỏe nhằm tránh nguy cơ out-of-sync và tuân thủ chặt chẽ chính sách quyền riêng tư của Google.
  3. **DataStore**: Chỉ dùng Jetpack DataStore Preferences để lưu trữ mục tiêu cá nhân (User Goals: bước chân, calo, nước uống) và các cài đặt người dùng.
  4. **QUY TẮC BẮT BUỘC VỀ CODE**: **TUYỆT ĐỐI KHÔNG VIẾT COMMENT TRONG BẤT KỲ FILE CODE NÀO** (không dùng `//`, `/* */`, `/** */`). Code phải tự sáng tỏ, đặt tên biến và hàm tường minh. Không chạy lệnh gradle build/assemble khi không được yêu cầu.
  5. **LOCALIZATION**: **LUÔN SỬ DỤNG TIẾNG ANH (ENGLISH)** làm ngôn ngữ chính thức cho toàn bộ giao diện (UI), strings trong `strings.xml`, labels và thông báo.

---

## 2. Ngăn Xếp Công Nghệ (Tech Stack)

| Thành phần | Phiên bản / Thư viện |
|---|---|
| **Android Gradle Plugin** | 8.x / 9.x |
| **Kotlin** | 2.x |
| **DI** | Dagger Hilt (`hilt-android`, `androidx.hilt:hilt-navigation-compose`) với KSP |
| **Health SDK** | `androidx.health.connect:connect-client:1.1.0-alpha11` |
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | `androidx.navigation:navigation-compose` |
| **Preferences** | `androidx.datastore:datastore-preferences` |

---

## 3. Quy Ước Mã Nguồn & Cấu Trúc Thư Mục

Mọi code mới phải tuân theo cấu trúc package sau: `com.haruma.health.kit`

- `di/`: Chứa các Hilt Module (`HealthModule`, `DataStoreModule`). Sử dụng `@InstallIn(SingletonComponent::class)` và `@Singleton` cho các dịch vụ dùng chung.
- `data/health/`: Chứa `HealthConnectManager.kt` và `HealthPermissions.kt`. Toàn bộ logic tương tác trực tiếp với SDK Health Connect phải gói gọn tại đây.
- `data/preferences/`: Chứa `UserPreferencesRepository.kt` đọc/ghi DataStore.
- `data/model/`: Chứa các Data Classes sạch: `DailyHealthSummary.kt`, `UserGoals.kt`, `MetricType.kt`.
- `ui/navigation/`: Chứa `Screen.kt` (định nghĩa Route & Sealed class màn hình) và `HealthNavHost.kt`.
- `ui/theme/`: Theme Material 3 chuẩn sức khỏe (màu sắc rực rỡ, hỗ trợ Dark Mode).
- `ui/components/`: Các Composable dùng chung (`ActivityRings`, `MetricCard`, `DateSelectorBar`, `SimpleBarChart`, `QuickLogSheet`).
- `ui/<feature>/`: Mỗi màn hình nằm trong folder riêng, bao gồm Screen, ViewModel, và UiState (ví dụ: `ui/dashboard/`, `ui/detail/`, `ui/settings/`).

---

## 4. Chính Sách Google Play Store Đối Với Health Connect

Bất kỳ thay đổi nào liên quan đến quyền hoặc Manifest **phải** đảm bảo:
1. Quyền trong `AndroidManifest.xml` phải khai báo đúng namespace `android.permission.health.*`.
2. Khai báo `<queries>` để app có thể kiểm tra package `com.google.android.apps.healthdata`.
3. Phải giữ nguyên Activity xử lý Intent Filter `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE`. Activity này sẽ mở đường link Web chính sách bảo mật thay vì code màn hình cứng.
