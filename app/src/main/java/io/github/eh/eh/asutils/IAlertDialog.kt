package io.github.eh.eh.asutils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import io.github.eh.eh.R

class IAlertDialog(private val context: Context?) {
    class Builder(private val context: Context?) {
        private var dialog: Dialog? = null

        init {
            dialog = Dialog(context!!)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.setCancelable(true)
            dialog!!.setContentView(R.layout.dialog)
            dialog!!.findViewById<Button>(R.id.yes).isVisible = false
            dialog!!.findViewById<Button>(R.id.no).isVisible = false
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

        fun message(msg: String?): Builder {
            dialog!!.findViewById<TextView>(R.id.message).text = msg
            return this
        }

        fun title(title: String?): Builder {
            dialog!!.findViewById<TextView>(R.id.title).text = title
            return this
        }

        fun positiveButton(text: String, listener: DialogInterface.OnClickListener): Builder {
            var b = dialog!!.findViewById<Button>(R.id.yes)
            b.text = text
            b.setOnClickListener {
                listener.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            }
            b.isVisible = true
            return this
        }

        fun negativeButton(text: String, listener: DialogInterface.OnClickListener): Builder {
            var b = dialog!!.findViewById<Button>(R.id.no)
            b.text = text
            b.setOnClickListener {
                listener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
            }
            b.isVisible = true
            return this
        }

        fun create(): Dialog {
            return dialog!!
        }
    }
}
