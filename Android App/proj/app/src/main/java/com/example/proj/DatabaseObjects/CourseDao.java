package com.example.proj.DatabaseObjects;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CourseDao {

    @Query("SELECT * FROM Courses")
    List<Course> getAll();

    @Query("SELECT * FROM Courses where id == :id")
    Course getCourse(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Course... courses);

    @Delete
    void deleteAll(Course... courses);

    @Query("DELETE FROM Courses")
    public void nukeTable();
}
