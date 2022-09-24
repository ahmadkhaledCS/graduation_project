package com.example.proj;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proj.DatabaseObjects.CourseDao;
import com.example.proj.DatabaseObjects.CoursesDatabase;
import com.example.proj.DatabaseObjects.DepartmentDao;
import com.example.proj.DatabaseObjects.DepartmentsDatabase;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SuggestedTable extends AppCompatActivity {

    TableLayout tableLayout;
    TableRow.LayoutParams params;
    int hoursCount;
    ArrayList<RegCourse> finalCourses;
    ArrayList<Node> finalLeaves;
    CoursesDatabase coursesDatabase;
    CourseDao courseDao;
    DepartmentsDatabase departmentsDatabase;
    DepartmentDao departmentDao;
    LottieAnimationView animationView;
    Chip showCourseNumberChip;
    boolean showCourseNumber;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_table);

        showCourseNumberChip=findViewById(R.id.showCourseNumberChip);
        showCourseNumber=showCourseNumberChip.isChecked();

        showCourseNumberChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refreshTable();
            }
        });
        tableLayout=findViewById(R.id.tableLayout);
        animationView=findViewById(R.id.animationView);

        hoursCount=15;
        Chip hoursChip=findViewById(R.id.hoursChip);
        hoursChip.setOnClickListener(v -> {
            NumberPicker picker=new NumberPicker(this);
            picker.setMinValue(3);
            picker.setMaxValue(21);
            picker.setValue(hoursCount);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("ادخل عدد الساعات")
                    .setView(picker)
                    .setNeutralButton("حفظ", (dialog, which) -> {
                        hoursCount=picker.getValue();
                        hoursChip.setText("عدد الساعات "+hoursCount);
                        refreshTable();
                    })
                    .show();
        });

        //force screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        tableLayout.removeAllViews();


        finalLeaves=new ArrayList<>();
        //shared preferences to get id and password
        SharedPreferences preferences=getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String id=preferences.getString("id",null);
        String password=preferences.getString("regPassword",null);

        //String to call api
        String url=String.format("http://10.0.2.2:8000/allowed/%s/%s",id,password);//virtual
        //String url=String.format("http://192.168.137.1:8000/allowed/%s/%s",id,password);//phone
        //request queue for Volley library
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        // constructing the get request
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray jsonArray;
                        // if the response contains plan log in was successful
                        if(response.has("courses")){
                            jsonArray=response.getJSONArray("courses");
                            viewData(jsonArray);
                        }else{
                            Log.d("saveData","wrong credentials");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
        // configure request timeout
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 200000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        requestQueue.add(request);

    }

    private void refreshTable() {
        tableLayout.removeAllViews();
        tableLayout.addView(headerRow());
        showCourseNumber=showCourseNumberChip.isChecked();
        ArrayList<Node>parents=new ArrayList<>();
        for (RegCourseClassrooms room:finalCourses.get(0).availableClassrooms){
            parents.add(new Node(finalCourses.get(0),
                    room.getStartAndEndTimes(),
                    null,
                    courseDao.getCourse(finalCourses.get(0).id).getPriority(),
                    finalCourses.get(0).creditHours,
                    room.id));
        }
        finalLeaves=new ArrayList<>();
        for(Node parent:parents){
            addLeaves(parent,1);
        }
        Node maxPriority=finalLeaves.get(0);
        for(Node node:finalLeaves)
            if(node.priority>maxPriority.priority && node.hours<=hoursCount)
                maxPriority=node;

        Node current=maxPriority;
        while (current!=null){
            printRow(current.course,current.id);
            current=current.parent;
        }
    }


    private View headerRow() {
        TableRow headerRow=new TableRow(this);
        headerRow.setBackgroundColor(0xFF000000);
        params=new TableRow.LayoutParams(-2,-2,1f);
        params.setMargins(1,1,1,1);
        String []fields;
        if(showCourseNumber)
            fields=new String[]{"رقم المادة","اسم المادة","عدد الساعات المعتمدة","رقم الشعبة", "الايام","الوقت","اسم الدكتور","الحالة"};
        else
            fields=new String[]{"اسم المادة","عدد الساعات المعتمدة","رقم الشعبة", "الايام","الوقت","اسم الدكتور","الحالة"};

        TextView textView;

        for (String field:fields){
            textView=new TextView(this);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setBackgroundResource(R.color.blue);
            textView.setText(field);
            headerRow.addView(textView,params);
        }
        return headerRow;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void viewData(JSONArray jsonArray) throws JSONException {
        coursesDatabase= Room.databaseBuilder(getApplicationContext(),
                CoursesDatabase.class, "Courses").allowMainThreadQueries().build();
        courseDao=coursesDatabase.courseDao();

        departmentsDatabase=Room.databaseBuilder(getApplicationContext(),
                DepartmentsDatabase.class, "Departments").allowMainThreadQueries().build();
        departmentDao=departmentsDatabase.departmentDao();


        ArrayList<RegCourse>regCourses=new ArrayList<>();
        //save database data into the above arrayList
        for(int i=0;i<jsonArray.length();i++){
            JSONObject object= (JSONObject) jsonArray.get(i);

            //create the course object and fill it from json object
            RegCourse regCourse=new RegCourse(
                    object.get("no").toString(),
                    (Integer)object.get("remainingHours"),
                    object.get("arabicName").toString(),
                    object.get("englishName").toString(),
                    (Integer)object.get("creditHours"),
                    (Integer)object.get("customHours"),
                    object.get("parallelCourseId"),
                    object.get("classificationId").toString(),
                    null //the available classrooms will be added separately
            );

            //json object to store available classrooms
            JSONArray classrooms=object.getJSONArray("available classrooms");
            //rooms array to get rooms details ex times instructor
            ArrayList<RegCourseClassrooms>rooms=new ArrayList<>();
            //temp classroom to add to array
            RegCourseClassrooms regCourseClassroom;
            for(int k=0;k< classrooms.length();k++){
                object= (JSONObject) classrooms.get(k);

                //getting the roomLocations ready
                Map<String,String>roomsLocations=new HashMap<>();
                JSONArray roomsLocationsArray=object.getJSONArray("rooms");
                for(int b=0;b<roomsLocationsArray.length();b++){
                    roomsLocations.put("id",((JSONObject)roomsLocationsArray.get(b)).get("id").toString());
                    roomsLocations.put("arabicName",((JSONObject)roomsLocationsArray.get(b)).get("arabicName").toString());
                    roomsLocations.put("englishName",((JSONObject)roomsLocationsArray.get(b)).get("englishName").toString());
                }

                //getting the coursesTimes ready
                ArrayList<Map<String,Integer>>times=new ArrayList<>();
                JSONArray timesArray=object.getJSONArray("times");
                Map<String,Integer>t;
                for(int b=0;b<timesArray.length();b++){
                    t=new HashMap<>();
                    t.put("day",(Integer)((JSONObject)timesArray.get(b)).get("day"));
                    t.put("endTime",(Integer)((JSONObject)timesArray.get(b)).get("endTime"));
                    t.put("startTime",(Integer)((JSONObject)timesArray.get(b)).get("startTime"));
                    t.put("startMinute",(Integer)((JSONObject)timesArray.get(b)).get("startMinute"));
                    t.put("startHour",(Integer)((JSONObject)timesArray.get(b)).get("startHour"));
                    t.put("endHour",(Integer)((JSONObject)timesArray.get(b)).get("endHour"));
                    t.put("endMinute",(Integer)((JSONObject)timesArray.get(b)).get("endMinute"));
                    times.add(t);
                }

                //getting the instructors ready
                Map<String,String>instructors=new HashMap<>();
                JSONArray instructorsArray=object.getJSONArray("instructors");
                for(int b=0;b<instructorsArray.length();b++){
                    instructors.put("id",((JSONObject)instructorsArray.get(b)).get("id").toString());
                    instructors.put("arabicName",((JSONObject)instructorsArray.get(b)).get("arabicName").toString());
                    instructors.put("englishName",((JSONObject)instructorsArray.get(b)).get("englishName").toString());
                }

                //adding room data including above maps
                regCourseClassroom=new RegCourseClassrooms(
                        object.get("id").toString(),
                        (Integer) object.get("sequence"),
                        roomsLocations,
                        times,
                        instructors,
                        (Integer) object.get("status"),
                        (Integer) object.get("maxNoOfStudents"),
                        (Integer) object.get("currentNoOfStudents"),
                        (Integer) object.get("confirmationStatus"),
                        (Integer) object.get("requiredHours"),
                        (Boolean) object.get("active")
                );
                //add the room to the rooms array witch will be added to the mainCourse
                rooms.add(regCourseClassroom);
            }
            //added rooms array to the mainCourse
            regCourse.setAvailableClassrooms(rooms);
            //adding the mainCourse to courses array
            regCourses.add(regCourse);
        }

        finalCourses=new ArrayList<>();
        for(RegCourse course:regCourses){
            if(departmentDao.getRemainingHours(Integer.parseInt(course.classificationId))>0)
                if (courseDao.getCourse(course.id)!=null)
                    finalCourses.add(course);
        }

        Collections.sort(finalCourses, Comparator.comparing(course-> -courseDao.getCourse(course.id).getPriority()));
        ArrayList<Node>parents=new ArrayList<>();
        for (RegCourseClassrooms room:finalCourses.get(0).availableClassrooms){
            parents.add(new Node(finalCourses.get(0),
                    room.getStartAndEndTimes(),
                    null,
                    courseDao.getCourse(finalCourses.get(0).id).getPriority(),
                    finalCourses.get(0).creditHours,
                    room.id));
        }
        for(Node parent:parents){
            addLeaves(parent,1);
        }
        Node maxPriority=finalLeaves.get(0);
        for(Node node:finalLeaves)
            if(node.priority>maxPriority.priority && node.hours<=hoursCount)
                maxPriority=node;

        tableLayout.addView(headerRow());
        animationView.setVisibility(View.GONE);
        Node current=maxPriority;
        while (current!=null){
            printRow(current.course,current.id);
            current=current.parent;
        }
    }

    private void addLeaves(Node parent, int i) {
        if(parent.hours>=hoursCount || i>=finalCourses.size() ){
                finalLeaves.add(parent);
            return;
        }

        for (RegCourseClassrooms room:finalCourses.get(i).availableClassrooms){
            Node sonNode=new Node(finalCourses.get(i),
                    room.getStartAndEndTimes(),
                    parent,
                    courseDao.getCourse(finalCourses.get(i).id).getPriority(),
                    finalCourses.get(i).creditHours,
                    room.id);

            if(noIntersection(sonNode.times,parent.times)){
                sonNode.times.addAll(parent.times);
                sonNode.priority+=parent.priority;
                sonNode.hours+=parent.hours;
                addLeaves(sonNode,i+1);
            }else{
                addLeaves(parent,i+2);
            }

        }
    }

    private boolean noIntersection(ArrayList<String> times, ArrayList<String> times1) {
        for(String interval:times) {
            int intervalStart=Integer.parseInt(interval.split("-")[0]);
            int intervalEnd=Integer.parseInt(interval.split("-")[1]);
            for (String interval1 : times1) {
                int intervalStart1=Integer.parseInt(interval1.split("-")[0]);
                int intervalEnd1=Integer.parseInt(interval1.split("-")[1]);
                if(!(intervalEnd1<=intervalStart || intervalEnd<=intervalStart1))
                    return false;
            }
        }
        return true;
    }


    private void printRow(RegCourse course,String roomId) {
        TableRow row=new TableRow(this);
        String []descriptions={"","متاحة","ملغاة","مغلقة"};
        row.setBackgroundColor(0xFF000000);
        RegCourseClassrooms firstRoom=course.availableClassrooms.get(0);
        for(RegCourseClassrooms room:course.availableClassrooms)
            if(room.id.equals(roomId)){
                firstRoom=room;
                break;
            }
        String []fields;
            if(showCourseNumber)
                fields=new String[]{
                        course.id,
                        course.arabicName,
                        course.creditHours+"",
                        firstRoom.id,
                        getDays(firstRoom),
                        firstRoom.times.get(0).get("startHour")+":"+firstRoom.times.get(0).get("startMinute")+"-"+firstRoom.times.get(0).get("endHour")+":"+firstRoom.times.get(0).get("endMinute"),
                        firstRoom.instructors.get("arabicName"),
                        descriptions[firstRoom.status],
                };
            else
                fields=new String[]{
                    course.arabicName,
                    course.creditHours+"",
                    firstRoom.id,
                    getDays(firstRoom),
                    firstRoom.times.get(0).get("startHour")+":"+firstRoom.times.get(0).get("startMinute")+"-"+firstRoom.times.get(0).get("endHour")+":"+firstRoom.times.get(0).get("endMinute"),
                    firstRoom.instructors.get("arabicName"),
                    descriptions[firstRoom.status],
            };


        TextView textView;

        for (String field:fields){
            textView=new TextView(this);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setBackgroundResource(R.color.blue);
            textView.setText(field);
            row.addView(textView,params);
        }
        tableLayout.addView(row);
    }
    public String getDays(RegCourseClassrooms room) {
        String []DAYS= {"", "ح", "ن", "ث", "ر", "خ", "س", "ج"};
        StringBuilder result= new StringBuilder();
        for(Map<String,Integer>t:room.times){
            result.append(DAYS[t.get("day")]).append(" ");
        }
        return result.toString();
    }

}
class RegCourse{
    String id;//save no here
    int remainingHours;
    //sections not implemented
    String arabicName;
    String englishName;
    int creditHours;
    int customHours;
    Object parallelCourseId;//???
    String classificationId;
    ArrayList<RegCourseClassrooms>availableClassrooms;

    public RegCourse(String id, int remainingHours, String arabicName, String englishName, int creditHours, int customHours, Object parallelCourseId, String classificationId, ArrayList<RegCourseClassrooms> availableClassrooms) {
        this.id = id;
        this.remainingHours = remainingHours;
        this.arabicName = arabicName;
        this.englishName = englishName;
        this.creditHours = creditHours;
        this.customHours = customHours;
        this.parallelCourseId = parallelCourseId;
        this.classificationId = classificationId;
        this.availableClassrooms = availableClassrooms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAvailableClassrooms(ArrayList<RegCourseClassrooms> availableClassrooms) {
        this.availableClassrooms = availableClassrooms;
    }

    @NonNull
    @Override
    public String toString() {
        return "RegCourse{" +
                "id='" + id + '\'' +
                ", remainingHours=" + remainingHours +
                ", arabicName='" + arabicName + '\'' +
                ", englishName='" + englishName + '\'' +
                ", creditHours=" + creditHours +
                ", customHours=" + customHours +
                ", parallelCourseId=" + parallelCourseId +
                ", classificationId='" + classificationId + '\'' +
                ", availableClassrooms=" + availableClassrooms +
                '}';
    }
}

class RegCourseClassrooms{
    String id;
    int sequence;
    Map<String,String>rooms;
    ArrayList<Map<String,Integer>>times;
    Map<String,String>instructors;
    int status;
    int maxNoOfStudents;
    int currentNoOfStudents;
    int confirmationStatus;
    int requiredHours;
    boolean active;

    public RegCourseClassrooms(String id, int sequence, Map<String, String> rooms, ArrayList<Map<String,Integer>>times, Map<String, String> instructors, int status, int maxNoOfStudents, int currentNoOfStudents, int confirmationStatus, int requiredHours, boolean active) {
        this.id = id;
        this.sequence = sequence;
        this.rooms = rooms;
        this.times = times;
        this.instructors = instructors;
        this.status = status;
        this.maxNoOfStudents = maxNoOfStudents;
        this.currentNoOfStudents = currentNoOfStudents;
        this.confirmationStatus = confirmationStatus;
        this.requiredHours = requiredHours;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getStartAndEndTimes(){
        ArrayList<String> result=new ArrayList<>();
        for(Map<String,Integer> map:times){
            result.add(map.get("startTime")+"-"+map.get("endTime"));
        }
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "RegCourseClassrooms{" +
                "id='" + id + '\'' +
                ", sequence=" + sequence +
                ", rooms=" + rooms +
                ", times=" + times +
                ", instructors=" + instructors +
                ", status=" + status +
                ", maxNoOfStudents=" + maxNoOfStudents +
                ", currentNoOfStudents=" + currentNoOfStudents +
                ", confirmationStatus=" + confirmationStatus +
                ", requiredHours=" + requiredHours +
                ", active=" + active +
                '}';
    }


}

class Node{
    RegCourse course;
    ArrayList<String>times;
    Node parent;
    int priority;
    int hours;
    String id;
    ArrayList<Node>children;

    public Node(RegCourse course, ArrayList<String>times, Node parent,int priority,int hours,String id) {
        this.course = course;
        this.times = times;
        this.parent = parent;
        this.priority=priority;
        this.hours=hours;
        this.id=id;
    }

    @NonNull
    @Override
    public String toString() {
        return "Node{" +
                "course=" + course.arabicName +
                ", times=" + times +
                ", parent=" + parent +
                ", priority=" + priority +
                ", children=" + children +
                '}';
    }
}