package com.mkarshnas6.karenstudio.karenbalance

import android.animation.ValueAnimator
import android.widget.TextView

class AnimationWrite {
    companion object {
        fun showAnimTitle(textView: TextView, text: String) {
            val animator = ValueAnimator.ofInt(0, text.length)
            animator.duration = 800
            animator.addUpdateListener { animation ->
                val index = animation.animatedValue as Int
                textView.text = text.substring(0, index)
            }
            animator.start()
        }
    }
}
