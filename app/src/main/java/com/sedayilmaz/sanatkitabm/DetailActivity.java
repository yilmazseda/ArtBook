package com.sedayilmaz.sanatkitabm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DetailActivity extends AppCompatActivity {
    Bitmap selectedImage;
    ImageView selectImageID;
    EditText artNameText,painterNameText, yearText;
    Button button;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_activiy);
        selectImageID = findViewById(R.id.selectImageID);
        artNameText = findViewById(R.id.artNameText);
        painterNameText = findViewById(R.id.painterNameText);
        yearText =findViewById(R.id.yearText);
        button = findViewById(R.id.buttonID);
        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);


        //Yoolanılan intent'i al
        Intent intent= getIntent();
        String info = intent.getStringExtra("info");//info içinde old ve new var

        if(info.matches("new"))
        {
            //yeni ekleme yapılacak demek
            //textler boş gelmelidir.
            //save butonu olmalıdır çünkü yeni veriler kaydedecek
            artNameText.setText("");
            painterNameText.setText("");
            yearText.setText("");
            button.setVisibility(View.VISIBLE);
            //Görsel eklemek için: eğeer yeni görsel ekelnecekse onun BİTMAP olduğu belli olsun diye
            //decodeResources() : drawable içindeki şeyler decode edilebilir
            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimages);
            selectImageID.setImageBitmap(selectImage);

        }
        else
        {
            //eski bir şey gösterilecek demektir
            //Eski şey bize id'de göndermiş olacak
            int artId = intent.getIntExtra("artId",1);//defaultvalue:1 == eğer yanlış bir işlem olursa ilk resim gösterilsin demektir.
            //buton kayboldun
            button.setVisibility(View.INVISIBLE);

            //Seçilen ID için SQLite'dan ona ait verileri çekicez
            //new String[]{String.valueOf(artId)} :String dizisi içinde selection argğmanı tanımlarız.İstediğimiz argğman artID olduğu için onu tanımladık
            try {
                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id=? ",new String[]{String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");
                while(cursor.moveToNext())
                {
                    //Bu veriler direkt olarak gözükecek hangi id seçildi ise
                    artNameText.setText(cursor.getString((artNameIx)));
                    painterNameText.setText(cursor.getString((painterNameIx)));
                    yearText.setText(cursor.getString((yearIx)));
                    //görsel byte dizi olarak tanımlandığı için onu da o şekilde almamız lazım
                    byte[] bytes = cursor.getBlob(imageIx);
                    //bitmap'e çevir
                    //byte dizisini görsel haline getirir
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);// bytes:data'nın kendisi,bytes.length:datanın uzunluğu
                    selectImageID.setImageBitmap(bitmap);
                }
                cursor.close();

            }
            catch (Exception e)
            {

            }
        }

    }

    public void selectImage(View view)
    {
        //Görsele tıklandığı an galeriye ulaşabilmek için kullanıcıdan izin al
        //izni 1 kere sormak yeterli.Kullanıcı ilk kez uygulamayı kullanınca izin isteriz
        //ContextCompat : API 23 öncesi ve sonrsındaki değişiklikleri bize hissettirmeden çalışmak için geliştirilmiştir
        //23 öncesinde izin istemeye gerek yoktu ama daha sonrdan izin almaya başladılar.
        //Eğer  ContextCompat kullanmazsak 23 ve öncesi sürümlerde uygulama hata verecektir
        //Yani 23 ve sonrasında çalışacak öncesine bulaşmayacak.
        //PERMISSON_GRANTED = İZİN VERİLDİ

        //İzin verilmediyse ne yapalım

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            //String dizisi içinde hangi izini istiyorsam onu yazdım
            //requestCode birden fazl izin isteğinde bulunabiliriz.Hangi isteğe hangi cevap geldi onu kontrol edebilmek için kullanırız
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        //İzin daha önce verildiyse ne yapalım galeriye gidelim
        else
        {
          //yine intent kullanılır fakat bu sefer activitye değil galerye gideriz
            //actıon_pick :topla demek yani git bir yerden bir şey topla(galeriden toplyacapız o yüzden uri verdik)
            Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //uri : dosya adresini belirtiyoruz ve biz onu alıp gelicez böylece dosya nerede kayıtlı anlayacağız
            startActivityForResult(intentToGallery,2); //sonuçta eli dolu gelecek uri ile gelecek
        }
    }
    //Resme tıklasın izin versin direkt olarak galeri açılsın.İzin vermezse hata mesajı versin.
    //bunun için onRequestpermissonResult kullanılır
    //grantResult : verilen değerler
    //grantResult içinde eleman var mı bak .Boş olması için kullanıcı izin vermeyebilir,telefon kapandı gibi

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==1)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                //içinde eleman varsa ve bu elemanın ilki izin verildi ise şunu yap
                Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
                //Kullanıcı izin verdi galeriye git
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //requestCode :2
        //resultCode : okey'i kontrol edicez yani kişi resme tıkladı, okey dedi
        //data : kullanıcının seçtiği data
        if(requestCode ==2  && resultCode== RESULT_OK && data!=null)
        {
            //Kişi resmi seçmiş ve geri gelen veri de null değil o yüzden işlemi yapabiliriz
            Uri imageData = data.getData(); //uri: dosyannın nerede olduğu
            //UnhandledException hatası : try-catch yaz diyor
            try {
                if(Build.VERSION.SDK_INT >=28)
                {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    selectImageID.setImageBitmap(selectedImage);
                }
                else
                {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    selectImageID.setImageBitmap(selectedImage);
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    //Galeriye gittikten sonra görsel seçilecek bu görsel seçildikten sonra ne yapılacak bu tanımlanck
    public void save(View view)
    {
        //Verileri SQLite'a kaydedeicez
        String artName = artNameText.getText().toString();
        String painterName = painterNameText.getText().toString();
        String year = yearText.getText().toString();

        //yeni bitmap'i tanımlayalım
        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        //Görseli veritabanına kaydetmek için veriye çevirmemiz gerekiyor.
        //Görsel bitmap olarak veritabanına kaydedilmez
        //compress(): veriyeçevirme işlemini yaparken kullancağımı özellikleri belirtmemize yarar
        //compress() bizden şunları ister : hangi formatta çevireyim (png,jpeg),kalitesi ne olsun(0,100),outputstream:byte dizisi halien getirmek için
        //1mb üzeri görselle çalışınca SQLite çökebilir.O yzğden görselleri küçülteceğiz

        //Outputstream: Java’daki çıkış işlemlerini sağlar. Bir yerden başka bir yere akan verilerdir. Bu veriler byte dizileri halindedir.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray(); //bytedizisi veriye çevrilmiş oldu Yani görsel veriye çevrilmiş oldu.

        //Veritabanı oluşturup verileri kaydedeceğiz
        try {
            //db oluşturalım
             database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            //table oluştur
            //byte dizsi kaydedeceğimiz için BLOB olarak tanımladık
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR ,paintername VARCHAR,year VARCHAR,image BLOB)");
            //değerleri static olarak yazamayız yabi value içinde(eski tanımalma gibi)
            //Girilecek değerler belli değil.Kullanıcı ne isterse onu kaydedecek bu yüzden ? kulladndık

            //? değerleri ayrı ayrı atılacak fakay exeSQL komutu ile yapılamayacak
            String sqlString = "INSERT INTO arts(artname,paintername,year,image) VALUES (?,?,?,?)";
            //sqlString string ifadesini SQL içerisinde çalışacak duruma çevireceğiz bu da SQLiteStatement ile yapılır
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);//stringi sql komutu gibi çalıştırır
            sqLiteStatement.bindString(1,artName); //? leri değişken ile bağlanacak (bindString ile)
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);//Image'leri bağladık.
            sqLiteStatement.execute(); //çalıştır
        }
        catch (Exception e)
        {

        }
        //Verileri aldık ve bir önceki aktiviteye geri döecez
        //finish(); //aktiviteyi komple kapatır.Hafıza olarak da verimli olacaktır
        Intent intent = new Intent(DetailActivity.this,MainActivity.class);
        //Daha önce açık olan tüm aktiviteleri kapat
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//daha önceki tüm aktiviteleri kapatır.
        startActivity(intent);


    }
    //Görsellerin küçük olduğuna emin olmak için yeni bir metot yazacağız.Çünkü kullanıcı çok büyük
    //boyutlu görseleri de indirip seçebilir.


     //ASLINDA BURADA YENİ BİTMAP OLUŞTURDUK

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize)
    {
    //büyük görselli yazıcaz verilern image'e maxsize verilir ki en fazla bu kadar boyutta olsun diyoruz
    //seçilen görsel yatay ise (1500*1200) biz diyoruz ki bunu şu boyutta al (300,200)
    //böylece alandan tasarrfu ederiz
    //öncelikli olarak görsel yatay mı dikey mi buna bakacağız

    int width = image.getWidth();
    int height = image.getHeight();
    float bitmapRatio = (float)width/ (float)height; //300.0/200.0 gibi
    //bitmapRatio> 1 ise genişlik(width) daha büyük yani görsel yataydır.Değilse dikeydir
    if(bitmapRatio > 1)
    {
        //resim yataydır
        //width maxsize olmalı
        width = maximumSize;
        height =(int)(width/ bitmapRatio);//height aynı oranda küçülmelidir
    }
    else
    {
        //resim dikey
        //height max olmalı
        height = maximumSize;
        width=(int)(height*bitmapRatio);//width aynı oranda küçülmelidir
    }
    return Bitmap.createScaledBitmap(image,width,height,true); //hangi görseli küçülteyim , hangi boyutta yapayım,filter yapayım mı diyor









    }



}