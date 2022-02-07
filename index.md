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

# Mobile Access Bandwidth in Practice: Measurement, Analysis, and Implications

![license](https://img.shields.io/badge/GuestOS-Androidx86-green "Android")
![license](https://img.shields.io/badge/Licence-Apache%202.0-blue.svg "Apache")

## Table of Contents
[Introduction](#introduction)

[Data Release](#data-release)

[Implementation of Cross-Layer and Cross-Technology Measurement Tool](#implementation-of-cross-layer-and-cross-technology-measurement-tool)

[Implementation of Swiftest](#implementation-of-swiftest)

## Introduction
Our study focuses on characterizing mobile access bandwidth in the wild. We work with a major commercial mobile bandwidth testing (BTS) app to analyze mobile access bandwidths of 3.54M end users based on fine-grained measurement and diagnostic information collected by our cross-layer and cross-technology measurement tool.
Additionally, our analysis provides insights on building ultrafast, ultra-light bandwidth testing services (BTSes) called Swiftest at scale.
Our new design dramatically reduces the test time of the commercial BTS app from 10 seconds to 1 second on average, with a 15$\times$ reduction on the backend cost.
This repository contains our implementation of the cross-layer and cross-technology measurement tool, our released data, and our implementation of Swiftest.

## Data Release
We have released a portion of data (with proper anonymization) for references (for detailed data, please click [here](https://github.com/mobilebandwidth/mobilebandwidth.github.io/tree/main/data)).
As to the full dataset, we are still in discussion with the authority to what extend can it be released.

## Implementation of Cross-Layer and Cross-Technology Measurement Tool
We have released the project of our cross-layer and cross-technology (CLCT) measurement tool [here](https://github.com/mobilebandwidth/mobilebandwidth.github.io/tree/main/CLCT-Measurement). Note that this project can be directly compiled and run using the [`Android Studio`](https://developer.android.com/studio) platform with the support of [`Java SE Development Kit 8`](https://www.oracle.com/java/technologies/downloads/).

## Implementation of Swiftest
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
