package com.example.leitor_qr_code.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ConnectionFactory extends SQLiteOpenHelper {
    public ConnectionFactory(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table tbcliente(id integer primary key autoincrement, "+
                "nome varchar(60), cpf varchar(20), telefone varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "DROP TABLE IF EXISTS tbcliente";
        db.execSQL(sql);
        onCreate(db);

    }

}
