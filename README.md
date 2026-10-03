# CompanyCall - Private Enterprise Calling System

Professional & clean corporate video and audio calling system for companies.

## Overview
CompanyCall is a closed, secure corporate communication system designed exclusively for verified employees and management. 

### Key Capabilities Included:
- **Two Integrated Portals**:
  - **Manager Admin Suite**: Real-time operations dashboard, active calls monitor with force-end, employee directory management with permission controls, chronological call audits, dialer with live directory match, STUN/TURN server configuration, and Google Drive backup integration.
  - **Employee Space**: Dedicated "Call Manager" direct line card, authorized colleague directory with real-time presence (online/in-call/offline), live numeric dialer with directory validation, personal call recents with missed call tracking, and Do Not Disturb mode.
- **In-Call Audio & Video Experience**:
  - Full-screen incoming call UI with pulsing animated rings, caller department info, and quick Accept/Decline.
  - CameraX integration with local camera preview, draggable floating PiP window, and feed swap.
  - Microphones mute/unmute, camera toggle, front/rear lens flip, speakerphone toggle, and upgrade from voice to video on the fly.
  - Call duration counter and live network quality monitor (`HD 1080p • 24ms`).

---

## Firebase Setup Steps (Production Deployment)

1. **Create Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/) and create a project named `CompanyCall`.
   - Upgrade to the **Blaze plan** (pay-as-you-go) to enable Cloud Functions triggers for push notifications and automated call cleanups.

2. **Enable Authentication**:
   - Navigate to **Authentication** -> **Sign-in method**.
   - Enable **Email/Password**. Disable all public providers.

3. **Deploy Cloud Firestore**:
   - Create Cloud Firestore in **production mode**, selecting the closest regional location.
   - Deploy `firestore.rules` included in the root of the project:
     ```bash
     firebase deploy --only firestore:rules
     ```

4. **Add Android App & Configuration**:
   - Register your Android application package: `com.aistudio.companycall.prod`
   - Download the generated `google-services.json` and place it in the `app/` directory.

5. **Manager Account Initialization**:
   - In Firebase Authentication, create the manager's login credentials.
   - In Firestore `users/{uid}`, create the manager document with:
     ```json
     {
       "userId": "vikram.ceo",
       "displayName": "Vikram Sharma",
       "role": "manager",
       "status": "active",
       "department": "Executive"
     }
     ```
   - In `settings/app`, define default settings (`ringTimeoutSec: 45`, `maintenanceMode: false`, STUN/TURN server endpoints).

6. **WebRTC NAT Traversal (STUN / TURN)**:
   - Configure your TURN server credentials in `settings/app.turnServers` (e.g., coturn or Metered/Twilio free tier).
