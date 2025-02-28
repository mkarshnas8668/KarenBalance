package com.mkarshnas6.karenstudio.karenbalance.db.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.ReportEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface ReportDAO {

    @Insert
    fun insertReport(report: ReportEntity)

    @get:Query("SELECT * FROM ${DBHandler.REPORT_TABLE} ORDER BY id DESC")
    val getReports: Flowable<List<ReportEntity>>

    @Query("SELECT * FROM ${DBHandler.REPORT_TABLE} WHERE id = :id")
    fun getMonthlyById(id: Int): Flowable<ReportEntity>

    @Query("DELETE FROM ${DBHandler.REPORT_TABLE} WHERE id = :id")
    fun deleteMonthlyById(id: Int)

    @Query("DELETE FROM ${DBHandler.REPORT_TABLE}")
    fun deleteAllMonthlys()

    @Update
    fun updateMonthly(monthly: ReportEntity)

}