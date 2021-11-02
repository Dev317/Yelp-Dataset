package com.cs.yelp_project.controller;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessService;
import com.cs.yelp_project.business.Category;
import com.cs.yelp_project.business.CategoryService;
import com.cs.yelp_project.checkin.CheckIn;
import com.cs.yelp_project.checkin.CheckInService;
import com.cs.yelp_project.city.City;
import com.cs.yelp_project.citycoords.CityCoords;
import com.cs.yelp_project.citycoords.CityCoordsService;
import com.cs.yelp_project.kmeans.Centroid;
import com.cs.yelp_project.kmeans.EuclideanDistance;
import com.cs.yelp_project.kmeans.KMeans;
import com.cs.yelp_project.kmeans.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.*;

@Controller
public class MapController {

    private final CheckInService checkInService;

    private final BusinessService businessService;

    private final CategoryService categoryService;

    private final CityCoordsService cityCoordsService;

    @Autowired
    public MapController(CheckInService checkInService, BusinessService businessService, CategoryService categoryService, CityCoordsService cityCoordsService ) {
        this.checkInService = checkInService;
        this.businessService = businessService;
        this.categoryService = categoryService;
        this.cityCoordsService = cityCoordsService;
    }

    @Value("${welcome.message}")
    private String message;

	@GetMapping("/mapv3/{categoriesArray}")
	public String main3(Model model, @PathVariable (value = "categoriesArray") String[] kMeansCategories) throws IOException {
		long time = System.nanoTime();
		model.addAttribute("message", message);

		List<Object[]> objList = new ArrayList<>();

		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();
		Set<String> cityNameSet = new HashSet<>();
		Set<City> citySet = new HashSet<>();
		Map<String, City> cityMap = new HashMap<>();

		Map<String, Integer> checkInMap = new HashMap<>();
		for (CheckIn checkIn : checkInList) {
			checkInMap.put(checkIn.getBusiness_id(), checkIn.getTotal_checkin());
		}

		for (Business business : businessList) {
			String businessCityName = business.getCity() + business.getState();

			String businessCategoryString = business.getCategories();
			String[] businessCategoryStringArray = parse(businessCategoryString);

			// Getting the business total checkins
			int businessCheckin = 0;
			try {
				business.setTotalCheckin(checkInMap.get(business.getBusiness_id()));
			} catch (NullPointerException e) {
				System.out.println("Business " + business.getBusiness_id() + " does not have enough checkins.");
				continue;
			}
			businessCheckin = business.getTotalCheckin();

			// Check if cityNameSet has the businessCityName
			if (!cityNameSet.contains(businessCityName)) {
				cityNameSet.add(businessCityName);

				// Created a new city object
				City newCity = new City(businessCityName);

				// Add the business into the newly created city object's business set
				Set<Business> newCityBusinessSet = new HashSet<>();
				newCityBusinessSet.add(business);
				newCity.setBusinessSet(newCityBusinessSet);

				// Create a new categoryFrequency map
				Map<String, Integer> categoryFrequency = new HashMap<>();
				for (String category : businessCategoryStringArray) {
					categoryFrequency.put(category, businessCheckin);
				}
				newCity.setCategoryFrequency(categoryFrequency);

				// add city new city into map
				cityMap.put(businessCityName, newCity);
			} else {

				// Retrieve the business city from the cityMap
				City currentCity = cityMap.get(businessCityName);

				// Update the city's current businessSet
				Set<Business> currentCityBusinessSet = currentCity.getBusinessSet();
				currentCityBusinessSet.add(business);
				currentCity.setBusinessSet(currentCityBusinessSet);

				// Update the city's current categoryFrequency map
				Map<String, Integer> currentCityCategoryFrequency = currentCity.getCategoryFrequency();
				for (String category : businessCategoryStringArray) {
					if (currentCityCategoryFrequency.containsKey(category)) {
						int newFrequency = currentCityCategoryFrequency.get(category) + businessCheckin;
						currentCityCategoryFrequency.put(category, newFrequency);
					}
				}
				currentCity.setCategoryFrequency(currentCityCategoryFrequency);
			}
		}

		List<Record> records = createRecordsFromMap(cityMap, kMeansCategories);

		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 7, new EuclideanDistance(), 1000);

		HashMap<Integer, String> colorCode = new HashMap<>();
		colorCode.put(1,"blue");
		colorCode.put(2,"pink");
		colorCode.put(3,"purple");
		colorCode.put(4,"red");
		colorCode.put(5,"yellow");
		colorCode.put(6,"green");
		colorCode.put(7,"brown");

		Integer colorCounter = 1;

		// Printing the cluster configuration
		clusters.forEach((key, value) -> {
			System.out.println("-------------------------- CLUSTER ----------------------------");

			// Sorting the coordinates to see the most significant tags first.
			System.out.println(key.toString());

			for (Record record : value) {
				System.out.print(record.getDescription());
				System.out.print("-");
			}

			System.out.println();
			System.out.println();
		});

		List<CityCoords> cityCoordsList = cityCoordsService.list();
		Map<String, Double[]> cityCoordsMap = new HashMap<>();
		for (CityCoords cityCoords : cityCoordsList) {
			String cityNameWithStateLower = (cityCoords.getCity() + cityCoords.getState()).toLowerCase();
			cityCoordsMap.put(cityNameWithStateLower, new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
		}

		for (Centroid centroid : clusters.keySet()) {
			for (Record record : clusters.get(centroid)) {
				Double[] coordinates = findCoordinates(record.getDescription().toLowerCase(), cityCoordsMap);

				if (coordinates != null) {
					objList.add(new Object[]{record.getDescription(), coordinates[0], coordinates[1], colorCode.get(colorCounter)});
				}

			}
			colorCounter++;
		}

		model.addAttribute("citySet", objList.toArray());


		System.out.println("Time elapsed for v3: " + (System.nanoTime() - time));
		return "index1";
	}

	public List<Record> createRecordsFromMap(Map<String, City> cityMap, String[] tags) throws IOException {
		List<Record> records = new ArrayList<>();
		Set<String> keySet = cityMap.keySet();

		for (String cityName : keySet) {
			City city = cityMap.get(cityName);

			Map<String, Integer> currentCategoryFrequency = city.getCategoryFrequency();
			Map<String, Double> tagMap = new HashMap<>();

			for (String key : tags) {
				if (currentCategoryFrequency.get(key) != null) {
					tagMap.put(key,currentCategoryFrequency.get(key).doubleValue());
				} else {
					tagMap.put(key, .0);
				}
			}

			// Only keep popular tags.
			records.add(new Record(cityName,tagMap));
		}

		return records;
	}


    @GetMapping("/mapv2/{categoriesArray}")
	public String main(Model model, @PathVariable (value = "categoriesArray") String[] kMeansCategories) throws IOException {
		long time = System.nanoTime();
		model.addAttribute("message", message);

		List<Object[]> objList = new ArrayList<>();

		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();
		Set<String> cityNameSet = new HashSet<>();
		Set<City> citySet = new HashSet<>();

		Map<String, Integer> checkInMap = new HashMap<>();
		for (CheckIn checkIn : checkInList) {
			checkInMap.put(checkIn.getBusiness_id(), checkIn.getTotal_checkin());
		}

		for (Business business : businessList) {
			String businessCityName = business.getCity();

			String businessCategoryString = business.getCategories();
			String[] businessCategoryStringArray = parse(businessCategoryString);

			// Getting the business total checkins
			int businessCheckin = 0;
			try {
				business.setTotalCheckin(checkInMap.get(business.getBusiness_id()));
			} catch (NullPointerException e) {
				System.out.println("Business " + business.getBusiness_id() + " does not have enough checkins.");
				continue;
			}
			businessCheckin = business.getTotalCheckin();

			// Check if cityNameSet has the businessCityName
			if (!cityNameSet.contains(businessCityName)) {
				cityNameSet.add(businessCityName);

				// Created a new city object
				City newCity = new City(businessCityName);

				// Add the business into the newly created city object's business set
				Set<Business> newCityBusinessSet = new HashSet<>();
				newCityBusinessSet.add(business);
				newCity.setBusinessSet(newCityBusinessSet);

				// Create a new categoryFrequency map
				Map<String, Integer> categoryFrequency = new HashMap<>();
				for (String category : businessCategoryStringArray) {
					categoryFrequency.put(category, businessCheckin);
				}
				newCity.setCategoryFrequency(categoryFrequency);

				// Update the new city object into city Set
				citySet.add(newCity);
			} else {

				// Retrieve the business city from the citySet
				City currentCity = null;
				for (City city : citySet) {
					if (city.getName().equals(businessCityName)) {
						currentCity = city;
						break;
					}
				}

				// Update the city's current businessSet
				Set<Business> currentCityBusinessSet = currentCity.getBusinessSet();
				currentCityBusinessSet.add(business);
				currentCity.setBusinessSet(currentCityBusinessSet);

				// Update the city's current categoryFrequency map
				Map<String, Integer> currentCityCategoryFrequency = currentCity.getCategoryFrequency();
				for (String category : businessCategoryStringArray) {
					if (currentCityCategoryFrequency.containsKey(category)) {
						int newFrequency = currentCityCategoryFrequency.get(category) + businessCheckin;
						currentCityCategoryFrequency.put(category, newFrequency);
					}
				}
				currentCity.setCategoryFrequency(currentCityCategoryFrequency);
			}
		}

		List<Record> records = createRecordsFromSet(citySet, kMeansCategories);

		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 7, new EuclideanDistance(), 1000);

		HashMap<Integer, String> colorCode = new HashMap<>();
		colorCode.put(1,"blue");
		colorCode.put(2,"pink");
		colorCode.put(3,"purple");
		colorCode.put(4,"red");
		colorCode.put(5,"yellow");
		colorCode.put(6,"green");
		colorCode.put(7,"brown");

		Integer colorCounter = 1;

		// Printing the cluster configuration
		clusters.forEach((key, value) -> {
			System.out.println("-------------------------- CLUSTER ----------------------------");

			// Sorting the coordinates to see the most significant tags first.
			System.out.println(key.toString());

			for (Record record : value) {
				System.out.print(record.getDescription());
				System.out.print("-");
			}

			System.out.println();
			System.out.println();
		});

		List<CityCoords> cityCoordsList = cityCoordsService.list();
		Map<String, Double[]> cityCoordsMap = new HashMap<>();
		for (CityCoords cityCoords : cityCoordsList) {
			cityCoordsMap.put(cityCoords.getCity().toLowerCase(), new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
		}

		for (Centroid centroid : clusters.keySet()) {
			for (Record record : clusters.get(centroid)) {
				Double[] coordinates = findCoordinates(record.getDescription().toLowerCase(), cityCoordsMap);

				if (coordinates != null) {
					objList.add(new Object[]{record.getDescription(), coordinates[0], coordinates[1], colorCode.get(colorCounter)});
				}

			}
			colorCounter++;
		}

		model.addAttribute("citySet", objList.toArray());


		System.out.println("Time elapsed for v2: " + (System.nanoTime() - time));
		return "index1";
	}

	public List<Record> createRecordsFromSet(Set<City> citySet, String[] tags) throws IOException {
		List<Record> records = new ArrayList<>();

		for (City city : citySet) {
			String cityName = city.getName();

			Map<String, Integer> currentCategoryFrequency = city.getCategoryFrequency();
			Map<String, Double> tagMap = new HashMap<>();

			for (String key : tags) {
				if (currentCategoryFrequency.get(key) != null) {
					tagMap.put(key,currentCategoryFrequency.get(key).doubleValue());
				} else {
					tagMap.put(key, .0);
				}
			}

			// Only keep popular tags.
			records.add(new Record(cityName,tagMap));
		}

		return records;
	}

    @GetMapping("/mapv1/{categoriesArray}")
    public String main1(Model model, @PathVariable (value = "categoriesArray") String[] kMeansCategories) throws IOException {
        long time = System.nanoTime();
		model.addAttribute("message", message);

        // For html templating later on
        List<Object[]> objList = new ArrayList<>();

		Set<City> citySet = new HashSet<>();
        List<Business> businessList = businessService.list();
        List<CheckIn> checkInList = checkInService.list();


		// STEP 1 NEW IMPLEMENTATION START//
		Map<String, Integer> checkInMap = new TreeMap<>();
		for (CheckIn checkIn : checkInList) {
			checkInMap.put(checkIn.getBusiness_id(), checkIn.getTotal_checkin());
		}
		for (Business business : businessList) {
			try {
				business.setTotalCheckin(checkInMap.get(business.getBusiness_id()));
			} catch (NullPointerException e) {
				System.out.println("Business " + business.getBusiness_id() + " does not have enough checkins.");
			}
		}

		// STEP 2 NEW IMPLEMENTATION START//
		//TODO step 2 new implementation code goes here
		Set<String> catWord = new HashSet<>();
		Map<String, Set<String>> businessCatMap = new HashMap<>();
		for (Business business : businessList) {
			String[] categories = parse(business.getCategories());
			Set<String> catName = new HashSet<>();
			for (String cat:categories) {
				catWord.add(cat);
				catName.add(cat);
			}
			businessCatMap.put(business.getBusiness_id(), catName);
		}
		// STEP 3 NEW IMPLEMENTATION START//
		//TODO step 3 new implementation code goes here
		Set<String> cityNameSet = new HashSet<>();
		for (Business business : businessList) {
			String cityName =  business.getCity();

			if (!cityNameSet.contains(cityName)) {
				cityNameSet.add(cityName);
				citySet.add(new City(cityName));
			}
		}

		// STEP 4 NEW IMPLEMENTATION START//
		//TODO step 4 new implementation code goes here

		Map<String, Set<Business>> cityBusinessMap = new HashMap<>();
		for (Business business : businessList) {
			String cityName = business.getCity();

			if (!cityBusinessMap.containsKey(cityName)) {
				Set<Business> cityBusinessSet = new HashSet<>();
				cityBusinessSet.add(business);
				cityBusinessMap.put(cityName, cityBusinessSet);
			} else {
				Set<Business> currCityBusinessSet = cityBusinessMap.get(cityName);
				currCityBusinessSet.add(business);
				cityBusinessMap.put(cityName,currCityBusinessSet);
			}
		}

		Set<String> setKey = cityBusinessMap.keySet();
		for (String key : setKey) {
			Set<Business> cityBusinessSet = cityBusinessMap.get(key);

			for (City city : citySet) {
				if (city.getName().equals(key)) {
					city.setBusinessSet(cityBusinessSet);
					break;
				}
			}
		}

		// STEP 5 NEW IMPLEMENTATION START//
		//TODO step 5 new implementation code goes here

		for (City city : citySet) {
			Map<String,Integer> freqMap = new HashMap<>();
			Set<Business> cityBusinessSet = city.getBusinessSet();

			Set<String> categoriesInCitySet = new HashSet<>();
			Set<Category> categoriesSet = new HashSet<>();

			for (Business business : cityBusinessSet) {
				int checkin = business.getTotalCheckin();

				Set<String> businessCategorySet =  businessCatMap.get(business.getBusiness_id());

				for (String category: businessCategorySet) {
					if (!categoriesInCitySet.contains(category)) {
						categoriesInCitySet.add(category);
					}

					if (!freqMap.containsKey(category)) {
						freqMap.put(category,checkin);
					} else {
						int value = freqMap.get(category);
						freqMap.put(category, value + checkin);
					}

				}
			}

			for (String category : categoriesInCitySet) {
				categoriesSet.add(new Category(category));
			}

			city.setCategorySet(categoriesSet);
			city.setCategoryFrequency(freqMap);

		}

        // Implementing K means
        List<Record> records = createRecordsFromSet(citySet, kMeansCategories);

        Map<Centroid, List<Record>> clusters = KMeans.fit(records, 7, new EuclideanDistance(), 1000);

        HashMap<Integer, String> colorCode = new HashMap<>();
        colorCode.put(1,"blue");
        colorCode.put(2,"pink");
        colorCode.put(3,"purple");
        colorCode.put(4,"red");
        colorCode.put(5,"yellow");
        colorCode.put(6,"green");
        colorCode.put(7,"brown");

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

        List<CityCoords> cityCoordsList = cityCoordsService.list();
        Map<String, Double[]> cityCoordsMap = new HashMap<>();
        for (CityCoords cityCoords : cityCoordsList) {
            cityCoordsMap.put(cityCoords.getCity().toLowerCase(), new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
        }

        for (Centroid centroid : clusters.keySet()) {
            for (Record record : clusters.get(centroid)) {
                Double[] coordinates = findCoordinates(record.getDescription().toLowerCase(), cityCoordsMap);

                if (coordinates != null) {
                    objList.add(new Object[]{record.getDescription(), coordinates[0], coordinates[1], colorCode.get(colorCounter)});
                }

            }
            colorCounter++;
        }

        model.addAttribute("cityStates", objList.toArray());

		System.out.println("Time elapsed for v1: " + (System.nanoTime() - time));

        return "index1"; //view
    }

	@GetMapping("/mapv0/{categoriesArray}")
	public String main0(Model model, @PathVariable (value = "categoriesArray") String[] kMeansCategories) throws IOException {
		long time = System.nanoTime();
		model.addAttribute("message", message);

		// For html templating later on
		List<Object[]> objList = new ArrayList<>();

		List<City> cityList = new ArrayList<>();
		Set<City> citySet = new HashSet<>();
		List<Category> categoryList = new ArrayList<>();
		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();

		for (CheckIn checkIn : checkInList) {

			// Get the business_id of a checkin row record
			String businessId = checkIn.getBusiness_id();

			for (Business business : businessList) {
				if (businessId.contains(business.getBusiness_id())) {
					business.setTotalCheckin(checkIn.getTotal_checkin());
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

		// Getting all the states
		List<String> cityNameList = new ArrayList<>();
		for (Business business : businessList) {
			String cityName =  business.getCity();

			if (!cityNameList.contains(cityName)) {
				cityNameList.add(cityName);
				cityList.add(new City(cityName));
			}
		}


		// Getting all the businesses in each city
		Map<String, List<Business>> mapCityBusiness = new TreeMap<>();

		for (Business business : businessList) {
			String cityName = business.getCity();

			String city = null;

			for (String tempCity : cityNameList) {
				if (tempCity.equals(cityName)) {
					city = tempCity;
					break;
				}
			}

			if (!mapCityBusiness.containsKey(city)) {
				List<Business> cityBusinessList = new ArrayList<>();
				cityBusinessList.add(business);
				mapCityBusiness.put(city, cityBusinessList);
			} else {
				List<Business> currCityBusinessList = mapCityBusiness.get(city);
				currCityBusinessList.add(business);
				mapCityBusiness.put(city,currCityBusinessList);
			}
		}

		// transfer back to city object list
		Set<String> keySet = mapCityBusiness.keySet();
		for (String key : keySet) {
			List<Business> cityBusinessList = mapCityBusiness.get(key);
			for (City city : cityList) {
				if (city.getName().equals(key)) {
					city.setBusinessList(cityBusinessList);
					break;
				}
			}
		}


//		 Getting all the categories in each city
		for (City city : cityList) {
			Map<String,Integer> frequencyMap = new TreeMap<String,Integer >();
			List<Business> cityBusinessList = city.getBusinessList();

			List<String> categoriesInCityState = new ArrayList<>();
			List<Category> categoriesList = new ArrayList<>();

			for (Business business : cityBusinessList) {
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
			city.setCategoryList(categoriesList);
			city.setCategoryFrequency(frequencyMap);
		}

		// Implementing K means
		List<Record> records = createRecords(cityList, kMeansCategories);

		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 7, new EuclideanDistance(), 1000);

		HashMap<Integer, String> colorCode = new HashMap<>();
		colorCode.put(1,"blue");
		colorCode.put(2,"pink");
		colorCode.put(3,"purple");
		colorCode.put(4,"red");
		colorCode.put(5,"yellow");
		colorCode.put(6,"green");
		colorCode.put(7,"brown");

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

		List<CityCoords> cityCoordsList = cityCoordsService.list();
		Map<String, Double[]> cityCoordsMap = new HashMap<>();
		for (CityCoords cityCoords : cityCoordsList) {
			cityCoordsMap.put(cityCoords.getCity().toLowerCase(), new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
		}

		for (Centroid centroid : clusters.keySet()) {
			for (Record record : clusters.get(centroid)) {
				Double[] coordinates = findCoordinates(record.getDescription().toLowerCase(), cityCoordsMap);

				if (coordinates != null) {
					objList.add(new Object[]{record.getDescription(), coordinates[0], coordinates[1], colorCode.get(colorCounter)});
				}

			}
			colorCounter++;
		}

		model.addAttribute("cityStates", objList.toArray());

		System.out.println("Time elapsed for v0: " + (System.nanoTime() - time));
		return "index1"; //view
	}



    public Double[] findCoordinates(String cityName, Map<String, Double[]> cityCoordsMap) {
        for (String key : cityCoordsMap.keySet()) {
            if (key.equals(cityName)) {
                return new Double[] {cityCoordsMap.get(key)[0], cityCoordsMap.get(key)[1]};
            }
        }
        return null;
    }

    public List<Map.Entry<String, Integer>> sortMap(City cityState) {
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

    public List<Record> createRecords(List<City> cityStateList, String[] tags) throws IOException {
        List<Record> records = new ArrayList<>();

        for (City cityState : cityStateList) {
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