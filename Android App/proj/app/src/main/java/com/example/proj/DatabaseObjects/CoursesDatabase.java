package com.example.proj.DatabaseObjects;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Course.class}, version = 1)
public abstract class CoursesDatabase extends RoomDatabase {

    public abstract CourseDao courseDao();

}
