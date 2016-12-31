package com.hrc.administrator.phonereversal;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity{
    private TextView textView;
    private SensorManager sensorManager;
    private Sensor sensor;
    private static final float CRITICAL_DOWN_ANGLE=-5.0f;
    private static final float CRITICAL_UP_ANGLE=5.0f;
    private int mReverseDownFlg=0;  //0表示向上，1表示向下
    private TelephonyManager telephonyManager;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        telephonyManager=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        MyPhoneListener listener=new MyPhoneListener();
        telephonyManager.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
        audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        textView=(TextView)findViewById(R.id.textviews);
        textView.setTextSize(24);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","注册监听");
        sensorManager.registerListener(sensorEventListener,sensor,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause","停止监听");
        sensorManager.unregisterListener(sensorEventListener);
    }

    private SensorEventListener sensorEventListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x=event.values[SensorManager.DATA_X];
            float y=event.values[SensorManager.DATA_Y];
            float z=event.values[SensorManager.DATA_Z];
            textView.setText("x="+x+" y="+y+" z="+z);
            if(event.values[SensorManager.DATA_Z]>=CRITICAL_UP_ANGLE){
                Log.d("listener","屏幕向上");
                mReverseDownFlg=0;
            }else if(event.values[SensorManager.DATA_Z]<=CRITICAL_DOWN_ANGLE&&mReverseDownFlg==0){
                Log.d("listener","屏幕向下");
                mReverseDownFlg=1;
            }
            /*if(mReverseDownFlg==1){
                Log.d("ll","设置声音为静音");
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }else{
                Log.d("11","设置声音为正常");
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }*/
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private class MyPhoneListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state){
                //正常状态
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                //响铃状态
                case TelephonyManager.CALL_STATE_RINGING:
                    if(mReverseDownFlg==1){
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    }else{
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }
                    break;
                //通话状态
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
            }
        }
    }
}
