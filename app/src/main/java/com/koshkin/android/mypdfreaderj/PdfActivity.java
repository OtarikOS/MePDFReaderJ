package com.koshkin.android.mypdfreaderj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class PdfActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String CURRENT_PAGE ="";

    private String path;
    private Button previous,next;
    private ImageButton min,max;
    private int currentPage;
    private ImageView imageView;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page curPage;
    private ParcelFileDescriptor descriptor;
    private  float currentZoomLevel =5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        path =getIntent().getStringExtra("fileName");
        setTitle(getIntent().getStringExtra("keyName"));

        // если в банлде есть номер страницы - забираем его
        if(savedInstanceState != null){
            currentPage = savedInstanceState.getInt(CURRENT_PAGE,0);
        }

        previous =findViewById(R.id.btn_preview);
        next= findViewById(R.id.btn_next);
        min = findViewById(R.id.min);
        max = findViewById(R.id.max);
        imageView = findViewById(R.id.image_view);

        // устанавливаем слушатели на кнопки
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        min.setOnClickListener(this);
        max.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            openPdfRenderer();
            displayPage(currentPage);
        } catch (Exception e) {
            Toast.makeText(this,"Файл защищен паролем",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(curPage!= null){
            outState.putInt(CURRENT_PAGE,curPage.getIndex());
        }
    }

    @Override
    protected void onStop() {
        try {
            closePdfRenderer();
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onStop();
    }

    private void closePdfRenderer() throws IOException {
        if(curPage!= null) curPage.close();
        if(pdfRenderer != null) pdfRenderer.close();
        if (descriptor != null) descriptor.close();
    }

    private void openPdfRenderer() {
        File file = new File(path);
        descriptor = null;
        pdfRenderer = null;
        try{
            descriptor = ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer= new PdfRenderer(descriptor);
        } catch (Exception e) {
            Toast.makeText(this,"Ошибка",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_preview:{
                //получаем индекс предыдущей страницы
                int index = curPage.getIndex()-1;
                displayPage(index);
                break;
            }case R.id.btn_next:{
                int index =curPage.getIndex()+1;
                displayPage(index);
                break;
            }
            // увеличиваем и уменьшаем зум
            case R.id.max:{
                ++currentZoomLevel;
                displayPage(curPage.getIndex());
                break;
            } case R.id.min:{
                --currentZoomLevel;
                displayPage(curPage.getIndex());
                break;
            }
        }
    }

    private void displayPage(int index) {
        if (pdfRenderer.getPageCount() <= index) return;
        // закрываем текущую страницу
        if (curPage != null) curPage.close();
        // открываем нужную страницу
        curPage = pdfRenderer.openPage(index);
        // определяем размеры Bitmap
        int newWidth = (int) (getResources().getDisplayMetrics().widthPixels * curPage.getWidth() / 72
                * currentZoomLevel / 40);
        int newHeight =
                (int) (getResources().getDisplayMetrics().heightPixels * curPage.getHeight() / 72
                        * currentZoomLevel / 64);
        Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        float dpiAdjustedZoomLevel = currentZoomLevel * DisplayMetrics.DENSITY_MEDIUM
                / getResources().getDisplayMetrics().densityDpi;
        matrix.setScale(dpiAdjustedZoomLevel, dpiAdjustedZoomLevel);
        curPage.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // отображаем результат рендера
        imageView.setImageBitmap(bitmap);
        // проверяем, нужно ли делать кнопки недоступными
        int pageCount = pdfRenderer.getPageCount();
        previous.setEnabled(0 != index);
        next.setEnabled(index + 1 < pageCount);
        min.setEnabled(currentZoomLevel != 2);
        max.setEnabled(currentZoomLevel != 12);
    }
}