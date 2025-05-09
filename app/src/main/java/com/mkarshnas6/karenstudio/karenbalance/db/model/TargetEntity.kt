package com.mkarshnas6.karenstudio.karenbalance.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler

@Entity(tableName = DBHandler.TARGET_TABLE)
data class TargetEntity (
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    @ColumnInfo val name : String = "Target",
    @ColumnInfo val price : Long ,
    @ColumnInfo val date : String ,
    @ColumnInfo val necessary : Boolean,
    @ColumnInfo val progress : Int = 0,
    @ColumnInfo val img : String = "@drawable/luxe_home"
//    @ColumnInfo val img : String = "android.resource://com.mkarshnas6.karenstudio.karenbalance/drawable/luxe_home"
)