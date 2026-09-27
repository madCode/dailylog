package com.app.dailylog.testutil

import android.Manifest
import android.os.Build
import androidx.test.rule.GrantPermissionRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

// PermissionChecker requests this on API 19-29 when the log file is read. Its dialog would
// cover MainActivity, which then never reaches RESUMED.
fun grantStoragePermission(): TestRule =
    if (Build.VERSION.SDK_INT in 19..29) GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    else RuleChain.emptyRuleChain()
