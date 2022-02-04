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

    private var mOffset = 0

    private lateinit var mUri: String

    val LIMIT = 20

    var mRowCount: Int? = null


    fun getTableContent(): LiveData<List<List<String>>?> {
        return tableContent
    }


    fun launchQuery(context: Context, uri: String) {
        this.mOffset = 0
        this.mUri = uri

        viewModelScope.launch {
            tableContent.postValue(fetchContentResolver(context, uri, mOffset))
        }
    }


//    @SuppressLint("Range")
    private suspend fun fetchContentResolver(context: Context, uri: String, offset: Int): List<List<String>>? {
        return withContext(Dispatchers.IO) {

            val rootList = mutableListOf<List<String>>()


            val cursor = context.contentResolver.query(
                Uri.parse(uri), null, null, null, "_id LIMIT $LIMIT OFFSET ${offset*LIMIT}"
            )


            if ((cursor != null) && (cursor.count > 0)) {

                if (mOffset == 0){
                    mRowCount = cursor.count
                }

                //HEADER
                val header = mutableListOf<String>()
                for (col in cursor.columnNames) {
                    header.add(col)
                }
                rootList.add(header)

                while (cursor.moveToNext()) {
                    val line = mutableListOf<String>()

                    for (i in 0 until cursor.columnCount) {

                        val value = when(cursor.getType(i)){
                            Cursor.FIELD_TYPE_BLOB -> cursor.getBlobOrNull(i).toString()
                            Cursor.FIELD_TYPE_FLOAT -> cursor.getFloatOrNull(i).toString()
                            Cursor.FIELD_TYPE_INTEGER -> cursor.getIntOrNull(i).toString()
                            Cursor.FIELD_TYPE_STRING -> cursor.getStringOrNull(i).toString()
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

    fun previous(context: Context) {
        if (this.mOffset <= 0){
            return
        }
        this.mOffset--

        viewModelScope.launch {
            tableContent.postValue(fetchContentResolver(context, mUri, mOffset))
        }
    }

    fun next(context: Context) {
        if (this.mOffset*this.LIMIT > this.mRowCount!!){
            return
        }

        this.mOffset++

        viewModelScope.launch {
            tableContent.postValue(fetchContentResolver(context, mUri, mOffset))
        }
    }
}