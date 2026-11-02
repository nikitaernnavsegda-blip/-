package com.example.flashlightprank

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var torchOn = false
    private var dialog: AlertDialog? = null
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var button: Button
    private lateinit var hint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        buildUi()

        if (cameraId == null) {
            Toast.makeText(this, "На этом устройстве нет вспышки", Toast.LENGTH_LONG).show()
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#101114"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val size = (resources.displayMetrics.density * 180).toInt()
        button = Button(this).apply {
            text = "ВКЛ"
            textSize = 28f
            setTextColor(Color.WHITE)
            background = circle(Color.parseColor("#2A2D34"))
            layoutParams = LinearLayout.LayoutParams(size, size)
            setOnClickListener { onButtonClick() }
        }

        hint = TextView(this).apply {
            text = "Нажмите, чтобы включить"
            setTextColor(Color.parseColor("#8A8F9A"))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, (resources.displayMetrics.density * 24).toInt(), 0, 0)
        }

        root.addView(button)
        root.addView(hint)
        setContentView(root)
    }

    private fun circle(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun onButtonClick() {
        if (cameraId == null) return
        if (torchOn) {
            setTorch(false)
        } else {
            setTorch(true)
            handler.postDelayed({ if (torchOn) showPrankDialog() }, 1000)
        }
    }

    private fun setTorch(enable: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, enable)
            torchOn = enable
            button.text = if (enable) "ВЫКЛ" else "ВКЛ"
            button.background = circle(
                if (enable) Color.parseColor("#F5C518") else Color.parseColor("#2A2D34")
            )
            button.setTextColor(if (enable) Color.BLACK else Color.WHITE)
            hint.text = if (enable) "Фонарик включён" else "Нажмите, чтобы включить"
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось переключить фонарик", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPrankDialog() {
        if (dialog?.isShowing == true) return

        dialog = AlertDialog.Builder(this)
            .setTitle("Фонарик Premium")
            .setMessage(
                "Чтобы выключить фонарик, оформите подписку за 499 ₽/мес.\n\n" +
                    "(Это шутка, оплаты в приложении нет.)"
            )
            .setPositiveButton("Оплатить 499 ₽") { d, _ ->
                Toast.makeText(this, "Шутка! Ничего не списано 😄", Toast.LENGTH_LONG).show()
                setTorch(false)
                d.dismiss()
            }
            .setNegativeButton("Закрыть") { d, _ ->
                setTorch(false)
                d.dismiss()
            }
            .setCancelable(true)
            .setOnCancelListener { setTorch(false) }
            .show()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
        dialog?.dismiss()
        if (torchOn) setTorch(false)
    }
}
