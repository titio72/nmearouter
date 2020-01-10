package com.aboni.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GDrive {
    private static final String APPLICATION_NAME = "NMEARouter";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "./";
    private static final String DUMPS_ROOT_ID = "0B--7j-n2mogkYWFpdUVXT3J6UEk";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "gdrive_credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param netHttpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport netHttpTransport) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                netHttpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("online")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        //upload( "/users/aboni/Downloads/sss.jpg", "application/image");
        for (String s : listFiles()) System.out.println(s);
    }

    public static String upload(String file, String mime) throws IOException, GeneralSecurityException {
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(netHttpTransport, JSON_FACTORY, getCredentials(netHttpTransport)).setApplicationName(APPLICATION_NAME).build();
        return createFile(service, file, mime);
    }

    public static Collection<String> listFiles() throws IOException, GeneralSecurityException {
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(netHttpTransport, JSON_FACTORY, getCredentials(netHttpTransport)).setApplicationName(APPLICATION_NAME).build();

        FileList result = service.files().list()
                .setPageSize(1000)
                .setQ("'0B--7j-n2mogkYWFpdUVXT3J6UEk' in parents")
                .execute();

        List<String> res = new ArrayList<>();
        result.getFiles().stream().
                filter(f -> f.getName().endsWith(".sql.tgz")).
                /*filter(f->{
                    try {
                        File ff = service.files().get(f.getId()).set("fields", "name, trashed, parents").execute();
                        return ff.getTrashed() == null || !ff.getTrashed();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }).*/
                        forEach(f -> res.add(f.getName()));

        return res;
    }

    private static String createFile(Drive service, String file, String mime) throws IOException {
        File fileMetadata = new File();
        java.io.File filePath = new java.io.File(file);

        fileMetadata.setName(filePath.getName());
        fileMetadata.setParents(Arrays.asList(DUMPS_ROOT_ID));
        FileContent mediaContent = new FileContent(mime, filePath);
        File gFile = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
        return gFile.getId();
    }

}
