package com.example.entradas2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "web_checker.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "config"
        const val COL_URL = "url"
        const val COL_WORD = "word"
        const val COL_SEMAFORO = "semaforo"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_URL TEXT,
                $COL_WORD TEXT,
                $COL_SEMAFORO TEXT
            )
        """
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Método para insertar o actualizar los datos
    fun saveConfig(url: String, word: String, semaforo: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COL_URL, url)
        values.put(COL_WORD, word)
        values.put(COL_SEMAFORO, semaforo)

        // Si los datos ya existen, se actualizarán, de lo contrario, se insertarán nuevos
        db.replace(TABLE_NAME, null, values)
        db.close()
    }

    // Método para obtener los datos
    fun getConfig(): Cursor {
        val db = readableDatabase
        return db.query(TABLE_NAME, null, null, null, null, null, null)
    }
}
