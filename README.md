# Yelp-Dataset

In this project, we will discover the Yelp Dataset and manipulate the data obtained to create algorithms that help to make business choices!

<h1>How To Parse The Dataset into Java Objects?</h1>

The 11 GB dataset can be downloaded here: https://www.yelp.com/dataset <br/>
Dataset documentation can be found here: https://www.yelp.com/dataset/documentation/main <br/>

The dataset is not in proper JSON structure and are not clearly labelled so it is necessary to do up EDA in python.

Yelp dataset consists of 5 smaller files which are `yelp_academic_dataset_business.json`, `yelp_academic_dataset_checkin.json`, `yelp_academic_dataset_review.json`, `yelp_academic_dataset_tip.json` and `yelp_academic_dataset_user.json`. Java classes that represent each JSON file will be created.

Currently, the `yelp_academic_dataset_business.json` and `yelp_academic_dataset_checkin.json` have been cleaned up and renamed as `business_dataset.json` and `checkin_dataset.json` </br>

The dataset will be parsed in Spring Boot Application and pushed to an in memory H2 database.</br>
Thus all you need to do to get the data into Java classes is just simply extract the data file in resources, e.g `business.zip` and run the Spring Boot Application!
