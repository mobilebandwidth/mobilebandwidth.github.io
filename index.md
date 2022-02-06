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

## Mobile Access Bandwidth in Practice: Measurement, Analysis, and Implications

Our study focuses on characterizing mobile access bandwidth in the wild. We work with a major commercial mobile bandwidth testing (BTS) app to analyze mobile access bandwidths of 3.54M end users based on fine-grained measurement and diagnostic information collected by our cross-layer and cross-technology measurement tool.
Additionally, our analysis provides insights on building ultrafast, ultra-light bandwidth testing services (BTSes) called Swiftest at scale.
Our new design dramatically reduces the test time of the commercial BTS app from 10 seconds to 1 second on average, with a 15$\times$ reduction on the backend cost.
This repository contains our implementation of the cross-layer and cross-technology measurement tool, our released data, and our implementation of Swiftest.

### Cross-Layer and Cross-Technology Measurement Tool
Coming in a few days.

### Data Release
We have released a portion of data (with proper anonymization) for references (for detailed data, please click [here](https://github.com/mobilebandwidth/mobilebandwidth.github.io/tree/main/data)).
As to the full dataset, we are still in discussion with the authority to what extend can it be released.

### Swiftest: Ultra-Fast, Ultra-Light BTS
Coming in a few days.
