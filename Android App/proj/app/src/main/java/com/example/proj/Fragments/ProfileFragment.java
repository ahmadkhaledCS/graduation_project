package com.example.proj.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.proj.DatabaseObjects.Department;
import com.example.proj.DatabaseObjects.DepartmentDao;
import com.example.proj.DatabaseObjects.DepartmentsDatabase;
import com.example.proj.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    LiveData<List<Department>> liveData;
    DepartmentsDatabase departmentsDatabase;
    DepartmentDao departmentDao;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences=view.getContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        TextView nameAndId=getView().findViewById(R.id.profileNameAndId);
        TextView status=getView().findViewById(R.id.profileStatus);
        TextView average=getView().findViewById(R.id.profileAverage);

        String name=preferences.getString("arabicName","null");
        String id=preferences.getString("id","null");
        nameAndId.setText(String.format("%s\n%s",name,id));

        /* old status description
        String statusText=preferences.getString("statusDesc","null");
        status.setText(String.format("Status:%s",statusText));
        */
        String []statusText=preferences.getString("statusDesc","none-none-none").split("-");
        status.setText(String.format("%s",statusText[0]));




        String averageText=preferences.getString("avg","null");
        average.setText(String.format("Average:%s",averageText));

        //Initialize the databases and data access objects
        departmentsDatabase = Room.databaseBuilder(getView().getContext(),
                DepartmentsDatabase.class, "Departments").allowMainThreadQueries().build();
        departmentDao = departmentsDatabase.departmentDao();

        liveData=departmentDao.getAllLive();
    }

    @Override
    public void onResume() {
        departmentDao.insertAll();
        CircularProgressIndicator []indicators={
                getView().findViewById(R.id.firstIndicator),getView().findViewById(R.id.secondIndicator),
                getView().findViewById(R.id.thirdIndicator),getView().findViewById(R.id.fourthIndicator),
                getView().findViewById(R.id.fifthIndicator),getView().findViewById(R.id.sixthIndicator),
                getView().findViewById(R.id.seventhIndicator),getView().findViewById(R.id.eighthIndicator)};

        TextView [] titles={
                getView().findViewById(R.id.firstTitle),getView().findViewById(R.id.secondTitle),
                getView().findViewById(R.id.thirdTitle),getView().findViewById(R.id.fourthTitle),
                getView().findViewById(R.id.fifthTitle),getView().findViewById(R.id.sixthTitle),
                getView().findViewById(R.id.seventhTitle),getView().findViewById(R.id.eighthTitle)};

        TextView [] percentages={
                getView().findViewById(R.id.firstNumbers),getView().findViewById(R.id.secondNumbers),
                getView().findViewById(R.id.thirdNumbers),getView().findViewById(R.id.fourthNumbers),
                getView().findViewById(R.id.fifthNumbers),getView().findViewById(R.id.sixthNumbers),
                getView().findViewById(R.id.seventhNumbers),getView().findViewById(R.id.eighthNumbers)};

        liveData.removeObservers(getViewLifecycleOwner());
        liveData.observe(getViewLifecycleOwner(), departments -> {
            int counter=0;
            for(Department department: Objects.requireNonNull(liveData.getValue())){
                indicators[counter].setProgress((int)(department.getCurrentHours()/department.getMaxHours()*100f));
                titles[counter].setText(department.getArabicName());
                percentages[counter].setText(department.getCurrentHours()+"/"+department.getMaxHours());
                counter++;
            }
        });
        super.onResume();
    }

}