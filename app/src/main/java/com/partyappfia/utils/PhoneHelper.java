package com.partyappfia.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.partyappfia.R;

/**
 * Created by Great Summit on 3/12/2016.
 */
public class PhoneHelper {
    public static String getIMEI(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getDeviceId();
        } catch (Exception e) {
            return "";
        }
    }

    public static String GetCountryZipCode(Context context){
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++){
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())){
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }
}
