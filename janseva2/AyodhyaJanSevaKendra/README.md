# Ayodhya Jan Seva Kendra

Starter implementation for a bilingual Android app (Kotlin + Firebase) and responsive admin web panel.

## Included
- Android app: Kotlin, Jetpack Compose, Material 3, Hindi/English strings, navigation, service catalogue, request form, request history, profile/contact.
- Firebase Auth phone sign-in scaffold, Firestore repository, Storage upload scaffold, FCM-ready dependencies.
- Firestore security rules and Storage rules.
- Admin panel: responsive React + Vite + Firebase, sign-in, application list/status updates, service price management starter.
- Firebase setup and APK build instructions.

## Important production limits
This is a functional development starter, not a live government-service integration or production-certified payment system.
- Add your own Firebase project configuration (`google-services.json` and web `.env`).
- Phone OTP requires Firebase Phone Authentication setup and authorized test numbers for development.
- UPI payment initiation/status verification requires a payment gateway merchant account and server-side webhook verification. Never trust a client-side success callback.
- Aadhaar/PAN/certificate services must be performed only through authorized providers/official portals. This app provides request/assistance workflows only; it does not claim UIDAI/NSDL/UTIITSL/government authorization.
- Before production, add server-side Cloud Functions for payment verification, receipt numbering, notifications, audit logs, rate limits, and sensitive-data retention/deletion workflows.
- Have the privacy policy, terms, data protection notices, and security rules reviewed before collecting real identity documents.

## Project layout
- `android/` Android Studio project
- `admin/` React admin dashboard
- `firebase/` Firestore and Storage rules

## Firebase setup
1. Create a Firebase project at https://console.firebase.google.com/.
2. Register Android app with package `in.nooralam.janseva`.
3. Download `google-services.json` into `android/app/`.
4. Enable Authentication > Phone. Configure SHA-1/SHA-256 fingerprints for release builds.
5. Create Firestore database and enable Storage.
6. Publish `firebase/firestore.rules` and `firebase/storage.rules`.
7. In `admin/`, copy `.env.example` to `.env` and fill in Firebase web app values.
8. Create the first admin user with Firebase Authentication. In Firestore create `users/{uid}` with `role: "admin"` (only do this yourself in trusted console). Do not expose admin role assignment in the app.
9. Set Firebase Storage rules. For production, tighten file type/size rules and use server-side malware scanning.

## Run Android
Open `android/` in Android Studio (Hedgehog or newer), let Gradle sync, add `google-services.json`, then Run.
Build APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
CLI build is available only after a Gradle installation or Gradle Wrapper is configured. This bundle currently does not include the Gradle Wrapper JAR, and this build environment has no Android SDK/Gradle installed. Use Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Expected output: `android/app/build/outputs/apk/debug/app-debug.apk`.

## Run admin panel
```bash
cd admin
npm install
cp .env.example .env
npm run dev
```
Admin access checks `users/{uid}.role == "admin"`.

## Firestore data model
- `users/{uid}`: name, phone, role (`customer` or `admin`), preferredLanguage
- `services/{serviceId}`: nameHi, nameEn, category, price, active
- `applications/{applicationId}`: customerId, customerName, serviceId, serviceName, details, documentPaths[], status, createdAt, updatedAt
- `payments/{paymentId}`: applicationId, customerId, amount, method, status, gatewayReference, createdAt
- `notifications/{notificationId}`: customerId, title, body, read, createdAt

Statuses: `submitted`, `under_review`, `accepted`, `rejected`, `in_progress`, `completed`.
Payment statuses: `pending`, `verified`, `failed`, `refunded`.

## Security notes
Rules are a baseline. Firestore rules cannot validate a real payment. Only a trusted backend/webhook can set `payments.status = verified`. Keep service account keys off devices and source control.


## Customer phone OTP flow
The Android Profile tab now contains Firebase Phone Authentication:
1. Enter number in E.164 format, e.g. `+919876543210`.
2. Firebase sends OTP (or auto-verifies on supported devices).
3. On success, app creates `users/{uid}` with `role: customer`.
4. Service request submission requires an authenticated UID; guest submissions are blocked.

For testing, use Firebase Console > Authentication > Sign-in method > Phone > Phone numbers for testing. Test phone numbers do not send real SMS.

## Admin account configuration
Admin email supplied for this project: `halimajanseva@gmail.com`.
1. In Firebase Authentication, create this account using Email/Password (or use the chosen supported admin sign-in provider).
2. Sign in once, then copy its UID from Authentication > Users.
3. In Firestore Console, create `users/{ADMIN_UID}` with:
   - `email`: `halimajanseva@gmail.com`
   - `role`: `admin`
   - `name`: `Noor Alam Ansari`
4. Do not allow public app code to set or change `role`. The updated rules only allow self-registration with `role: customer`; admin role is assigned only in trusted Firebase Console/server.
5. Admin panel currently signs in with Firebase Email/Password. Enable Email/Password provider in Firebase Auth. The web app checks the Firestore role before showing admin data.

## Deploy rules
Firebase Console > Firestore Database > Rules: paste `firebase/firestore.rules`.
Firebase Console > Storage > Rules: paste `firebase/storage.rules`.
Do not launch with test mode rules.

## Remaining before production
- Add a server-side admin bootstrap / custom-claims Cloud Function and audit log.
- Add authenticated document-picker/upload UI and enforce retention/access controls.
- Implement UPI gateway orders and server-side signed webhook verification. No client-side payment-success writes.
- Build actual receipt PDF generation server-side.
- Add consent, privacy policy URL, terms, grievance/contact details, and security testing.

## Documents and customer tracking update
The Android request form now lets a signed-in customer select one PDF/JPEG/PNG document (max 10 MB by Storage rules), uploads it to `customer-documents/{uid}/...`, and stores only the Storage path in the application document. The customer request screen listens for live status updates. The admin dashboard can open uploaded documents for authorized admins.

## Privacy and terms
Drafts are included at `PRIVACY_POLICY.md` and `TERMS_AND_CONDITIONS.md`. They are templates, not legal advice. Publish final reviewed versions at stable HTTPS URLs before launch.

## Deploy Firebase rules and admin hosting
Install Firebase CLI (`npm install -g firebase-tools`), sign in (`firebase login`), select your Firebase project (`firebase use --add`), then from the project root run:
```bash
firebase deploy --only firestore:rules,firestore:indexes,storage
cd admin && npm install && npm run build
cd .. && firebase deploy --only hosting
```
The Firebase Hosting site must use the same Firebase project as the app. Configure the Hosting domain in Firebase Authentication > Settings > Authorized domains.

## Build verification status
The source project has not yet been compiled or device-tested in this environment. The Android SDK/Gradle toolchain and Firebase project configuration are not present here. Do not treat the ZIP as a signed or installable APK.

## Current implementation boundary
This bundle includes OTP sign-in, customer request submission, one-file secure upload, live request status, admin email/password sign-in with Firestore role gate, admin application status updates, document opening, customer list and service price editing. It does not include a payment provider or government API integration. Do not collect actual customer documents until Firebase rules have been deployed and tested. Payment collection remains disabled until the owner chooses a provider (e.g. Razorpay/PayU/Cashfree), obtains merchant credentials, and a trusted server webhook is implemented and tested.

See `PAYMENTS_SETUP.md` for the required secure payment-gateway workflow. Payment collection is intentionally not enabled until provider selection and merchant/server credentials are available.

## Manual smoke test checklist
- Verify Android package id matches Firebase Android app registration.
- Test OTP with Firebase test phone numbers before enabling real SMS.
- Sign in as a customer; verify `users/{uid}` has role `customer`.
- Submit a test application and confirm it appears only to that customer and admins.
- Upload a test PDF/image under 10 MB; verify the object path is `customer-documents/{uid}/...`.
- Sign in to admin web panel using the configured admin email; verify non-admin users are blocked.
- Update application status and verify the customer app reflects it live.
- Verify customer cannot read another customer's application or documents.
- Test invalid file types, oversized files, logged-out access and role escalation attempts.
