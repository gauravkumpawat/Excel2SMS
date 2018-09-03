package com.example.welcome.jaguar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

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

public class SelectMobileNo extends AppCompatActivity {

    ListView lv;
    List<String> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mobile_no);

        Intent it=getIntent();
        final String opt=it.getStringExtra("opt");
        TextView title=findViewById(R.id.tv);
        if(opt.equals("mobile")){
            title.setText("Select Mobile No.");
        }
        else if(opt.equals("email")){
            title.setText("Select For Email Id");
        }
        lv=findViewById(R.id.listView);
        arrayList=new ArrayList<>();
        addComponentsToArrayList();

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(SelectMobileNo.this,android.R.layout.simple_list_item_single_choice,arrayList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(opt.equals("mobile"))
                    MainActivity.mobcol=(int)l;
                else
                    MainActivity.emailcol=(int)l;
                onBackPressed();
            }
        });
    }
    private void addComponentsToArrayList() {
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
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
