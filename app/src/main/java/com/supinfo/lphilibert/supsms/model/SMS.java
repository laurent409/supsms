package com.supinfo.boutrig.supsms.model;

/**
 * Created by Massinissa on 28/01/2015.
 */
public class SMS {
    private String body;
    private String adress;

    public SMS(String strAdress, String strBody){
        adress = strAdress;
        body = strBody;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }
}
