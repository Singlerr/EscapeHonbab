package io.github.eh.eh

import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import io.github.eh.eh.asutils.IAlertDialog
import io.github.eh.eh.asutils.Utils
import io.github.eh.eh.db.LoginDatabase
import io.github.eh.eh.http.HTTPBootstrap
import io.github.eh.eh.http.HTTPContext
import io.github.eh.eh.http.HttpStatus
import io.github.eh.eh.http.StreamHandler
import io.github.eh.eh.http.bundle.RequestBundle
import io.github.eh.eh.http.bundle.ResponseBundle
import io.github.eh.eh.serverside.User
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {
    private var instance: LoginActivity? = null
    private lateinit var databaseHelper: LoginDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Slide(Gravity.RIGHT)
        }
        setContentView(R.layout.activity_login)
        instance = this

        databaseHelper = LoginDatabase(this, "session", null, 1)
        if (databaseHelper.sessionExists()) {
            var cursor = databaseHelper.select()
            cursor.moveToFirst()
            var id = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("id"))
            var password = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("password"))
            if (id != null && password != null) {
                val bootstrap: HTTPBootstrap = HTTPBootstrap.builder()
                    .port(Env.HTTP_PORT)
                    .host(Env.API_URL)
                    .streamHandler(object : StreamHandler {
                        override fun onWrite(outputStream: HTTPContext) {
                            val user = User()
                            user.userId = id
                            user.password = password
                            var req = RequestBundle()
                            req.setMessage(user)
                            outputStream.write(req)
                        }

                        override fun onRead(obj: Any?) {
                            if (obj is ResponseBundle) {
                                if (obj.responseCode == HttpStatus.SC_OK) {
                                    var user = obj.getMessage(User::class.java)
                                    intentSupport(user)
                                } else {

                                    loginFailed()
                                }
                            }
                        }

                    }).build()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        bootstrap.submit()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        loginFailedIO()
                    }
                }
            }
        }

        // login button
        btn_login.setOnClickListener {
            val id = etv_id.text.toString()
            val pw = etv_password.text.toString()
            val bootstrap: HTTPBootstrap = HTTPBootstrap.builder()
                .port(Env.HTTP_PORT)
                .host(Env.API_URL)
                .streamHandler(object : StreamHandler {
                    override fun onWrite(outputStream: HTTPContext) {
                        val user = User()
                        user.userId = id
                        user.password = pw
                        var req = RequestBundle()
                        req.setMessage(user)
                        outputStream.write(req)
                    }

                    override fun onRead(obj: Any?) {
                        if (obj is ResponseBundle) {
                            if (obj.responseCode == HttpStatus.SC_OK) {
                                var user = obj.getMessage(User::class.java)
                                intentSupport(user)
                                databaseHelper.insertOrUpdate(id, pw)
                            } else {
                                loginFailed()
                            }
                        }
                    }

                }).build()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    bootstrap.submit()
                } catch (e: Exception) {
                    loginFailedIO()
                }
            }
        }
        // go to RegisterActivity
        btn_moveToRegister.setOnClickListener {
            val toregisterintent = Intent(this, RegisterActivity::class.java)
            startActivity(toregisterintent)
        }

        // go to FindPassWordActivity
        btn_findPassword.setOnClickListener {
            val tofindpwintent = Intent(this, FindPasswordActivity::class.java)
            startActivity(tofindpwintent)
        }
        btn_previousPage.setOnClickListener {
            var dialog = IAlertDialog.Builder(instance)
                .message("????????? ?????? ?????????????????????????")
                .title("??????")
                .positiveButton("??????") { _, _ ->
                    run {
                        finishAndRemoveTask()
                        exitProcess(0)
                    }
                }
                .negativeButton("??????") { _, _ -> //TODO

                }.create()
            dialog.show()
        }

    }

    //go to MainActivity
    private fun intentSupport(user: User?) {
        val tomainintent = Intent(this, MainActivity::class.java)
        Utils.setEssentialData(
            intent = tomainintent,
            user = user,
            className = this::class.java.name
        )
        startActivity(tomainintent)
    }

    //Toast message when login failed
    private fun loginFailed() {
        msg_error.text = "??????????????? ???????????? ????????????."
        //Utils.showMessageBox(this,"????????? ??????","????????? ??????, ???????????? ??????????????? ?????? ??????????????????.",AlertDialog.BUTTON_POSITIVE)
        //Toast.makeText(this, "????????? ??????, ???????????? ??????????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
    }

    private fun loginFailedIO() {
        msg_error.text = "???????????? ??? ??? ????????????."
    }
}


