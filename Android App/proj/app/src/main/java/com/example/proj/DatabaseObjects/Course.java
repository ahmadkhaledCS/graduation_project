package com.example.proj.DatabaseObjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(tableName = "Courses")
public class Course {
    @PrimaryKey
    @NonNull
    String id;
    String name;
    String result;
    String hours;
    String prerequisite;
    String brother;
    int depId;
    int priority;


    public Course(@NonNull String id, String name, String result, String hours, String prerequisite, String brother, int depId,int priority) {
        this.id = id;
        this.name = name;
        this.result = result;
        this.hours = hours;
        this.prerequisite = prerequisite;
        this.brother=brother;
        this.depId = depId;
        this.priority=priority;
    }

    public void setPriority(int priority) {
        this.priority=priority;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getResult() {
        return result;
    }

    public String getHours() {
        return hours;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public String getBrother() {
        return brother;
    }

    public int getDepId() {
        return depId;
    }
    public int getPriority() {
        return priority;
    }

    @NonNull
    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", result='" + result + '\'' +
                ", hours='" + hours + '\'' +
                ", prerequisite='" + prerequisite + '\'' +
                ", brother='" + brother + '\'' +
                ", depId=" + depId +
                '}';
    }


}
