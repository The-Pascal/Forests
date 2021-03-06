package com.example.forests.actionsActivities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.forests.R
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_send_referral.*

class SendReferral : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_referral)

//        createLink()
        send_referral_btn.setOnClickListener {
//            sendInvitation()
        }
    }
//
//    private var mInvitationUrl: Uri? = null
//
//    fun createLink() {
//        val uid = FirebaseAuth.getInstance().uid
//        val invitationLink = "https://forests.page.link/?invitedby=$uid"
//        val f = FirebaseDynamicLinks.getInstance().createDynamicLink()
//        f.link = Uri.parse(invitationLink)
//        f.domainUriPrefix = "https://forests.page.link"

//        Firebase.dynamicLinks.shortLinkAsync {
//            link = Uri.parse(invitationLink)
//            domainUriPrefix = "https://example.page.link"
//            androidParameters("com.example.android") {
//                minimumVersion = 125
//            }
//            iosParameters("com.example.ios") {
//                appStoreId = "123456789"
//                minimumVersion = "1.0.1"
//            }
//        }.addOnSuccessListener { shortDynamicLink ->
//            mInvitationUrl = shortDynamicLink.shortLink
//            // ...
//        }
        // [END ddl_referral_create_link]
    }

//    fun sendInvitation() {
//        // [START ddl_referral_send]
//        val subject = String.format("%s wants you to play MyExampleGame!", "referrerName")
//        val invitationLink = mInvitationUrl.toString()
//        val msg = "Let's play MyExampleGame together! Use my referrer link: $invitationLink"
////        val msgHtml = String.format("<p>Let's play MyExampleGame together! Use my " +
////                "<a href=\"%s\">referrer link</a>!</p>", invitationLink)
//
//        val intent = Intent(Intent.ACTION_SENDTO).apply {
//            data = Uri.parse("mailto:") // only email apps should handle this
//            putExtra(Intent.EXTRA_SUBJECT, subject)
//            putExtra(Intent.EXTRA_TEXT, msg)
////            putExtra(Intent.EXTRA_HTML_TEXT, msgHtml)
//        }
//        intent.resolveActivity(packageManager)?.let {
//            startActivity(intent)
//        }
//        // [END ddl_referral_send]
//    }
//}