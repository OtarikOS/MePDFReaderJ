package com.koshkin.android.mypdfreaderj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuAdapter;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private static final int REQUEST_PERMISSION = 1;

    private ArrayList<PdfFile> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            initViews();
        }
    }

    private void initList(String path) {
        try {
            File file = new File(path);
            File[] fileList = file.listFiles();
            String fileName;
            for (File f : fileList) {
                if (f.isDirectory()) {
                    initList(f.getAbsolutePath());
                } else {
                    fileName = f.getName();
                    if (fileName.endsWith(".pdf")) {
                        arrayList.add(new PdfFile(fileName, f.getAbsolutePath()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initViews();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initViews();
                } else {
                    // в разрешении отказано (в первый раз, когда чекбокс "Больше не спрашивать" ещё не показывается)
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        finish();
                    } else {
                        // в разрешении отказано (выбрано "Больше не спрашивать")
                        // показываем диалог, сообщающий о важности разрешения
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Вы отказались предоставлять разрешение на чтение хранилища.\n\nЭто необходимо для работы приложения."
                                + "\n\n"
                                + "Нажмите \"Предоставить\", чтобы предоставить приложению разрешения.")

                        // при согласии откроется окно настроек, в котором пользователю нужно будет вручную предоставить разрешения

                                .setPositiveButton("Предоставить", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package",getPackageName(),null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                })
                        // закрываем приложение
                                .setNegativeButton("Отказаться", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                        builder.setCancelable(false);
                        builder.create().show();
                    }
                }
                break;
            }
        }
    }

    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public PdfFile getItem(int position) {
            return (PdfFile) arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null){
                view =getLayoutInflater().inflate(R.layout.list_item,parent,false);
            }

            PdfFile pdfFile =getItem(position);
            TextView name = view.findViewById(R.id.text_file_name);
            name.setText(pdfFile.getFileName());
            return view;
        }
    };

    private void initViews() {
        // получаем путь до внешнего хранилища
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        initList(path);
        // устанавливаем адаптер в ListView
        listView.setAdapter(adapter);
        // когда пользователь выбирает PDF-файл из списка, открываем активность для просмотра
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,PdfActivity.class);
                intent.putExtra("keyName",arrayList.get(position).getFileName());
                intent.putExtra("fileName", arrayList.get(position).getFilePath());
                startActivity(intent);
            }
        });
    }



















}
