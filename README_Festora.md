# 🎉 Festora – Smart Event Hosting & Management App

**Course:** Android Mobile Development  
**Instructor:** Adin Ashby  
**Developed by:** Aditya Sharma, Harsh Parmar, Nand Kapatel  
**Institution:** LaSalle College, Montreal  

---

## 📖 Overview

**Festora** is an Android-based mobile application built using **Java** and **Firebase**, designed to help users host, discover, and manage events efficiently.  
The app provides features such as **user authentication**, **event creation**, **image uploads**, **real-time Firestore data**, and **profile management** — all in one smooth, Material Design–styled interface.

---

## 🚀 Features

- 🔐 **User Authentication** (Firebase Email/Password & Google Sign-In)  
- 📝 **Event Creation & Upload** (Title, Category, Date, Image, Description)  
- 📅 **Home Feed** with real-time Firestore event listings  
- 👤 **Profile Management** with hosted events  
- 📷 **Firebase Storage Integration** for event images  
- 💬 **Support, Notifications, and Terms screens**  
- 🎨 **Material Design UI** and responsive layouts  

---

## 🏗️ Architecture

Android App → Firebase Authentication → Cloud Firestore → Firebase Storage

The app follows a **client–cloud architecture**, ensuring secure, real-time synchronization between the app and Firebase.

---

## 🧩 Core Components

| File | Description |
|------|--------------|
| `LoginActivity.java` | Handles user login via Firebase Authentication |
| `SignupActivity.java` | Registers new users and validates credentials |
| `HomeActivity.java` | Displays event list from Firestore using RecyclerView |
| `CreateEventActivity.java` | Allows users to create new events and upload images |
| `EventAdapter.java` | Binds Firestore event data to UI |
| `ProfileActivity.java` | Displays user info and hosted events |
| `Event.java` / `EventDoc.java` | Define Firestore document structure |
| `FirestoreContract.java` | Centralizes Firestore collection and field names |

---

## 🧰 Tools & Technologies

- **Language:** Java  
- **IDE:** Android Studio  
- **Database:** Firebase Cloud Firestore  
- **Authentication:** Firebase Auth  
- **Storage:** Firebase Storage  
- **UI Design:** XML, Material Design Components  
- **Version Control:** Git & GitHub  
- **Build System:** Gradle  

---

## ⚙️ Setup & Installation

1. Clone the repository  
   ```bash
   git clone https://github.com/your-username/Festora.git
   cd Festora
   ```

2. Open the project in **Android Studio**.

3. Connect your Firebase project:
   - Add `google-services.json` to `/app/src/`
   - Enable Authentication, Firestore, and Storage from the Firebase Console.

4. Sync Gradle and run the app on an emulator or physical device.

---

## 🔍 App Workflow

1. **LaunchGateActivity** → User chooses Sign In or Sign Up  
2. **LoginActivity / SignupActivity** → Firebase Authentication  
3. **HomeActivity** → Fetches and displays events from Firestore  
4. **CreateEventActivity** → Hosts new events with images  
5. **ProfileActivity** → Shows user profile and hosted events  

---

## 🧪 Testing

- Tested on Android 12 (API 31) and Android 13 (API 33)
- Verified Firebase read/write operations and authentication
- Confirmed navigation and UI consistency across activities

---

## 🧠 Challenges & Solutions

| Challenge | Solution |
|------------|-----------|
| Firebase Authentication setup | Configured `google-services.json` and enabled sign-in methods |
| Async Firestore operations | Used proper success and failure listeners |
| Navigation and activity redirection | Managed Intents with flags and task stacks |
| Image upload delays | Compressed and uploaded asynchronously to Firebase Storage |

---

## 🚧 Future Enhancements

- 🎟️ Ticket booking and QR check-in  
- 🔔 Push notifications  
- 💬 Real-time chat for event discussions  
- 📊 Event analytics and recommendations  

---

## 🏁 Conclusion

**Festora** successfully integrates Firebase with Android to create a dynamic, cloud-backed event management solution.  
It simplifies event hosting, improves user interaction, and showcases practical skills in Android app development, Firebase integration, and UI design.

---

## 👨‍💻 Credits

Developed by  
- Aditya Sharma  
- Harsh Parmar  
- Nand Kapatel  

Under the supervision of **Instructor: Adin Ashby**  
**LaSalle College, Montreal**

---

## 🪪 License

This project is developed for academic purposes and is not licensed for commercial use.
