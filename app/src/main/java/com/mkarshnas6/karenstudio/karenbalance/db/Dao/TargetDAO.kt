package com.mkarshnas6.karenstudio.karenbalance.db.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.TargetEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface TargetDAO {

    @Insert
    fun insertTarget(target: TargetEntity)

    @get:Query("SELECT * FROM ${DBHandler.TARGET_TABLE} ORDER BY id DESC")
    val getTargets: Flowable<List<TargetEntity>>

    @Query("SELECT * FROM ${DBHandler.TARGET_TABLE} WHERE id = :id")
    fun getTargetById(id: Int): Flowable<TargetEntity>

    @Query("DELETE FROM ${DBHandler.TARGET_TABLE} WHERE id = :id")
    fun deleteTargetById(id: Int)

    @Query("DELETE FROM ${DBHandler.TARGET_TABLE}")
    fun deleteAllTargets()

    @Update
    fun updateTarget(target: TargetEntity)

}