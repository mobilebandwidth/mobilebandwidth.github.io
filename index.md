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

### Introduction

Recent advances in mobile technologies such as 5G and WiFi 6E do not seem to deliver the promised mobile access bandwidth.
To effectively characterize mobile access bandwidth in the wild, we work with a major commercial mobile bandwidth testing app to analyze mobile access bandwidths of 3.54M end users based on fine-grained measurement and diagnostic information.
Our analysis presents a surprising and frustrating fact---in the past two years, the average WiFi
bandwidth remains largely unchanged, while the average 4G/5G bandwidth decreases remarkably. Our analysis further reveals the root causes---the bottlenecks in the underlying infrastructure (e.g., devices and wired Internet access) and side effects of aggressively migrating radio resources from
4G to 5G---with implications on closing the technology gaps. Additionally, our analysis provides insights on building ultrafast, ultra-light bandwidth testing services (BTSes) at scale.
Our new design dramatically reduces the test time of the commercial BTS app from 10 seconds to 1 second on average, with a 15$\times$ reduction on the backend cost.

### Code and Data Releases
Coming in a few days.
