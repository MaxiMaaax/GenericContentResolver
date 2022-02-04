package com.maximaaax.genericcontentresolver

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.maximaaax.genericcontentresolver.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    companion object {
        val URLS = arrayOf(
            Telephony.Threads.CONTENT_URI.toString(),
            Telephony.Sms.CONTENT_URI.toString(),
            Telephony.Mms.CONTENT_URI.toString(),
            ContactsContract.Contacts.CONTENT_URI.toString(),
            Telephony.Sms.Outbox.CONTENT_URI.toString(),
            Telephony.Sms.Inbox.CONTENT_URI.toString(),
            Telephony.Sms.Sent.CONTENT_URI.toString(),
        )

        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS
        )
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.contains(false)) {
                this.checkAndRequestPermissions()
            } else {
                this.permissionsGranted()
            }
        }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        this.checkAndRequestPermissions()
    }


    private fun checkAndRequestPermissions() {
        if (this.checkMultiplePermissions(PERMISSIONS)) {
            this.permissionsGranted()
        } else {
            requestPermissionLauncher.launch(PERMISSIONS)
        }
    }


    private fun permissionsGranted() {
        viewModel.getTableContent().observe(this) { tableContent ->
            this.updateTableContent(tableContent)
        }


        URLS.sort()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, URLS)
        binding.urlInput.setAdapter(adapter)

        binding.goButton.setOnClickListener {
            viewModel.launchQuery(this, binding.urlInput.text.toString())
        }
    }


    @SuppressLint("Range")
    private fun updateTableContent(tableContent: List<List<String>>?) {
        tableContent?.let {

            binding.emptyCursorTextView.gone()
            binding.table.visible()


            val tableParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )

            val rowParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )


            // Find Tablelayout defined in main.xml
            val table = binding.table

            table.removeAllViews()

            for (line in it) {

                val tableRow = TableRow(this)
                tableRow.layoutParams = rowParams

                for (element in line) {
                    val textView = TextView(this).apply {
                        text = element
                        layoutParams = rowParams
                        setBackgroundResource(R.drawable.row_border)
                        setPadding(16.px)
                    }

                    // Add textView to row.
                    tableRow.addView(textView)
                }

                // Add row to TableLayout.
                //tr.setBackgroundResource(R.drawable.sf_gradient_03);
                table.addView(
                    tableRow,
                    tableParams
                )
            }

        } ?: kotlin.run {
            binding.emptyCursorTextView.visible()
            binding.table.gone()
        }
    }


    private fun checkMultiplePermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }
}