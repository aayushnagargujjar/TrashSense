
## 🗑️ TrashSense

![splashgif](https://github.com/user-attachments/assets/9decf0cf-e624-4f50-99ce-a41111ad5eb8)

**TrashSense** is a smart Android application that empowers users to classify waste, track their environmental impact, and build sustainable habits using AI + Firebase.
It features a user profile, eco leaderboard, personalized sustainability tips, and analytics dashboards for CO₂ and water usage.
Sure! Here's the corrected and properly formatted version of the **Download Now** section:


📥 **Download Now**

* 🔗 [OneDrive APK](https://indianinstituteoftechnol299-my.sharepoint.com/:u:/g/personal/24je0724_iitism_ac_in/EeFeLuBby5VHtJt97nvyifsB3Mp1s4aajGk2_YgvnTPhgA?e=FanB4Y)
* 🔗 [Google Drive APK](https://lnkd.in/gVWf2MFr)


## 🚀 What’s New

### 🆕 v1.1.0 Updates

* ✏️ **Username Editing**: Now users can edit their display name directly from the profile screen.
* 🎨 **UI/UX Enhancements**: Improved layout consistency and smoother user experience.
* 🐛 **Bug Fixes**: Faster image uploads, better error handling, and smoother transitions.

---

## 📱 App Features

### 🌱 Sustainability Tips

* Personalized eco-friendly tips based on user behavior
* Stored and fetched dynamically from **Firebase Firestore**

### 🧠 AI Eco Dashboard

#### 🔍 Waste Classification

* Upload or capture waste images via Floating Action Button
* Image classified using custom **TFLite model**
* Labels mapped into: **Recyclable**, **Organic**, **Hazardous**

#### 📅 Daily Logging & Onboarding

* Users complete a 3-step flow: meal type → food category → item
* Structured fragments enable smooth data collection

#### 📈 CO₂ & Water Impact Forecast

* Prediction powered by **Flask + Prophet** (deployed on **Render**)
* Forecast personalized using Firebase UID
* Visual analytics using **MPAndroidChart**

### 👤 Profile & Eco-Leaderboard

* Firebase Auth for login (email/password)
* Profile picture upload to **Cloudinary**
* **Username editing**, logout, and session handling
* Leaderboard to track and compare eco performance

![IMG\_20250529\_123506](https://github.com/user-attachments/assets/ba935b68-6e6e-485e-a5a9-9cde9b2f8b7a)

---

## 📦 Tech Stack

| Layer         | Technology                     |
| ------------- | ------------------------------ |
| Language      | Kotlin                         |
| UI/UX         | XML, BottomNavigation          |
| ML            | TensorFlow Lite (TFLite Model) |
| Backend API   | Flask + Prophet + Firebase     |
| Database      | Firebase Firestore             |
| Auth          | Firebase Authentication        |
| API Hosting   | Render                         |
| Visualization | MPAndroidChart                 |

---

## 🔧 Installation

```bash
git clone https://github.com/aayushnagargujjar/TrashSense.git
cd TrashSense
```

1. Open the project in **Android Studio**
2. Add your `google-services.json` to the `app/` directory
3. Sync Gradle and run on emulator/device

---

## 🌐 Backend Setup

1. Deploy the Flask server with `render.yaml` on [Render](https://render.com)
2. Install required packages:

   ```bash
   pip install flask prophet firebase-admin gunicorn
   ```
3. Add a Render cron job for daily forecast updates

---

## 🤖 Machine Learning

* Custom TFLite model trained on waste categories:
  `Metal, Glass, Paper, Trash, Plastic, Battery, etc.`
* Image classification triggers label mapping and updates user logs in Firestore

---

## 🧪 Testing

* Tested on Android API levels 24 to 34
* Image upload, classification, prediction response, and Firestore integration
* Edge case handling: no internet, null predictions, long loading

---

## 👨‍💻 Developed By

**Aayush Nagar Gujjar**
🔗 [GitHub Profile](https://github.com/aayushnagargujjar)

---

## 💡 Contribute or Collaborate?

If you're passionate about climate tech or mobile ML apps — feel free to fork, star, or open a PR!
