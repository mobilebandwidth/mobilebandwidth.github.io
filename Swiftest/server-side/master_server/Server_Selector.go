package main

import (
	"fmt"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"net/http"
	"sort"
	"strconv"
	"sync"
)
import "github.com/gin-gonic/gin"

var r *gin.Engine

type Config struct {
	BPSleep             int      `yaml:"bp_sleep"`
	DownloadSizeSleep   int      `yaml:"download_size_sleep"`
	TimeWindow          int      `yaml:"time_window"`
	TestTimeout         int      `yaml:"test_timeout"`
	GetInfoInterval     int      `yaml:"get_info_interval"`
	MaxTrafficUse4g     int      `yaml:"max_traffic_use_4_g"`
	MaxTrafficUse5g     int      `yaml:"max_traffic_use_5_g"`
	MaxTrafficUseWifi   int      `yaml:"max_traffic_use_wifi"`
	MaxTrafficUseOthers int      `yaml:"max_traffic_use_others"`
	KSimilar            int      `yaml:"k_similar"`
	Threshold           float64  `yaml:"threshold"`
	MaxBandwidth        float64  `yaml:"max_bandwidth"` // bandwidth limit for each server
	Servers             []string `yaml:"servers"`
}

var GlobalConfig Config

func init() {
	GlobalConfig = Config{}
	config, err := ioutil.ReadFile("./config.yaml")
	if err != nil {
		fmt.Print(err)
	}
	err = yaml.Unmarshal(config, &GlobalConfig)
	if err != nil {
		fmt.Print(err)
	} else {
		fmt.Printf("config: %+v\n", GlobalConfig)
	}
}

func getBandwidthUsed(ip string) float64 {
	resp, err := http.Get("http://" + ip + ":8000/bandwidth")
	if err != nil {
		fmt.Println(err)
		return 10000
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	bandwidthUsed, _ := strconv.ParseFloat(string(body), 64)
	return bandwidthUsed * 8 / 1024 / 1024
}

type bdu struct {
	ip string
	bd float64 // bandwidthUsed
}

func SS(eBandwidth float64) (int, []string) {
	var bandwidthUsed []bdu
	wg := sync.WaitGroup{}
	wg.Add(len(GlobalConfig.Servers))
	buCh := make(chan bdu, len(GlobalConfig.Servers))
	for _, ip := range GlobalConfig.Servers {
		ip := ip
		go func() {
			bandwidthUsed := bdu{ip: ip, bd: getBandwidthUsed(ip)}
			buCh <- bandwidthUsed
			wg.Done()
		}()
	}
	wg.Wait()
	close(buCh)
	for bu := range buCh {
		bandwidthUsed = append(bandwidthUsed, bu)
	}
	sort.Slice(bandwidthUsed, func(i, j int) bool {
		return bandwidthUsed[i].bd < bandwidthUsed[j].bd
	})
	fmt.Println(bandwidthUsed)
	num := 0
	var ipList []string
	for _, bu := range bandwidthUsed {
		rest := GlobalConfig.MaxBandwidth - bu.bd
		if rest <= 0 {
			continue
		} else {
			num++
			ipList = append(ipList, bu.ip)
			eBandwidth -= rest
			if eBandwidth <= 0 {
				break
			}
		}
	}
	if eBandwidth > 0 {
		return -1, nil
	} else {
		return num, ipList
	}
}

func main() {
	r = gin.Default()
	r.GET("/hello", func(c *gin.Context) {
		c.JSON(http.StatusOK, `hello, Swiftest!`)
	})
	r.GET("/speedtest/iplist/available", func(c *gin.Context) {
		type Res struct {
			ServerNum int      `json:"server_num"`
			IpList    []string `json:"ip_list"`
			ClientIP  string   `json:"client_ip"`
		}
		var res Res
		res.ServerNum = len(GlobalConfig.Servers)
		res.IpList = GlobalConfig.Servers
		res.ClientIP = c.ClientIP()
		c.JSON(http.StatusOK, res)
	})
	r.POST("/speedtest/info", func(c *gin.Context) {
		type Req struct {
			NetworkType        string   `json:"network_type"`
			ServersSortedByRTT []string `json:"servers_sorted_by_rtt"`
		}
		var req Req
		err := c.BindJSON(&req)
		if err != nil {
			fmt.Println("err")
			return
		}
		type Res struct {
			ServerNum         int      `json:"server_num"`
			IpList            []string `json:"ip_list"`
			TestTimeout       int      `json:"test_timeout"`
			DownloadSizeSleep int      `json:"download_size_sleep"`
			BPSleep           int      `json:"bp_sleep"`
			TimeWindow        int      `json:"time_window"`
			KSimilar          int      `json:"k_similar"`
			MaxTrafficUse     int      `json:"max_traffic_use"`
			Threshold         float64  `json:"threshold"`
			GetInfoInterval   int      `json:"get_info_interval"`
		}
		var res Res
		res.BPSleep = GlobalConfig.BPSleep
		res.DownloadSizeSleep = GlobalConfig.DownloadSizeSleep
		res.TimeWindow = GlobalConfig.TimeWindow
		res.TestTimeout = GlobalConfig.TestTimeout
		res.MaxTrafficUse = GlobalConfig.MaxTrafficUseOthers
		res.KSimilar = GlobalConfig.KSimilar
		res.Threshold = GlobalConfig.Threshold
		res.GetInfoInterval = GlobalConfig.GetInfoInterval
		var eBandwidth float64
		if req.NetworkType == "LTE" || req.NetworkType == "3G" || req.NetworkType == "2G" {
			eBandwidth = 400
			res.MaxTrafficUse = GlobalConfig.MaxTrafficUse4g
		} else if req.NetworkType == "WIFI" {
			eBandwidth = 1500
			res.MaxTrafficUse = GlobalConfig.MaxTrafficUseWifi
		} else if req.NetworkType == "5G" {
			eBandwidth = 1000
			res.MaxTrafficUse = GlobalConfig.MaxTrafficUse5g
		} else {
			eBandwidth = 500
			res.MaxTrafficUse = GlobalConfig.MaxTrafficUseOthers
		}
		res.ServerNum, res.IpList = SS(eBandwidth)
		c.JSON(http.StatusOK, res)
	})
	if err := r.Run(); err != nil {
		fmt.Println(err)
	}
}
