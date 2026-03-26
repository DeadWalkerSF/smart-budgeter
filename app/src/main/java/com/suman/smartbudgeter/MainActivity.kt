package com.suman.smartbudgeter

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.suman.smartbudgeter.ui.BudgeterRoot
import com.suman.smartbudgeter.ui.theme.SmartBudgeterTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartBudgeterTheme {
                BudgeterRoot(activity = this)
            }
        }
    }
}
