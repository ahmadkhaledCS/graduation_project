package com.example.proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.example.proj.DatabaseObjects.Course;
import com.example.proj.DatabaseObjects.CourseDao;
import com.example.proj.DatabaseObjects.CoursesDatabase;
import com.example.proj.DatabaseObjects.DepartmentDao;
import com.example.proj.DatabaseObjects.DepartmentsDatabase;
import com.example.proj.Tree.BaseTreeAdapter;
import com.example.proj.Tree.TreeNode;
import com.example.proj.Tree.TreeView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;


public class TreeViewActivity extends AppCompatActivity {
/**
 * this activity renders the tree structure
 * **/
    CoursesDatabase coursesDatabase;
    CourseDao courseDao;
    DepartmentsDatabase departmentsDatabase;
    DepartmentDao departmentDao;

    List<Course>courses;
    TreeNode root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_view);

        //force screen rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Initialize the databases and data access objects
        departmentsDatabase = Room.databaseBuilder(getApplicationContext(),
                DepartmentsDatabase.class, "Departments").allowMainThreadQueries().build();

        departmentDao = departmentsDatabase.departmentDao();
        coursesDatabase = Room.databaseBuilder(getApplicationContext(),
                CoursesDatabase.class, "Courses").allowMainThreadQueries().build();

        viewTree(false,false);

        Chip showMarkChip=findViewById(R.id.showMarkChip);
        Chip showTypeChip =findViewById(R.id.showTypeChip);

        showMarkChip.setOnCheckedChangeListener((buttonView, isChecked) -> viewTree(showMarkChip.isChecked(), showTypeChip.isChecked()));

        showTypeChip.setOnCheckedChangeListener((buttonView, isChecked) -> viewTree(showMarkChip.isChecked(), showTypeChip.isChecked()));


    }
    private void viewTree(boolean showMark,boolean showType){
        TreeView treeView = findViewById(R.id.idTreeView);
        BaseTreeAdapter<ViewHolder> adapter = new BaseTreeAdapter<ViewHolder>(this, R.layout.tree_view_node) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(View view) {
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, Object data, int position) {
                // inside our on bind view holder method we
                // are setting data from object to text view.
                viewHolder.textView.setText(data.toString());

            }
        };
        treeView.setAdapter(adapter);
        courseDao=coursesDatabase.courseDao();

        root = new TreeNode("الخطة الشجرية");
        courses=courseDao.getAll();

        List<Course>mainCourses=new ArrayList<>();

        for(Course course:courses){
            if(noPre(course) && noBro(course)){
                if(course.getDepId()!=2)//مادة اختياري جامعة
                    mainCourses.add(course);
            }
        }

        for(Course course:mainCourses){
            root.addChild(getTree(course,showMark, showType));
        }

        adapter.setRootNode(root);
    }

    private TreeNode getTree(Course course,boolean showMark,boolean showType) {
        String data=course.getName();
        if(showType)
            data+="\nنوع المتطلب: "+departmentDao.getArabicName(course.getDepId());
        if(showMark)
            data+="\nالعلامة: "+course.getResult();
        TreeNode node=new TreeNode(data);

        for(Course c:courses)
            if(c.getPrerequisite()!=null && c.getPrerequisite().equals(course.getName()))
                node.addChild(getTree(c,showMark, showType));
        return node;
    }


    private boolean noPre(Course course){
        return (course.getPrerequisite()==null || course.getPrerequisite().equals(""));
    }
    private boolean noBro(Course course){
        return (course.getBrother()==null ||course.getBrother().equals(""));
    }
}