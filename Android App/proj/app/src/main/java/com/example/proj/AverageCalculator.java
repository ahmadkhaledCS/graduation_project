package com.example.proj;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.room.Room;

import com.example.proj.DatabaseObjects.Course;
import com.example.proj.DatabaseObjects.CourseDao;
import com.example.proj.DatabaseObjects.CoursesDatabase;
import com.example.proj.DatabaseObjects.DepartmentDao;
import com.example.proj.DatabaseObjects.DepartmentsDatabase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AverageCalculator extends AppCompatActivity {
    DepartmentsDatabase departmentsDatabase;
    DepartmentDao departmentDao;
    CoursesDatabase coursesDatabase;
    CourseDao courseDao;
    int finishedHours;
    float currentAverage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_calculator);

        //Initialize the databases and data access objects
        departmentsDatabase = Room.databaseBuilder(getApplicationContext(),
                DepartmentsDatabase.class, "Departments").allowMainThreadQueries().build();
        departmentDao = departmentsDatabase.departmentDao();

        coursesDatabase = Room.databaseBuilder(getApplicationContext(),
                CoursesDatabase.class, "Courses").allowMainThreadQueries().build();
        courseDao=coursesDatabase.courseDao();

        SharedPreferences preferences=getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        currentAverage=Float.parseFloat(preferences.getString("avg","0"));
        finishedHours=0;

        for(Course course:courseDao.getAll()){
            if(course.getResult().matches("[A-F][+-]?"))
                finishedHours+=Integer.parseInt(course.getHours());
        }

        TextView totalCompletedHours=findViewById(R.id.totalCompletedHours);
        TextView myAverage=findViewById(R.id.myAverage);
        TextView expectedMark=findViewById(R.id.expectedMark);

        totalCompletedHours.setText("عدد الساعات التراكمية: "+finishedHours);
        myAverage.setText("المعدل الحالي: "+currentAverage);



        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-1);
        params.weight=2;
        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(-1,-1);
        params1.weight=1;

        ArrayList<String> options=new ArrayList<>(Arrays.asList("A","A-","B+","B","B-","C+","C","C-","D+","D","D-","no mark"));
        HashMap<String, Float> map=new HashMap<>();
        map.put("A",4f);
        map.put("A-",3.75f);
        map.put("B+",3.5f);
        map.put("B",3.0f);
        map.put("B-",2.75f);
        map.put("C+",2.5f);
        map.put("C",2.0f);
        map.put("C-",1.75f);
        map.put("D+",1.5f);
        map.put("D",1.25f);
        map.put("D-",1.0f);

        LinearLayout mainLayout=findViewById(R.id.mainLayout);
        LinearLayout innerLayout;
        TextView name;
        TextInputLayout textInputLayout;
        AutoCompleteTextView autoCompleteTextView;


        HashMap<AutoCompleteTextView,Integer>autoCompleteTextViews=new HashMap<>();

        for(Course course:courseDao.getAll()){
            if(course.getResult().trim().matches("مسجل|بديل.*") && !course.getHours().matches("0")){
                innerLayout=new LinearLayout(this);
                name=new TextView(this);
                textInputLayout = new TextInputLayout(new ContextThemeWrapper(this, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_FilledBox_Dense_ExposedDropdownMenu));
                autoCompleteTextView=new AutoCompleteTextView(this);


                textInputLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                textInputLayout.setHint("العلامة المتوقعة:");
                autoCompleteTextView.setInputType(0x00000000);
                autoCompleteTextView.setAdapter(new ArrayAdapter<>(this,R.layout.dropdown_list_item,options));
                textInputLayout.addView(autoCompleteTextView);


                name.setGravity(Gravity.CENTER);
                name.setTextSize(16f);
                name.setText(course.getName());

                autoCompleteTextViews.put(autoCompleteTextView,Integer.parseInt(course.getHours()));
                innerLayout.addView(textInputLayout,params1);
                innerLayout.addView(name,params);
                mainLayout.addView(innerLayout);
            }

            MaterialButton calculate=findViewById(R.id.calculateButton);
            calculate.setOnClickListener(v -> {
                int hours=0;
                float marks;
                float total=finishedHours*currentAverage;
                boolean done=true;

                for(AutoCompleteTextView textView:autoCompleteTextViews.keySet()){
                    if(textView.getText().toString().equals("no mark"))
                        continue;
                    if(textView.getText().toString().equals("")){
                        textView.setError("الرجاء ادخال العلامة");
                        textView.requestFocus();
                        done=false;
                        break;
                    }else{
                        textView.setError(null);
                        marks=map.get(textView.getText().toString());
                        hours+=autoCompleteTextViews.get(textView);
                        total+=marks*autoCompleteTextViews.get(textView);
                    }
                }
                if(done)
                    expectedMark.setText("العلامة المتوقعة: "+round(total/(finishedHours+hours),2));
            });

        }
    }
    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        return bd;
    }
}