package com.cs.yelp_project.controller;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessService;
import com.cs.yelp_project.business.Category;
import com.cs.yelp_project.business.CategoryService;
import com.cs.yelp_project.checkin.CheckIn;
import com.cs.yelp_project.checkin.CheckInService;
import com.cs.yelp_project.citystate.CityState;
import com.cs.yelp_project.kmeans.Centroid;
import com.cs.yelp_project.kmeans.EuclideanDistance;
import com.cs.yelp_project.kmeans.KMeans;
import com.cs.yelp_project.kmeans.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Controller
public class MapController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private CategoryService categoryService;

    @Value("${welcome.message}")
    private String message;

    @GetMapping("/map")
    public String main(Model model) throws IOException {
        model.addAttribute("message", message);

        List<Object[]> objList = new ArrayList<>();
//        Object[] obj = new Object[];
//        obj[0] = new Object[]{"Raj Ghat", 28.648608, 77.250925, "blue"};
//        obj[1] = new Object[]{"Purana Qila", 28.618174, 77.242686, "blue"};
//        obj[2] = new Object[]{"Red Fort",  28.663973, 77.241656, "pink"};
//        obj[3] = new Object[]{"India Gate", 28.620585, 77.228609, "pink"};
//        obj[4] = new Object[]{"Jantar Mantar", 28.636219, 77.213846, "purple"};
//        obj[5] =new Object[]{"Akshardham", 28.622658, 77.277704, "purple"};




        List<CityState> cityStateList = new ArrayList<>();
        List<Category> categoryList = new ArrayList<>();
        List<Business> businessList = businessService.list();

        // findTotalCheckin in each business -- bottle nek
        int counter = 0;
        List<CheckIn> checkInList = checkInService.list();
        for (CheckIn checkIn : checkInList) {

            String businessId = checkIn.getBusiness_id();

            for (Business business : businessList) {
                if (counter > checkInList.size()) {
                    System.out.println("Repeat ALERT!!!");
                    break;
                }

                if (counter < checkInList.size() && businessId.contains(business.getBusiness_id())) {
                    business.setTotalCheckin(checkIn.getTotal_checkin());
                    counter++;
                    break;
                }
            }
        }

        // Getting all the categories and store into database
        List<String> categoriesWord = new ArrayList<>();
        for (Business business : businessList) {
            String[] categories = parse(business.getCategories());

            for (String cat:categories) {
                if (!categoriesWord.contains(cat)) {
                    categoriesWord.add(cat);
                }
            }
        }

        for (String cat: categoriesWord) {
            categoryService.save(new Category(cat));
        }

        // Getting all the states
        List<String> cityNameList = new ArrayList<>();
        for (Business business : businessList) {
            String cityName =  business.getCity();

            if (!cityNameList.contains(cityName)) {
                cityNameList.add(cityName);
                cityStateList.add( new CityState(cityName));
            }
        }


        // Getting all the businesses in each state
        Map<String, List<Business>> map = new HashMap<>();

        for (Business business : businessList) {
            String cityName = business.getCity();

            String cityState = null;

            for (String tempCityState : cityNameList) {
                if (tempCityState.equals(cityName)) {
                    cityState = tempCityState;
                    break;
                }
            }

            if (!map.containsKey(cityState)) {
                List<Business> newList = new ArrayList<>();
                newList.add(business);
                map.put(cityState,newList);
            } else {
                List<Business> currList = map.get(cityState);
                currList.add(business);
                map.put(cityState,currList);
            }
        }

        // transfer back to cityStateList object list
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
//			System.out.println(key);
            List<Business> businesses = map.get(key);
            for (CityState city : cityStateList) {
                if (city.getName().equals(key)) {
                    city.setBusinessList(businesses);
                    break;
                }
            }
        }

//		 Getting all the categories in each cityState
        for (CityState cityState : cityStateList) {
            Map<String,Integer> frequencyMap = new TreeMap<String,Integer >();
            List<Business> businessesInCityState = cityState.getBusinessList();
            List<String> categoriesInCityState = new ArrayList<>();
            List<Category> categoriesList = new ArrayList<>();

            for (Business business : businessesInCityState) {
                int totalCheckin = business.getTotalCheckin();

                List<String> businessCategoryList = new ArrayList<>();

                String[] categories = parse(business.getCategories());

                for (String category : categories) {
                    businessCategoryList.add(category);
                }

                for (String category: businessCategoryList) {
                    if (!categoriesInCityState.contains(category)) {
                        categoriesInCityState.add(category);
                    }

                    if (!frequencyMap.containsKey(category)) {
                        frequencyMap.put(category,totalCheckin);
                    } else {
                        int value = frequencyMap.get(category);
                        frequencyMap.put(category, value + totalCheckin);
                    }

                }
            }

            for (String category : categoriesInCityState) {
                categoriesList.add(new Category(category));
            }

//			frequencyMap = valueSort(frequencyMap);

            cityState.setCategoryList(categoriesList);
            cityState.setCategoryFrequency(frequencyMap);
        }


        // Implementing K means
        final String[] kMeansCategories = new String[] {"Mexican", "Kebab", "Latin American", "Tex-Mex","New Mexican Cuisine", "Wraps"};

        List<Record> records = createRecords(cityStateList, kMeansCategories);

        Map<Centroid, List<Record>> clusters = KMeans.fit(records, 3, new EuclideanDistance(), 1000);

        HashMap<Integer, String> colorCode = new HashMap<>();
        colorCode.put(1,"blue");
        colorCode.put(2,"pink");
        colorCode.put(3,"purple");
        Integer colorCounter = 1;

        // Printing the cluster configuration
        clusters.forEach((key, value) -> {
            System.out.println("-------------------------- CLUSTER ----------------------------");

            // Sorting the coordinates to see the most significant tags first.
            System.out.println(key.toString());

            for (Record record : value) {
                System.out.print(record.toString());
            }

            System.out.println();
            System.out.println();
        });

        for (Centroid centroid : clusters.keySet()) {
            for (Record record : clusters.get(centroid)) {
                Double[] coordinates = findCoordinates(record.getDescription(), cityStateList);
                objList.add(new Object[]{record.getDescription(), coordinates[0], coordinates[1], colorCode.get(colorCounter)});

            }
            colorCounter++;
        }

        model.addAttribute("cityStates", objList.toArray());


        return "index1"; //view
    }

    public Double[] findCoordinates(String cityStateName, List<CityState> cityStateList) {
        Double maxLat = Double.MIN_VALUE;
        Double minLat = Double.MAX_VALUE;
        Double maxLong = Double.MAX_VALUE;
        Double minLong = Double.MIN_VALUE;

        for (CityState cityState:cityStateList) {
            if (cityStateName.equals(cityState.getName())) {
                List<Business> stateBusinesses = cityState.getBusinessList();

                System.out.println(cityStateName);
                for (Business business : stateBusinesses) {
                    Double latitude = business.getLatitude();
                    Double longitude = business.getLongitude();

                    if (latitude > maxLat) {
                        maxLat = latitude;
                    }

                    if (latitude < minLat) {
                        minLat = latitude;
                    }

                    if (longitude > maxLong) {
                        maxLong = longitude;
                    }

                    if (longitude < minLong) {
                        minLong = longitude;
                    }
                }

                break;
            }
        }

        Double[] coordinates = new Double[2];
        coordinates[0] = (maxLat + minLat) / 2;
        coordinates[1] = (maxLong + minLong) / 2;
        System.out.print(coordinates[0]);
        System.out.println(",");
        System.out.print(coordinates[1]);
        return coordinates;
    }

    public List<Map.Entry<String, Integer>> sortMap(CityState cityState) {
        Map<String, Integer> testFrequencyMap = cityState.getCategoryFrequency();
        List sortedList = new ArrayList(testFrequencyMap.entrySet());
        Collections.sort(sortedList, Collections.reverseOrder(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        }));
        return sortedList;
    }

    public List<Record> createRecords(List<CityState> cityStateList, String[] tags) throws IOException {
        List<Record> records = new ArrayList<>();

        for (CityState cityState : cityStateList) {
            String cityName = cityState.getName();
            Map<String, Integer> currentFrequencyMap = cityState.getCategoryFrequency();
            Map<String, Double> tagMap = new HashMap<>();

            for (String key : tags) {
                if (currentFrequencyMap.get(key) != null) {
                    tagMap.put(key, currentFrequencyMap.get(key).doubleValue());
                } else {
                    tagMap.put(key, .0);
                }
            }


            // Only keep popular tags.
            records.add(new Record(cityName, tagMap));
        }

        return records;
    }

    public String[] parse(String str) {
        String[] tempArr = str.split(", ");
        String[] result = new String[tempArr.length];
        for (int idx = 0; idx < tempArr.length; idx++) {
            result[idx] = tempArr[idx];
        }
        return result;
    }
}
