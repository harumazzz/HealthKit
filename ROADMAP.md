# Kế Hoạch Triển Khai Toàn Diện Ứng Dụng Health Kit (Android Native + Jetpack Compose)

Dự án ứng dụng theo dõi sức khỏe cá nhân trên nền tảng Android, sử dụng **Health Connect SDK** chính thức của Google, giao diện hiện đại bằng **Jetpack Compose (Material 3)**, áp dụng kiến trúc **MVVM + Hilt DI + Navigation Compose + DataStore**.

---

## 1. Kiến Trúc & Ngăn Xếp Công Nghệ (Tech Stack)

- **Ngôn ngữ**: Kotlin 2.x
- **Localization**: Toàn bộ UI, strings trong `strings.xml`, nhãn và thông báo **luôn sử dụng Tiếng Anh (English)**.
- **UI Framework**: Jetpack Compose + Material 3 (hỗ trợ Dynamic Color & Dark/Light theme)
- **Dependency Injection**: Dagger Hilt (`hilt-android`, `hilt-navigation-compose`, KSP)
- **Kho dữ liệu Sức khỏe (Single Source of Truth)**: `androidx.health.connect:connect-client:1.1.0-alpha11`
  - *Không dùng Room DB cho dữ liệu sức khỏe*: Health Connect bản chất là cơ sở dữ liệu mã hóa cục bộ trên thiết bị, hỗ trợ truy vấn nhanh theo khoảng thời gian (`TimeRangeFilter`) cho bất kỳ ngày nào trong quá khứ (hôm qua, tuần trước, tháng trước). Tránh việc lưu trữ trùng lặp gây lệch dữ liệu (out-of-sync).
- **Lưu trữ Cài đặt & Mục tiêu (Preferences)**: Jetpack DataStore Preferences
- **Điều hướng**: Jetpack Navigation Compose
- **Biểu đồ & Đồ họa**: Compose Canvas API tùy biến hiệu năng cao

---

## 2. Cấu Trúc Thư Mục Chuẩn (Project Structure)

```text
app/src/main/java/com/haruma/health/kit/
├── HealthKitApp.kt                      # Application class (@HiltAndroidApp)
├── MainActivity.kt                      # Main Activity chứa Navigation Host & BottomBar
│
├── di/                                  # Hilt Modules
│   ├── HealthModule.kt                  # Cung cấp HealthConnectClient, HealthConnectManager (@Singleton)
│   └── DataStoreModule.kt               # Cung cấp DataStore & UserPreferencesRepository (@Singleton)
│
├── data/
│   ├── health/
│   │   ├── HealthConnectManager.kt      # Quản lý SDK status, permissions, aggregate query, read/write record
│   │   └── HealthPermissions.kt         # Danh sách quyền Health Connect
│   ├── preferences/
│   │   └── UserPreferencesRepository.kt # Quản lý Daily Goals & Settings qua DataStore
│   └── model/
│       ├── DailyHealthSummary.kt        # Dữ liệu tổng hợp theo ngày (Steps, Calories, HeartRate, Sleep, Water)
│       ├── MetricType.kt                # Enum các loại chỉ số
│       └── UserGoals.kt                 # Mục tiêu ngày của người dùng
│
├── ui/
│   ├── navigation/
│   │   ├── Screen.kt                    # Định nghĩa các Route & BottomBar items (English)
│   │   └── HealthNavHost.kt             # Navigation Graph chính
│   ├── theme/                           # Color, Type, Shape, Theme (Material 3)
│   ├── components/                      # UI Components tái sử dụng
│   │   ├── ActivityRings.kt             # Vòng tròn vận động vẽ bằng Canvas
│   │   ├── DateSelectorBar.kt           # Thanh chuyển ngày (Today, Yesterday, DatePicker)
│   │   ├── MetricCard.kt                # Thẻ hiển thị chỉ số chuyên nghiệp
│   │   ├── SimpleBarChart.kt            # Biểu đồ cột lịch sử bằng Canvas
│   │   └── QuickLogSheet.kt             # BottomSheet ghi nhanh nước/cân nặng
│   ├── dashboard/                       # Màn hình chính Dashboard
│   ├── detail/                          # Màn hình chi tiết chỉ số & Biểu đồ
│   ├── settings/                        # Màn hình Cài đặt mục tiêu & Liên kết
│   └── privacy/
│       └── PrivacyRationaleActivity.kt  # Activity bắt intent ACTION_SHOW_PERMISSIONS_RATIONALE mở link Web
```

---

## 3. Lộ Trình Triển Khai 4 Giai Đoạn

### Giai đoạn 1: Base Setup, Health Connect Core & Navigation (ĐÃ HOÀN THÀNH)
- [x] Cấu hình Gradle (`libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`) với Hilt 2.60.1, KSP 2.2.10-2.0.2, Health Connect, Navigation Compose, DataStore, Material Icons Extended.
- [x] Thiết lập localization: Strings và UI sử dụng tiếng Anh chuẩn.
- [x] Khai báo Android Manifest: Health Permissions, `<queries>`, Privacy Rationale Activity.
- [x] Tạo `PrivacyRationaleActivity` mở URL Web chính sách bảo mật qua trình duyệt.
- [x] Tạo `HealthKitApp` với `@HiltAndroidApp`.
- [x] Xây dựng Data Models (`DailyHealthSummary`, `UserGoals`, `MetricType`).
- [x] Xây dựng `HealthPermissions` & `HealthConnectManager` (kiểm tra availability, check quyền, query aggregate).
- [x] Xây dựng `UserPreferencesRepository` lưu Daily Goals qua DataStore.
- [x] Tạo Hilt Modules (`HealthModule`).
- [x] Dựng Scaffold + Bottom Navigation Bar kết nối 3 màn hình: Dashboard, Detail, Settings.

---

### Giai đoạn 2: Dashboard Screen & Date Selector (ĐÃ HOÀN THÀNH)
- [x] Xây dựng `DateSelectorBar`: Nút chọn "Today", "Yesterday", nút lùi/tiến ngày và picker chọn ngày bất kỳ.
- [x] Xây dựng `ActivityRings`: Vòng tròn 3 màu (Move/Calories, Steps, Active Time/Water) vẽ bằng Canvas Compose có animation.
- [x] Xây dựng `MetricCard`: Các card chỉ số bước chân, calo, nhịp tim gần nhất, thời lượng ngủ, lượng nước, quãng đường, cân nặng.
- [x] Xây dựng `QuickLogSheet`: ModalBottomSheet ghi nhanh nước uống (250ml, 500ml) hoặc cân nặng vào Health Connect.
- [x] Xây dựng `DashboardViewModel` & `DashboardUiState`: Xử lý tự động nạp dữ liệu của ngày được chọn và refresh khi có cập nhật.
- [x] Xử lý Banner/Card nhắc cấp quyền Health Connect mượt mà nếu chưa cấp đủ quyền kèm điều hướng người dùng vào Settings của hệ thống để cấp quyền.

---

### Giai đoạn 3: Metric Detail & Historical Charts
- [ ] Bộ lọc khung thời gian: **Past 7 Days (Week)** hoặc **Past 30 Days (Month)**.
- [ ] Bộ chọn chỉ số: Steps, Calories Burned, Sleep, Heart Rate.
- [ ] `SimpleBarChart`: Biểu đồ cột tự vẽ bằng Canvas Compose, hiển thị vạch mục tiêu (Goal Line), highlight ngày cao nhất, hỗ trợ chạm vào cột để xem số liệu chi tiết.
- [ ] Thẻ tóm tắt thống kê: Mức trung bình hàng ngày (Daily Average), ngày cao nhất (Peak Day), tổng tích lũy trong chu kỳ.
- [ ] `MetricDetailViewModel`: Query aggregate theo khoảng ngày (`aggregateGroupByPeriod`) từ Health Connect.

---

### Giai đoạn 4: Settings Screen, Goals & Google Play Store Compliance
- [ ] Cài đặt mục tiêu hàng ngày (Daily Goals): Tùy chỉnh bước chân, calo, nước uống và lưu tức thì vào DataStore.
- [ ] Quản lý liên kết Health Connect: Nút mở trực tiếp trang cài đặt quyền Health Connect của hệ điều hành.
- [ ] Mục Chính sách quyền riêng tư (Privacy Policy): Mở liên kết Web qua Chrome Custom Tabs / Trình duyệt.
- [ ] Kiểm thử toàn diện trên thiết bị / máy ảo (flow xin quyền, query dữ liệu hôm nay/hôm qua, ghi dữ liệu).
- [ ] Cấu hình Proguard/R8 rules sẵn sàng cho bản phát hành Release lên Google Play Store.
