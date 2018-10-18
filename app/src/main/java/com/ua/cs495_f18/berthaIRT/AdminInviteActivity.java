package com.ua.cs495_f18.berthaIRT;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AdminInviteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_invite);

        final Button buttonSubmitKey = (Button) findViewById(R.id.button_admin_submitkey);
        buttonSubmitKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSubmitKey();
            }
        });
    }

    private void actionSubmitKey() {
        StaticUtilities.hideSoftKeyboard(AdminInviteActivity.this);
        final EditText inputEmail = findViewById(R.id.input_admin_invite_email); //check
        EditText inputConfirm = findViewById(R.id.input_admin_invite_confirm); //TODO
        AlertDialog.Builder b = new AlertDialog.Builder(AdminInviteActivity.this);
        b.setTitle("Success");
        b.setMessage("An email has been sent to " + inputEmail + ".  The new administrator will log in with the supplied credentials.");
        b.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(AdminInviteActivity.this, AdminPortalActivity.class));
                        finish();
                    }
                });
        AlertDialog confirmationDialog = b.create();
        confirmationDialog.show();

    }
}