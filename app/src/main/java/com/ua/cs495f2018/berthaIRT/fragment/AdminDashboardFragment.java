package com.ua.cs495f2018.berthaIRT.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ua.cs495f2018.berthaIRT.AdminLoginActivity;
import com.ua.cs495f2018.berthaIRT.Client;
import com.ua.cs495f2018.berthaIRT.Interface;
import com.ua.cs495f2018.berthaIRT.MetricsActivity;
import com.ua.cs495f2018.berthaIRT.R;
import com.ua.cs495f2018.berthaIRT.adapter.AddRemoveAdapter;
import com.ua.cs495f2018.berthaIRT.dialog.AddRemoveDialog;
import com.ua.cs495f2018.berthaIRT.dialog.InputDialog;
import com.ua.cs495f2018.berthaIRT.dialog.YesNoDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminDashboardFragment extends Fragment {
    View view;
    AddRemoveDialog d;

    public AdminDashboardFragment(){

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater flater, ViewGroup tainer, Bundle savedInstanceState){
        view = flater.inflate(R.layout.fragment_admin_dashboard, tainer, false);

        view.findViewById(R.id.dashboard_button_metrics).setOnClickListener(v1 ->
                startActivity(new Intent(getActivity(), MetricsActivity.class)));
//
        //not a user attribute
//        view.findViewById(R.id.dashboard_button_editinstitutionname).setOnClickListener(v1 ->
//                Util.showInputDialog(getContext(),"Your Institution Name", null, Client.currentUserGroupID,"Update", x-> actionUpdateAttribute("institution", x)) );

//        view.findViewById(R.id.dashboard_button_editemblem).setOnClickListener(v1 -> actionEditEmblem());
//
        //sets the text for registration
        if(Client.userGroupStatus.equals("Closed"))
            ((TextView) view.findViewById(R.id.dashboard_button_registration)).setText(R.string.open_registration);

        //if you toggle registration
        view.findViewById(R.id.dashboard_button_registration).setOnClickListener(v1 -> actionToggleRegistration());

        //if you edit admin name
        view.findViewById(R.id.dashboard_button_editmyname).setOnClickListener(v1 ->{
                    InputDialog d = new InputDialog(getContext(),"Your Full Name", "", x -> Client.cogNet.updateCognitoAttribute("name", x, ()-> Toast.makeText(getContext(), "Update successful.", Toast.LENGTH_SHORT).show()));
                    d.show();
                    ((TextView) Objects.requireNonNull(d.findViewById(R.id.inputdialog_input))).setHint(Client.userAttributes.get("name"));
        });

//        view.findViewById(R.id.dashboard_button_resetpassword).setOnClickListener(v1 ->
//                new YesNoDialog(getActivity(), "Are you sure?", "A temporary code for you to reset your password will be sent to your email and you will be logged out.", new Interface.YesNoHandler() {
//                    @Override
//                    public void onYesClicked() { actionResetPassword(); }
//                    @Override
//                    public void onNoClicked() { }
//                }).show());

        //if you logout
        view.findViewById(R.id.dashboard_button_logout).setOnClickListener(v1 ->
                new YesNoDialog(getActivity(),"Are you sure you want to Logout?", "", new Interface.YesNoHandler() {
                    @Override
                    public void onYesClicked() { actionLogOut(); }
                    @Override
                    public void onNoClicked() { }
                }).show());

        //view.findViewById(R.id.dashboard_button_addremoveadmin).setOnClickListener(v1 -> actionAddRemoveAdmin());

        //set up the info at the top of the dashboard
        ((TextView) view.findViewById(R.id.dashboard_alt_name)).setText(Client.userAttributes.get("name"));
        ((TextView) view.findViewById(R.id.dashboard_alt_institution)).setText(Client.userGroupName);
        ((TextView) view.findViewById(R.id.dashboard_alt_accesscode)).setText(Client.userAttributes.get("custom:groupID"));
        return view;
    }

    public void actionToggleRegistration() {
        Client.net.toggleRegistration(getContext(), (r)->{
            Toast.makeText(getContext(), "Registration set to " + r, Toast.LENGTH_SHORT).show();
            if(r.equals("Closed")) ((TextView) view.findViewById(R.id.dashboard_button_registration)).setText(R.string.open_registration);
            else ((TextView) view.findViewById(R.id.dashboard_button_registration)).setText(R.string.close_registration);
            Client.userGroupStatus = r;
        });
    }

//    //Currently working on
//    private void actionChangeRegistration() {
//        TextView tvRegistration = view.findViewById(R.id.dashboard_button_registration);
//        String message = "You are about to CLOSE your group to new members.  No one may use your institution's access code until you reopen.";
//        if(tvRegistration.getText() == "Open Registration") message = "You are about to OPEN your group to new members and your access code will become active.";
//        new YesNoDialog(getActivity(),"Changing Registration", message, new Interface.YesNoHandler() {
//            @Override
//            public void onYesClicked() { toggleRegistration(); }
//            @Override
//            public void onNoClicked() { }
//        }).show();
//    }
//
//    private void toggleRegistration() {
//        /*Client.net.secureSend("admin/toggleregistration", null, (r)->{
//            if(r.equals("Closed"))
//                ((TextView) view.findViewById(R.id.dashboard_button_registration)).setText("Open Registration");
//            else
//                ((TextView) view.findViewById(R.id.dashboard_button_registration)).setText("Close Registration");
//
//        });*/
//    }
//
//
//    private void actionChangeInstitutionName(String s) {
//        //TODO change on server
//        Toast.makeText(getActivity(),"Inst name " + s, Toast.LENGTH_SHORT).show();
//    }
//
//    private void actionEditEmblem() {
//        //TODO change on server
//        Toast.makeText(getActivity(),"Emblem", Toast.LENGTH_SHORT).show();
//    }

    private void actionLogOut(){
        startActivity(new Intent(getActivity(), AdminLoginActivity.class));
        Objects.requireNonNull(getActivity()).finish();
    }

//    private void actionResetPassword() {
//
//    }

/*    private void actionAddRemoveAdmin() {
        //Get the admins and display dialog
        List<String> admins = new ArrayList<>();
        Client.net.pullAdmins(getContext(),()-> {
            d = new AddRemoveDialog(getActivity(), Client.adminsList, this::actionAddAdmin, this::actionRemoveAdmin, null);
            d.show();
            ((EditText) Objects.requireNonNull(d.findViewById(R.id.addremove_input))).setHint("Admin Email");
        });
    }

    private void actionAddAdmin(String admin) {
        new YesNoDialog(getActivity(), "Are you sure? ", "About to add " + admin + " as an Admin?", new Interface.YesNoHandler() {
            @Override
            public void onYesClicked() {
                Client.net.secureSend(getContext(), "/group/join/admin", admin, x->
                        ((AddRemoveAdapter) ((RecyclerView) Objects.requireNonNull(d.findViewById(R.id.addremove_rv))).getAdapter()).addToList(admin));
            }

            @Override
            public void onNoClicked() {
            }
        }).show();
    }

    private void actionRemoveAdmin(String admin) {
        new YesNoDialog(getActivity(),"Are you sure?", "About to remove " + admin + " as an Admin?", new Interface.YesNoHandler() {
            @Override
            public void onYesClicked() {
                Client.net.secureSend(getContext(), "/group/remove/admin", admin,null);
            }

            @Override
            public void onNoClicked() {
                //add that admin back to the list
                ((AddRemoveAdapter) ((RecyclerView) Objects.requireNonNull(d.findViewById(R.id.addremove_rv))).getAdapter()).addToList(admin);
            }
        }).show();
    }*/
}