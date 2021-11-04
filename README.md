# Yelp-Dataset

In this project, we will discover the Yelp Dataset and manipulate the data obtained to create algorithms that help to make business choices!

<h1>How To Parse The Dataset into Java Objects?</h1>

The 11 GB dataset can be downloaded here: https://www.yelp.com/dataset <br/>
Dataset documentation can be found here: https://www.yelp.com/dataset/documentation/main <br/>

The dataset is not in proper JSON structure and are not clearly labelled so it is necessary to do up EDA in python.

Yelp dataset consists of 5 smaller files which are `yelp_academic_dataset_business.json`, `yelp_academic_dataset_checkin.json`, `yelp_academic_dataset_review.json`, `yelp_academic_dataset_tip.json` and `yelp_academic_dataset_user.json`. Java classes that represent each JSON file will be created.

<h1> How to compile and run Spring Boot App</h1>

Currently, the `yelp_academic_dataset_business.json` and `yelp_academic_dataset_checkin.json` have been cleaned up and renamed as `business_dataset.json` and `checkin_dataset.json` </br>

The dataset will be parsed in Spring Boot Application and pushed to an in memory H2 database.</br>
Thus all you need to do to get the data into Java classes is just simply extract the data file in resources, e.g `business.zip` (if it is still zipped) and run the Spring Boot Application!

To display the clustering algorithm, just need to call the API from the MapController. 
</br> Eg: `localhost:8080/mapv{versionNumber}/{categoriesArray}` to use different implementations and cluster according to different categories.

`categoriesArray`: specify different categories e.g., Mexican, Kebabs 

`map{versionNumber}`: can be any version e.g., mapv2 

!! Note that: </br>
<li> mapv0 : the original implementation which might take 8-10 minutes
<li> mapv1 to mapv3 : optimized implementation which takes around 10 secs or less 
    
<h1>Final Product</h1>
    
Test URL: http://localhost:8080/mapv3/Mexican,Kebab </br>
Clustering results can be observed on the rendered Google map with locations, that have same pin color, belong to a cluster. </br>
</br>
![alt text](https://github.com/Dev317/Yelp-Dataset/blob/optimize-draft/map.PNG)

<h1>Experimental Results</h1>

`Time measurement` and `Memory usage` can be observed on the web app and at the terminal when running the Spring Boot app. </br>
</br>
![alt text](https://github.com/Dev317/Yelp-Dataset/blob/optimize-draft/Webapp%20experimental%20result.jpg)
</br>

<h2>Terminal record</h2>

`Algorithm` 
![alt text](https://github.com/Dev317/Yelp-Dataset/blob/optimize-draft/Algorithm%20Time%20and%20Memory%20Usage%20Capture.png) </br>

`Business Memory Usage`
![alt text](https://github.com/Dev317/Yelp-Dataset/blob/optimize-draft/Business%20Memory%20Usage%20Capture.png) </br>

`Checkin Memory Usage`
![alt text](https://github.com/Dev317/Yelp-Dataset/blob/optimize-draft/Checkin%20Memory%20Usage%20Capture.png) </br>

`City Coordinates Memory Usage`
![alt text](https://github.com/Dev317/Yelp-Dataset/blob/optimize-draft/CityCoords%20Memory%20Usage%20Capture.png) </br>
