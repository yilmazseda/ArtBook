package com.sedayilmaz.sanatkitabm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
ListView listView;
ArrayList<String> nameArray ;
ArrayList<Integer> idArray;
ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listView);
        nameArray= new ArrayList<String>();
        idArray= new ArrayList<Integer>();
        //simple_list_item_1 : sadce string göstereceğimiz zaman kullanılır.
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameArray);
        listView.setAdapter(arrayAdapter);

        //listview'a tıklanma adapterı ekle
       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               //position : nereye tıklandığını verir
               Intent intent = new Intent(MainActivity.this,DetailActivity.class);
               //Aktiviteyi çalıştırmadan önce diğer tarafa bazı bilgileri göndericez
               intent.putExtra("artId",idArray.get(position));//artId'i al,Nereden alıcaz(idArray),get(position) indexi verir.Kullanıcı nereye tıklarsa o indeksi veri
               //Kullanıcı listeden de tıklasa  menüden de tıklasa DetailActivity'e gidecek.
               //Listeden tıklarsa kaydedilen bilgilri görür
               //Menüden tıklarsa yeni bilgi giderreceği boş textleri vs görür
               //Bizim bunu  da ayırt etmemiz lazım
               intent.putExtra("info","old");//Adına info dedik.Eğer listeden tıklara old yani kaydedilen bilgiler ekrana gelir


               startActivity(intent);
           }
       });

        getData();

    }

    //onCreateOptionsMenu :Hangi menüyü göstereceğ iz
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //xml'i aktivite içerisinde göstermen için kullanılır :Inflater
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art,menu); //hangi menü
        return super.onCreateOptionsMenu(menu);
    }
    public void getData()
    {
        try {
            //VERİLERİ AL VE LİSTVİEW 'E EKLE
            SQLiteDatabase database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null); //bir önceki metodda oluşturduk o yüzden burada open yapacak
            Cursor cursor = database.rawQuery("SELECT * FROM arts",null);//cursor ile sorgu oluştururuz
            //sadece seçilan id'e ait veriler diğer activiyde gözükecek
            //İlk aktivite de isimleri göstereceksek onu alırız.
            int nameIX =cursor.getColumnIndex("artname");
            int idIX =cursor.getColumnIndex("id");
            while (cursor.moveToNext())
            {
                //veriler bu dizilere eklenir bunlar da listView içinde gösterilecek
                nameArray.add(cursor.getString(nameIX));
                idArray.add(cursor.getInt(idIX));
                //Bunlar listeye bağlanacak o yüzden arrayadapter tanımlarız
            }
            arrayAdapter.notifyDataSetChanged();//Yeni bir veri ekledim sen de bunu listende göster anlamıında
            cursor.close();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    //onOptionsItemSelected :Kullanıcı bir item'i seçerse ne yapacağımızı yazarız
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //mENÜDEN NE SEÇİLDİĞİNİ KONTROL ETMEMİZ LAZIM
        //add_art_item bu id'e tıklanılırsa (==) şunu yap.Ne yapacak diğer aktiviteye gidecek o yüzden Intent tanımlarız

        if(item.getItemId() == R.id.add_art_item)
        {
            Intent intent = new Intent(MainActivity.this,DetailActivity.class);
            //Buradan da tıkalrsa yeni bir bilgi ekleyeceği sayfa gelecek ona da new diyelim
            intent.putExtra("info","new");
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}