package com.pos.mahmoud.pos.activites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.pos.mahmoud.pos.R;
import com.telpo.tps550.api.TelpoException;

import com.telpo.tps550.api.magnetic.MagneticCard;

/**
 * For Magnetic stripe card test.
 * @author linhx
 * @date 2015-02-27
 *
 */
public class MegneticActivity extends Activity {
    private EditText editText1,editText2,editText3;
    private Button click,quit;
    Handler handler;
    Thread readThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.magnetic_main);
        editText1 = (EditText) findViewById(R.id.editText_track1);
        editText2 = (EditText) findViewById(R.id.editText_track2);
        editText3 = (EditText) findViewById(R.id.editText_track3);
        click = (Button) findViewById(R.id.button_open);
        quit = (Button) findViewById(R.id.button_quit);
        quit.setEnabled(false);
        handler = new Handler()
        {

            @Override
            public void handleMessage(Message msg)
            {
                editText1.setText("");
                editText2.setText("");
                editText3.setText("");
                String[] TracData = (String[])msg.obj;
                for(int i=0; i<3; i++){
                    if(TracData[i] != null){
                        switch (i)
                        {
                            case 0:
                                editText1.setText(TracData[i]);
                                break;
                            case 1:
                                editText2.setText(TracData[i]);
                                break;
                            case 2:
                                editText3.setText(TracData[i]);
                                break;

                        }

                        readThread.interrupt();
                        // readThread = null;
                        click.setEnabled(true);
                        quit.setEnabled(false);
                    }
                }
            }

        };


        try {
            MagneticCard.open(MegneticActivity.this);
        } catch (Exception e) {
            click.setEnabled(false);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("error");
            alertDialog.setMessage("oprn");
            alertDialog.setPositiveButton("k",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MegneticActivity.this.finish();
                }
            });
            alertDialog.show();
        }

        click.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editText1.setText("");
                editText2.setText("");
                editText3.setText("");
                readThread = new ReadThread();
                readThread.start();
                click.setEnabled(false);
                quit.setEnabled(true);
            }
        });

        quit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                readThread.interrupt();
                readThread = null;
                click.setEnabled(true);
                quit.setEnabled(false);
            }
        });
    }

    protected void onDestroy() {

        if (readThread != null)
        {
            readThread.interrupt();
        }
        MagneticCard.close();
        super.onDestroy();

    }

    private class ReadThread extends Thread
    {
        String[] TracData = null;

        @Override
        public void run()
        {
            MagneticCard.startReading();
            while (!Thread.interrupted()){
                try{
                    TracData = MagneticCard.check(1000);
                    Log.d("tag",TracData.length+"" );
                    handler.sendMessage(handler.obtainMessage(1, TracData));
                    MagneticCard.startReading();

                }catch (TelpoException e){
                }
            }
        }

    }

}


