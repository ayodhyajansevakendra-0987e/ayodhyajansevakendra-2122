# Ayodhya Jan Seva Kendra — सेटअप गाइड (हिंदी)

यह ZIP Android ऐप का Kotlin/Jetpack Compose source project, React web admin panel और Firebase security/deployment files देता है। यह अभी APK या live-hosted website नहीं है। नीचे दिए गए Firebase और Android Studio steps पूरे होने के बाद ही वास्तविक OTP/डेटा काम करेगा।

## 1. Firebase प्रोजेक्ट बनाइए
1. https://console.firebase.google.com/ खोलकर नया Firebase project बनाएं।
2. Android app जोड़ें: package/application ID बिल्कुल `in.nooralam.janseva` रखें।
3. Android app की `google-services.json` डाउनलोड करके इस जगह रखें: `android/app/google-services.json`। इसे ZIP में शामिल न करें; यह आपके Firebase project की config है।
4. Authentication में **Phone** provider चालू करें। पहले Firebase के test phone numbers से परीक्षण करें। असली SMS के लिए Firebase की billing/region/verification आवश्यकताएं पूरी करें।
5. Firestore Database बनाएं और Storage चालू करें।
6. `firebase/firestore.rules` को Firestore Rules में और `firebase/storage.rules` को Storage Rules में publish करें। `firebase/firestore.indexes.json` के indexes भी deploy करें।

## 2. Admin अकाउंट बनाएं
1. Firebase Authentication > Users में Email/Password provider चालू करें।
2. `halimajanseva@gmail.com` के लिए admin user बनाएं। पासवर्ड अपने पास रखें; किसी को चैट में न भेजें।
3. उस user का UID कॉपी करें। Firestore में `users/{UID}` document बनाएं और fields रखें:
   - `email`: `halimajanseva@gmail.com`
   - `name`: `Noor Alam Ansari`
   - `role`: `admin`
4. Admin role केवल Firebase Console या trusted server से दें। ग्राहक ऐप से role बदलने की अनुमति न दें।

## 3. Android ऐप चलाएं / APK बनाएं
1. Android Studio में `android/` folder खोलें।
2. Gradle sync पूरा होने दें और ऊपर दिया `google-services.json` सही जगह रखें।
3. Firebase Console में Phone Auth के लिए app के SHA-1/SHA-256 fingerprints जोड़ें।
4. पहले test phone number से OTP login, request submit, document upload और request status जांचें।
5. APK के लिए Android Studio में **Build > Build Bundle(s) / APK(s) > Build APK(s)** चुनें।
6. Debug APK आम तौर पर `android/app/build/outputs/apk/debug/app-debug.apk` में मिलेगा। Public release के लिए अलग release signing key बनाएं और उसे सुरक्षित रखें।

## 4. Web Admin Panel चलाएं / deploy करें
1. `admin/.env.example` की कॉपी बनाकर `admin/.env` नाम रखें।
2. Firebase Console > Project settings > Your apps > Web app से values भरें: `VITE_FIREBASE_API_KEY`, `VITE_FIREBASE_AUTH_DOMAIN`, `VITE_FIREBASE_PROJECT_ID`, `VITE_FIREBASE_STORAGE_BUCKET`, `VITE_FIREBASE_MESSAGING_SENDER_ID`, `VITE_FIREBASE_APP_ID`।
3. Terminal में:
   ```bash
   cd admin
   npm install
   npm run dev
   ```
4. Hosting के लिए Firebase CLI install/login करें, project select करें, फिर project root से:
   ```bash
   firebase deploy --only firestore:rules,firestore:indexes,storage
   cd admin && npm run build
   cd .. && firebase deploy --only hosting
   ```
5. Firebase Authentication > Settings > Authorized domains में Hosting domain जोड़ें।

## 5. अभी कौन-से काम अकाउंट/मर्चेंट सेटअप के कारण बाकी हैं
- Firebase project और `google-services.json` जोड़ना।
- Phone OTP के वास्तविक SMS और SHA fingerprints का परीक्षण।
- Admin account बनाकर UID पर `role=admin` लगाना।
- Web admin `.env` values भरकर Hosting deploy करना।
- Android Studio से APK build, device testing और release signing।
- Payment gateway (Razorpay/PayU/Cashfree आदि) चुनना, merchant onboarding और trusted server webhook बनाना। भुगतान सुविधा अभी जानबूझकर चालू नहीं है।
- Aadhaar/PAN/सरकारी सेवाओं के लिए केवल अधिकृत provider/official portal का integration; कोई सरकारी API access इस project में शामिल नहीं है।
- Privacy Policy और Terms को कानूनी समीक्षा के बाद सार्वजनिक HTTPS URL पर publish करना।

## सुरक्षा
किसी को OTP, Firebase account password, UPI PIN, service-account private key या release keystore न दें। ग्राहक के असली Aadhaar/PAN/बैंक दस्तावेज़ production security review और rules testing के पहले अपलोड न करें।
