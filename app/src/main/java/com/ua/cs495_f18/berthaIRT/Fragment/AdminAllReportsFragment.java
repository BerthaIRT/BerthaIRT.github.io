package com.ua.cs495_f18.berthaIRT.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ua.cs495_f18.berthaIRT.R;
import com.ua.cs495_f18.berthaIRT.Adapter.AdminReportCardAdapter;
import com.ua.cs495_f18.berthaIRT.ReportObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AdminAllReportsFragment extends Fragment {

    SwipeRefreshLayout swipeContainer;
    View v;
    private RecyclerView recyclerView;
    private List<ReportObject> reportList = new ArrayList<>();
    private String filter = "";

    public AdminAllReportsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_admin_all_reports, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.view_fragment_admin_all_reports);
        AdminReportCardAdapter recyclerViewAdapter = new AdminReportCardAdapter(getContext(),reportList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.fragment_all_reports);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                populateFragment();
                if(swipeContainer.isRefreshing())
                    swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateFragment();
    }

    private void populateFragment() {
        //get the current Date & time
        String date = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm", Locale.getDefault()).format(new Date());

        //if there is no filter then populate everything
        if (filter.equals("")) {
            reportList.clear();
            reportList.add(new ReportObject("1111111", "Bullying", date, time, "Open"));
            reportList.add(new ReportObject("3333333", "Cheating", date, time, "Open"));
            reportList.add(new ReportObject("6124511", "Cyberbullying", date, time, "Open"));
            reportList.add(new ReportObject("1111111", "Bullying", date, time, "Open"));
            reportList.add(new ReportObject("3333333", "Cheating", date, time, "Open"));
            reportList.add(new ReportObject("6124511", "Cyberbullying", date, time, "Open"));
            reportList.add(new ReportObject("1111111", "Bullying", date, time, "Open"));
        }
        else {
            reportList.clear();
            reportList.add(new ReportObject("6124511", "Cyberbullying", date, time, "Open"));
        }
    }

    public void setFilter(String string) {
        //Toast.makeText(getActivity(),"3: " + string,Toast.LENGTH_SHORT).show();
        filter = string;
        populateFragment();
    }

}
