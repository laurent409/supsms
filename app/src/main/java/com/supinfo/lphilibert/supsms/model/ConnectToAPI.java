package com.supinfo.boutrig.supsms.model;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.Players;
import com.google.gson.Gson;
import com.supinfo.boutrig.supsms.LoginActivity;
import com.supinfo.boutrig.supsms.MainActivity;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class ConnectToAPI {

    public boolean isConnected;
    public boolean isSaved;
    public String httpResponse;


    // METHODE QUI ETABLIE LA CONNEXION POUR FAIRE LA BACKUP DES SMS
    public void BackupSmsConnection(String typeAction, String loginGetStr, String passwordGetStr, Activity activity, String gsonSMS, String box){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://91.121.105.200/API/");

            List<NameValuePair> nameValuePairs = new ArrayList<>(5);    // ON CREE UN TABLEAU A DEUX ENTREES
            nameValuePairs.add(new BasicNameValuePair("action", typeAction));
            nameValuePairs.add(new BasicNameValuePair("login", loginGetStr));
            nameValuePairs.add(new BasicNameValuePair("password", passwordGetStr));
            nameValuePairs.add(new BasicNameValuePair("box", box));
            nameValuePairs.add(new BasicNameValuePair("sms", gsonSMS));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs)); // ON ENVOIE A L'API

            ResponseHandler<String> stringResponseHandler = new BasicResponseHandler();
            httpResponse = httpClient.execute(httpPost,stringResponseHandler);  // ON RECUPERE LA REPONSE DE L'API

            if (httpResponse.contains("\"success\":true,")){    // ON VERIFIE LA REPONSE DE L'API
                Log.d("CheckSuccess: ", httpResponse);
                isSaved = true;
            }else{
                isSaved = false;
            }

        }catch (Exception e){
            Log.d("BackupSmsConnection ERROR", String.valueOf(e)); // ON AFFICHE L'EXCEPTION
            //Log.d("ERROR HTTPRESPONSE: ", String.valueOf(httpResponse) );
        }
    }

    // METHODE QUI ETABLIE LA CONNEXION POUR LE LOGIN
    public void LoginConnection(String loginGetStr, String passwordGetStr, Activity activity){

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://91.121.105.200/API/");

            List<NameValuePair> nameValuePairs = new ArrayList<>(3);
            nameValuePairs.add(new BasicNameValuePair("action", "login"));
            nameValuePairs.add(new BasicNameValuePair("login", ""+loginGetStr+""));
            nameValuePairs.add(new BasicNameValuePair("password",""+passwordGetStr+""));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            ResponseHandler<String> stringResponseHandler = new BasicResponseHandler();
            String httpResponseLog = httpClient.execute(httpPost,stringResponseHandler);
            Log.d("HttpresponseLogin", httpResponseLog);

            if (httpResponseLog.contains("\"success\":true,")){
                Intent intent = new Intent(activity, MainActivity.class); // ON CREE UN INTENT POUR
                intent.putExtra("loginGetStr", loginGetStr);              // ENVOYER LE CONTENU DES
                intent.putExtra("passwordGetStr", passwordGetStr);        // TEXTVIEUWS A L'ACTIVITY MAIN
                activity.startActivity(intent); // ON LANCE L'ACTIVITY MAIN UNE FOIS L'UTILISATEUR S'EST "LOGUE"
                isConnected = true;
            }else{
                isConnected = false;
            }

        }catch (ClientProtocolException e){
            Toast.makeText(activity.getApplicationContext(), "Error LOGIN 1: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("Erreur 1: ", e.getMessage());
        }catch (Exception e){
            Toast.makeText(activity.getApplicationContext(),"Error LOGIN 2" + e, Toast.LENGTH_LONG).show();
            Log.d("Erreur 1: ", e.getMessage());
        }
    }


    // METHODE QUI ETABLIE LA CONNEXION AVEC L'API AFIN DE FAIRE LA BACKUP DES CONTACTS
    public void BackupContactsConnection(String typeAction, String loginGetStr, String passwordGetStr, Activity activity, String gsonContacts){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://91.121.105.200/API/");

            List<NameValuePair> nameValuePairs = new ArrayList<>(4);
            nameValuePairs.add(new BasicNameValuePair("action", "backupcontacts"));
            nameValuePairs.add(new BasicNameValuePair("login", ""+loginGetStr+""));
            nameValuePairs.add(new BasicNameValuePair("password",""+passwordGetStr+""));

            // For backup Contacts

            nameValuePairs.add(new BasicNameValuePair("contacts", gsonContacts));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            ResponseHandler<String> stringResponseHandler = new BasicResponseHandler();
            httpResponse = httpClient.execute(httpPost,stringResponseHandler);

            if (httpResponse.contains("\"success\":true,")){
                Log.d("CheckSuccess: ", httpResponse);
                isSaved = true;
            }else{
                isSaved = false;
            }

        }catch (ClientProtocolException e){
            Toast.makeText(activity.getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("Erreur 1: ", e.getMessage());
        }catch (Exception e){
            Toast.makeText(activity.getApplicationContext(),"Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("Erreur 2 : ", e.getMessage());
        }
    }

}
