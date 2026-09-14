package com.haruma.health.kit.ui.privacy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity

class PrivacyRationaleActivity : ComponentActivity() {

    private val privacyPolicyUrl = "https://github.com/haruma/health-kit/blob/main/PRIVACY_POLICY.md"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
        startActivity(intent)
        finish()
    }
}
