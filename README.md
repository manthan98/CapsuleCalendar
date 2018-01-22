# CapsuleCalendar

University of Toronto (UofT Hacks V) Winner - second place overall, Bitalino best health hack.

## What it does
CapsuleCalendar is an Android application that lets one take a picture of their prescriptions or pill bottles and have them saved to their calendars (as reminders) based on the recommended intake amounts (on prescriptions). The user will then be notified based on the frequency outlined by the physician on the prescription. The application simply requires taking a picture, its been developed with the user in mind and does not require one to go through the calendar reminder, everything is pre-populated for the user through the optical-character recognition (OCR) processing when they take a snap of their prescription/pill bottle.

## Implementation details
A modified version of Google's open source cloud-vision Optical Character Recognition (OCR) engine is applied in real-time on photos (during capture). All neccessary details are than prepopulated for user to confirm prescription specifics (dosage, quantity, medication name), and then synced to Google Calendar. Authentication is powered by Firebase.

## Run application
Simply download the source code, and compile on Android Studio with a physical Android device (does not support emulator as the camera is required).

## Notes
Built by Austin A., Manthan S., Daniel J., Ashley S - official submission can be found [here](https://devpost.com/software/capsulecalendar-hikprv).
