package com.supinfo.boutrig.supsms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.supinfo.boutrig.supsms.model.ConnectToAPI;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {

    private TextView loginGet;
    private TextView passwordGet;
    private String loginGetStr;
    private String passwordGetStr;
    private Context context;
    //private boolean isConnected;
    private ProgressDialog progressDialog;
    private ConnectToAPI connectToAPI = new ConnectToAPI();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this; // ON DECLARE LE CONTEXT ACTUEL

        progressDialog = new ProgressDialog(LoginActivity.this);

        Button logingButton = (Button) findViewById(R.id.loginButton); // ON RECUPERE LE BOUTON LOGIN
        logingButton.setOnClickListener(new View.OnClickListener() {    // ON LANCE LE LISTENER DE L'EVENEMENT OnClick
            @Override
            public void onClick(View v) {
                // ON RECUPERE LE LOGIN ET LE PASSWORD ENTRES PAR L'UTILISATEUR
                // ET ON LES PARSE EN STRING
                loginGet = (TextView) findViewById(R.id.loginText);
                passwordGet = (TextView) findViewById(R.id.passwordText);
                loginGetStr = loginGet.getText().toString();
                passwordGetStr = passwordGet.getText().toString();

                // ON INSTANCIE LA CLASSE ConnectionData ET ON ETABLIE LA CONNEXION
                ConnectionData connectionData = new ConnectionData();
                connectionData.execute();
                // ON FERME LA CONNEXION
                connectionData.cancel(true);
            }
        });
    }

    //ANCIENNE METHODE POUR SE "LOGUER"
/**
    public void connectionPost(){

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://91.121.105.200/API/");

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(3);
            nameValuePairs.add(new BasicNameValuePair("action", "login"));
            nameValuePairs.add(new BasicNameValuePair("login", ""+loginGetStr+""));
            nameValuePairs.add(new BasicNameValuePair("password",""+passwordGetStr+""));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            ResponseHandler<String> stringResponseHandler = new BasicResponseHandler();
            String httpResponse = httpClient.execute(httpPost,stringResponseHandler);

            if (httpResponse.contains("\"success\":true,")){
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("loginGetStr", loginGetStr);
                intent.putExtra("passwordGetStr", passwordGetStr);
                startActivity(intent);
                isConnected = true;
            }else{
                isConnected = false;
            }
        }catch (ClientProtocolException e){
            Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // LORSQUE ON SE "LOGOUT" ON APPUIS SUR LE BOUTON "ARRIERE" ON RETURNE VERS LE "BUREAU"
    @Override
    public void onBackPressed(){ moveTaskToBack(true); }


    // CLASSE QUI NOUS PERMET D'EXECUTER DES ACTIONS EN TACHE EN FOND
    // AFIN DE NE PAS BLOQUER L'APPLICATION
    class ConnectionData extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute(){  // ON AFFICHE UNE ProgressDialog EN PRE-EXECUTION
            progressDialog.show(LoginActivity.this, "Please wait...", "Connecting to the API...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // INITIALISATION DU THREAD ACTUEL EN TANT QUE LOOPER,
            // ET ON CREE LE HANDLER QUI PERMET DE GERER LE LOOPER
            Looper.prepare();
            // ON ETABLIE LA CONNEXION POUR SE LOGUER
            connectToAPI.LoginConnection(loginGetStr,passwordGetStr,LoginActivity.this);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!connectToAPI.isConnected) { // SI LE MOT DE PASSE EST FAUX
                        // ON AFFICHE UN MESSAGE
                        Toast.makeText(context, "Login or password is wrong /!\\", Toast.LENGTH_LONG).show();

                        // ON ARRETE LE LOADER
                        progressDialog.dismiss();
                        progressDialog.cancel();

                        // ON REDEMARRE L'ACTIVITE  LOGIN
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(context, "Welcome !", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });

            //progressDialog.cancel();
            //connectionData.cancel(true);
            //Looper.loop();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            //progressDialog.dismiss();
            //progressDialog.cancel();
        }
    }
}
