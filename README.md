## BadaFriend
### team TailStar

### 개발환경 설치 및 실행
```
해당링크를 참고하여 AVD와 WearOS emulator 연결
AVD에는 DIVE-APP, WearOS Emulator에는 DIVE-WearOS를 실행합니다.
https://developer.android.com/training/wearables/get-started/connect-phone?hl=ko

실제 갤럭시 워치 연결 참고 자료
https://minuhome.tistory.com/6188
```

### 프로젝트 구조
```
project-root/
│        
├── README.md
├── app/
│   ├── src.main/
│   │   ├── java.com.example/dive_app/
│   │   │   ├── data/
│   │   │   │   ├── dao/
│   │   │   │   │   └── HealthRecordDao
│   │   │   │   ├── db/
│   │   │   │   │   └── HealthDatabase.kt
│   │   │   │   └── repository/
│   │   │   │       ├── HealthRepository
│   │   │   │       └── WearDataRepository
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── AirQuality
│   │   │   │   │   ├── FishingPoint
│   │   │   │   │   ├── HealthRecord
│   │   │   │   │   ├── TideInfoData
│   │   │   │   │   └── WeatherData
│   │   │   │   └── viewmodel/
│   │   │   │       ├── AirQualityViewModel
│   │   │   │       ├── FishingPointViewModel
│   │   │   │       ├── HealthViewModel
│   │   │   │       ├── LocationViewModel
│   │   │   │       ├── TideViewModel
│   │   │   │       └── WeatherViewModel
│   │   │   ├── sensor/
│   │   │   │       └── HeartRateSensorManager
│   │   │   ├── ui/
│   │   │   │   ├── component/
│   │   │   │   │   └── CircleButton.kt
│   │   │   │   ├── screen/
│   │   │   │   │   ├── AirQualityScreen.kt
│   │   │   │   │   ├── CurrentLocationScreen.kt
│   │   │   │   │   ├── DangerAlertScreen.kt
│   │   │   │   │   ├── FishingDetailScreen.kt
│   │   │   │   │   ├── FishingPointScreen.kt
│   │   │   │   │   ├── HealthScreen.kt
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── LocationScreen.kt
│   │   │   │   │   ├── SeaWeatherScreen.kt
│   │   │   │   │   ├── TideDetailScreen.kt
│   │   │   │   │   ├── TideWatchScreen.kt
│   │   │   │   │   ├── WeatherMenuScreen.kt
│   │   │   │   │   └── WeatherScreen.kt
│   │   │   │   └── theme/
│   │   │   │       └── theme.kt
│   │   │   ├── util/
│   │   │   │   └── LocationUtil
│   │   │   ├── MainActivity
│   │   │   └── MainApp
│   │   ├── res/
            ├── drawable/
                ├── ic_my_location.xml
                └── sea_background.png
```

### 개발 버전 관리
```
```
