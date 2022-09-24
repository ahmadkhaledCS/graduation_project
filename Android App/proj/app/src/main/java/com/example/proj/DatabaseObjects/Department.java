package com.example.proj.DatabaseObjects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Departments")
public class Department {
    @PrimaryKey
    int id;
    int currentHours;
    int maxHours;
    String arabicName;
    String englishName;

    public Department(int id, int currentHours, int maxHours, String arabicName, String englishName) {
        this.id = id;
        this.currentHours = currentHours;
        this.maxHours = maxHours;
        this.arabicName = arabicName;
        this.englishName = englishName;
    }

    public int getId() {
        return id;
    }

    public int getCurrentHours() {
        return currentHours;
    }

    public int getMaxHours() {
        return maxHours;
    }

    public String getArabicName() {
        return arabicName;
    }

    public String getEnglishName() {
        return englishName;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", currentHours=" + currentHours +
                ", maxHours=" + maxHours +
                ", arabicName='" + arabicName + '\'' +
                ", englishName='" + englishName + '\'' +
                '}';
    }
}
