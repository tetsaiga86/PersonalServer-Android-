# PersonalServer-Android Client
## Introduction:
This is an Android client app that connects to an express server running on a windows computer over https. This client is meant to be paired with the windows server that can be found here: https://github.com/tetsaiga86/PersonalServerDeployment-Windows-

## Directions For Setup:
- Download the latest version, found here: https://docs.google.com/uc?id=0B03BKOm7R9exdVg5UmhZWm1DTFk&export=download
- Transfer the apk file to your android device and install (Note: Only Android version 4.0.3 and higher is supported at this time) 
- Start the windows server
- Start Personal Server app
- Click Scan QR Code
 * If the app does not find a suitable QR scanner it will prompt the user to download it
 * After installing the QR app return to Personal Server app 
- Click QR Code Button
- Scan the QR Code that was generated by the windows server
- After scanning the QR Code you can close the pop-up on your pc
- When running the same instance of the server, clicking Login will used the saved QR information to access the server

If all is working correctly, you should see a listing of your server root directory.

## Features:
- Client uses Basic Auth wrapped in HTTPS to access Server for secure ease of use
- Stream video or music over mobile data or wifi (MX Player is highly recommended for this feature)
- View photos
- Download files or directories directly to your phone
- Rename files or directories on server
- Delete files or directories on server
- Full access to your home server files on the go! (As long as your home computer is on and running the Personal Server)

## Known issues:
- Uploading from client to server is not yet implemented on client app
- Streaming using default video/music app is currently not working (Use Mx Player)

## Coming Soon:
- Native music/video player for better user experience
- Native Picture viewer and Pdf reader
- Native Book reader
- Improved UI experience
