package com.example.bts_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BandwidthTest {

    Context context;

    final static private int DatabaseTimeout = 8000;
    final static private int NewServerTime = 2000;              // Time interval for adding new servers
    final static private int TestTimeout = 5000;                // Maximum test duration
    final static private int MaxTrafficUse = 200;               // Maximum traffic limit

    final static private int SamplingInterval = 10;             // Time interval for Sampling
    final static private int SamplingWindow = 50;               // Sampling overlap

    final static private int CheckerSleep = 50;                 // Time interval between checks
    final static private int CheckerWindowSize = 10;            // SimpleChecker window size
    final static private int CheckerSelectedSize = 8;           // SimplerChecker selected size
    final static private double CheckerThreshold = 0.03;        // 3% threshold
    final static private int CheckerTimeoutWindow = 50;         // Window size when overtime
    final static private int GetInfoInterval = 500;

    private final ArrayList<String> serverIP = new ArrayList<>(Arrays.asList("x.x.x.x", "x.x.x.x"));
    final private String databaseIp = "x.x.x.x";

    static String network_type;
    static MyNetworkInfo myNetworkInfo;
    boolean stop = false;

    BandwidthTest(Context context) { this.context = context; }

    public void stop() {
        stop = true;
    }


    static class SimpleChecker extends Thread {
        ArrayList<Double> speedSample;
        boolean finish;
        Double simpleSpeed;

        SimpleChecker(ArrayList<Double> speedSample) {
            this.speedSample = speedSample;
            this.finish = false;
            this.simpleSpeed = 0.0;
        }

        public void run() {
            while (!finish) {
                try {
                    sleep(CheckerSleep);

                    int n = speedSample.size();
                    if (n < CheckerWindowSize) continue;

                    ArrayList<Double> recentSamples = new ArrayList<>();
                    for (int i = n - CheckerWindowSize; i < n; ++i)
                        recentSamples.add(speedSample.get(i));
                    Collections.sort(recentSamples);
                    int windowNum = CheckerWindowSize - CheckerSelectedSize + 1;
                    for (int i = 0; i < windowNum; ++i) {
                        int j = i + CheckerSelectedSize - 1;
                        double lower = recentSamples.get(i), upper = recentSamples.get(j);
                        if ((upper - lower) / upper < CheckerThreshold) {
                            double res = 0;
                            for (int k = i; k <= j; ++k)
                                res += recentSamples.get(k);
                            simpleSpeed = res / CheckerSelectedSize;
                            finish = true;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    double res = 0.0;
                    int n = speedSample.size();
                    for (int k = n - CheckerTimeoutWindow; k < n; ++k)
                        res += speedSample.get(k);
                    simpleSpeed = res / CheckerTimeoutWindow;
                    break;
                }
            }
        }

        public double getSpeed(){
            return simpleSpeed;
        }
    }

    static class DownloadThread extends Thread {
        DatagramSocket socket;
        InetAddress address;
        int port;
        int size;

        DownloadThread(String ip, int port) {
            try {
                this.address = InetAddress.getByName(ip);
                this.port = port;
                this.socket = new DatagramSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            byte[] send_data = "1".getBytes();
            DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, address, port);
            try {
                socket.send(send_packet);

                int BUFFER_SIZE = 1024;
                byte[] receive_buf = new byte[BUFFER_SIZE * 2];
                DatagramPacket receive_packet = new DatagramPacket(receive_buf, receive_buf.length);

                while (true) {
                    socket.receive(receive_packet);
                    String receive_data = new String(receive_packet.getData(), 0, receive_packet.getLength());
                    size += receive_data.length();
                }
            } catch (IOException e) {
//                Log.d("UDP Test", "socket closed.");
            }
        }
    }

    static class AddServerThread extends Thread {
        ArrayList<DownloadThread> downloadThread;
        int sleepTime;

        AddServerThread(ArrayList<DownloadThread> downloadThread, int sleepTime) {
            this.downloadThread = downloadThread;
            this.sleepTime = sleepTime;
        }

        public void run() {
            for (DownloadThread t : downloadThread) {
                t.start();
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class SendThread extends Thread {
        String databaseIp;

        SendThread(String ip) {
            this.databaseIp = ip;
        }

        public void run() {
            Gson gson = new Gson();
            String cell_info_json = gson.toJson(myNetworkInfo.cellInfo);
            String wifi_info_json = gson.toJson(myNetworkInfo.wifiInfo);

            Log.d("NetworkType", network_type);
            Log.d("CellInfo", cell_info_json);
            Log.d("WifiInfo", wifi_info_json);

            try {
                JSONObject obj = new JSONObject();
                obj.put("wifi_info", cell_info_json);
                obj.put("cell_info", wifi_info_json);

                String path = "http://"+databaseIp+"/swiftest/";
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(DatabaseTimeout);
                conn.setReadTimeout(DatabaseTimeout);
                conn.setRequestProperty("content-type", "application/json");
                OutputStream outStream = conn.getOutputStream();
                outStream.write(obj.toString().getBytes());
                outStream.flush();
                outStream.close();

                conn.connect();
                StringBuilder msg = new StringBuilder();
                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        msg.append(line).append("\n");
                    }
                    reader.close();
                }
                conn.disconnect();
                Log.d("conn response", String.valueOf(conn.getResponseCode()));
                Log.d("database response", msg.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public double SpeedTest() throws InterruptedException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("No permission:", "ACCESS_FINE_LOCATION");
                return 0;
            }
        }

        stop = false;
        network_type = getNetworkType();
        List<MyNetworkInfo.CellInfo> cellInfos = getCellInfo();
        MyNetworkInfo.WifiInfo wifiInfo = getWifiInfo();
        myNetworkInfo = new MyNetworkInfo(String.valueOf(Build.VERSION.SDK_INT), network_type, cellInfos, wifiInfo);

        Timer collectTimer = new Timer();
        collectTimer.schedule(new ContinuesUpdateTask(myNetworkInfo), 0, GetInfoInterval);

        ArrayList<DownloadThread> downloadThread = new ArrayList<>();
        for (String ip : serverIP)
            downloadThread.add(new DownloadThread(ip, 9876));
        AddServerThread sendThread = new AddServerThread(downloadThread, NewServerTime);

        ArrayList<Double> speedSample = new ArrayList<>();
        SimpleChecker checker = new SimpleChecker(speedSample);

        long startTime = System.currentTimeMillis();
        sendThread.start();
        checker.start();

        ArrayList<Double> sizeRecord = new ArrayList<>();
        ArrayList<Long> timeRecord = new ArrayList<>();
        int posRecord = 0;
        while (true) {
            try {
                Thread.sleep(SamplingInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long downloadSize = 0;
            for (DownloadThread t : downloadThread)
                downloadSize += t.size;
            double downloadSizeMBits = (double) (downloadSize) / 1024 / 1024 * 8;
            long nowTime = System.currentTimeMillis();
            sizeRecord.add(downloadSizeMBits);
            timeRecord.add(nowTime);

            if (timeRecord.size() >= 2)
                speedSample.add((downloadSizeMBits - sizeRecord.get(posRecord)) * 1000.0 / (nowTime - timeRecord.get(posRecord)));

            while (nowTime - timeRecord.get(posRecord) >= SamplingWindow)
                posRecord++;

            if (checker.finish) {
                Log.d("Bandwidth Test", "Test succeed.");
                break;
            }
            if (nowTime - startTime >= TestTimeout) {
                Log.d("Bandwidth Test", "Exceeding the time limit.");
                break;
            }
            if (downloadSizeMBits / 8 >= MaxTrafficUse) {
                Log.d("Bandwidth Test", "Exceeding the traffic limit.");
                break;
            }
            if (stop) {
                Log.d("Bandwidth Test", "Testing Stopped.");
                break;
            }
        }
        for (DownloadThread t : downloadThread)
            t.socket.close();
        checker.interrupt();
        checker.join();
        collectTimer.cancel();
        new SendThread(databaseIp).start();

        String bandwidth_Mbps = String.format(Locale.CHINA, "%.4f", checker.getSpeed());
        String duration_s = String.format(Locale.CHINA, "%.2f", (double) (System.currentTimeMillis() - startTime) / 1000);
        String traffic_MB = String.format(Locale.CHINA, "%.4f", sizeRecord.get(sizeRecord.size() - 1) / 8);

        Log.d("bandwidth_Mbps", bandwidth_Mbps);
        Log.d("duration_s", duration_s);
        Log.d("traffic_MB", traffic_MB);
        Logger.d(speedSample);
        return checker.getSpeed();
    }

    /*
        This method is deprecated in API level 28 by Android documentation,
        but still work in my phone with API level 30.
    */
    String getNetworkType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable())
            return "NONE";
        int connectionType = networkInfo.getType();
        if (connectionType == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if (connectionType == ConnectivityManager.TYPE_MOBILE) {
            int cellType = networkInfo.getSubtype();
            switch (cellType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:     // api< 8: replace by 11
                case TelephonyManager.NETWORK_TYPE_GSM:      // api<25: replace by 16
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:   // api< 9: replace by 12
                case TelephonyManager.NETWORK_TYPE_EHRPD:    // api<11: replace by 14
                case TelephonyManager.NETWORK_TYPE_HSPAP:    // api<13: replace by 15
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA: // api<25: replace by 17
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:      // api<11: replace by 13
                case TelephonyManager.NETWORK_TYPE_IWLAN:    // api<25: replace by 18
                case 19: // LTE_CA
                    return "4G";
                case TelephonyManager.NETWORK_TYPE_NR:       // api<29: replace by 20
                    return "5G";
                default:
                    return "unknown";
            }
        }
        return "unknown";
    }

    MyNetworkInfo.CellInfoCdma getCellInfoCdma(CellIdentityCdma cellIdentity, CellSignalStrengthCdma signalStrength) {
        String basestationId = String.valueOf(cellIdentity.getBasestationId());
        String networkId = String.valueOf(cellIdentity.getNetworkId());
        String systemId = String.valueOf(cellIdentity.getSystemId());
        String asuLevel = String.valueOf(signalStrength.getAsuLevel());
        String cdmaDbm = String.valueOf(signalStrength.getCdmaDbm());
        String cdmaEcio = String.valueOf(signalStrength.getCdmaEcio());
        String cdmaLevel = String.valueOf(signalStrength.getCdmaLevel());
        String dbm = String.valueOf(signalStrength.getDbm());
        String evdodbm = String.valueOf(signalStrength.getEvdoDbm());
        String evdoEcio = String.valueOf(signalStrength.getEvdoEcio());
        String evdoLevel = String.valueOf(signalStrength.getEvdoLevel());
        String evdoSnr = String.valueOf(signalStrength.getEvdoSnr());
        String level = String.valueOf(signalStrength.getLevel());
        return new MyNetworkInfo.CellInfoCdma("CDMA", basestationId, networkId, systemId, asuLevel, cdmaDbm, cdmaEcio, cdmaLevel, dbm, evdodbm, evdoEcio, evdoLevel, evdoSnr, level);
    }

    MyNetworkInfo.CellInfoGsm getCellInfoGsm(CellIdentityGsm cellIdentity, CellSignalStrengthGsm signalStrength) {
        String arfcn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            arfcn = String.valueOf(cellIdentity.getArfcn());
        else arfcn = "Added in API level 24";
        String bsic;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            bsic = String.valueOf(cellIdentity.getBsic());
        else bsic = "Added in API level 24";
        String cid = String.valueOf(cellIdentity.getCid());
        String lac = String.valueOf(cellIdentity.getLac());
        String mcc, mnc, mobileNetworkOperator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mcc = cellIdentity.getMccString();
            mnc = cellIdentity.getMncString();
            mobileNetworkOperator = cellIdentity.getMobileNetworkOperator();
        } else {
            mcc = String.valueOf(cellIdentity.getMcc());
            mnc = String.valueOf(cellIdentity.getMnc());
            mobileNetworkOperator = "Added in API level 28";
        }
        String asuLevel = String.valueOf(signalStrength.getAsuLevel());
        String bitErrorRate;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            bitErrorRate = String.valueOf(signalStrength.getBitErrorRate());
        else bitErrorRate = "Added in API level 29";
        String dbm = String.valueOf(signalStrength.getDbm());
        String level = String.valueOf(signalStrength.getLevel());
        String rssi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            rssi = String.valueOf(signalStrength.getRssi());
        else rssi = "Added in API level 30";
        String timingAdvance;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            timingAdvance = String.valueOf(signalStrength.getTimingAdvance());
        else timingAdvance = "Added in API level 26";
        return new MyNetworkInfo.CellInfoGsm("GSM", arfcn, bsic, cid, lac, mcc, mnc, mobileNetworkOperator, asuLevel, bitErrorRate, dbm, level, rssi, timingAdvance);
    }

    MyNetworkInfo.CellInfoLte getCellInfoLte(CellIdentityLte cellIdentity, CellSignalStrengthLte signalStrength) {
        String[] bands;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int[] bands_int = cellIdentity.getBands();
            bands = new String[bands_int.length];
            for (int i = 0; i < bands_int.length; ++i)
                bands[i] = String.valueOf(bands_int[i]);
        } else bands = new String[]{"Added in API level 30"};
        String bandwidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            bandwidth = String.valueOf(cellIdentity.getBandwidth());
        else bandwidth = "Added in API level 28";
        String ci = String.valueOf(cellIdentity.getCi());
        String earfcn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            earfcn = String.valueOf(cellIdentity.getEarfcn());
        else earfcn = "Added in API level 24";
        String mcc, mnc, mobileNetworkOperator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mcc = cellIdentity.getMccString();
            mnc = cellIdentity.getMncString();
            mobileNetworkOperator = cellIdentity.getMobileNetworkOperator();
        } else {
            mcc = String.valueOf(cellIdentity.getMcc());
            mnc = String.valueOf(cellIdentity.getMnc());
            mobileNetworkOperator = "Added in API level 28";
        }
        String pci = String.valueOf(cellIdentity.getPci());
        String tac = String.valueOf(cellIdentity.getTac());
        String asuLevel = String.valueOf(signalStrength.getAsuLevel());
        String cqi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            cqi = String.valueOf(signalStrength.getCqi());
        else cqi = "Added in API level 26";
        String cqiTableIndex;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            cqiTableIndex = String.valueOf(signalStrength.getCqiTableIndex());
        else cqiTableIndex = "Added in API level 31";
        String dbm = String.valueOf(signalStrength.getDbm());
        String level = String.valueOf(signalStrength.getLevel());
        String rsrp, rsrq;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rsrp = String.valueOf(signalStrength.getRsrp());
            rsrq = String.valueOf(signalStrength.getRsrq());
        } else rsrp = rsrq = "Added in API level 26";
        String rssi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            rssi = String.valueOf(signalStrength.getRssi());
        else rssi = "Added in API level Q";
        String rssnr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            rssnr = String.valueOf(signalStrength.getRssnr());
        else rssnr = "Added in API level Q";
        String timingAdvance = String.valueOf(signalStrength.getTimingAdvance());
        return new MyNetworkInfo.CellInfoLte("LTE", bands, bandwidth, ci, earfcn, mcc, mnc, mobileNetworkOperator, pci, tac, asuLevel, cqi, cqiTableIndex, dbm, level, rsrp, rsrq, rssi, rssnr, timingAdvance);
    }

    MyNetworkInfo.CellInfoWcdma getCellInfoWcdma(CellIdentityWcdma cellIdentity, CellSignalStrengthWcdma signalStrength) {
        String cid = String.valueOf(cellIdentity.getCid());
        String lac = String.valueOf(cellIdentity.getLac());
        String mcc, mnc, mobileNetworkOperator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mcc = cellIdentity.getMccString();
            mnc = cellIdentity.getMncString();
            mobileNetworkOperator = cellIdentity.getMobileNetworkOperator();
        } else {
            mcc = String.valueOf(cellIdentity.getMcc());
            mnc = String.valueOf(cellIdentity.getMnc());
            mobileNetworkOperator = "Added in API level 28";
        }
        String psc = String.valueOf(cellIdentity.getPsc());
        String uarfcn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            uarfcn = String.valueOf(cellIdentity.getUarfcn());
        else uarfcn = "Added in API level 24";
        String asuLevel = String.valueOf(signalStrength.getAsuLevel());
        String dbm = String.valueOf(signalStrength.getDbm());
        String ecNo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ecNo = String.valueOf(signalStrength.getEcNo());
        else ecNo = "Added in API level 30";
        String level = String.valueOf(signalStrength.getLevel());
        return new MyNetworkInfo.CellInfoWcdma("WCDMA", cid, lac, mcc, mnc, mobileNetworkOperator, psc, uarfcn, asuLevel, dbm, ecNo, level);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    MyNetworkInfo.CellInfoTdscdma getCellInfoTdscdma(CellIdentityTdscdma cellIdentity, CellSignalStrengthTdscdma signalStrength) {
        String cid = String.valueOf(cellIdentity.getCid());
        String cpid = String.valueOf(cellIdentity.getCpid());
        String lac = String.valueOf(cellIdentity.getLac());
        String mcc = cellIdentity.getMccString();
        String mnc = cellIdentity.getMncString();
        String mobileNetworkOperator;
        String uarfcn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mobileNetworkOperator = String.valueOf(cellIdentity.getMobileNetworkOperator());
            uarfcn = String.valueOf(cellIdentity.getUarfcn());
        } else mobileNetworkOperator = uarfcn = "Added in API level 29";
        String asuLevel = String.valueOf(signalStrength.getAsuLevel());
        String dbm = String.valueOf(signalStrength.getDbm());
        String level = String.valueOf(signalStrength.getLevel());
        String rscp = String.valueOf(signalStrength.getRscp());
        return new MyNetworkInfo.CellInfoTdscdma("TDSCDMA", cid, cpid, lac, mcc, mnc, mobileNetworkOperator, uarfcn, asuLevel, dbm, level, rscp);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    MyNetworkInfo.CellInfoNr getCellInfoNr(CellIdentityNr cellIdentity, CellSignalStrengthNr signalStrength) {
        String[] bands;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int[] bands_int = cellIdentity.getBands();
            bands = new String[bands_int.length];
            for (int i = 0; i < bands_int.length; ++i)
                bands[i] = String.valueOf(bands_int[i]);
        } else bands = new String[]{"Added in API level 30"};
        String mcc = cellIdentity.getMccString();
        String mnc = cellIdentity.getMncString();
        String nci = String.valueOf(cellIdentity.getNci());
        String nrarfcn = String.valueOf(cellIdentity.getNrarfcn());
        String pci = String.valueOf(cellIdentity.getPci());
        String tac = String.valueOf(cellIdentity.getTac());
        String asuLevel = String.valueOf(signalStrength.getAsuLevel());
        List<String> csicqiReport;
        String csicqiTableIndex;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            List<Integer> csicqiReport_int = signalStrength.getCsiCqiReport();
            csicqiReport = new ArrayList<>(csicqiReport_int.size());
            for (int i = 0; i < csicqiReport_int.size(); ++i)
                csicqiReport.set(i, String.valueOf(csicqiReport_int.get(i)));
            csicqiTableIndex = String.valueOf(signalStrength.getCsiCqiTableIndex());
        } else {
            csicqiReport = new ArrayList<>();
            csicqiTableIndex = "Added in API level 31";
        }
        String csiRsrp = String.valueOf(signalStrength.getCsiRsrp());
        String csiRsrq = String.valueOf(signalStrength.getCsiRsrq());
        String csiSinr = String.valueOf(signalStrength.getCsiSinr());
        String dbm = String.valueOf(signalStrength.getDbm());
        String level = String.valueOf(signalStrength.getLevel());
        String ssRsrp = String.valueOf(signalStrength.getSsRsrp());
        String ssRsrq = String.valueOf(signalStrength.getSsRsrq());
        String ssSinr = String.valueOf(signalStrength.getSsSinr());
        return new MyNetworkInfo.CellInfoNr("NR", bands, mcc, mnc, nci, nrarfcn, pci, tac, asuLevel, csicqiReport, csicqiTableIndex, csiRsrp, csiRsrq, csiSinr, dbm, level, ssRsrp, ssRsrq, ssSinr);
    }


    @SuppressLint("MissingPermission")
    List<MyNetworkInfo.CellInfo> getCellInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        List<MyNetworkInfo.CellInfo> myCellInfoList = new ArrayList<>();
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo.isRegistered()) {
                if (cellInfo instanceof CellInfoCdma) myCellInfoList.add(getCellInfoCdma(((CellInfoCdma) cellInfo).getCellIdentity(), ((CellInfoCdma) cellInfo).getCellSignalStrength()));
                if (cellInfo instanceof CellInfoGsm)  myCellInfoList.add(getCellInfoGsm(((CellInfoGsm) cellInfo).getCellIdentity(), ((CellInfoGsm) cellInfo).getCellSignalStrength()));
                if (cellInfo instanceof CellInfoLte)  myCellInfoList.add(getCellInfoLte(((CellInfoLte) cellInfo).getCellIdentity(), ((CellInfoLte) cellInfo).getCellSignalStrength()));
                if (cellInfo instanceof CellInfoWcdma)  myCellInfoList.add(getCellInfoWcdma(((CellInfoWcdma) cellInfo).getCellIdentity(), ((CellInfoWcdma) cellInfo).getCellSignalStrength()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (cellInfo instanceof CellInfoTdscdma)  myCellInfoList.add(getCellInfoTdscdma(((CellInfoTdscdma) cellInfo).getCellIdentity(), ((CellInfoTdscdma) cellInfo).getCellSignalStrength()));
                    if (cellInfo instanceof CellInfoNr)  myCellInfoList.add(getCellInfoNr((CellIdentityNr) ((CellInfoNr) cellInfo).getCellIdentity(), (CellSignalStrengthNr) ((CellInfoNr) cellInfo).getCellSignalStrength()));
                }
            }
        }
        return myCellInfoList;
    }

    MyNetworkInfo.WifiInfo getWifiInfo() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String rssi = String.valueOf(wifiInfo.getRssi());
        String linkSpeed = String.valueOf(wifiInfo.getLinkSpeed());
        String networkId = String.valueOf(wifiInfo.getNetworkId());
        String frequency = String.valueOf(wifiInfo.getFrequency());
        String hiddenSSID = String.valueOf(wifiInfo.getHiddenSSID());

        String passpointFqdn, passpointProviderFriendlyName, rxLinkSpeedMbps, txLinkSpeedMbps;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (wifiInfo.getPasspointFqdn() == null)
                passpointFqdn = "NULL";
            else
                passpointFqdn = wifiInfo.getPasspointFqdn();
            if (wifiInfo.getPasspointProviderFriendlyName() == null)
                passpointProviderFriendlyName = "NULL";
            else
                passpointProviderFriendlyName = wifiInfo.getPasspointProviderFriendlyName();
            rxLinkSpeedMbps = String.valueOf(wifiInfo.getRxLinkSpeedMbps());
            txLinkSpeedMbps = String.valueOf(wifiInfo.getTxLinkSpeedMbps());
        } else {
            passpointFqdn = "Added in API level 29";
            passpointProviderFriendlyName = "Added in API level 29";
            rxLinkSpeedMbps = "Added in API level 29";
            txLinkSpeedMbps = "Added in API level 29";
        }
        String maxSupportedRxLinkSpeedMbps, maxSupportedTxLinkSpeedMbps, wifiStandard;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            maxSupportedRxLinkSpeedMbps = String.valueOf(wifiInfo.getMaxSupportedRxLinkSpeedMbps());
            maxSupportedTxLinkSpeedMbps = String.valueOf(wifiInfo.getMaxSupportedTxLinkSpeedMbps());
            wifiStandard = String.valueOf(wifiInfo.getWifiStandard());
        } else {
            maxSupportedRxLinkSpeedMbps = "Added in API level 30";
            maxSupportedTxLinkSpeedMbps = "Added in API level 30";
            wifiStandard = "Added in API level 30";
        }
        String currentSecurityType, subscriptionId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            currentSecurityType = String.valueOf(wifiInfo.getCurrentSecurityType());
            subscriptionId = String.valueOf(wifiInfo.getSubscriptionId());
        } else {
            currentSecurityType = "Added in API level 31";
            subscriptionId = "Added in API level 31";
        }
        return new MyNetworkInfo.WifiInfo(rssi, linkSpeed, networkId, frequency,
                passpointFqdn, passpointProviderFriendlyName, rxLinkSpeedMbps, txLinkSpeedMbps,
                maxSupportedRxLinkSpeedMbps, maxSupportedTxLinkSpeedMbps, wifiStandard,
                currentSecurityType, subscriptionId, hiddenSSID);
    }

    public class ContinuesUpdateTask extends TimerTask {
        public MyNetworkInfo myNetworkInfo;

        ContinuesUpdateTask(MyNetworkInfo myNetworkInfo) {
            this.myNetworkInfo = myNetworkInfo;
        }

        public void run() {
            monitorWiFiInfo();
            monitorCellInfo();
        }

        public void monitorWiFiInfo() {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String rssi = String.valueOf(wifiInfo.getRssi());
            String linkSpeed = String.valueOf(wifiInfo.getLinkSpeed());
            this.myNetworkInfo.wifiInfo.wifi_linkSpeed += (";" + linkSpeed);
            this.myNetworkInfo.wifiInfo.wifi_rssi += (";" + rssi);
            String rxLinkSpeedMbps, txLinkSpeedMbps;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                rxLinkSpeedMbps = String.valueOf(wifiInfo.getRxLinkSpeedMbps());
                txLinkSpeedMbps = String.valueOf(wifiInfo.getTxLinkSpeedMbps());
                this.myNetworkInfo.wifiInfo.wifi_rxLinkSpeedMbps += (";" + rxLinkSpeedMbps);
                this.myNetworkInfo.wifiInfo.wifi_txLinkSpeedMbps += (";" + txLinkSpeedMbps);
            }
        }

        @SuppressLint("MissingPermission")
        public void monitorCellInfo() {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo.isRegistered()) {
                    if (cellInfo instanceof CellInfoCdma) {
                        CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                        for (MyNetworkInfo.CellInfo myCellInfo : this.myNetworkInfo.cellInfo)
                            if (myCellInfo.cell_Type.equals("CDMA")) {
                                MyNetworkInfo.CellInfoCdma myCellInfoCdma = (MyNetworkInfo.CellInfoCdma) myCellInfo;
                                if (myCellInfoCdma.cell_basestationId.equals(String.valueOf(cellInfoCdma.getCellIdentity().getBasestationId()))) {
                                    myCellInfoCdma.cell_cdmaDbm += (";" + cellInfoCdma.getCellSignalStrength().getCdmaDbm());
                                    myCellInfoCdma.cell_dbm += (";" + cellInfoCdma.getCellSignalStrength().getDbm());
                                    break;
                                }
                            }
                    }
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        for (MyNetworkInfo.CellInfo myCellInfo : this.myNetworkInfo.cellInfo)
                            if (myCellInfo.cell_Type.equals("GSM")) {
                                MyNetworkInfo.CellInfoGsm myCellInfoGsm = (MyNetworkInfo.CellInfoGsm) myCellInfo;
                                if (myCellInfoGsm.cell_cid.equals(String.valueOf(cellInfoGsm.getCellIdentity().getCid()))) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                        myCellInfoGsm.cell_rssi += (";" + cellInfoGsm.getCellSignalStrength().getRssi());
                                    myCellInfoGsm.cell_dbm += (";" + cellInfoGsm.getCellSignalStrength().getDbm());
                                    break;
                                }
                            }
                    }
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        for (MyNetworkInfo.CellInfo myCellInfo : this.myNetworkInfo.cellInfo)
                            if (myCellInfo.cell_Type.equals("LTE")) {
                                MyNetworkInfo.CellInfoLte myCellInfoLte = (MyNetworkInfo.CellInfoLte) myCellInfo;
                                if (myCellInfoLte.cell_ci.equals(String.valueOf(cellInfoLte.getCellIdentity().getCi())) && myCellInfoLte.cell_pci.equals(String.valueOf(cellInfoLte.getCellIdentity().getPci()))) {
                                    myCellInfoLte.cell_dbm += (";" + cellInfoLte.getCellSignalStrength().getDbm());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        myCellInfoLte.cell_rsrp += ";";
                                        myCellInfoLte.cell_rsrp += String.valueOf(cellInfoLte.getCellSignalStrength().getRsrp());
                                        myCellInfoLte.cell_rsrq += ";";
                                        myCellInfoLte.cell_rsrq += String.valueOf(cellInfoLte.getCellSignalStrength().getRsrq());
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                        myCellInfoLte.cell_rssi += (";" + cellInfoLte.getCellSignalStrength().getRssi());
                                    break;
                                }
                            }
                    }
                    if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                        for (MyNetworkInfo.CellInfo myCellInfo : this.myNetworkInfo.cellInfo)
                            if (myCellInfo.cell_Type.equals("WCDMA")) {
                                MyNetworkInfo.CellInfoWcdma myCellInfoWcdma = (MyNetworkInfo.CellInfoWcdma) myCellInfo;
                                if (myCellInfoWcdma.cell_cid.equals(String.valueOf(cellInfoWcdma.getCellIdentity().getCid()))) {
                                    myCellInfoWcdma.cell_dbm += (";" + cellInfoWcdma.getCellSignalStrength().getDbm());
                                    break;
                                }
                            }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (cellInfo instanceof CellInfoTdscdma) {
                            CellInfoTdscdma cellInfoTdscdma = (CellInfoTdscdma) cellInfo;
                            for (MyNetworkInfo.CellInfo myCellInfo : this.myNetworkInfo.cellInfo)
                                if (myCellInfo.cell_Type.equals("TDSCDMA")) {
                                    MyNetworkInfo.CellInfoTdscdma myCellInfoTdscdma = (MyNetworkInfo.CellInfoTdscdma) myCellInfo;
                                    if (myCellInfoTdscdma.cell_cid.equals(String.valueOf(cellInfoTdscdma.getCellIdentity().getCid()))) {
                                        myCellInfoTdscdma.cell_dbm += (";" + cellInfoTdscdma.getCellSignalStrength().getDbm());
                                        myCellInfoTdscdma.cell_rscp += (";" + cellInfoTdscdma.getCellSignalStrength().getRscp());
                                        break;
                                    }
                                }
                        }
                        if (cellInfo instanceof CellInfoNr) {
                            CellInfoNr cellInfoNr = (CellInfoNr) cellInfo;
                            for (MyNetworkInfo.CellInfo myCellInfo : this.myNetworkInfo.cellInfo)
                                if (myCellInfo.cell_Type.equals("NR")) {
                                    MyNetworkInfo.CellInfoNr myCellInfoNr = (MyNetworkInfo.CellInfoNr) myCellInfo;
                                    CellIdentityNr cellIdentityNr = (CellIdentityNr) cellInfoNr.getCellIdentity();
                                    CellSignalStrengthNr cellSignalStrengthNr = (CellSignalStrengthNr) cellInfoNr.getCellSignalStrength();
                                    if (myCellInfoNr.cell_nci.equals(String.valueOf((cellIdentityNr.getNci())))) {
                                        myCellInfoNr.cell_dbm += (";" + cellInfoNr.getCellSignalStrength().getDbm());
                                        myCellInfoNr.cell_ssRsrp += (";" + cellSignalStrengthNr.getSsRsrp());
                                        myCellInfoNr.cell_ssRsrq += (";" + cellSignalStrengthNr.getSsRsrq());
                                        myCellInfoNr.cell_ssSinr += (";" + cellSignalStrengthNr.getSsSinr());
                                        break;
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}
