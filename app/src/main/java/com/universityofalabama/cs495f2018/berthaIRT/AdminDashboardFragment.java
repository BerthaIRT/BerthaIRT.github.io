package com.universityofalabama.cs495f2018.berthaIRT;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class AdminDashboardFragment extends Fragment {
    View view;

    public AdminDashboardFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater flater, ViewGroup tainer, Bundle savedInstanceState){
        view = flater.inflate(R.layout.fragment_admin_dashboard, tainer, false);

        view.findViewById(R.id.dashboard_button_metrics).setOnClickListener(v1 ->
                startActivity(new Intent(getActivity(), MetricsActivity.class)));

        view.findViewById(R.id.dashboard_button_editinstitutionname).setOnClickListener(v1 -> actionEditInstitutionName());

        view.findViewById(R.id.dashboard_button_editemblem).setOnClickListener(v1 -> actionEditEmblem());

        view.findViewById(R.id.dashboard_button_registration).setOnClickListener(v1 -> actionChangeRegistration());

        view.findViewById(R.id.dashboard_button_editmyname).setOnClickListener(v1 -> actionChangeMyName());

        view.findViewById(R.id.dashboard_button_resetpassword).setOnClickListener(v1 -> actionChangePassword());

        view.findViewById(R.id.dashboard_button_logout).setOnClickListener(v1 -> actionLogOut());

        view.findViewById(R.id.dashboard_button_inviteadmin).setOnClickListener(v1 -> actionInviteNewAdmin());

        view.findViewById(R.id.dashboard_button_removeadmin).setOnClickListener(v1 -> actionRemoveAdmin());

        return view;
    }

    private void actionEditInstitutionName() {
        Toast.makeText(getActivity(),"Inst name", Toast.LENGTH_SHORT).show();
    }

    private void actionEditEmblem() {
        Toast.makeText(getActivity(),"Emblem", Toast.LENGTH_SHORT).show();
    }

    //Currently working on
    private void actionChangeRegistration() {
        TextView tvRegistration = view.findViewById(R.id.dashboard_button_registration);
        String message = "You are about to CLOSE your group to new members.  No one may use your institution's access code until you reopen.";
        if(tvRegistration.getText() == "Open Registration") message = "You are about to OPEN your group to new members and your access code will become active.";
        Util.showYesNoDialog(getActivity(),"Changing Registration", message,
                "Confirm", "Cancel", this::toggleRegistration, null);

    }

    private void toggleRegistration() {
        Client.net.secureSend("admin/toggleregistration", null, (r)->{
            if(r.equals("Closed"))
                ((TextView) view.findViewById(R.id.dashboard_button_registration)).setText("Open Registration");
            else
                ((TextView) view.findViewById(R.id.dashboard_button_registration)).setText("Close Registration");

        });
    }

    private void actionChangeMyName() {
        Toast.makeText(getActivity(),"Name", Toast.LENGTH_SHORT).show();
    }

    private void actionChangePassword() {
        Toast.makeText(getActivity(),"Pass", Toast.LENGTH_SHORT).show();
    }

    private void actionLogOut() {
        Util.showYesNoDialog(getActivity(),"Are you sure you want to Logout?", "",
                "Logout", "Cancelt", this::logOut, null);
    }

    private void logOut(){
        //TODO delete from shared preferences

        startActivity(new Intent(getActivity(), AdminLoginActivity.class));
        getActivity().finish();
    }

    private void actionInviteNewAdmin() {
        Toast.makeText(getActivity(),"Invite", Toast.LENGTH_SHORT).show();
    }

    private void actionRemoveAdmin() {
        Toast.makeText(getActivity(),"Remove", Toast.LENGTH_SHORT).show();
    }
}
