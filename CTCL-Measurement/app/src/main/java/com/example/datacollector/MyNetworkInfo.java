package com.example.datacollector;

import java.util.List;

public class MyNetworkInfo {
    public String apiLevel;
    public String connectionType;
    public List<CellInfo> cellInfo;
    public WifiInfo wifiInfo;

    public static class CellInfo {
        public String cell_Type;
        public CellInfo(String cellType) {
            this.cell_Type = cellType;
        }
    }

    public static class CellInfoCdma extends CellInfo {
        public String cell_basestationId;
        public String cell_networkId;
        public String cell_systemId;
        public String cell_asuLevel;
        public String cell_cdmaDbm;
        public String cell_cdmaEcio;
        public String cell_cdmaLevel;
        public String cell_dbm;
        public String cell_evdodbm;
        public String cell_evdoEcio;
        public String cell_evdoLevel;
        public String cell_evdoSnr;
        public String cell_level;

        public CellInfoCdma(String cell_type, String basestationId, String networkId, String systemId, String asuLevel, String cdmaDbm, String cdmaEcio, String cdmaLevel, String dbm, String evdodbm, String evdoEcio, String evdoLevel, String evdoSnr, String level) {
            super(cell_type);
            this.cell_basestationId = basestationId;
            this.cell_networkId = networkId;
            this.cell_systemId = systemId;
            this.cell_asuLevel = asuLevel;
            this.cell_cdmaDbm = cdmaDbm;
            this.cell_cdmaEcio = cdmaEcio;
            this.cell_cdmaLevel = cdmaLevel;
            this.cell_dbm = dbm;
            this.cell_evdodbm = evdodbm;
            this.cell_evdoEcio = evdoEcio;
            this.cell_evdoLevel = evdoLevel;
            this.cell_evdoSnr = evdoSnr;
            this.cell_level = level;
        }
    }

    public static class CellInfoGsm extends CellInfo {
        public String cell_arfcn;
        public String cell_bsic;
        public String cell_cid;
        public String cell_lac;
        public String cell_mcc;
        public String cell_mnc;
        public String cell_mobileNetworkOperator;
        public String cell_asuLevel;
        public String cell_bitErrorRate;
        public String cell_dbm;
        public String cell_level;
        public String cell_rssi;
        public String cell_timingAdvance;

        public CellInfoGsm(String cell_type, String arfcn, String bsic, String cid, String lac, String mcc, String mnc, String mobileNetworkOperator, String asuLevel, String bitErrorRate, String dbm, String level, String rssi, String timingAdvance) {
            super(cell_type);
            this.cell_arfcn = arfcn;
            this.cell_bsic = bsic;
            this.cell_cid = cid;
            this.cell_lac = lac;
            this.cell_mcc = mcc;
            this.cell_mnc = mnc;
            this.cell_mobileNetworkOperator = mobileNetworkOperator;
            this.cell_asuLevel = asuLevel;
            this.cell_bitErrorRate = bitErrorRate;
            this.cell_dbm = dbm;
            this.cell_level = level;
            this.cell_rssi = rssi;
            this.cell_timingAdvance = timingAdvance;
        }
    }

    public static class CellInfoLte extends CellInfo {
        public String[] cell_bands;
        public String cell_bandwidth;
        public String cell_ci;
        public String cell_earfcn;
        public String cell_mcc;
        public String cell_mnc;
        public String cell_mobileNetworkOperator;
        public String cell_pci;
        public String cell_tac;
        public String cell_asuLevel;
        public String cell_cqi;
        public String cell_cqiTableIndex;
        public String cell_dbm;
        public String cell_level;
        public String cell_rsrp;
        public String cell_rsrq;
        public String cell_rssi;
        public String cell_rssnr;
        public String cell_timingAdvance;

        public CellInfoLte(String cell_type, String[] bands, String bandwidth, String ci, String earfcn, String mcc, String mnc, String mobileNetworkOperator, String pci, String tac, String asuLevel, String cqi, String cqiTableIndex, String dbm, String level, String rsrp, String rsrq, String rssi, String rssnr, String timingAdvance) {
            super(cell_type);
            this.cell_bands = bands;
            this.cell_bandwidth = bandwidth;
            this.cell_ci = ci;
            this.cell_earfcn = earfcn;
            this.cell_mcc = mcc;
            this.cell_mnc = mnc;
            this.cell_mobileNetworkOperator = mobileNetworkOperator;
            this.cell_pci = pci;
            this.cell_tac = tac;
            this.cell_asuLevel = asuLevel;
            this.cell_cqi = cqi;
            this.cell_cqiTableIndex = cqiTableIndex;
            this.cell_dbm = dbm;
            this.cell_level = level;
            this.cell_rsrp = rsrp;
            this.cell_rsrq = rsrq;
            this.cell_rssi = rssi;
            this.cell_rssnr = rssnr;
            this.cell_timingAdvance = timingAdvance;
        }
    }

    public static class CellInfoWcdma extends CellInfo {
        public String cell_cid;
        public String cell_lac;
        public String cell_mcc;
        public String cell_mnc;
        public String cell_mobileNetworkOperator;
        public String cell_psc;
        public String cell_uarfcn;
        public String cell_asuLevel;
        public String cell_dbm;
        public String cell_ecNo;
        public String cell_level;

        public CellInfoWcdma(String cell_type, String cid, String lac, String mcc, String mnc, String mobileNetworkOperator, String psc, String uarfcn, String asuLevel, String dbm, String ecNo, String level) {
            super(cell_type);
            this.cell_cid = cid;
            this.cell_lac = lac;
            this.cell_mcc = mcc;
            this.cell_mnc = mnc;
            this.cell_mobileNetworkOperator = mobileNetworkOperator;
            this.cell_psc = psc;
            this.cell_uarfcn = uarfcn;
            this.cell_asuLevel = asuLevel;
            this.cell_dbm = dbm;
            this.cell_ecNo = ecNo;
            this.cell_level = level;
        }
    }

    public static class CellInfoTdscdma extends CellInfo {
        public String cell_cid;
        public String cell_cpid;
        public String cell_lac;
        public String cell_mcc;
        public String cell_mnc;
        public String cell_mobileNetworkOperator;
        public String cell_uarfcn;
        public String cell_asuLevel;
        public String cell_dbm;
        public String cell_level;
        public String cell_rscp;

        public CellInfoTdscdma(String cell_type, String cid, String cpid, String lac, String mcc, String mnc, String mobileNetworkOperator, String uarfcn, String asuLevel, String dbm, String level, String rscp) {
            super(cell_type);
            this.cell_cid = cid;
            this.cell_cpid = cpid;
            this.cell_lac = lac;
            this.cell_mcc = mcc;
            this.cell_mnc = mnc;
            this.cell_mobileNetworkOperator = mobileNetworkOperator;
            this.cell_uarfcn = uarfcn;
            this.cell_asuLevel = asuLevel;
            this.cell_dbm = dbm;
            this.cell_level = level;
            this.cell_rscp = rscp;
        }
    }

    public static class CellInfoNr extends CellInfo {
        public String[] cell_bands;
        public String cell_mcc;
        public String cell_mnc;
        public String cell_nci;
        public String cell_nrarfcn;
        public String cell_pci;
        public String cell_tac;
        public String cell_asuLevel;
        public List<String> cell_csicqiReport;
        public String cell_csicqiTableIndex;
        public String cell_csiRsrp;
        public String cell_csiRsrq;
        public String cell_csiSinr;
        public String cell_dbm;
        public String cell_level;
        public String cell_ssRsrp;
        public String cell_ssRsrq;
        public String cell_ssSinr;

        public CellInfoNr(String cell_type, String[] bands, String mcc, String mnc, String nci, String nrarfcn, String pci, String tac, String asuLevel, List<String> csicqiReport, String csicqiTableIndex, String csiRsrp, String csiRsrq, String csiSinr, String dbm, String level, String ssRsrp, String ssRsrq, String ssSinr) {
            super(cell_type);
            this.cell_bands = bands;
            this.cell_mcc = mcc;
            this.cell_mnc = mnc;
            this.cell_nci = nci;
            this.cell_nrarfcn = nrarfcn;
            this.cell_pci = pci;
            this.cell_tac = tac;
            this.cell_asuLevel = asuLevel;
            this.cell_csicqiReport = csicqiReport;
            this.cell_csicqiTableIndex = csicqiTableIndex;
            this.cell_csiRsrp = csiRsrp;
            this.cell_csiRsrq = csiRsrq;
            this.cell_csiSinr = csiSinr;
            this.cell_dbm = dbm;
            this.cell_level = level;
            this.cell_ssRsrp = ssRsrp;
            this.cell_ssRsrq = ssRsrq;
            this.cell_ssSinr = ssSinr;
        }
    }


    public static class WifiInfo {
        /* Added in API 1 */
        public String wifi_rssi;
        public String wifi_linkSpeed;
        public String wifi_networkId;
        public String wifi_hiddenSSID;

        /* Added in API 21 */
        public String wifi_frequency;

        /* Added in API 29 */
        public String wifi_passpointFqdn;
        public String wifi_passpointProviderFriendlyName;
        public String wifi_rxLinkSpeedMbps;
        public String wifi_txLinkSpeedMbps;

        /* Added in API 30 */
        public String wifi_maxSupportedRxLinkSpeedMbps;
        public String wifi_maxSupportedTxLinkSpeedMbps;
        public String wifi_wifiStandard;

        /* Added in API 31 */
        public String wifi_currentSecurityType;
        public String wifi_subscriptionId;

        public WifiInfo(String rssi, String linkSpeed, String networkId, String frequency,
                        String passpointFqdn, String passpointProviderFriendlyName, String rxLinkSpeedMbps, String txLinkSpeedMbps,
                        String maxSupportedRxLinkSpeedMbps, String maxSupportedTxLinkSpeedMbps, String wifiStandard,
                        String currentSecurityType, String subscriptionId, String hiddenSSID) {
            this.wifi_rssi = rssi;
            this.wifi_linkSpeed = linkSpeed;
            this.wifi_networkId = networkId;
            this.wifi_frequency = frequency;
            this.wifi_passpointFqdn = passpointFqdn;
            this.wifi_passpointProviderFriendlyName = passpointProviderFriendlyName;
            this.wifi_rxLinkSpeedMbps = rxLinkSpeedMbps;
            this.wifi_txLinkSpeedMbps = txLinkSpeedMbps;
            this.wifi_maxSupportedRxLinkSpeedMbps = maxSupportedRxLinkSpeedMbps;
            this.wifi_maxSupportedTxLinkSpeedMbps = maxSupportedTxLinkSpeedMbps;
            this.wifi_wifiStandard = wifiStandard;
            this.wifi_currentSecurityType = currentSecurityType;
            this.wifi_subscriptionId = subscriptionId;
            this.wifi_hiddenSSID = hiddenSSID;
        }
    }

    public MyNetworkInfo(String apiLevel, String connectionType, List<CellInfo> cellInfo, WifiInfo wifiInfo) {
        this.apiLevel = apiLevel;
        this.connectionType = connectionType;
        this.cellInfo = cellInfo;
        this.wifiInfo = wifiInfo;
    }
}
