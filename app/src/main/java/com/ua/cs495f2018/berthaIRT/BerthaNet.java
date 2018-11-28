package com.ua.cs495f2018.berthaIRT;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoServiceConstants;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ua.cs495f2018.berthaIRT.dialog.WaitDialog;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.ua.cs495f2018.berthaIRT.Client.aesDecrypter;
import static com.ua.cs495f2018.berthaIRT.Client.aesEncrypter;
import static com.ua.cs495f2018.berthaIRT.Client.rsaDecrypter;
import static com.ua.cs495f2018.berthaIRT.CognitoNet.session;


public class BerthaNet {
    static final String ip = "http://54.236.113.200";

    //Utilities for converting objects to server-friendly JSONs
    public JsonParser jp;
    public Gson gson;

    //Volley RequestQueue
    RequestQueue netQ;

    public BerthaNet(Context c) {
        jp = new JsonParser();
        gson = new Gson();
        netQ = Volley.newRequestQueue(c);
    }

    //Basic network HTTP request.
    //secureSend will call this function with strings already encrypted.
    //If the user is logged in, their JWT is attached to the Authentication header.
    //Only one string is sent as the body.  Up to calling functions to parse JSON / map values
    private void netSend(Context ctx, String path, final String body, final Interface.WithStringListener callback) {
        StringRequest req = new StringRequest(Request.Method.PUT, ip.concat(path), callback::onEvent, error -> {
            Toast.makeText(ctx, error.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println(error.getMessage());
        }) {
            @Override
            public byte[] getBody(){
                return body.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (session != null) {
                    Map<String, String> header = new HashMap<>();
                    header.put("Authentication", Util.asHex(session.getIdToken().getJWTToken().getBytes()));
                    return header;
                }
                return super.getHeaders();
            }
        };
        netQ.add(req);
    }

    //Secured network HTTP request.  Must be logged in with initialized ciphers.
    private void secureSend(Context ctx, String path, final String params, final Interface.WithStringListener callback) {
        Interface.WithStringListener wrapper = response -> {
            //Result will be hex-encoded for URL safety and encrypted with AES for security
            try {
                //Decode hex into bytes
                byte[] encrypted = Util.fromHexString(response);
                //Use cipher to decrypt bytes
                String decrypted = new String(aesDecrypter.doFinal(encrypted));
                System.out.println("Decrypted: " + decrypted);
                //Do the original callback with the decrypted string
                callback.onEvent(decrypted);
            } catch (Exception e) {
                System.out.println("Unable to decrypt server response!");
                e.printStackTrace();
            }
        };
        //Encrypt the data
        byte[] encrypted = new byte[0];
        try {
            encrypted = aesEncrypter.doFinal(params.getBytes());
        } catch (Exception e) {
            System.out.println("Unable to encrypt data packet!");
            e.printStackTrace();
        }
        //Data is byte code which won't translate well when sending over URL, so hex-encode it
        String encoded = Util.asHex(encrypted);
        //Send encrypted string as normal, with the wrapper as callback to decrypt response
        netSend(ctx, path, encoded, wrapper);
    }

    //We need to recieve an AES key from the server in order to encrypt our requests.
    //When this is called, an RSA key will have been made and updated to Cognito user attributes.
    public void recieveAESKey(Context ctx, Interface.WithGenericListener callback) {
        //Nothing needs to be sent since all used info is included in JWT, automatically added in netSend
        netSend(ctx, "/keys/issue", "", r -> {
            try {
                //AES keys come in two parts, the key itself, and initialization vectors
                JsonObject jay = jp.parse(r).getAsJsonObject();
                String encodedKey = jay.get("key").getAsString();
                String encodedIv = jay.get("iv").getAsString();

                //Response is hex-encoded for URL safety, so decode to byte
                byte[] decodedKey = Util.fromHexString(encodedKey);
                byte[] decodedIv = Util.fromHexString(encodedIv);

                //Now use RSA cipher to decrypt the AES key
                byte[] decryptedKey = rsaDecrypter.doFinal(decodedKey);
                byte[] decryptedIv = rsaDecrypter.doFinal(decodedIv);

                //Use data to make a new SecretKeySpec
                IvParameterSpec iv = new IvParameterSpec(decryptedIv);
                SecretKeySpec spec = new SecretKeySpec(decryptedKey, "AES");

                //Initialize AES ciphers.  Will be used for all further secure communication.//
                Cipher encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
                Cipher decrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
                encrypter.init(Cipher.ENCRYPT_MODE, spec, iv);
                decrypter.init(Cipher.DECRYPT_MODE, spec, iv);

//                secureSend(ctx, "/keys/verify", "", rr->{
//                    if(rr.equals("SECURE")) callback.onEvent(new Cipher[]{encrypter, decrypter});
//                });
                callback.onEvent(new Cipher[]{encrypter, decrypter});
            } catch (Exception e) {
                System.out.println("Unable to initialize AES ciphers!");
                e.printStackTrace();
            }
        });
    }

    public void lookupGroup(Context ctx, String groupID, Interface.WithVoidListener callback){
        netSend(ctx, "/group/info", groupID, r->{
            JsonObject jay = jp.parse(r).getAsJsonObject();
            Client.userGroupName = jay.get("groupName").getAsString();
            if(!Client.userGroupName.equals("NONE"))
                Client.userGroupStatus = jay.get("groupStatus").getAsString();
            callback.onEvent();
        });
    }

    public void pullAll(Context ctx, Interface.WithVoidListener callback) {
        secureSend(ctx, "/group/reports", "", r->{
            JsonArray idList = jp.parse(r).getAsJsonArray();
            pullReports(ctx, idList,
                    ()->pullAlerts(ctx, callback)
            );
        });
    }

    public void pullReports(Context ctx, JsonArray ids, Interface.WithVoidListener callback){
        for(int i=ids.size()-1; i>=0; i--){
            Integer reportID = ids.get(i).getAsInt();
            secureSend(ctx, "/report/pull", reportID.toString(), r->{
                Report report =  gson.fromJson(r, Report.class);
                Client.reportMap.put(reportID, report);
                if(Client.activeReport != null && Client.activeReport.getReportID().equals(reportID)) Client.activeReport = report;
            });
        }
        callback.onEvent();
    }

    public void pullAlerts(Context ctx, Interface.WithVoidListener callback){
        secureSend(ctx, "/group/alert/pull", "", rr->{
            JsonArray alertList = jp.parse(rr).getAsJsonArray();
            Client.alertList = new ArrayList<>();
            for(JsonElement e : alertList) Client.alertList.add(gson.fromJson(e.getAsString(), Message.class));
            callback.onEvent();
        });
    }

//    public void sendNewReport(Context ctx, Interface.WithVoidListener callback){
//        //todo: this might be dangerous with the refresh function running idk check this part out if shit blows up ya know
//        secureSend(ctx, "/report/create", gson.toJson(Client.activeReport), r->{
//            Client.activeReport = Client.net.gson.fromJson(r, Report.class);
//            Client.reportMap.put(Client.activeReport.getReportID(), Client.activeReport);
//            callback.onEvent();
//        });
//    }

    public void syncActiveReport(Context ctx, Interface.WithVoidListener callback){
        //todo: this might be dangerous with the refresh function running idk check this part out if shit blows up ya know
        WaitDialog d = new WaitDialog(ctx);
        d.show();
        String path = "/report/update";
        if(Client.activeReport.getReportID() == null) path = "/report/create";
        secureSend(ctx, path, gson.toJson(Client.activeReport), r->{
            Client.activeReport = Client.net.gson.fromJson(r, Report.class);
            Client.reportMap.put(Client.activeReport.getReportID(), Client.activeReport);
            d.dismiss();
            callback.onEvent();
        });
    }

    public void toggleRegistration(Context ctx, Interface.WithStringListener callback){
        secureSend(ctx, "/group/togglestatus", "", callback);
    }

    public void dismissAlert(Context ctx, Integer messageID, Interface.WithVoidListener callback) {
        secureSend(ctx, "/group/alert/dismiss", messageID.toString(), r->callback.onEvent());
    }

    public void createGroup(Context ctx, String email, String institution, Interface.WithVoidListener callback) {
        JsonObject req = new JsonObject();
        req.addProperty("newAdmin", email);
        req.addProperty("groupName", institution);

        netSend(ctx, "/group/create", req.toString(), r -> {
            if (r.equals(email)) callback.onEvent();
        });
    }

    public void joinGroup(Context ctx, String groupID, Interface.WithVoidListener callback) {
        netSend(ctx, "/group/join", groupID, (r) ->
                Client.performLogin(ctx, r, "BeRThAfirsttimestudent", x -> {
                    //Login successful and details stored - launch main activity
                    if (x.equals("SECURE"))
                        callback.onEvent();
                }));
    }
}