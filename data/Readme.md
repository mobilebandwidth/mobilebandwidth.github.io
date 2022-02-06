## Data Release

We currently release a portion of data (with proper anonymization) for references, including the fine-grained collected data of 50K tests in 4G, 5G, and WiFi 4, 5, 6 access technologies (10K tests each). These data are organized in 4G.csv, 5G.csv, wifi4.csv, wifi5.csv, and wifi6.csv, respectively. We list the specific information we collect coupled with the regarding description for each access technology as follows.

### 4G.csv

| Column Name                | Description                                                  |
| -------------------------- | ------------------------------------------------------------ |
| user_uid                   | Unique ID generated to identify a user (cannot be related to the user’s true indentity) |
| brand                      | Mobile device brand                                          |
| model                      | Mobile device model                                          |
| network_type               | Network type                                                 |
| os_version                 | Android version                                              |
| user_isp_id                | ISP id                                                       |
| user_region_id             | Region (Province) of a user                                  |
| user_city_id               | City of a user                                               |
| bandwidth_Mbps             | Bandwidth testing result in Mbps                             |
| cell_asuLevel              | LTE signal strength in ASU value                             |
| cell_dbm                   | LTE signal strength in dBm                                   |
| cell_level                 | Abstract level value for the overall signal quality          |
| cell_mcc                   | 3-digit mobile country code                                  |
| cell_mnc                   | 2 or 3-digit mobile network code                             |
| cell_mobileNetworkOperator | 5 or 6-digit code (MCC+MNC) for mobile network operator      |
| cell_rssi                  | Received signal strength indication (RSSI) in dBm            |
| cell_timingAdvance         | Timing advance value for LTE                                 |
| cell_bands                 | Bands of the LTE connection                                  |
| cell_bandwidth             | Cell bandwidth in kHz                                        |
| cell_ci                    | 28-bit cell identity code                                    |
| cell_earfcn                | 18-bit absolute radio frequency channel node                 |
| cell_tac                   | 16-bit Tracking Area Code                                    |
| cell_rsrp                  | Reference signal received power in dBm                       |
| cell_rsrq                  | Reference signal received quality                            |
| cell_rssnr                 | Reference signal signal-to-noise ratio                       |

### 5G.csv

| Information    | Description                                                  |
| -------------- | ------------------------------------------------------------ |
| user_uid       | Unique ID generated to identify a user (cannot be related to the user’s true indentity) |
| brand          | Mobile device brand                                          |
| model          | Mobile device model                                          |
| network_type   | Network type                                                 |
| os_version     | Android version                                              |
| user_isp_id    | ISP id                                                       |
| user_region_id | Region (Province) of a user                                  |
| user_city_id   | City of a user                                               |
| bandwidth_Mbps | Bandwidth testing result in Mbps                             |
| cell_asuLevel  | LTE signal strength in ASU value                             |
| cell_dbm       | LTE signal strength in dBm                                   |
| cell_level     | Abstract level value for the overall signal quality          |
| cell_mcc       | 3-digit mobile country code                                  |
| cell_mnc       | 2 or 3-digit mobile network code                             |
| cell_bands     | Bands of the LTE connection                                  |
| cell_tac       | 16-bit Tracking Area Code                                    |
| cell_nrarfcn   | New radio absolute radio frequency channel number            |
| cell_csiRsrp   | CSI reference signal received power                          |
| cell_csiRsrq   | CSI reference signal received quality                        |
| cell_ssRsrp    | SS reference signal received power                           |
| cell_ssRsrq    | SS reference signal received quality                         |
| cell_ssSinr    | SS signal-to-noise and interference ratio                    |

### wifi4/wifi5/wifi6.csv

| Information                      | Description                                                  |
| -------------------------------- | ------------------------------------------------------------ |
| user_uid                         | Unique ID of a user (cannot be related to the user’s true indentity) |
| brand                            | Mobile device brand                                          |
| model                            | Mobile device model                                          |
| network_type                     | Network type                                                 |
| os_version                       | Android version                                              |
| user_isp_id                      | ISP id                                                       |
| user_region_id                   | Region (Province) of a user                                  |
| user_city_id                     | City of a user                                               |
| bandwidth_Mbps                   | Bandwidth testing result in Mbps                             |
| wifi_rssi                        | The received signal strength indicator of the current 802.11 network in dBm |
| wifi_linkSpeed                   | Current link speed                                           |
| wifi_hiddenSSID                  | Whether this network does not broadcast its SSID             |
| wifi_frequency                   | Current WiFi frequency in MHz                                |
| wifi_rxLinkSpeedMbps             | Current receive link speed in Mbps                           |
| wifi_txLinkSpeedMbps             | Current transmit link speed in Mbps                          |
| wifi_maxSupportedRxLinkSpeedMbps | Maximum supported receive link speed in Mbps                 |
| wifi_maxSupportedTxLinkSpeedMbps | Maximum supported transmit link speed in Mbps                |
| wifi_wifiStandard                | Connection Wi-Fi standard                                    |
