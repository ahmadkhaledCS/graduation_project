package com.example.proj;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proj.DatabaseObjects.Course;
import com.example.proj.DatabaseObjects.CourseDao;
import com.example.proj.DatabaseObjects.CoursesDatabase;
import com.example.proj.DatabaseObjects.Department;
import com.example.proj.DatabaseObjects.DepartmentDao;
import com.example.proj.DatabaseObjects.DepartmentsDatabase;
import com.example.proj.Tree.TreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SaveDataToDatabase extends Service {
/**
 * this class is a service to get plan from api and
 * then add courses to databases using room database**/
    DepartmentsDatabase departmentsDatabase;
    DepartmentDao departmentDao;
    CoursesDatabase coursesDatabase;
    CourseDao courseDao;
    List<Course> courses;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //Initialize the databases and data access objects
        departmentsDatabase = Room.databaseBuilder(getApplicationContext(),
                DepartmentsDatabase.class, "Departments").allowMainThreadQueries().build();
        departmentDao = departmentsDatabase.departmentDao();
        departmentDao.nukeTable();

        coursesDatabase = Room.databaseBuilder(getApplicationContext(),
                CoursesDatabase.class, "Courses").allowMainThreadQueries().build();
        courseDao=coursesDatabase.courseDao();
        courseDao.nukeTable();

        //shared preferences to get id and password
        SharedPreferences preferences=getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String id=preferences.getString("id",null);
        String password=preferences.getString("regPassword",null);

        //String to call api
        //String url=String.format("http://192.168.137.1:8000/plan/%s/%s",id,password);//phone
        String url=String.format("http://10.0.2.2:8000/plan/%s/%s",id,password);//emulator
        //request queue for Volley library
        RequestQueue requestQueue= Volley.newRequestQueue(this);

        // constructing the get request
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray jsonArray;
                        // if the response contains plan log in was successful
                        if(response.has("plan")){
                            jsonArray=response.getJSONArray("plan");
                            saveData(jsonArray);
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
                return 50000;
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

        return START_NOT_STICKY;
    }
    /**
     * save the data thar was scrapped into databases
     * **/
    private void saveData(JSONArray jsonArray) throws JSONException {
        Department[]departments=new Department[jsonArray.length()];
        for (int i=0;i<jsonArray.length();i++){
            JSONObject department= (JSONObject) jsonArray.get(i);
            Department d1=new Department(
                    Integer.parseInt(department.get("id").toString()),
                    Integer.parseInt(department.get("currentHours").toString()),
                    Integer.parseInt(department.get("maxHours").toString()),
                    department.get("arabicName").toString(),
                    department.get("englishName").toString());
            departments[i]=d1;

            JSONArray courses=department.getJSONArray("marks");
            for(int j=0;j<courses.length();j++){
                JSONObject course= (JSONObject) courses.get(j);
                String []preAndBro=getPreAndBro(course.get("status").toString());
                Course c1=new Course(
                        course.get("id").toString(),
                        course.get("name").toString(),
                        course.get("result").toString(),
                        course.get("hours").toString(),
                        preAndBro[0],
                        preAndBro[1],
                        d1.getId(),
                        0
                );
                courseDao.insertAll(c1);
            }
        }
        departmentDao.insertAll(departments);
        addPriority();

    }

    private void addPriority() {
        List<Course> mainCourses=new ArrayList<>();
        courses=courseDao.getAll();
        for(Course course:courses){
            if(noPre(course) && noBro(course) && course.getDepId()!=2)
                mainCourses.add(course);
        }
        for(Course course:mainCourses)
            getTree(course);
        for (Course course: courses)
            courseDao.insertAll(course);
    }

    private TreeNode getTree(Course course) {
        TreeNode node=new TreeNode(course.getName());
        for(Course c:courses)
            if(c.getPrerequisite()!=null && c.getPrerequisite().equals(course.getName()))
                node.addChild(getTree(c));
        course.setPriority(node.getNodeCount());
        return node;
    }


    private boolean noPre(Course course){
        return (course.getPrerequisite()==null || course.getPrerequisite().equals(""));
    }
    private boolean noBro(Course course){
        return (course.getBrother()==null ||course.getBrother().equals(""));
    }

    /**
     * get the prerequisite and the brothers of the course from course description
     * **/
    public static String[] getPreAndBro(String status) {
        String pre=null;
        String bro=null;
        for(String entry:status.split(" أو |السنة - | و ")){
            if(!entry.trim().contains("امتحان")&&!entry.trim().matches("\\d")&&!entry.trim().contains("متزامن مع")&& !entry.isEmpty())
                pre=(entry.contains("يجب ان يكون الطالب قد قطع"))?entry.replaceAll("\\D","").trim():entry.trim();
            if (entry.trim().contains("متزامن مع")){
                bro=entry.replace("متزامن مع","").trim();
            }
        }
        return new String[]{pre,bro};
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
