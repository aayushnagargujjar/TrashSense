🗑️ TrashSense


TrashSense is a smart Android application that empowers users to classify waste, track their environmental impact, and build sustainable habits using AI + Firebase.
It features a user profile, eco leaderboard, personalized sustainability tips, and analytics dashboards for CO₂ and water usage.

📥 Download Now

🔗 OneDrive APK

🔗 Google Drive APK

🚀 What’s New
🆕 v1.1.0 Updates
✏️ Username Editing: Now users can edit their display name directly from the profile screen.

🎨 UI/UX Enhancements: Improved layout consistency and smoother user experience.

🐛 Bug Fixes: Faster image uploads, better error handling, and smoother transitions.

📱 App Features
🌱 Sustainability Tips
Personalized eco-friendly tips based on user behavior

Stored and fetched dynamically from Firebase Firestore

🧠 AI Eco Dashboard
🔍 Waste Classification
Upload or capture waste images via Floating Action Button

Image classified using custom TFLite model

Labels mapped into: Recyclable, Organic, Hazardous

📅 Daily Logging & Onboarding
Users complete a 3-step flow: meal type → food category → item

Structured fragments enable smooth data collection

📈 CO₂ & Water Impact Forecast
Prediction powered by Flask + Prophet (deployed on Render)

Forecast personalized using Firebase UID

Visual analytics using MPAndroidChart

👤 Profile & Eco-Leaderboard
Firebase Auth for login (email/password)

Profile picture upload to Cloudinary

Username editing, logout, and session handling

Leaderboard to track and compare eco performance



📦 Tech Stack
Layer	Technology
Language	Kotlin
UI/UX	XML, BottomNavigation
ML	TensorFlow Lite (TFLite Model)
Backend API	Flask + Prophet + Firebase
Database	Firebase Firestore
Auth	Firebase Authentication
API Hosting	Render
Visualization	MPAndroidChart

🔧 Installation
bash
Copy
Edit
git clone https://github.com/aayushnagargujjar/TrashSense.git
cd TrashSense
Open the project in Android Studio

Add your google-services.json to the app/ directory

Sync Gradle and run on emulator/device

🌐 Backend Setup
Deploy the Flask server with render.yaml on Render

Install required packages:

bash
Copy
Edit
pip install flask prophet firebase-admin gunicorn
Add a Render cron job for daily forecast updates

🤖 Machine Learning
Custom TFLite model trained on waste categories:
Metal, Glass, Paper, Trash, Plastic, Battery, etc.

Image classification triggers label mapping and updates user logs in Firestore

🧪 Testing
Tested on Android API levels 24 to 34

Image upload, classification, prediction response, and Firestore integration

Edge case handling: no internet, null predictions, long loading

👨‍💻 Developed By
Aayush Nagar Gujjar
🔗 GitHub Profile

💡 Contribute or Collaborate?
If you're passionate about climate tech or mobile ML apps — feel free to fork, star, or open a PR!
