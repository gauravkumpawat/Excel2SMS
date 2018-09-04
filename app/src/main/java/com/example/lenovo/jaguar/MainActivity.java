package com.example.welcome.jaguar;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    ArrayAdapter<String> listContent;
    static List<String> content;
    public static String filepath,filename;
    static int sendOption = 0;
    static int mobcol=-1,emailcol=-1;
    static String msg,subject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.list);
        subject="";

        content=new ArrayList<>();
        content.add("Select Excel file");
        content.add("Select Action");
        content.add("Create Template");


        listContent=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_multiple_choice,content);
        listView.setAdapter(listContent);
        listView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if (content.get((int) l).matches("(.*) file(.*)")) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) == PackageManager.PERMISSION_GRANTED) {
                    selectFile(view);
                    CheckedTextView textView = (CheckedTextView) view;
                    textView.setChecked(true);
                } else
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            200);
            }
            else {
                selectFile(view);
                CheckedTextView textView = (CheckedTextView) view;
                textView.setChecked(true);
            }
        }

        //below code will execute when we want to receive action to be performed like sending sms or emails
        else if (content.get((int) l).matches("(.*) Action(.*)")) {
            if (filename != null) {
                CheckedTextView textView = (CheckedTextView) view;
                textView.setChecked(true);
                selectAction(view);
            } else
                Toast.makeText(this, "Select Excel File", Toast.LENGTH_SHORT).show();
        }
        //Create Template Option
        else if (content.get((int) l).matches("(.*) Template(.*)")) {
            Intent it = getIntent();
            if (filename != null && sendOption != 0) {
                it = new Intent(MainActivity.this, TypeMessage.class);
                startActivity(it);
                CheckedTextView textView = (CheckedTextView) view;
                textView.setChecked(true);
            } else
                Toast.makeText(MainActivity.this, "Please DO it stepwise", Toast.LENGTH_SHORT).show();
        }

        //Select Mobile No or Email Id Column
        else if(content.get((int)l).matches("(.*) Column(.*)")) {
            if (filename != null && sendOption != 0 && msg!=null) {
                Intent intent = new Intent(MainActivity.this, SelectMobileNo.class);
                if (content.get((int)l).matches("(.*) Mobile(.*)")) {
                    intent.putExtra("opt", "mobile");
                } else if (content.get((int)l).matches("(.*) Email(.*)")) {
                    intent.putExtra("opt", "email");
                }
                if(!(content.contains("Preview Messages")))
                    content.add("Preview Messages");
                listContent.notifyDataSetChanged();
                startActivity(intent);

                CheckedTextView textView = (CheckedTextView) view;
                textView.setChecked(true);
            }

            else
                Toast.makeText(MainActivity.this, "Please Do It Stepwise", Toast.LENGTH_SHORT).show();
        }


        else if(content.get((int)l).matches("(.*)Subject(.*)")){
            CheckedTextView textView=(CheckedTextView)view;
            textView.setChecked(true);
            if(emailcol!=-1) {
                Intent it = new Intent(MainActivity.this, TypeSubject.class);
                startActivity(it);
            }
            else
                Toast.makeText(MainActivity.this,"Do it Stepwise",Toast.LENGTH_SHORT).show();
        }
        //Preview Messages
        else if(content.get((int)l).matches("(.*) Messages(.*)") && sendOption!=0){
            if(subject!=null) {
                Intent it = new Intent(MainActivity.this, Send.class);
                startActivity(it);
            }
            else
                Toast.makeText(MainActivity.this,"Do it Stepwise",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        //Show permission explanation dialog...
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},200);
                    }else{
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                        Toast.makeText(this,"Please Give permission to read Files",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }
                return;
            }

        }
    }

    public void selectFile(final View view){
        final SimpleFileDialog FileOpenDialog = new SimpleFileDialog(MainActivity.this, "FileOpen",
                new SimpleFileDialog.SimpleFileDialogListener() {
                    @Override
                    public void onChosenDir(String chosenDir, String filenm) {
                        // The code in this function will be executed when the dialog OK button is pushed
                        filepath = chosenDir;
                        filename = filenm;
                        if (!(filename.endsWith(".xls") || filename.endsWith(".xlsx"))) {
                            Toast.makeText(MainActivity.this, "Give a valid excel file", Toast.LENGTH_SHORT).show();
                            filepath=filename=null;
                            CheckedTextView textView = (CheckedTextView) view;
                            textView.setChecked(false);
                        }
                    }
                });

        //You can change the default filename using the public variable "Default_File_Name"
        FileOpenDialog.Default_File_Name = "";
        FileOpenDialog.chooseFile_or_Dir();
    }

    public void selectAction(final View view)
    {
            final Dialog dialog=new Dialog(this);
            dialog.setContentView(R.layout.whattosend);
            dialog.setTitle("Select What To Send");

            dialog.setCancelable(true);
            Button bt1=dialog.findViewById(R.id.okbtn);
            Button bt2=dialog.findViewById(R.id.cnclbtn);
            final CheckBox email=dialog.findViewById(R.id.ema);
            final CheckBox mobile=dialog.findViewById(R.id.mob);
            if(sendOption==12) {
                email.setChecked(true);
                mobile.setChecked(true);
            }
            else if(sendOption==1)
                mobile.setChecked(true);
            else if(sendOption==2){
                email.setChecked(true);
            }


            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(email.isChecked() && mobile.isChecked()){
                        sendOption=12;
                        dialog.dismiss();
                        if(!(content.contains("Select Column for Mobile no.")))
                            content.add("Select Column for Mobile no.");
                        if(!(content.contains("Select Column for Email ID"))) {
                            content.add("Select Column for Email ID");
                            content.add("Type Subject For Email");
                        }

                    }
                    else if(mobile.isChecked()){
                        sendOption=1;
                        dialog.dismiss();
                        if(!(content.contains("Select Column for Mobile no."))) {
                            content.add("Select Column for Mobile no.");
                        }
                        if(content.contains("Select Column for Email ID")) {
                            content.remove(content.indexOf("Select Column for Email ID"));
                            content.remove(content.indexOf("Type Subject For Email"));
                        }

                    }
                    else if(email.isChecked()){
                        sendOption=2;
                        dialog.dismiss();
                        if(!(content.contains("Select Column for Email ID"))) {
                            content.add("Select Column for Email ID");
                            content.add("Type Subject For Email");
                        }
                        if(content.contains("Select Column for Mobile no."))
                            content.remove(content.indexOf("Select Column for Mobile no."));
                    }
                    else{
                        Toast.makeText(MainActivity.this,"Please select at least one",Toast.LENGTH_SHORT).show();
                        CheckedTextView textView = (CheckedTextView) view;
                        textView.setChecked(true);
                    }

                    listContent.notifyDataSetChanged();
                }
            });
            bt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckedTextView textView = (CheckedTextView) view;
                    textView.setChecked(false);
                    dialog.dismiss();

                    if(content.contains("Select Column for Mobile no."))
                        content.remove("Select Column for Mobile no.");
                    if(content.contains("Select Column for Email ID")) {
                        content.remove("Select Column for Email ID");
                        content.remove("Type Subject For Email");
                    }
                    listContent.notifyDataSetChanged();
                }
            });
            dialog.show();

    }
}