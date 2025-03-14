package com.mkarshnas6.karenstudio.karenbalance.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mkarshnas6.karenstudio.karenbalance.db.Dao.DailyDAO
import com.mkarshnas6.karenstudio.karenbalance.db.Dao.ReportDAO
import com.mkarshnas6.karenstudio.karenbalance.db.Dao.TargetDAO
import com.mkarshnas6.karenstudio.karenbalance.db.model.DailyEntity
import com.mkarshnas6.karenstudio.karenbalance.db.model.ReportEntity
import com.mkarshnas6.karenstudio.karenbalance.db.model.TargetEntity

@Database(
    entities = [ReportEntity::class, DailyEntity::class,TargetEntity::class],
    version = DBHandler.DATABASE_VERSION
)
abstract class DBHandler : RoomDatabase() {

    abstract fun reportDao(): ReportDAO
    abstract fun dailyDao(): DailyDAO
    abstract fun targetDao():TargetDAO

    companion object {
        private const val DATABASE_NAME = "mainDB_karenBalance"
        const val DATABASE_VERSION = 2

        const val REPORT_TABLE = "reportTable"
        const val DAILY_TABLE = "dailyTable"
        const val TARGET_TABLE = "targetTable"

        private var INSTANCE: DBHandler? = null

        fun getDatabase(context: Context): DBHandler {

            if (INSTANCE == null)
                INSTANCE = Room.databaseBuilder(
                    context,
                    DBHandler::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

            return INSTANCE!!

        }

    }


}