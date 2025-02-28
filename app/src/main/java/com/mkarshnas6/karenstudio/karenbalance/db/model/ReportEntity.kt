package com.mkarshnas6.karenstudio.karenbalance.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mkarshnas6.karenstudio.karenbalance.ExpenseEntity
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler

@Entity(tableName = DBHandler.REPORT_TABLE)
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo override val price: Int,
    @ColumnInfo override val description: String,
    @ColumnInfo override val date: String,
    @ColumnInfo override val time: String,
) : ExpenseEntity
