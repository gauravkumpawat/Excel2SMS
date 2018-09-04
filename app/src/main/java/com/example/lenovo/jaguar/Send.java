package com.example.lenovo.jaguar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Send extends AppCompatActivity {

    ListView list;
    List<SMessage> arrayList;
    Iterator rowIter;
    Button send;
    private static final String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM
    };
    private InternetDetector internetDetector;
    EditText edtToAddress,edtSubject;
    CustomAdapter adapter;
    CheckBox ch;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    GoogleAccountCredential mCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(android.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlesend);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initializing Internet Checker
        internetDetector = new InternetDetector(getApplicationContext());

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        send = findViewById(R.id.button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.sendOption == 1) {
                    if ((checkSelfPermission(Manifest.permission.SEND_SMS)) == PackageManager.PERMISSION_GRANTED)
                        sendSms();
                    else {
                        ActivityCompat.requestPermissions(Send.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                200);
                    }
                } else if (MainActivity.sendOption == 2)
                    sendMail(view);
                else if (MainActivity.sendOption == 12) {
                    if ((checkSelfPermission(Manifest.permission.SEND_SMS)) == PackageManager.PERMISSION_GRANTED) {
                        sendSms();
                        sendMail(view);
                    } else {
                        ActivityCompat.requestPermissions(Send.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                200);
                    }
                }
            }
        });

        try {

            // Creating Input Stream
            File file = new File(MainActivity.filepath, MainActivity.filename);
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            rowIter = mySheet.rowIterator();
        } catch (Exception e) {
            Toast.makeText(Send.this, "Something is wrong with Your File", Toast.LENGTH_SHORT).show();
        }
        list = findViewById(R.id.listsend);
        arrayList = new ArrayList<SMessage>();

        int i = 0, j = 0, end = 0, col = 0, no = 0, subcol = 0;
        StringBuffer msgbuffer, subbuffer;
        msgbuffer = subbuffer = null;
        msgbuffer = new StringBuffer(MainActivity.msg);
        if (MainActivity.sendOption == 2)
            subbuffer = new StringBuffer(MainActivity.subject);
        if (MainActivity.sendOption == 12) {
            subbuffer = new StringBuffer(MainActivity.subject);
        }
        String cont = new String();
        String email = new String();
        String sub = new String();
        ArrayList<Integer> colnos = new ArrayList<>();
        ArrayList<Integer> subnos = new ArrayList<>();

        if (MainActivity.msg.matches("(.*)#(.*)")) {
            outer:
            for (i = 0; i < msgbuffer.length(); i++) {
                if (msgbuffer.charAt(i) == '#') {
                    col = 0;
                    for (j = i + 1; j < msgbuffer.length(); j++, end++) {
                        char c = msgbuffer.charAt(j);
                        if (c == '#') {
                            i = j;
                            continue outer;
                        } else {
                            try {
                                col = (col * 10) + Integer.parseInt(String.valueOf(c));
                            } catch (Exception e) {
                                Toast.makeText(Send.this, "Invalid Column", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                if (end != 0) {
                    colnos.add(col);
                }
                end = 0;

            }
            if (MainActivity.subject != null && MainActivity.subject.matches("(.*)#(.*)")) {
                outer:
                for (i = 0; i < subbuffer.length(); i++) {
                    if (subbuffer.charAt(i) == '#') {
                        subcol = 0;
                        for (j = i + 1; j < subbuffer.length(); j++, end++) {
                            char c = subbuffer.charAt(j);
                            if (c == '#') {
                                i = j;
                                continue outer;
                            } else {
                                try {
                                    subcol = (subcol * 10) + Integer.parseInt(String.valueOf(c));
                                } catch (Exception e) {
                                    Toast.makeText(Send.this, "Invalid Column", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                    if (end != 0) {
                        subnos.add(subcol);
                    }
                    end = 0;

                }
            }
            int count = 0;
            rowIter.next();
            while (rowIter.hasNext()) {
                cont = "";
                email = "";
                msgbuffer = new StringBuffer(MainActivity.msg);
                try {
                    Iterator it = colnos.iterator();
                    /** We now need something to iterate through the cells.**/
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    if (cont.equals("") && MainActivity.sendOption == 1) {
                        HSSFCell cell = myRow.getCell(MainActivity.mobcol);
                        BigDecimal d = BigDecimal.valueOf(Double.parseDouble(cell.toString()));
                        cont = String.valueOf(d);
                    } else if (email.equals("") && MainActivity.sendOption == 2) {
                        email = myRow.getCell(MainActivity.emailcol).toString();
                    } else if (cont.equals("") && email.equals("") && MainActivity.sendOption == 12) {
                        cont = myRow.getCell(MainActivity.mobcol).toString();
                        email = myRow.getCell(MainActivity.emailcol).toString();
                    }

                    if (cont.equals("") && MainActivity.sendOption == 1)
                        continue;
                    else if (email.equals("") && MainActivity.sendOption == 2)
                        continue;
                    else if (cont.equals("") && email.equals("") && MainActivity.sendOption == 12)
                        continue;

                    while (it.hasNext()) {
                        col = (int) it.next();
                        HSSFCell mycell = myRow.getCell(col);
                        String s = new String(msgbuffer);
                        msgbuffer = new StringBuffer(s.replaceAll("#" + col + "#", mycell.toString()));
                    }
                    if (MainActivity.sendOption == 2 || MainActivity.sendOption == 12) {
                        Iterator subit = subnos.iterator();
                        while (subit.hasNext()) {
                            int scol = (int) subit.next();
                            HSSFCell mycell = myRow.getCell((int) scol);
                            String s = new String(subbuffer);
                            subbuffer = new StringBuffer(s.replaceAll("#" + scol + "#", mycell.toString()));
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(Send.this, "Invalid Column", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                if (MainActivity.sendOption == 1)
                    arrayList.add(new SMessage(msgbuffer.toString(), cont, "", ""));
                else if (MainActivity.sendOption == 2)
                    arrayList.add(new SMessage(msgbuffer.toString(), "", email, subbuffer.toString()));
                else if (MainActivity.sendOption == 12)
                    arrayList.add(new SMessage(msgbuffer.toString(), cont, email, subbuffer.toString()));
            }
            adapter = new CustomAdapter();
            list.setAdapter(adapter);

        }
    }

    private void sendMail(View view) {

        if (Utils.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            startActivityForResult(mCredential.newChooseAccountIntent(), Utils.REQUEST_ACCOUNT_PICKER);
        } else {
            ActivityCompat.requestPermissions(Send.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1003);
        }
        for(int i=0;i<list.getChildCount();i++) {


            View vie=list.getChildAt(i);
            EditText sub = vie.findViewById(R.id.sub);
            EditText em = vie.findViewById(R.id.eid);
            ch = vie.findViewById(R.id.msgchk);

            edtSubject=sub;
            edtToAddress=em;
            getResultsFromApi(vie);
        }
    }

    private void getResultsFromApi(View view) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(view);
        } else if (!internetDetector.checkMobileInternetConn()) {
            showMessage(view, "No network connection available.");
        } else {
            new Send.MakeRequestTask(this, mCredential).execute();
        }
    }


    private void sendSms() {
        try {
            SmsManager s = SmsManager.getDefault();

            final ProgressDialog di = new ProgressDialog(this);
            di.setTitle("Sending...");
            di.setMessage("Text Message...");
            di.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            di.setIndeterminate(false);
            di.setMax(list.getChildCount());
            di.show();
            for (int i = 0; i < list.getChildCount(); i++) {
                di.setProgress(i + 1);
                View vie = list.getChildAt(i);
                EditText ed2 = (EditText) vie.findViewById(R.id.mobet);
                CheckBox ch1 = vie.findViewById(R.id.msgchk);
                if (ch1.isChecked()) {
                    s.sendTextMessage(String.valueOf(ed2.getText()), null, ch1.getText().toString(), null, null);
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
                        }
                    });
        }}} catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_PERMISSION_GET_ACCOUNTS:
                chooseAccount(send);
                break;

            case 200: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Send.this, Manifest.permission.SEND_SMS)) {
                        //Show permission explanation dialog...
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 200);
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                        Toast.makeText(this, "Please Give permission to Send SMS", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    private void chooseAccount(View view) {
        if (Utils.checkPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi(view);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), Utils.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            ActivityCompat.requestPermissions(Send.this,
                    new String[]{Manifest.permission.GET_ACCOUNTS}, Utils.REQUEST_PERMISSION_GET_ACCOUNTS);
        }
    }


    private void showMessage(View view, String message) {
        Toast.makeText(Send.this, message, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            super.onBackPressed();
        }
        return true;
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    // Method to Show Info, If Google Play Service is Not Available.
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    // Method for Google Play Services Error Info
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                Send.this,
                connectionStatusCode,
                Utils.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    showMessage(send, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi(send);
                }
                break;
            case Utils.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi(send);
                    }
                }
                break;
            case Utils.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi(send);
                }
                break;
            }
        }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return arrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = Send.this.getLayoutInflater();
            view = inflater.inflate(R.layout.sendlist, null, true);
            final CheckBox msgch = view.findViewById(R.id.msgchk);
            EditText mob = view.findViewById(R.id.mobet);
            EditText ema = view.findViewById(R.id.eid);
            EditText sub = view.findViewById(R.id.sub);

            msgch.setChecked(true);
            msgch.setText(arrayList.get(i).msg);


            if (MainActivity.sendOption == 1) {
                ema.setVisibility(View.GONE);
                sub.setVisibility(View.GONE);
                mob.setText(arrayList.get(i).cont);
            } else if (MainActivity.sendOption == 2) {
                mob.setVisibility(View.GONE);
                ema.setText(arrayList.get(i).email);
                sub.setText(arrayList.get(i).subject);
            } else if (MainActivity.sendOption == 12) {
                mob.setText(arrayList.get(i).cont);
                ema.setText(arrayList.get(i).email);
                sub.setText(arrayList.get(i).subject);
            }
            return view;
        }
    }

    class SMessage {
        String msg, cont, email, subject;

        SMessage(String msg, String cont, String email, String subject) {
            this.msg = msg;
            this.cont = cont;
            this.email = email;
            this.subject = subject;
        }
    }



    class MakeRequestTask extends AsyncTask<Void, Void, String> {

        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;
        private View view = send;
        private Send activity;

        MakeRequestTask(Send activity, GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String getDataFromApi() throws IOException {
            // getting Values for to Address, from Address, Subject and Body
            String user = "me";
            String to = edtToAddress.getText().toString();
            String from = mCredential.getSelectedAccountName();
            String subject = Utils.getString(edtSubject);
            String body = ch.getText().toString();
            MimeMessage mimeMessage;
            String response = "";
            try {
                mimeMessage = createEmail(to, from, subject, body);
                response = sendMessage(mService, user, mimeMessage);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return response;
        }

        // Method to send email
        private String sendMessage(Gmail service,
                                   String userId,
                                   MimeMessage email)
                throws MessagingException, IOException {
            Message message = createMessageWithEmail(email);
            // GMail's official method to send email with oauth2.0
            message = service.users().messages().send(userId, message).execute();

            System.out.println("Message id: " + message.getId());
            System.out.println(message.toPrettyString());
            return message.getId();
        }

        // Method to create email Params
        private MimeMessage createEmail(String to,
                                        String from,
                                        String subject,
                                        String bodyText) throws MessagingException {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session);
            InternetAddress tAddress = new InternetAddress(to);
            InternetAddress fAddress = new InternetAddress(from);

            email.setFrom(fAddress);
            email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
            email.setSubject(subject);

            // Create Multipart object and add MimeBodyPart objects to this object
            Multipart multipart = new MimeMultipart();

            // Changed for adding attachment and text
            // email.setText(bodyText);

            BodyPart textBody = new MimeBodyPart();
            textBody.setText(bodyText);
            multipart.addBodyPart(textBody);

            //Set the multipart object to the message object
            email.setContent(multipart);
            return email;
        }

        private Message createMessageWithEmail(MimeMessage email)
                throws MessagingException, IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            email.writeTo(bytes);
            String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
            Message message = new Message();
            message.setRaw(encodedEmail);
            return message;
        }

        @Override
        protected void onPreExecute() {
//            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
//            mProgress.hide();
            if (output == null || output.length() == 0) {
                showMessage(view, "No results returned.");
            } else {
                showMessage(view, output);
            }
        }

        @Override
        protected void onCancelled() {
//            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Utils.REQUEST_AUTHORIZATION);
                } else {
                    showMessage(view, "The following error occurred:\n" + mLastError);
                    Log.v("Error", mLastError + "");
                }
            } else {
                showMessage(view, "Request Cancelled.");
            }
        }
    }
}
