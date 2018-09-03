package com.example.welcome.jaguar;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeSubject extends AppCompatActivity implements View.OnClickListener{

    EditText msg;
    Button add,done,choose;
    List<String> arrayList;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_message);

        //Opening Excel File Columns and adding them in arrayList
        arrayList=new ArrayList<String>();
        try {
            // Creating Input Stream
            File file = new File(MainActivity.filepath, MainActivity.filename);
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();

            HSSFRow myRow = (HSSFRow) rowIter.next();
            Iterator cellIter = myRow.cellIterator();
            while (cellIter.hasNext()) {
                HSSFCell myCell = (HSSFCell) cellIter.next();
                arrayList.add(myCell.toString());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Creating Database To Store Templates
        db=openOrCreateDatabase("Jaguar",MODE_PRIVATE,null);
        db.execSQL("create table if not exists subject(msg varchar)");



        //Creating reference with XML file
        msg=findViewById(R.id.msgbox);
        add=findViewById(R.id.addColn);
        done=findViewById(R.id.done);
        choose=findViewById(R.id.choose);

        if(MainActivity.subject!=null)
            msg.setText(MainActivity.subject);
        add.setOnClickListener(TypeSubject.this);
        done.setOnClickListener(this);
        choose.setOnClickListener(this);

    }
    public void onClick(View view){
        switch(view.getId()){
            case R.id.addColn:
                selColumn();
                break;
            case R.id.choose:
                choose();
                break;
            case R.id.done:
                done();
                break;

        }
    }

    private void done() {

        CheckBox ch1=findViewById(R.id.checkbox);
        Cursor c1;
        if(ch1.isChecked()) {
            c1 = db.rawQuery("select * from subject where msg='" + msg.getText().toString() + "'", null);
            if(c1.getCount()==0 && !msg.getText().toString().trim().equals(""))
                db.execSQL("insert into subject values('" + msg.getText().toString() + "')");
        }
        MainActivity.subject=msg.getText().toString();
        onBackPressed();
        db.close();
    }

    private void choose() {
        final Dialog dialog=new Dialog(TypeSubject.this);
        dialog.setContentView(R.layout.sel_template);
        dialog.setTitle("Select Saved Subject Template");
        dialog.setCancelable(true);

        final ListView list=dialog.findViewById(R.id.list);

        final ArrayList<String> dbmsg=new ArrayList<>();
        Cursor cursor=db.rawQuery("Select * from subject",null);
        while(cursor.moveToNext()) {
            dbmsg.add(cursor.getString(0));
        }
        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(TypeSubject.this,android.R.layout.simple_list_item_multiple_choice,dbmsg);
        list.setAdapter(adapter);
        dialog.show();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                CheckedTextView textView=(CheckedTextView) view;
                textView.setChecked(true);

                final int a=(int)l;
                final Button use=dialog.findViewById(R.id.add);
                final Button remove=dialog.findViewById(R.id.remove);
                use.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        msg.setText(dbmsg.get(a));
                        msg.setSelection(msg.length());
                        dialog.dismiss();
                    }
                });
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db.execSQL("delete from subject where msg='"+dbmsg.get(a)+"'");
                        dialog.dismiss();
                    }
                });
            }
        });

    }

    private void selColumn() {
        final Dialog dialog=new Dialog(TypeSubject.this);
        dialog.setContentView(R.layout.select_col_dialog);
        dialog.setTitle("Select Column");
        dialog.setCancelable(true);

        final ListView list=dialog.findViewById(R.id.list);

        ArrayAdapter<String> aa=new ArrayAdapter<String>(TypeSubject.this,android.R.layout.simple_list_item_1,arrayList);
        list.setAdapter(aa);
        dialog.show();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                msg.setText(msg.getText()+" #"+(l)+"# ");
                msg.setSelection(msg.length());
                dialog.dismiss();
            }
        });
    }
}
