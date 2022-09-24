package com.example.proj.DatabaseObjects;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Department.class}, version = 1)
public abstract class DepartmentsDatabase extends RoomDatabase {

    public abstract DepartmentDao departmentDao();

}
