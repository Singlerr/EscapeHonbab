package io.github.eh.eh

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.github.eh.eh.asutils.IAlertDialog
import io.github.eh.eh.asutils.Utils
import io.github.eh.eh.http.HTTPBootstrap
import io.github.eh.eh.http.HTTPContext
import io.github.eh.eh.http.HttpStatus
import io.github.eh.eh.http.StreamHandler
import io.github.eh.eh.http.bundle.RequestBundle
import io.github.eh.eh.http.bundle.ResponseBundle
import io.github.eh.eh.http.bundle.VerificationBundle
import io.github.eh.eh.serverside.User
import kotlinx.android.synthetic.main.activity_verification.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*
import kotlin.collections.HashSet

class VerificationActivity : AppCompatActivity() {
    private lateinit var user: User
    private var timer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            exitTransition = Slide(Gravity.RIGHT)
        }
        setContentView(R.layout.activity_verification)
        user = Utils.getUser(intent)!!
        user.blackList = HashSet()
        user.friends = HashSet()
        etv_verificationPhoneNumber.setText(user.phoneNumber, TextView.BufferType.EDITABLE)
        startTimer()

        btn_reRequest.setOnClickListener {
            resetTimer()
            var http = HTTPBootstrap.builder()
                .host(Env.REQ_AUTH_CODE_API)
                .port(Env.HTTP_PORT)
                .streamHandler(object : StreamHandler {
                    override fun onWrite(outputStream: HTTPContext) {
                        var reqBundle = RequestBundle()
                        reqBundle.setMessage(user)
                        outputStream.write(reqBundle)
                    }

                    override fun onRead(obj: Any?) {
                        if (obj is ResponseBundle) {
                            if (obj.responseCode == HttpStatus.SC_OK) {
                                var intent =
                                    Intent(applicationContext, VerificationActivity::class.java)
                                Utils.setEssentialData(intent, user, this::class.java.name)
                                startActivity(intent)
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    var dialog = IAlertDialog.Builder(applicationContext)
                                        .title("??????")
                                        .message("????????? ????????? ??????????????????. ?????? ??? ?????? ??????????????????.")
                                        .positiveButton("??????") { _, _ -> finish() }.create()
                                    dialog.show()
                                }
                            }
                        }
                    }
                }).build()
            CoroutineScope(Dispatchers.IO).launch {
                http.submit()
            }
        }
        btn_moveToProfileSetting.setOnClickListener {
            var code = etv_verificationCode.text.toString()
            if (!code.matches(Regex("\\d{5}"))) {
                var dialog = IAlertDialog.Builder(this)
                    .message("???????????? ????????? ?????? ??????????????????(???: 12345)")
                    .title("??????")
                    .positiveButton("??????") { dialog, _ ->
                        dialog.dismiss()
                    }.create()
                dialog.show()
                return@setOnClickListener
            }

            var http = HTTPBootstrap.builder()
                .port(Env.HTTP_PORT)
                .host(Env.AUTH_CHK_API_URL)
                .streamHandler(object : StreamHandler {
                    override fun onWrite(outputStream: HTTPContext) {
                        var bundle = VerificationBundle(user.phoneNumber!!, code)
                        outputStream.write(bundle)
                    }

                    override fun onRead(obj: Any?) {
                        if (obj is ResponseBundle) {
                            if (obj.responseCode == HttpStatus.SC_OK) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    var dialog = IAlertDialog.Builder(this@VerificationActivity)
                                        .title("??????")
                                        .message("????????? ?????????????????????.")
                                        .positiveButton("??????") { _, _ ->
                                            var intent = Intent(
                                                this@VerificationActivity,
                                                ProfileSettingActivity::class.java
                                            )
                                            user.userId = user.phoneNumber
                                            Utils.setEssentialData(
                                                intent,
                                                user,
                                                this::class.java.name
                                            )
                                            startActivity(intent)
                                        }.create()
                                    dialog.show()
                                }
                            } else {
                                when (obj.responseCode) {
                                    HttpStatus.SC_NOT_FOUND -> {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            var dialog =
                                                IAlertDialog.Builder(this@VerificationActivity)
                                                    .title("??????")
                                                    .message("????????? ??????????????????.")
                                                    .positiveButton("??????") { dialog, _ ->
                                                        dialog.dismiss()
                                                    }.create()
                                            dialog.show()
                                        }
                                    }
                                    HttpStatus.SC_BAD_REQUEST -> {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            var dialog =
                                                IAlertDialog.Builder(this@VerificationActivity)
                                                    .title("??????")
                                                    .message("????????? ??????????????????.")
                                                    .positiveButton("??????") { dialog, _ ->
                                                        dialog.dismiss()
                                                    }.create()
                                            dialog.show()
                                        }
                                    }
                                    else -> {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            var dialog =
                                                IAlertDialog.Builder(this@VerificationActivity)
                                                    .title("??????")
                                                    .message("????????? ????????? ??????????????????. ?????? ??? ?????? ??????????????????.")
                                                    .positiveButton("??????") { _, _ ->
                                                        finish()
                                                    }.create()
                                            dialog.show()
                                        }
                                    }
                                }

                            }
                        }
                    }

                }).build()
            CoroutineScope(Dispatchers.IO).launch {
                http.submit()
            }

        }
    }

    private fun resetTimer() {
        timer = object :
            CountDownTimer(Env.VERIFICATION_TIME_OUT.toLong(), 1000) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTick(p0: Long) {
                var second = p0 / 1000
                var date = LocalTime.ofSecondOfDay(second)
                timeLeft.text = date.toString()

            }

            override fun onFinish() {
                var dialog = IAlertDialog.Builder(this@VerificationActivity)
                    .title("?????? ??????")
                    .message("?????? ????????? ?????????????????????. ?????? ???????????????.")
                    .positiveButton("??????") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                dialog.create().show()
                timeLeft.text = ""
            }
        }
        timeLeft.text = ""
    }

    private fun startTimer() {
        if (timer == null) {
            timer = object :
                CountDownTimer(Env.VERIFICATION_TIME_OUT.toLong(), 1000) {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onTick(p0: Long) {
                    var second = p0 / 1000
                    var date = LocalTime.ofSecondOfDay(second)
                    timeLeft.text = date.toString()

                }

                override fun onFinish() {
                    var dialog = IAlertDialog.Builder(this@VerificationActivity)
                        .title("?????? ??????")
                        .message("?????? ????????? ?????????????????????. ?????? ???????????????.")
                        .positiveButton("??????") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                    dialog.create().show()
                    timeLeft.text = ""
                }
            }
            timer!!.start()
        } else {
            timer!!.start()
        }
    }

    private fun stopTimer() {
        if (timer != null)
            timer!!.cancel()
    }

    override fun onBackPressed() {
        IAlertDialog.Builder(this)
            .title("??????")
            .message("?????? ????????? ?????????????????????????")
            .positiveButton("???") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .negativeButton("?????????") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }
}