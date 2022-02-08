<head>
    <script src="https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML" type="text/javascript"></script>
    <script type="text/x-mathjax-config">
        MathJax.Hub.Config({
            tex2jax: {
            skipTags: ['script', 'noscript', 'style', 'textarea', 'pre'],
            inlineMath: [['$','$']]
            }
        });
    </script>
</head>
<br />
<center style="font-size:45px;color:green;line-height:20px"> Mobile Access Bandwidth in Practice: </center>
<center style="font-size:30px;color:green;line-height:100px"> Measurement, Analysis, and Implications </center>

![license](https://img.shields.io/badge/GuestOS-Androidx86-green "Android")
![license](https://img.shields.io/badge/Licence-Apache%202.0-blue.svg "Apache")
## Table of Contents
[Introduction](#introduction)

[Data Release](#data-release)

[Implementation of Cross-Layer and Cross-Technology Measurement Tool](#implementation-of-cross-layer-and-cross-technology-measurement-tool)

[Implementation of New Bandwidth Testing Service (Swiftest)](#implementation-of-new-bandwidth-testing-service-swiftest)

## Introduction
Our study focuses on characterizing mobile access bandwidth in the wild. We work with a major commercial mobile bandwidth testing (BTS) app to analyze mobile access bandwidths of 3.54M end users based on fine-grained measurement and diagnostic information collected by our cross-layer and cross-technology measurement tool.
Additionally, our analysis provides insights on building ultrafast, ultra-light bandwidth testing services (BTSes) called Swiftest at scale.
Our new design dramatically reduces the test time of the commercial BTS app from 10 seconds to 1 second on average, with a 15$\times$ reduction on the backend cost.
This repository contains our implementation of the cross-layer and cross-technology measurement tool, our released data, and our implementation of Swiftest.

## Data Release
Our data include two parts: a four-month fine-grained dataset and a two-year coarse-grained dataset (provided by BTS-APP operation team). Currently, we have released a portion of the representative sample data (with proper anonymization) belonging to the first dataset for references, including the fine-grained collected data of 50K tests in 4G, 5G, and WiFi 4, 5, 6 access technologies (10K tests each). These data are organized in 4G.csv, 5G.csv, wifi4.csv, wifi5.csv, and wifi6.csv, respectively (for detailed data, please click [here](https://github.com/mobilebandwidth/mobilebandwidth.github.io/tree/main/data)). For each CSV file, we list the specific information coupled with the regarding description as follows. As to the remainder, we are still in discussion with the authority to what extend can it be released.

### 4G.csv

| Column Name                | Description                                                  |
| -------------------------- | ------------------------------------------------------------ |
| `user_uid`                   | Unique ID generated to identify a user (cannot be related to the user’s true indentity) |
| `brand`                      | Mobile device brand                                          |
| `model`                      | Mobile device model                                          |
| `network_type`               | Network type                                                 |
| `os_version`                 | Android version                                              |
| `user_isp_id`                | ISP id                                                       |
| `user_region_id`             | Region (province) of a user                                  |
| `user_city_id`               | City of a user                                               |
| `bandwidth_Mbps`             | Bandwidth testing result in Mbps                             |
| `cell_asuLevel`              | LTE signal strength in ASU value                             |
| `cell_dbm`                   | LTE signal strength in dBm                                   |
| `cell_level`                 | Abstract level value for the overall signal quality          |
| `cell_mcc`                   | 3-digit mobile country code                                  |
| `cell_mnc`                   | 2 or 3-digit mobile network code                             |
| `cell_mobileNetworkOperator` | 5 or 6-digit code (MCC+MNC) for mobile network operator      |
| `cell_rssi`                  | Received signal strength indication (RSSI) in dBm            |
| `cell_timingAdvance`         | Timing advance value for LTE                                 |
| `cell_bands`                 | Bands of the LTE connection                                  |
| `cell_bandwidth`             | Cell bandwidth in kHz                                        |
| `cell_ci`                    | 28-bit cell identity code                                    |
| `cell_earfcn`                | 18-bit absolute radio frequency channel node                 |
| `cell_tac`                   | 16-bit Tracking Area Code                                    |
| `cell_rsrp`                  | Reference signal received power in dBm                       |
| `cell_rsrq`                  | Reference signal received quality                            |
| `cell_rssnr`                 | Reference signal signal-to-noise ratio                       |

### 5G.csv

| Information    | Description                                                  |
| -------------- | ------------------------------------------------------------ |
| `user_uid`       | Unique ID generated to identify a user (cannot be related to the user’s true indentity) |
| `brand`          | Mobile device brand                                          |
| `model`          | Mobile device model                                          |
| `network_type`   | Network type                                                 |
| `os_version`     | Android version                                              |
| `user_isp_id`    | ISP id                                                       |
| `user_region_id` | Region (province) of a user                                  |
| `user_city_id`   | City of a user                                               |
| `bandwidth_Mbps` | Bandwidth testing result in Mbps                             |
| `cell_asuLevel`  | LTE signal strength in ASU value                             |
| `cell_dbm`       | LTE signal strength in dBm                                   |
| `cell_level`     | Abstract level value for the overall signal quality          |
| `cell_mcc`       | 3-digit mobile country code                                  |
| `cell_mnc`       | 2 or 3-digit mobile network code                             |
| `cell_bands`     | Bands of the LTE connection                                  |
| `cell_tac`       | 16-bit Tracking Area Code                                    |
| `cell_nrarfcn`   | New radio absolute radio frequency channel number            |
| `cell_csiRsrp`   | CSI reference signal received power                          |
| `cell_csiRsrq`   | CSI reference signal received quality                        |
| `cell_ssRsrp`    | SS reference signal received power                           |
| `cell_ssRsrq`    | SS reference signal received quality                         |
| `cell_ssSinr`    | SS signal-to-noise and interference ratio                    |

### wifi4/wifi5/wifi6.csv

| Information                      | Description                                                  |
| -------------------------------- | ------------------------------------------------------------ |
| `user_uid`                         | Unique ID of a user (cannot be related to the user’s true indentity) |
| `brand`                            | Mobile device brand                                          |
| `model`                            | Mobile device model                                          |
| `network_type`                     | Network type                                                 |
| `os_version`                       | Android version                                              |
| `user_isp_id`                      | ISP id                                                       |
| `user_region_id`                   | Region (province) of a user                                  |
| `user_city_id`                     | City of a user                                               |
| `bandwidth_Mbps`                   | Bandwidth testing result in Mbps                             |
| `wifi_rssi`                        | The received signal strength indicator of the current 802.11 network in dBm |
| `wifi_linkSpeed`                   | Current link speed                                           |
| `wifi_hiddenSSID`                  | Whether this network does not broadcast its SSID             |
| `wifi_frequency`                   | Current WiFi frequency in MHz                                |
| `wifi_rxLinkSpeedMbps`             | Current receive link speed in Mbps                           |
| `wifi_txLinkSpeedMbps`             | Current transmit link speed in Mbps                          |
| `wifi_maxSupportedRxLinkSpeedMbps` | Maximum supported receive link speed in Mbps                 |
| `wifi_maxSupportedTxLinkSpeedMbps` | Maximum supported transmit link speed in Mbps                |
| `wifi_wifiStandard`                | Connection Wi-Fi standard                                    |

## Implementation of Cross-Layer and Cross-Technology Measurement Tool
We have released the project of our cross-layer and cross-technology (CLCT) measurement tool [here](https://github.com/mobilebandwidth/mobilebandwidth.github.io/tree/main/CLCT-Measurement). Note that this project can be directly compiled and run using the [`Android Studio`](https://developer.android.com/studio) platform with the support of [`Java SE Development Kit 8`](https://www.oracle.com/java/technologies/downloads/).

## Implementation of New Bandwidth Testing Service (Swiftest)
Currently we are scrutinizing the codebase to avoid possible anonymity violation. To this end, we will release Swiftest's source code in a *module-by-module manner* as soon as we have finished examining a module and acquire its release permission from the authority. The codebase of Swiftest is organized as follows.
```
Swiftest
|---- client-side
|---- server-side
          |---- test-server
          |---- master-server
```
+ `Swiftest/client-side` is an Android Studio project similar to the CLCT measurement tool.
+ `Swiftest/server-side/test-server` currently includes a simplified version of test servers' transmission logic. We are still negotiating with BTS-APP's operational team to acquire the release permission of the entire transmission logic of test servers.
+ `Swiftest/server-side/master-server` contains the source code (in Go) and the executable binary of master-server.


The released part can be found [here](https://github.com/mobilebandwidth/mobilebandwidth.github.io/tree/main/Swiftest).
