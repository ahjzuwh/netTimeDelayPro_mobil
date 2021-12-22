package com.joyray.ktpro.plugins;

import android.app.Activity;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.joyray.ktpro.MainActivity;

import java.lang.reflect.Method;

public class MyPhoneStateListener extends PhoneStateListener {
    /* Get the Signal strength from the provider, each tiome there is an update  从得到的信号强度,每个tiome供应商有更新*/
    public static final int INVALID = Integer.MAX_VALUE;

    public int signalStrengthDbm = INVALID;
    public int signalStrengthAsuLevel = INVALID;
    private static  MyPhoneStateListener instances  = null;
    public static MyPhoneStateListener getInstants(Activity activity) {
        if(instances==null&&activity!=null){
            instances = new  MyPhoneStateListener();
            startListen(activity);
        }
        return instances;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);
        signalStrengthDbm = getSignalStrengthByName(signalStrength, "getDbm");
        signalStrengthAsuLevel = getSignalStrengthByName(signalStrength, "getAsuLevel");
    }

    private int getSignalStrengthByName(SignalStrength signalStrength, String methodName)
    {
        try
        {
            Class classFromName = Class.forName(SignalStrength.class.getName());
            java.lang.reflect.Method method = classFromName.getDeclaredMethod(methodName);
            Object object = method.invoke(signalStrength);
            return (int)object;
        }
        catch (Exception ex)
        {
            return INVALID;
        }
    }

    public int getStrengthDbm() {
        return signalStrengthDbm;
    }

    public int getStrengthAsuLevel() {
        return signalStrengthAsuLevel;
    }

    private static void startListen(Activity mainActivity){
        TelephonyManager mTelephonyManager = (TelephonyManager) mainActivity.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(instances,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
}
