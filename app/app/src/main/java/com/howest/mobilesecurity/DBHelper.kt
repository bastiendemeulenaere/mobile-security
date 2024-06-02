package com.howest.mobilesecurity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_NAME = "my_database.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, password TEXT)")
        db?.execSQL("CREATE TABLE user_pokemon (id INTEGER PRIMARY KEY, user_id INTEGER, pokemon_name TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        db?.execSQL("DROP TABLE IF EXISTS user_pokemon")
        onCreate(db)
    }

    class User(val id: Int, val name: String, val password: String)

    fun insertUser(name: String, password: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("name", name)
            put("password", password)
        }
        db.insert("users", null, values)

        db.close()
    }

    fun savePokemonForUser(userId: Int, pokemonName: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("user_id", userId)
            put("pokemon_name", pokemonName)
        }
        db.insert("user_pokemon", null, values)

        db.close()
    }

    fun getUserPokemon(userId: Int): List<String> {
        val db = readableDatabase
        val pokemonList = mutableListOf<String>()

        val projection = arrayOf("pokemon_name")
        val selection = "user_id = ?"
        val selectionArgs = arrayOf(userId.toString())
        val cursor = db.query("user_pokemon", projection, selection, selectionArgs, null, null, null)

        while (cursor.moveToNext()) {
            val pokemonName = cursor.getString(cursor.getColumnIndexOrThrow("pokemon_name"))
            pokemonList.add(pokemonName)
        }

        cursor.close()
        return pokemonList
    }

    @SuppressLint("Recycle")
    fun checkPasswordMatch(name: String, pwd: String): Boolean {
        val db = readableDatabase

        val projection = arrayOf("password")
        val selection = "name = ?"
        val selectionArgs = arrayOf(name)
        val cursor = db.query("users", projection, selection, selectionArgs, null, null, null)

        return if (cursor.moveToFirst()) {
            val passwordFromDb = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            pwd == passwordFromDb
        } else {
            false
        }
    }

    fun getUserByUsername(username: String): User? {
        val db = readableDatabase

        val projection = arrayOf("id", "name", "password")
        val selection = "name = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query("users", projection, selection, selectionArgs, null, null, null)

        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            User(id, name, password)
        } else {
            null
        }
    }


    @SuppressLint("Recycle")
    fun checkForUserInUse(name: String): Boolean {
        val db = readableDatabase

        val projection = arrayOf("name")
        val selection = "name = ?"
        val selectionArgs = arrayOf(name)
        val cursor = db.query("users", projection, selection, selectionArgs, null, null, null)

        return if (cursor.moveToFirst()) {
            val nameFromDb = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            name == nameFromDb
        } else {
            false
        }
    }

    fun addCurrentUserToFirebase(name: String) {
        val db = readableDatabase

        val projection = arrayOf("id", "name", "password")
        val selection = "name = ?"
        val selectionArgs = arrayOf(name)
        val cursor = db.query("users", projection, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val username = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val dbPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))

            val database = FirebaseDatabase.getInstance()

            val ref: DatabaseReference = database.getReference("users/$username")

            val currentUser = hashMapOf(
                "username" to username,
                "password" to dbPassword
            )
            ref.setValue(currentUser)
        }
        cursor.close()
    }
}




