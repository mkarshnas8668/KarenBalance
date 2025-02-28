package com.mkarshnas6.karenstudio.karenbalance.db.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.DailyEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface DailyDAO {

    @Insert
    fun insertDaily(daily: DailyEntity)

    @get:Query("SELECT * FROM ${DBHandler.DAILY_TABLE} ORDER BY id DESC")
    val getDailys: Flowable<List<DailyEntity>>

    @Query("SELECT * FROM ${DBHandler.DAILY_TABLE} WHERE id = :id")
    fun getDailyById(id: Int): Flowable<DailyEntity>

    @Query("DELETE FROM ${DBHandler.DAILY_TABLE} WHERE id = :id")
    fun deleteDailyById(id: Int)

    @Query("DELETE FROM ${DBHandler.DAILY_TABLE}")
    fun deleteAllDailys()

    @Update
    fun updateDaily(daily: DailyEntity)

    @Query("SELECT date FROM ${DBHandler.DAILY_TABLE} ORDER BY id DESC LIMIT 1")
    fun getLatestDate(): Flowable<String>
}
