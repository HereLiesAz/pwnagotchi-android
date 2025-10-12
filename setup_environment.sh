#!/bin/bash

# This script helps set up the development environment for the Pwnagotchi Android project.
# It includes dependencies for both the Python-based Pwnagotchi and the Android application.

echo "Setting up Pwnagotchi Android project environment..."

# --- Python Dependencies (for Pwnagotchi) ---
echo "Installing Python dependencies..."
# It's recommended to use a virtual environment
# python3 -m venv venv
# source venv/bin/activate
pip install -r requirements.txt

# --- Android Dependencies (Manual Steps) ---
echo "--------------------------------------------------"
echo "Android Development Environment Setup (Manual)"
echo "--------------------------------------------------"
echo "The following steps need to be performed manually."
echo ""
echo "1. Install Android Studio:"
echo "   Download and install Android Studio from https://developer.android.com/studio"
echo ""
echo "2. Install Java JDK:"
echo "   Android Studio usually comes with its own JDK. If you need to install it separately,"
echo "   we recommend OpenJDK 11 or later."
echo ""
echo "3. Install Android SDK:"
echo "   Use the SDK Manager in Android Studio to install the Android SDK Platform for API level 33."
echo "   You will also need the Android SDK Build-Tools."
echo ""
echo "4. Open the Project:"
echo "   Open the 'pwnagotchi-android' directory in Android Studio."
echo ""
echo "5. Build the Project:"
echo "   Android Studio should automatically sync the Gradle project. You can then build the"
echo "   project by clicking on 'Build' > 'Make Project'."
echo "--------------------------------------------------"

echo "Environment setup script finished."
