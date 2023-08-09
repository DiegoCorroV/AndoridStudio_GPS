package com.example.gps_sms

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.icu.text.SimpleDateFormat
import com.google.android.gms.location.LocationRequest
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editTextPhoneNumber: EditText

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val PERMISSION_REQUEST_SMS_CODE = 456
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextPhoneNumber = findViewById(R.id.phoneNumberEditText)
        val btnSendSMS: Button = findViewById(R.id.getLocationButton)
        btnSendSMS.setOnClickListener {
            requestLocationAndSendSMS()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
    private fun formatTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }
    private fun requestLocationAndSendSMS() {
        val phoneNumber = editTextPhoneNumber.text.toString().trim()

        if (phoneNumber.isEmpty()) {
            showToast("Por favor, ingresa un número de teléfono.")
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Si el permiso ACCESS_FINE_LOCATION ya está otorgado, obtener la ubicación
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val time = location.time
                    val formattedTime = formatTime(time)
                    println(formattedTime) // Imprime una fecha y hora más legible
                    val message = "Latitud: $latitude, Longitud: $longitude, Tiempo: $formattedTime"

                    // Envía el SMS con la información del GPS
                    sendSMS(phoneNumber, message)
                } else {
                    showToast("No se pudo obtener la ubicación.")
                }
            }
        } else {
            // Si el permiso ACCESS_FINE_LOCATION no está otorgado, solicitarlo al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                showToast("SMS enviado.")
            } catch (e: Exception) {
                showToast("Error al enviar el SMS.")
                e.printStackTrace()
            }
        } else {
            // Si el permiso SEND_SMS no está otorgado, solicitarlo al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST_SMS_CODE
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso ACCESS_FINE_LOCATION otorgado, puedes obtener la ubicación y enviar el SMS
                    requestLocationAndSendSMS()
                } else {
                    showToast("Permiso de ubicación denegado. No se pudo obtener la ubicación ni enviar el SMS.")
                }
            }
            PERMISSION_REQUEST_SMS_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso SEND_SMS otorgado, puedes enviar el SMS nuevamente
                    val phoneNumber = editTextPhoneNumber.text.toString().trim()
                    val message = "Tu mensaje aquí" // Personaliza el mensaje como desees
                    sendSMS(phoneNumber, message)
                } else {
                    showToast("Permiso de SMS denegado. No se pudo enviar el SMS.")
                }
            }
        }
    }
}





