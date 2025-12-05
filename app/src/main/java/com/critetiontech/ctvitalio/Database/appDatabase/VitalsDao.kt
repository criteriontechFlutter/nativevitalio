package com.critetiontech.ctvitalio.Database.appDatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface VitalsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(entity: VitalsEntity)

    @Query(value = "SELECT * FROM vitals_table WHERE id = 1")
    suspend fun getVitals(): VitalsEntity?}
