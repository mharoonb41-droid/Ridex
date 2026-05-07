RideX — Ride Sharing App:
 A real-time ride sharing Android application built with Java & Firebase — similar to Uber.
 Features:

**User Authentication** — Email/Password login & registration via Firebase Auth
**Rider Module** — Request rides, view price & distance, track driver in real-time
**Driver Module** — Accept ride requests, navigate to rider & destination
**Google Maps Integration** — Live map, route drawing with golden polyline
**Live Location** — Real-time GPS location tracking for both rider & driver
**Price Calculation** — Auto fare calculation based on distance (Rs. 100 base + Rs. 50/km)
**Distance & ETA** — Shows distance and estimated time before booking
**Real-time Updates** — Firebase Realtime Database syncs ride status instantly
**Route Finding** — Directions API draws turn-by-turn route on map


Project Structure:

RideX/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/ridex/
│   │   │   ├── MainActivity.java          # Login Screen
│   │   │   ├── RegisterActivity.java      # Register Screen
│   │   │   ├── RiderActivity.java         # Rider Dashboard + Map
│   │   │   ├── DriverActivity.java        # Driver Dashboard + Map
│   │   │   ├── MapsHelper.java            # Route & Price Calculator
│   │   │   └── models/
│   │   │       ├── User.java              # User Data Model
│   │   │       └── RideRequest.java       # Ride Request Model
│   │   │
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml      # Login UI
│   │   │   │   ├── activity_register.xml  # Register UI
│   │   │   │   ├── activity_rider.xml     # Rider UI
│   │   │   │   └── activity_driver.xml    # Driver UI
│   │   │   └── drawable/
│   │   │       └── input_background.xml   # Custom Input Style
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── google-services.json               # Firebase Config (not committed)
│   └── build.gradle.kts
│
└── build.gradle.kts



App Workflow


┌─────────────────────────────────────────────┐
│                  RideX App                  │
└─────────────────────────────────────────────┘
                      │
              ┌───────▼────────┐
              │  Login Screen  │
              └───────┬────────┘
                      │
          ┌───────────┴───────────┐
          │                       │
    ┌─────▼──────┐         ┌──────▼─────┐
    │   RIDER    │         │   DRIVER   │
    └─────┬──────┘         └──────┬─────┘
          │                       │
    Map Open               Map Open
    Location Get           Location Get
          │                       │
    Tap Destination        Wait for Requests
    Check Price                   │
    See Route              New Request Alert
          │                       │
    Book Ride ──► Firebase ◄── Accept Ride
          │                       │
    "Driver Coming"        Draw Route to Rider
          │                       │
    "Ride Complete" ◄──── Complete Ride


Tech Stack

| Technology | Purpose |
|---|---|
| **Java** | Primary programming language |
| **Android Studio** | IDE |
| **Firebase Authentication** | User login & registration |
| **Firebase Realtime Database** | Real-time ride requests & status |
| **Google Maps SDK** | Interactive map display |
| **Google Directions API** | Route drawing between locations |
| **Google Distance Matrix API** | Distance & ETA calculation |
| **FusedLocationProvider** | Accurate GPS location |



 Firebase Database Structure

json
{
  "users": {
    "userId": {
      "name": "Ali Hassan",
      "email": "ali@gmail.com",
      "phone": "03001234567",
      "role": "rider"
    }
  },
  "rideRequests": {
    "riderId": {
      "riderId": "abc123",
      "driverId": "xyz789",
      "destination": "Lahore Airport",
      "riderLat": 31.5204,
      "riderLng": 74.3587,
      "destLat": 31.5216,
      "destLng": 74.4036,
      "price": "Rs. 350",
      "status": "pending | accepted | completed",
      "timestamp": 1234567890
    }
  }
}




Getting Started:

# Prerequisites
- Android Studio (latest version)
- JDK 17
- Firebase Account
- Google Cloud Console Account

# Installation

**1. Clone the repository**
bash
git clone https://github.com/yourusername/RideX.git
cd RideX


**2. Firebase Setup**
- Go to [firebase.google.com](https://firebase.google.com)
- Create a new project named `RideX`
- Add Android app with package `com.example.ridex`
- Download `google-services.json` and place in `app/` folder
- Enable **Authentication** (Email/Password)
- Enable **Realtime Database** (test mode)

**3. Google Maps Setup**
- Go to [console.cloud.google.com](https://console.cloud.google.com)
- Enable these APIs:
  - Maps SDK for Android
  - Directions API
  - Distance Matrix API
- Create an API Key

**4. Add API Keys**

In `AndroidManifest.xml`:
xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY"/>


In `RiderActivity.java` and `DriverActivity.java`:
java
private static final String API_KEY = "YOUR_GOOGLE_MAPS_API_KEY";


**5. Firebase Database Rules**
json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "rideRequests": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}


**6. Run the app**
- Open project in Android Studio
- Click **Run **

---

##  Pricing Formula


Base Fare  =  Rs. 100
Per KM     =  Rs. 50

Total Price = Rs. 100 + (distance_in_km × Rs. 50)

Example:
  5 km ride = Rs. 100 + (5 × 50) = Rs. 350


##  Ride Status Flow

| Status | Rider Sees | Driver Sees |
|---|---|---|
| `pending` |  Searching for driver... |  New ride request! |
| `accepted` |  Driver mil gaya! | Navigate to rider |
| `completed` |  Ride Complete! | Next ride wait |

---

##  Upcoming Features

- [ ] Push Notifications (Firebase Cloud Messaging)
- [ ]  Driver Rating System
- [ ]  In-app Chat (Rider ↔ Driver)
- [ ]  Driver Live Location on Rider's Map
- [ ]  JazzCash / Easypaisa Payment Integration
- [ ]  Ride History Screen
- [ ]  Profile Screen with Photo Upload
- [ ]  Dark / Light Theme Toggle



## Contributing

Pull requests are welcome! For major changes, please open an issue first.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

##  Developer
[M.Haroon]
Built with ❤️ as a real-world Android learning project.

> _Inspired by Uber, Indrive, and Yango_
