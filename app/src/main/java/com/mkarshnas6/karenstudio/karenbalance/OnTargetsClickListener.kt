package com.mkarshnas6.karenstudio.karenbalance

import com.mkarshnas6.karenstudio.karenbalance.db.model.TargetEntity

interface OnTargetsClickListener {
    fun onTargetsClick(targets: TargetEntity)
}