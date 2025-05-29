## 🗑️ TrashSense
![splashgif](https://github.com/user-attachments/assets/9decf0cf-e624-4f50-99ce-a41111ad5eb8)

**TrashSense** is a smart Android application that helps users track their daily waste activities, 
predict environmental impact, and promote sustainable habits using AI.
The app includes a leaderboard, user profile, intelligent suggestions, and visual analytics for CO₂ and water usage based on user behavior.

## 🚀 Features

### 🌱 Motivation & Tips
![Screenshot_20250529_121428](https://github.com/user-attachments/assets/b4ade73f-df91-4f02-96d8-2ad974dc94b1)

* Custom eco-friendly tips rendered in `HomeFragment`.
* Uses Firestore for storing and fetching suggestions.

### 📊 AI Eco Dashboard

1. 🔍 Waste Identification
   ![Screenshot_20250529_120819](https://github.com/user-attachments/assets/1b722861-f00f-4f2c-ad32-76f65b84a503)

* Upload images of waste (e.g., plastic, organic, hazardous).
* AI-based classification using a custom TensorFlow Lite model.
* Category suggestions: Recyclable, Organic, or Hazardous.

2. 📅 Daily Logging
![Screenshot_20250529_120804](https://github.com/user-attachments/assets/d68c15e0-bbee-4e41-8bf2-0bbe263f2d80)

* Users can log daily actions related to eco-conscious activities.
* Fragments used for structured step-by-step onboarding.
  
3. Prediction
   
* Uses Prophet forecasting API deployed via Flask & Render.
* Predicts CO₂ and water usage based on user history.
* Data visualized in MPAndroidChart.

### 🧑 Profile & Leaderboard
![Screenshot_20250529_120739](https://github.com/user-attachments/assets/c0a38011-5688-462a-b841-dd0db0a38a26)
![Uploading Screenshot_20250529_120730.jpg…]()

* Firebase Authentication and Firestore database.
* Track personal performance and see top users.
* Editable profile and logout support.


###Upload and share your Thoughts
![Screenshot_20250529_120828](https://github.com/user-attachments/assets/6a875235-ab10-441e-ad78-43e01726427c)

## 📷 Screenshots
* HomeFragment UI with motivational tips.
* ProfileFragment with chart and leaderboard.
* UploadFragment image picker and classification result.
* AI Eco Dashboard prediction graph.

---

## 📦 Tech Stack

| Layer       | Technology                    |
| ----------- | ----------------------------- |
| Language    | Kotlin                        |
| UI/UX       | XML Layouts, BottomNavigation |
| ML          | TensorFlow Lite (image model) |
| Backend API | Flask + Prophet + Firebase    |
| Database    | Firebase Firestore            |
| Auth        | Firebase Authentication       |
| Hosting     | Render for Flask ML API       |
| Charts      | MPAndroidChart                |

---

## 🔧 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/aayushnagargujjar/TrashSense.git
   cd TrashSense
   ```

2. Open with Android Studio.

3. Sync Gradle and configure Firebase:

   * Download `google-services.json` from your Firebase project.
   * Place it in `app/`.

4. Run the app on emulator or device.

---

## 🌐 Cloud Setup

* Set up Flask server on [Render](https://render.com/) with `render.yaml`.
* Install dependencies: `firebase-admin`, `prophet`, `gunicorn`, `flask`.
* Schedule daily forecast updates via Render cron jobs.

---

## 🤖 Machine Learning

* Custom TFLite model trained on:

  * Metal, Glass, Paper, Trash, Battery, Plastic, etc.
* Image classifier returns label mapped to waste category.
* Classification results update Firestore under user’s logs.

---

## 🧪 Testing

* Manual testing on Android API 24–34.
* Test image uploads, prediction response, Firestore sync.
* Check edge cases like null predictions or offline mode.

---

## 🤝 Contributors

* Aayush Nagar Gujjar — [GitHub Profile](https://github.com/aayushnagargujjar)


