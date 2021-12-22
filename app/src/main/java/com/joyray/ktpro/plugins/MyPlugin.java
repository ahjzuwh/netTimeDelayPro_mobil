package com.joyray.ktpro.plugins;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.aruistar.cordova.baidumap.BaiduMapLocation;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MyPlugin extends CordovaPlugin {
    /**
     * LOG TAG
     */
    boolean pingState = false;
    boolean locationState = false;
    boolean cellsState = false;
    JSONArray pingResult = new JSONArray();
    JSONObject locationResult = new JSONObject();
    JSONObject cellResult = new JSONObject();
    private JSONObject allargs = new JSONObject();
    private static final String LOG_TAG = BaiduMapLocation.class.getSimpleName();
    /**
     * JS回调接口对象
     */
    public static CallbackContext cbCtx = null;
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        cbCtx = callbackContext;
        if ("getBaseStationInfo".equals(action)) {
            System.out.println("BaseStation-args"+args);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    if (!needsToAlertForRuntimePermission()) {
                        JSONObject  resultObj = getGsmCellLocation();
                        String timeFlag = args.optJSONObject(0).optString("timeFlag");
                        try {
                            resultObj.put("timeFlag",timeFlag);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
                        cbCtx.sendPluginResult(pluginResult);
                    } else {
                        requestPermission();
                        // 会在onRequestPermissionResult时performGetLocation
                    }
                }
            });
            return true;
        }else if ("getAllMsgs".equals(action)) {
            allargs = args.getJSONObject(0);
            getAllMsgs();
            return true;
        }else if ("postData".equals(action)) {
            System.out.println("=========================>1");
            allargs = args.getJSONObject(0);
            System.out.println("=========================>2"+allargs);
            String url = allargs.getString("url");
            System.out.println("=========================>3"+url);
            JSONObject postData = allargs.getJSONObject("postData");
            Map<String, String> cookies = new HashMap<String, String>();
            Hashtable<String, String> headerParams  = new Hashtable<String, String>();
            Hashtable<String, String> dataParams = new Hashtable<String, String>();
            Iterator<String> postKeys = postData.keys();
            while (postKeys.hasNext()) {
                String key = postKeys.next();
                dataParams.put(key,postData.getString(key));
            }
            com.alibaba.fastjson.JSONObject result = GetDataUtil.getHttpDatas(url,dataParams,cookies, Connection.Method.POST,headerParams);
            System.out.println("=========================>"+result);
            JSONObject resultObj = new JSONObject(result.toJSONString());
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
            cbCtx.sendPluginResult(pluginResult);
            return true;
        }
        return false;
    }

    private void getAllMsgs() {
        JSONObject resultObj = new  JSONObject();
        Date startDate= new Date();
        pingState = false;
        locationState = false;
        cellsState = false;
        try {
            resultObj.put("starTime",startDate.getTime());
            SimpleDateFormat MMddYYYY_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            resultObj.put("starTimeStr",MMddYYYY_HHmmss.format(startDate));
            if (!needsToAlertForRuntimePermission()) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        JSONArray pingargs = null;
                        try {
                            pingargs = allargs.getJSONArray("pingargs");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        getPingResult(pingargs);
                        performGetLocation();
                        getGsmCellLocation();
                    }
                });
            } else {
                requestPermission();
                // 会在onRequestPermissionResult时performGetLocation
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!pingState||!locationState||!cellsState){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        PluginResult pluginResult;
        try {
            resultObj.put("pingResult",pingResult);
            resultObj.put("locationResult",locationResult);
            resultObj.put("cellResult",cellResult);
            pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
        } catch (JSONException e) {
            e.printStackTrace();
            pluginResult = new PluginResult(PluginResult.Status.ERROR, resultObj);
        }
        cbCtx.sendPluginResult(pluginResult);
    }




    /**
     * 百度定位客户端
     */
    public LocationClient mLocationClient = null;

    private LocationClientOption mOption;

    /**
     * 百度定位监听
     */
    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            JSONObject json = new JSONObject();
            try {
                json.put("time", location.getTime());
                SimpleDateFormat MMddYYYY_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                json.put("timeStr",MMddYYYY_HHmmss.format(new Date()));
                json.put("locType", location.getLocType());
                json.put("locTypeDescription", location.getLocTypeDescription());
                json.put("latitude", location.getLatitude());
                json.put("longitude", location.getLongitude());
                json.put("radius", location.getRadius());

                json.put("countryCode", location.getCountryCode());
                json.put("country", location.getCountry());
                json.put("citycode", location.getCityCode());
                json.put("city", location.getCity());
                json.put("district", location.getDistrict());
                json.put("street", location.getStreet());
                json.put("addr", location.getAddrStr());
                json.put("province", location.getProvince());

                json.put("userIndoorState", location.getUserIndoorState());
                json.put("direction", location.getDirection());
                json.put("locationDescribe", location.getLocationDescribe());

                PluginResult pluginResult;
                if (location.getLocType() == BDLocation.TypeServerError
                        || location.getLocType() == BDLocation.TypeNetWorkException
                        || location.getLocType() == BDLocation.TypeCriteriaException) {

                    json.put("describe", "定位失败");
                    json.put("isok", false);
                } else {
                    json.put("isok", true);
                }

            } catch (JSONException e) {
                String errMsg = e.getMessage();
                try {
                    json.put("describe", "定位失败");
                    json.put("isok", false);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            } finally {
                mLocationClient.stop();
                locationState = true;
                locationResult = json;
            }
        }

    };

    /**
     * 安卓6以上动态权限相关
     */

    private static final int REQUEST_CODE = 100001;

    private boolean needsToAlertForRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || !cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ArrayList<String> permissionsToRequire = new ArrayList<String>();

        if (!cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsToRequire.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (!cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsToRequire.add(Manifest.permission.ACCESS_FINE_LOCATION);


        if (!cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsToRequire.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (!cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsToRequire.add(Manifest.permission.ACCESS_FINE_LOCATION);

        String[] _permissionsToRequire = new String[permissionsToRequire.size()];
        _permissionsToRequire = permissionsToRequire.toArray(_permissionsToRequire);
        cordova.requestPermissions(this, REQUEST_CODE, _permissionsToRequire);
    }




    /**
     * 权限获得完毕后进行定位
     */
    private void performGetLocation() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(this.webView.getContext());
            mLocationClient.registerLocationListener(myListener);
            mLocationClient.setLocOption(getDefaultLocationClientOption());
        }

        mLocationClient.start();
    }


    public LocationClientOption getDefaultLocationClientOption() {
        if (mOption == null) {
            mOption = new LocationClientOption();
            mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
            mOption.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
            mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            mOption.setOpenGps(true); // 可选，默认false,设置是否使用gps
            mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
            mOption.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
            mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            mOption.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
            mOption.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用

        }
        return mOption;
    }




    private void getPingResult(JSONArray pingargs) {
        JSONArray resultList = new JSONArray();
        if (pingargs != null && pingargs.length() > 0) {
            int length = pingargs.length();
            for (int index = 0; index < length; index++) {
                String query = "";
                String timeout = "";
                String count = "";
                String pingByte = "64";
                String version = "";
                long timeFlag = 0l;
                JSONObject request = new JSONObject();
                JSONObject responseJson = new JSONObject();
                SimpleDateFormat MMddYYYY_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                try{
                    JSONObject obj = pingargs.optJSONObject(index);
                    query = obj.optString("query");
                    timeout = obj.optString("timeout");
                    count = obj.optString("retry");
                    pingByte = obj.optString("pingByte");
                    version = obj.optString("version");
                    timeFlag = obj.getLong("timeFlag");
                    request.put("query",query);
                    request.put("timeout",timeout);
                    request.put("retry",count);
                    request.put("version",version);
                    Date startDate= new Date();
                    request.put("starTime",startDate.getTime());
                    request.put("starTimeStr",MMddYYYY_HHmmss.format(startDate));
                    JSONObject result = doPing(query,timeout,count,version,pingByte);
                    Date endDate= new Date();
                    responseJson.put("endTime",endDate.getTime());
                    responseJson.put("endTimeStr",MMddYYYY_HHmmss.format(endDate));
                    Map<String,String> out = new HashMap<String, String>();
                    out= parse(result,out);
                    JSONObject r = new JSONObject();
                    JSONObject finalResponse = new JSONObject();
                    finalResponse.put("timeFlag",timeFlag);
                    r.put("target", query);
                    if (Double.parseDouble(out.get("avgRtt")) > 0) {
                        responseJson.put("status", "success");
                        r.put("avgRtt", out.get("avgRtt"));
                        r.put("maxRtt", out.get("maxRtt"));
                        r.put("minRtt", out.get("minRtt"));
                        r.put("pctTransmitted",out.get("pctTransmitted"));
                        r.put("pctReceived",out.get("pctReceived"));
                        r.put("pctLoss",out.get("pctLoss"));
                        responseJson.put("result", r);
                        finalResponse.put("response",responseJson);
                        finalResponse.put("request",request);
                        resultList.put(finalResponse);
                    } else {
                        responseJson.put("status", "timeout");
                        r.put("avgRtt", 0);
                        r.put("maxRtt", 0);
                        r.put("minRtt", 0);
                        r.put("pctTransmitted",out.get("pctTransmitted"));
                        r.put("pctReceived",out.get("pctReceived"));
                        r.put("pctLoss","100%");
                        responseJson.put("result", r);
                        finalResponse.put("response",responseJson);
                        finalResponse.put("request",request);
                        resultList.put(finalResponse);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        pingState = true;
        pingResult = resultList;
    }


    private JSONObject doPing(String ip, String timeout, String retry, String version,String pingByte){
        System.out.println("doPing \n");
        System.out.println(ip + "\n");
        String inputLine = "";
        String stringLine = "";
        String transmitted ="";
        double avgRtt = 0;
        double minRtt = 0;
        double maxRtt = 0;
        JSONObject r = new JSONObject();
        Runtime runtime = Runtime.getRuntime();
        try {
            System.out.println(version);

            String command = "/system/bin/ping -n ";
            if(version.toLowerCase().equals("v6")){
                command = "/system/bin/ping6 -n ";
            }
            if(Integer.parseInt(timeout) > 0){
                command=    command+ " -W "+timeout;
            }
            if(Integer.parseInt(retry) > 0){
                command=    command+ " -c "+retry+ " ";
            }
            int _pingByte = Integer.parseInt(pingByte);
            if(_pingByte!=0&&_pingByte!=64){
                command=    command+ " -s "+_pingByte+ " ";
            }
            System.out.println(">>"+command+ip);
            Process mIpAddrProcess = runtime.exec(command + ip);
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println("mExitValue"+mExitValue);
            if (mExitValue == 0) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mIpAddrProcess.getInputStream()));
                inputLine = bufferedReader.readLine();
                while ((inputLine != null)) {
                    System.out.println("Input Line:    "+inputLine);
                    if (inputLine.length() > 0 && inputLine.contains("transmitted")) {
                        transmitted = inputLine;
                    }
                    if (inputLine.length() > 0 && inputLine.contains("avg")) {
                        stringLine = inputLine;
                    }
                    inputLine = bufferedReader.readLine();
                }
                if(stringLine!=null){
                    String afterEqual = stringLine.substring(stringLine.indexOf("=")+1, stringLine.length()).trim();
                    String [] items = afterEqual.split("/");
                    avgRtt = Double.valueOf(items[1]);
                    minRtt = Double.valueOf(items[0]);
                    maxRtt = Double.valueOf(items[2]);
                    r.put("avgRtt",avgRtt);
                    r.put("minRtt",minRtt);
                    r.put("maxRtt",maxRtt);
                    String s []= transmitted.trim().split(",");
                    r.put("pctTransmitted",s[0].trim().split(" ")[0]);
                    r.put("pctReceived",s[1].trim().split(" ")[0]);
                    r.put("pctLoss",s[2].trim().split(" ")[0]);
                }else{
                    r.put("avgRtt",0);
                }
            } else {
                avgRtt = 0;
                r.put("avgRtt",0);
                r.put("pctTransmitted",retry);
                r.put("pctReceived",0);
            }
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            System.out.println(" Exception:" + ignore);
        }  catch (IOException e) {
            e.printStackTrace();
            System.out.println(" Exception:" + e);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(" Exception:" + e);
        }
        return r;
    }


    private  Map<String,String> parse(JSONObject json , Map<String,String> out) throws JSONException{
        Iterator<String> keys = json.keys();
        while(keys.hasNext()){
            String key = keys.next();
            String val = null;
            try{
                JSONObject value = json.getJSONObject(key);
                parse(value,out);
            }catch(Exception e){
                val = json.getString(key);
            }

            if(val != null){
                out.put(key,val);
            }
        }
        return out;
    }

    @SuppressLint("MissingPermission")
    private JSONObject getGsmCellLocation() {
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) this.cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            // 返回值MCC + MNC
            String operator = mTelephonyManager.getNetworkOperator();
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            // 中国移动和中国联通获取LAC、CID的方式
            GsmCellLocation location = (GsmCellLocation) mTelephonyManager.getCellLocation();
            int lac = location.getLac();
            int cellId = location.getCid();
            JSONObject json = new JSONObject();
            SimpleDateFormat MMddYYYY_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            json.put("timeStr",MMddYYYY_HHmmss.format(new Date()));
            json.put("MCC",mcc);
            json.put("MNC",mnc);
            json.put("LAC",lac);
            json.put("CID",cellId);
            json.put("Dbm",MyPhoneStateListener.getInstants(this.cordova.getActivity()).getStrengthDbm());
            json.put("Asu",MyPhoneStateListener.getInstants(this.cordova.getActivity()).getStrengthAsuLevel());
            // 中国电信获取LAC、CID的方式
            /*CdmaCellLocation location1 = (CdmaCellLocation) mTelephonyManager.getCellLocation();
            lac = location1.getNetworkId();
            cellId = location1.getBaseStationId();
            cellId /= 16;*/
            // 获取邻区基站信息
            List<NeighboringCellInfo> infos = mTelephonyManager.getNeighboringCellInfo();
            JSONArray cellInfos = new JSONArray();
            int strength = 0;
            if(infos!=null){
                json.put("cellsNum" , infos.size());
                for (NeighboringCellInfo info : infos) { // 根据邻区总数进行循环
                    JSONObject infoJson = new JSONObject();
                    infoJson.put(" LAC",info.getLac()); // 取出当前邻区的LAC
                    infoJson.put(" CID",info.getCid()); // 取出当前邻区的CID
                    infoJson.put(" BSSS" , (-113 + 2 * info.getRssi())); // 获取邻区基站信号强度
                    strength+=(-133+2*info.getRssi());// 获取邻区基站信号强度
                    cellInfos.put(infoJson);
                }
                json.put("cellInfos" , cellInfos);
                json.put("strength" , strength);
            }else{
                json.put("cellsNum" , 0);
                json.put("cellInfos" , cellInfos);
                json.put("strength" , strength);
            }
            cellsState = true;
            cellResult = json;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return cellResult;
    }

    /**
     * 安卓6以上动态权限相关
     */


    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (cbCtx == null || requestCode != REQUEST_CODE)
            return;
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                JSONObject json = new JSONObject();
                json.put("describe", "操作失败");
                LOG.e(LOG_TAG, "权限请求被拒绝");
                cbCtx.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, json));
                return;
            }
        }
        getAllMsgs();
    }


}
