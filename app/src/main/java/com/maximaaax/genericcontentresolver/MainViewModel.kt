package com.maximaaax.genericcontentresolver

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.core.database.getBlobOrNull
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val tableContent: MutableLiveData<List<List<String>>?> = MutableLiveData(null)


    fun getTableContent(): LiveData<List<List<String>>?> {
        return tableContent
    }


    fun launchQuery(context: Context, uri: String) {
        viewModelScope.launch {
            tableContent.postValue(fetchContentResolver(context, uri))
        }
    }


    @SuppressLint("Range")
    private suspend fun fetchContentResolver(context: Context, uri: String): List<List<String>>? {
        return withContext(Dispatchers.IO) {

            val rootList = mutableListOf<List<String>>()


            val cursor = context.contentResolver.query(
                Uri.parse(uri), null, null, null, null
            )


            if ((cursor != null) && (cursor.count > 0)) {

                //HEADER
                val header = mutableListOf<String>()
                for (col in cursor.columnNames) {
                    header.add(col)
                }
                rootList.add(header)

                while (cursor.moveToNext()) {
                    val line = mutableListOf<String>()

                    for (col in cursor.columnNames) {

                        val value = when(val index = cursor.getType(cursor.getColumnIndex(col))){
                            Cursor.FIELD_TYPE_BLOB -> cursor.getBlobOrNull(index).toString()
                            Cursor.FIELD_TYPE_FLOAT -> cursor.getFloatOrNull(index).toString()
                            Cursor.FIELD_TYPE_INTEGER -> cursor.getIntOrNull(index).toString()
                            Cursor.FIELD_TYPE_STRING -> cursor.getStringOrNull(index).toString()
                            else -> {
                                "null"
                            }
                        }

                        line.add(value)
                    }

                    rootList.add(line)
                }

                cursor.close()

                rootList

            } else {
                cursor?.close()

                null
            }
        }
    }
}