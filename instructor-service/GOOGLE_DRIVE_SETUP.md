# Google Drive API Setup Guide

## 1. Enable Google Drive API

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project or select an existing one.
3. Enable the Google Drive API:
   - Navigate to "APIs & Services" > "Library".
   - Search for "Google Drive API" and enable it.

## 2. Create OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials".
2. Click "Create Credentials" and select "OAuth client ID".
3. Select "Web application" as the application type.
4. Add the following authorized redirect URIs:
   - `http://localhost:8083/oauth2/callback`
   - `http://localhost:8888/Callback`
5. Click "Create" and download the JSON file.
6. Rename the downloaded file to `credentials.json` and place it in `src/main/resources/`.

## 3. Create a Folder in Google Drive

1. Go to [Google Drive](https://drive.google.com/).
2. Create a new folder (e.g., "CyberLearnIX_Uploads").
3. Right-click the folder and select "Share".
4. Click on "Change" and select "Anyone with the link" as "Viewer".
5. Copy the folder ID from the URL (e.g., in `https://drive.google.com/drive/folders/abc123xyz`, the ID is `abc123xyz`).
6. Update `google.drive.folder.id` in `application.properties` with this ID.

## 4. Update application.properties

Make sure the following properties are set in `application.properties`:

```properties
# Google Drive Configuration
google.drive.folder.id=your_google_drive_folder_id
google.drive.credentials.path=classpath:credentials.json
google.drive.application.name=CyberLearnIX

# File Upload Configuration
file.upload-dir=./uploads
file.max-size=10MB
file.allowed-extensions=.jpg,.jpeg,.png,.gif,.mp4,.mov,.avi
```

## 5. Test the Integration

1. Start the application.
2. Use the file upload endpoint to upload a file.
3. Check the response for the Google Drive URL.
4. Verify the file appears in your Google Drive folder.

## Troubleshooting

- **Permission Denied**: Ensure the service account has the necessary permissions in Google Cloud Console.
- **File Not Found**: Verify the `credentials.json` file is in the correct location.
- **Invalid Folder ID**: Double-check the folder ID in `application.properties`.

For more information, refer to the [Google Drive API documentation](https://developers.google.com/drive/api/v3/about-sdk).
