package com.hikers.sender

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private val DEVICE_ID = "hikers-tv"
    private lateinit var database: FirebaseDatabase
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        statusText = TextView(this).apply {
            text = "Connecting..."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        root.addView(statusText)

        root.addView(makeButton("POWER") { sendCommand("POWER") })
        root.addView(spacer())

        // D-pad cross layout
        root.addView(makeButton("▲ UP") { sendCommand("DPAD_UP") })

        val middleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        middleRow.addView(makeButton("◄ LEFT") { sendCommand("DPAD_LEFT") })
        middleRow.addView(makeButton("OK") { sendCommand("DPAD_CENTER") })
        middleRow.addView(makeButton("RIGHT ►") { sendCommand("DPAD_RIGHT") })
        root.addView(middleRow)

        root.addView(makeButton("▼ DOWN") { sendCommand("DPAD_DOWN") })
        root.addView(spacer())

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        navRow.addView(makeButton("BACK") { sendCommand("BACK") })
        navRow.addView(makeButton("HOME") { sendCommand("HOME") })
        root.addView(navRow)
        root.addView(spacer())

        val volRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        volRow.addView(makeButton("VOL −") { sendCommand("VOLUME_DOWN") })
        volRow.addView(makeButton("VOL +") { sendCommand("VOLUME_UP") })
        root.addView(volRow)

        setContentView(root)
        listenForConnectionState()
    }

    private fun makeButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 16f
            setPadding(24, 32, 24, 32)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            setOnClickListener { onClick() }
        }
    }

    private fun spacer(): TextView {
        return TextView(this).apply {
            height = 24
        }
    }

    private fun sendCommand(action: String) {
        val timestamp = System.currentTimeMillis()
        val commandRef = database.getReference("remote/$DEVICE_ID/command")
        commandRef.child("action").setValue(action)
        commandRef.child("timestamp").setValue(timestamp)
        statusText.text = "Sent: $action"
        statusText.setTextColor(Color.BLUE)
    }

    private fun listenForConnectionState() {
        database.getReference(".info/connected")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    if (connected) {
                        statusText.text = "Connected — ready to send commands"
                        statusText.setTextColor(Color.parseColor("#008800"))
                    } else {
                        statusText.text = "Disconnected"
                        statusText.setTextColor(Color.RED)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
