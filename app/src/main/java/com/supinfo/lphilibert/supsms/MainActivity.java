package com.supinfo.boutrig.supsms;

import com.google.gson.Gson;    // GSON lib (Google JSON)
import com.supinfo.boutrig.supsms.model.ConnectToAPI;
import com.supinfo.boutrig.supsms.model.Contact;
import com.supinfo.boutrig.supsms.model.SMS;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class MainActivity extends Activity {

    private Button logoutButton;
    private Button backupSmsButton;
    private Button backupContactsButton;
    private Button aboutButton;
    private String jsonSmsInbox;
    //private String loginGetStr;
    //private String passwordGetStr;
    private String jsonSmsSent;
    private String jsonContacts;
    public String httpResponseINBOX;
    public String httpResponseSENT;
    public String httpResponseCONTACTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ON RECUPERE LES BOUTONS DU LAYOUT
        logoutButton = (Button) findViewById(R.id.logout);
        backupSmsButton = (Button) findViewById(R.id.backupSms);
        backupContactsButton = (Button) findViewById(R.id.backupContacts);
        aboutButton = (Button) findViewById(R.id.about);

        final String loginGetStr , passwordGetStr;

        Bundle bundleForMainActivity = getIntent().getExtras();
        loginGetStr = bundleForMainActivity.getString("loginGetStr");
        passwordGetStr = bundleForMainActivity.getString("passwordGetStr");

        // LORS D'UN CLIC SUR LE BOUTON ABOUT
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AboutActivity.class);
                startActivity(intent); // ON AFFICHE L'ACTIVITE ABOUT
            }
        });

        //
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent); // ON REVIENT SUR LA PAGE LOGIN
            }
        });

        backupSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean testInbox, testSent;
                //ConnectToAPI connectToAPI = new ConnectToAPI();

                // ON CREE DEUX LISTES DE TYPE SMS
                ArrayList<SMS> SmsInbox = new ArrayList();
                ArrayList<SMS> SmsSent = new ArrayList();

                // ON CREE LES URI POUR INBOX ET SENT
                Uri uriInBox = Uri.parse("content://sms/inbox");
                Uri uriSent = Uri.parse("content://sms/sent");

                // ON DECLARE DEUX CURSEURS
                Cursor cursorInbox = getContentResolver().query(uriInBox, new String[]{"_id","address","date", "body"}, null, null, null);
                Cursor cursorSent = getContentResolver().query(uriSent, new String[]{"_id","address","date", "body"}, null, null, null);

                testInbox = cursorInbox.getCount() > 0;
                testSent = cursorSent.getCount() > 0;

                // ON REMONTE DANS LA LISTE
                cursorInbox.moveToFirst();
                cursorSent.moveToFirst();

                // GOOGLE  GSON (LIBRAIRIE EXTERNE)
                Gson gson = new Gson();

                if(testInbox) { // S'IL A DES SMS DANS LA INBOX
                    while (cursorInbox.moveToNext()){ // TANT QU'IL Y A DES SMS, ON RECUPERE L'ADRESSE ET LE BODY
                        String address = cursorInbox.getString(1);
                        String body = cursorInbox.getString(3);
                        // ON LES AJOUTE DANS L'OBJET
                        SmsInbox.add(new SMS(address, body));
                    }

                    // ON CONVERTIT L'OBJET  SmsInbox EN JSON
                    jsonSmsInbox = gson.toJson(SmsInbox);

                    Log.d("debug: Number of SmsInbox", String.valueOf(SmsInbox.size()));
                    Log.d("debug gson", String.valueOf(jsonSmsInbox));
                    //connectToAPI.BackupSmsConnection(loginGetStr,passwordGetStr, MainActivity.this,jsonSmsInbox,"inbox");
                }

                // MEME PRINCIPE QU'AU DESSUS
                if(testSent) {
                    while (cursorSent.moveToNext()){
                        String address = cursorSent.getString(1);
                        String body = cursorSent.getString(3);

                        SmsSent.add(new SMS(address, body));
                    }
                    jsonSmsSent = gson.toJson(SmsSent);
                    Log.d("debug: Number of SMS SENT", String.valueOf(SmsSent.size()));
                    //connectToAPI.BackupSmsConnection(loginGetStr, passwordGetStr, MainActivity.this, jsonSmsSent, "sent");
                }

                // ON EXECUTE LE BACKUP EN BACKGROUND (VOIR LA CLASSE TOUT EN BAS)
                BackupSmsConnectionData backupSmsConnectionData = new BackupSmsConnectionData();
                backupSmsConnectionData.execute();

                Toast.makeText(getApplicationContext(), "Synchronization of messages...", Toast.LENGTH_SHORT).show();

                backupSmsConnectionData.cancel(true);
                cursorInbox.close();
                cursorSent.close();
                //Toast.makeText(getApplicationContext(),httpResponseINBOX , Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), httpResponseSENT, Toast.LENGTH_LONG).show();
            }
        });

        backupContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Gson gson = new Gson();
                ArrayList<Contact> contactArrayList = new ArrayList();

                //contacts
                Boolean testContacts;
                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String idPhone = ContactsContract.CommonDataKinds.Phone._ID;
                String displayNamePhone = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
                String numberPhone = ContactsContract.CommonDataKinds.Phone.NUMBER;
                Cursor cursorContacts = getContentResolver().query(uriPhone, new String[]{idPhone, displayNamePhone,numberPhone}, null, null, displayNamePhone + " ASC");

                //mails addresses
                Boolean testMails;
                Uri uriMail = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
                String idMailContact = ContactsContract.CommonDataKinds.Email._ID;
                String displayNameEmailContact = ContactsContract.CommonDataKinds.Email.DISPLAY_NAME;
                String emailContact = ContactsContract.CommonDataKinds.Email.ADDRESS;
                Cursor cursorMailContacts = getContentResolver().query(uriMail, new String[]{idMailContact,displayNameEmailContact,emailContact}, null, null, displayNameEmailContact+ " ASC");

                testContacts = cursorContacts.getCount() > 0;
                testMails = cursorMailContacts.getCount() > 0;
                //Integer nbContacts = managedCursor.getCount(); TO KNOW HOW MANY CONTACTS (PHONE NUMBER) ARE INTO THE CELL PHONE
                //Integer nbContactForEmail = cursorMailContacts.getCount(); TO KNOW HOW MANY CONTACTS (EMAIL ADDRESSES) ARE INTO THE CELL PHONE


                cursorContacts.moveToFirst();
                cursorMailContacts.moveToFirst();
                if (testContacts && testMails) {

                    for (Integer i = 1; i <= cursorContacts.getCount(); i++) {
                        Integer idContact = Integer.parseInt(cursorContacts.getString(0));
                        String nameContact = cursorContacts.getString(1);
                        String phoneContact = cursorContacts.getString(2);
                        String mailContact = null; //IF THERE'S NOT A MAIL ADDRESS ATTACHED WITH THE CONTACT
                        Integer idEmailContact = Integer.parseInt(cursorMailContacts.getString(0));
                        String nameMailContact = cursorMailContacts.getString(1);// TEST FOR SEEING WHAT'S THE NAME OF THE CONTACT => NULL
                        String mailContactTest = cursorMailContacts.getString(2); //TEST FOR SEEING THE CONTENT OF MAIL ADDRESS

                        if ( ( (idContact+1) == idEmailContact ) && ( cursorMailContacts.getString(2) != null ) )
                                mailContact = cursorMailContacts.getString(2);

                        cursorContacts.moveToNext();
                        cursorMailContacts.moveToNext();
                        contactArrayList.add(new Contact(idContact, nameContact, phoneContact, mailContact));
                        //contactArrayList.add(new Contact(idContact, nameContact, phoneContact, null));
                    }
                    jsonContacts = gson.toJson(contactArrayList);
                    Log.d("debug: Number of CONTACTS", String.valueOf(contactArrayList.size()));
                }
                BackupContactConnectionData backupContactConnectionData = new BackupContactConnectionData();
                backupContactConnectionData.execute();
                Toast.makeText(getApplicationContext(), "Synchronization of contacts...", Toast.LENGTH_SHORT).show();
                backupContactConnectionData.cancel(true);
                cursorContacts.close();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class BackupSmsConnectionData extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            Looper.prepare();

            final String loginGetStr , passwordGetStr;

            Bundle bundleForMainActivity = getIntent().getExtras();
            loginGetStr = bundleForMainActivity.getString("loginGetStr");
            passwordGetStr = bundleForMainActivity.getString("passwordGetStr");

            ConnectToAPI connectToAPI = new ConnectToAPI();
            connectToAPI.BackupSmsConnection("backupsms", loginGetStr, passwordGetStr, MainActivity.this, jsonSmsInbox, "inbox");
            httpResponseINBOX = connectToAPI.httpResponse;

            connectToAPI.BackupSmsConnection("backupsms", loginGetStr, passwordGetStr, MainActivity.this, jsonSmsSent, "sent");
            httpResponseSENT = connectToAPI.httpResponse;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("debug HttpResponse 1", "httpResponseINBOX = " + httpResponseINBOX);
                    Log.d("debug HttpResponse 2", "httpResponseSENT = " + httpResponseSENT);
                    Toast.makeText(getApplicationContext(), "The backup is done successfully", Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), "HTTPRESPONSE SENT" + httpResponseSENT, Toast.LENGTH_LONG).show();
                }
                // CLOSE ASYNC TASK
            });
            Looper.loop();
            return null;
        }
    }

    class BackupContactConnectionData extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Looper.prepare();

            final String loginGetStr , passwordGetStr;

            Bundle bundleForMainActivity = getIntent().getExtras();
            loginGetStr = bundleForMainActivity.getString("loginGetStr");
            passwordGetStr = bundleForMainActivity.getString("passwordGetStr");

            ConnectToAPI connectToAPI = new ConnectToAPI();
            connectToAPI.BackupContactsConnection("backupcontacts", loginGetStr, passwordGetStr, MainActivity.this, jsonContacts);
            httpResponseCONTACTS = connectToAPI.httpResponse;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("debug HttpResponse", "httpResponseCONTACTS = " + httpResponseCONTACTS);
                    Toast.makeText(getApplicationContext(), "The backup is done successfully" , Toast.LENGTH_LONG).show();
                }
                // CLOSE ASYNC TASK

            });
            Looper.loop();
            return null;
        }
    }
}
