package com.xzy.sendmsgdemo

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val sendSMS = 0x11
    private var stringBuffer: StringBuffer = StringBuffer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        btn_send.setOnClickListener {
            val stringBufferLength = stringBuffer.length
            stringBuffer.delete(0, stringBufferLength)
            sendSMS()
        }
    }

    private fun requestPermission() {
        //判断Android版本是否大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            writeConsole("动态请求权限")
            val checkCallPhonePermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            val checkReadPhoneStatePermission = ContextCompat
                .checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED
                || checkReadPhoneStatePermission != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE),
                    sendSMS
                )
                return
            } else {
                writeConsole("已获取相关权限")
                //sendSMS()
                //已有权限
            }
        } else {
            //API 版本在23以下直接发送短息
            writeConsole("系统版本在 23 以下，无需请求权限")
            //sendSMS()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            sendSMS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeConsole("已获取相关权限")
                //sendSMS()
            } else {
                // Permission Denied
                Toast.makeText(this@MainActivity, "权限被拒", Toast.LENGTH_SHORT)
                    .show()
                writeConsole("权限被拒")

            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    //发送短信
    private fun sendSMS() {
        if (!checkIsRealPhone()) {
            writeConsole("检测到是模拟器环境，无法发送短信")
            writeConsole("短信发送失败")
            return
        }
        if (!checkHasSimCard(this)) {
            writeConsole("没有检测到 SIM 卡")
            writeConsole("短信发送失败")
            return
        }
        val content: String = et_msg_text.text.toString().trim()
        val phone: String = et_phoneNumber.text.toString().trim()
        if (content.isEmpty() && phone.isEmpty()) {
            Toast.makeText(this, "手机号或内容不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        writeConsole("开始发送短信")
        val manager: SmsManager = SmsManager.getDefault()
        val strings: ArrayList<String> = manager.divideMessage(content)
        for (i in strings.indices) {
            manager.sendTextMessage(phone, null, content, null, null)
        }
        Toast.makeText(this@MainActivity, "短信指令已发送，请检查手机是否收到短信", Toast.LENGTH_SHORT).show()
        writeConsole("短信指令已发送，请检查手机是否收到短信")
    }

    private fun writeConsole(str: String) {
        stringBuffer.append(str).append("\n")
        tv_log.text = stringBuffer.toString()
    }

    /**
     * 判断是否是真机 -- 通过 bt 的有无或者 bt 的名称判断
     * */
    private fun checkIsRealPhone(): Boolean {
        val bt = BluetoothAdapter.getDefaultAdapter()
        return if (bt == null) {
            false
        } else {
            val btName = bt.name
            btName.isNotEmpty()
        }
    }


    @SuppressLint("MissingPermission", "HardwareIds")
    fun checkHasSimCard(context: Context): Boolean {
        val tm = context.applicationContext
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simSer = tm.simSerialNumber
        return simSer.isNotEmpty()
    }

}