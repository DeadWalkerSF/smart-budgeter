package com.deadwalkersf.smartbudgeter

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.deadwalkersf.smartbudgeter.ui.BudgeterRoot
import com.deadwalkersf.smartbudgeter.ui.theme.SmartBudgeterTheme

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
