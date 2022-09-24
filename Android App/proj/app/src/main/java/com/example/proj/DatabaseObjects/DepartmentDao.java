package com.example.proj.DatabaseObjects;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DepartmentDao {
    @Query("SELECT * FROM Departments")
    LiveData<List<Department>> getAllLive();

    @Query("SELECT * FROM Departments")
    List<Department> getAll();

    @Query("SELECT (maxHours-currentHours) as sub FROM Departments WHERE id == :id")
    int getRemainingHours(int id);

    @Query("SELECT englishName FROM Departments WHERE id == :id")
    String getEnglishName(int id);

    @Query("SELECT arabicName FROM Departments WHERE id == :id")
    String getArabicName(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Department... departments);

    @Delete
    void deleteAll(Department... departments);

    @Query("DELETE FROM Departments")
    public void nukeTable();
}
