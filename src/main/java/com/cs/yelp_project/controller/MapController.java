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
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory before: " + usedMemoryBefore);
		long time = System.nanoTime();
		model.addAttribute("message", message);

		// For location mapping
		List<Object[]> objList = new ArrayList<>();

		// Retrieving business objects and checkin objects from database
		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();

		Set<String> cityNameSet = new HashSet<>();
		Map<String, City> cityMap = new TreeMap<>();

		// Get the mapping for business and total checkins
		Map<String, Integer> checkInMap = new TreeMap<>();
		Set<City> citySet = new HashSet<>();

		for (CheckIn checkIn : checkInList) {
			checkInMap.put(checkIn.getBusiness_id(), checkIn.getTotal_checkin());
		}

		/* Time Complexity: O(n^2)
		 * Loop through the businessList with n number of business objects
		 * 		Retrieve business categoryString : O(1) operation
		 * 		Parse the categoryString to get all the categories: O(n)
		 *
		 * 		Update each business checkin : O(1) operation
		 * 		Check if cityNameSet already has businessCityName : O(1) opeartion
		 * 			Loop through the categoryString and update city's category set: O(n)
		 * 			Update the frequency of each category: O(1) operation
		 * */
		for (Business business : businessList) {
			String businessCityName = (business.getCity() + "-" + business.getState()).toLowerCase();

			String businessCategoryString = business.getCategories();
			String[] businessCategoryStringArray = parse(businessCategoryString);

			// Getting the business total checkins
			int businessCheckin = 0;
			try {
				business.setTotalCheckin(checkInMap.get(business.getBusiness_id()));
			} catch (NullPointerException e) {
				System.out.println("Business " + business.getBusiness_id() + " does not have checkins.");
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

		// Implementing K means
		// Time complexity : O(nml) ~ based on research
		List<Record> records = createRecordsFromMap(cityMap, kMeansCategories);
		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 5, new EuclideanDistance(), 10000);

		HashMap<Integer, String> colorCode = new HashMap<>();
		colorCode.put(1,"blue");
		colorCode.put(2,"pink");
		colorCode.put(3,"purple");
		colorCode.put(4,"red");
		colorCode.put(5,"yellow");

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

		/* Time Complexity: O(n)
		 * Loop through the city coordinate list: O(n)
		 * 		Create a map with key as name of city and value as the coordinates
		 * */
		List<CityCoords> cityCoordsList = cityCoordsService.list();
		Map<String, Double[]> cityCoordsMap = new HashMap<>();
		for (CityCoords cityCoords : cityCoordsList) {
			String cityNameWithStateLower = (cityCoords.getCity() + "-" + cityCoords.getState()).toLowerCase();
			cityCoordsMap.put(cityNameWithStateLower, new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
		}

		/* Time Complexity: O(n^2)
		 * Loop through the found cluster list: O(n)
		 * 			Loop through the record list in each cluster: O(n)
		 * 				Create a new object with city name and respective coordinates and color code
		 * */
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


		double timeInSeconds = (System.nanoTime() - time) / 1_000_000_000;
		String implementationTimes = "Total time elapsed for v3: " + timeInSeconds + "s";
		System.out.println("Time elapsed for v3: " + implementationTimes);

		long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
		String memoryUsed = "Memory increased: " + (usedMemoryAfter-usedMemoryBefore)/ 1000000 + "MB";
		System.out.println(memoryUsed);

		model.addAttribute("timeTaken", implementationTimes);
		model.addAttribute("memoryUsed", memoryUsed);
		System.gc();
		return "index1";
	}

	// Time complexity: O(n^2)
	// Loop through keySet of cityMap : O(n)
	// 		Loop through the tag key to update the frequency : O(n)
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
	public String main2(Model model, @PathVariable (value = "categoriesArray") String[] kMeansCategories) throws IOException {
		System.gc();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory before: " + usedMemoryBefore);
		long time = System.nanoTime();
		model.addAttribute("message", message);

		List<Object[]> objList = new ArrayList<>();
		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();
		Set<String> cityNameSet = new HashSet<>();
		Set<City> citySet = new HashSet<>();

		// Time Complexity: O(n)
		// Loop through the checkInList: O(n)
		//			create a new mapping for each CheckIn and its respective number of Check-In
		Map<String, Integer> checkInMap = new TreeMap<>();

		for (CheckIn checkIn : checkInList) {
			checkInMap.put(checkIn.getBusiness_id(), checkIn.getTotal_checkin());
		}

		/* Time Complexity: O(n^2)
		* Loop through the businessList: O(n)
		* 		Retrieve business categoryString : O(1)
		* 		Parse the categoryString to get all the categories: O(n)
		*
		* 		Update each business checkin : O(1)
		* 		Check if cityNameSet already has businessCityName : O(1)
		* 			Loop through the categoryString and update city's category set: O(n)
		* 			Update the frequency of each category: O(1)
		* */
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

		// Implementing K means
		// Time Complexity: O(nml) ~ based on research
		List<Record> records = createRecordsFromSet(citySet, kMeansCategories);
		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 5, new EuclideanDistance(), 100000);

		HashMap<Integer, String> colorCode = new HashMap<>();
		colorCode.put(1,"blue");
		colorCode.put(2,"pink");
		colorCode.put(3,"purple");
		colorCode.put(4,"red");
		colorCode.put(5,"yellow");

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

		/* Time Complexity: O(n)
		 * Loop through the city coordinate list: O(n)
		 * 		Create a map with key as name of city and value as the coordinates
		 * */
		List<CityCoords> cityCoordsList = cityCoordsService.list();
		Map<String, Double[]> cityCoordsMap = new HashMap<>();
		for (CityCoords cityCoords : cityCoordsList) {
			cityCoordsMap.put(cityCoords.getCity().toLowerCase(), new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
		}

		/* Time Complexity: O(n^2)
		 * Loop through the found cluster list: O(n)
		 * 			Loop through the record list in each cluster: O(n)
		 * 				Create a new object with city name and respective coordinates and color code
		 * */
		for (Centroid centroid : clusters.keySet()) {
			System.out.println(centroid.toString());
			for (Record record : clusters.get(centroid)) {
				Double[] coordinates = findCoordinates(record.getDescription().toLowerCase(), cityCoordsMap);

				if (coordinates != null) {
					objList.add(new Object[]{record.getDescription(), coordinates[0], coordinates[1], colorCode.get(colorCounter)});
				}
			}
			colorCounter++;
		}

		model.addAttribute("citySet", objList.toArray());

		double timeInSeconds = (System.nanoTime() - time) / 1_000_000_000;
		String implementationTimes = "Total time elapsed for v2: " + timeInSeconds + "s";
		System.out.println("Time elapsed for v2: " + implementationTimes);

		long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
		String memoryUsed = "Memory increased: " + (usedMemoryAfter-usedMemoryBefore)/ 1000000 + "MB";
		System.out.println(memoryUsed);

		model.addAttribute("timeTaken", implementationTimes);
		model.addAttribute("memoryUsed", memoryUsed);
		System.gc();

		return "index1";
	}

	/* Time complexity: O(n^2)
	 * 	Loop through the city set : O(n)
	 * 		Loop through the key set of the categoryFrequencyMap: O(n)
	 * 			Update the tag map: O(1)
	 * */
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
		System.gc();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory before: " + usedMemoryBefore);
		long time = System.nanoTime();
		model.addAttribute("message", message);
		// For html templating later on
		List<Object[]> objList = new ArrayList<>();

		List<City> cityList = new ArrayList<>();
		Set<City> citySet = new HashSet<>();
		List<Category> categoryList = new ArrayList<>();
		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();

		/* Loop through and map all business_id : checkintotals from checkinlist to a treemap 	n: number of checkin entries
		 * 		Loop through all businesses and retrieve the checkintotals from the tree map			m: number of business entries, O(1) treemap retrieval
		 * Complexity = O(n + m) = O(n)
		 */
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

		/* 	Create a hashset that stores all the string categories
		 * 	Create a new map where the key is the business id and the value is the business' category set
		 *  Loop through the business list : n number of businesses
		 * 		Retrieve the category string of each business : O(1)
		 * 		Create a new string category set
		 * 		Loop through the category string characters to parse all the categories: m number of characters
		 *
		 *      Loop through the category string array: l number of categories
		 * 			Add each category to the set : O(1)
		 * 			Add each category to the string category set : O(1)
		 *
		 * 		Create a new mapping for the map with the business id and string category set: O(1) operation
		 * Complexity = O(n * (m + l))
		 */
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

		/* Create a hashset that contains the city names
		 *  	Loop through the business list : n number of businesses
		 * 			Get the city name in each business : O(1)
		 * 			Check if city name is already in the list
		 * 				If no, add to the hashset and to the city List : O(1)
		 *
		 * complexity = O(n)
		 */
		Set<String> cityNameSet = new HashSet<>();
		for (Business business : businessList) {
			String cityName =  (business.getCity() + "-" + business.getState()).toLowerCase();

			if (!cityNameSet.contains(cityName)) {
				cityNameSet.add(cityName);
				citySet.add(new City(cityName));
			}
		}

		/*  Create a mapping where string city is the key and set of business is the value
		 * 	Loop through the business list: n number of businesses
		 * 		Get the city name : O(1)
		 * 		Check if cityName set contains such city name : O(1)
		 * 			If there is, create a new mapping : O(1)
		 * 			If no, update the current key-pair : O(1)
		 *
		 * 	Loop through the key set in the map : m number of city names
		 * 		Loop through the city set : m number of city objects
         * 			Check a city object has the same city name as the key
         * 				If there is, update the business set of that city object
		 * complexity = O(n + ml)
		 */
		Map<String, Set<Business>> cityBusinessMap = new HashMap<>();
		for (Business business : businessList) {
			String cityName = (business.getCity() + "-" + business.getState()).toLowerCase();

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


		/* Loop through the city set : n number of cities
		 * 		Create a new freq map
		 * 		Retrieve a city business set : O(1)
		 * 		Create a new hash set to find all category names in a city
		 * 		Create a new hash set to find all categories in a city
		 *
		 * 		Loop through the city business set : m number of businesses
		 * 			Retrieve the number of checkins: O(1)
		 * 			Retrieve the business category set : O(1)
		 * 			Loop through the business category set : l number of categories
		 * 				Add new category for category name list : O(1)
		 * 				Add new mapping/update mapping for freq map : O(1)
		 * Time complexity = O(nml)
		 */
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
		// Time Complexity: O(mnl) ~ based on research
		List<Record> records = createRecordsFromSet(citySet, kMeansCategories);
		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 5, new EuclideanDistance(), 10000);

		HashMap<Integer, String> colorCode = new HashMap<>();
		colorCode.put(1,"blue");
		colorCode.put(2,"pink");
		colorCode.put(3,"purple");
		colorCode.put(4,"red");
		colorCode.put(5,"yellow");

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

		/* Time Complexity: O(n)
		 * Loop through the city coordinate list: O(n)
		 * 		Create a map with key as name of city and value as the coordinates
		 * */
		List<CityCoords> cityCoordsList = cityCoordsService.list();
		Map<String, Double[]> cityCoordsMap = new HashMap<>();
		for (CityCoords cityCoords : cityCoordsList) {
			cityCoordsMap.put((cityCoords.getCity() + "-" + cityCoords.getState()).toLowerCase(), new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
		}

		/* Time Complexity: O(n^2)
		 * Loop through the found cluster list: O(n)
		 * 			Loop through the record list in each cluster: O(n)
		 * 				Create a new object with city name and respective coordinates and color code
		 * */
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

		double timeInSeconds = (System.nanoTime() - time) / 1_000_000_000;
		String implementationTimes = "Total time elapsed for v1: " + timeInSeconds + "s";
		System.out.println("Time elapsed for v1: " + implementationTimes);

		long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
		String memoryUsed = "Memory increased: " + (usedMemoryAfter-usedMemoryBefore)/ 1000000 + "MB";
		System.out.println(memoryUsed);

		model.addAttribute("timeTaken", implementationTimes);
		model.addAttribute("memoryUsed", memoryUsed);
		System.gc();

		return "index1";
	}

    @GetMapping("/mapv0/{categoriesArray}")
    public String main0(Model model, @PathVariable (value = "categoriesArray") String[] kMeansCategories) throws IOException {
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory before: " + usedMemoryBefore);
		long time = System.nanoTime();
		model.addAttribute("message", message);

		// For html templating later on
		List<Object[]> objList = new ArrayList<>();

		List<City> cityList = new ArrayList<>();
		List<Category> categoryList = new ArrayList<>();
		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();

		/* Step 1: assign the total check in count of a business by business_id from checkin dataset to business dataset
		* Old implementation:
		* 	Loop through all check in values							n: number of checkin entries
		* 		Loop through all businesses until a match is found 	x m: number of business entries
	 	* Time complexity = O(n * m) = O(n^2)
		*/
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

		/* Step 2: Parse through categories of all businesses and find a list of distinct categories
		 * Old implementation:
		 * 	Loop through all businesses to their categories 		m: number of businesses
		 *		Loop through the category string characters         a: number of characters in a string
		 * 		Loop through the category string 					* (a + b): number of categories in a business
		 * 			Check if categoriesWord contains it already		* c: number of categories added so far
		 * 	Save categories 										+ c
		 * Time complexity = O(m * (a + b) * c + c) = O(n^3)
		 * estimate: O(m * 10 * b + b) = O(n^2) since number of categories usually < 10
		 */
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

		/* Step 3: get all the cities
		 * Old implementation:
		 * 	Loop through all businesses to get their city 			m: number of businesses
		 * 			Check if cityNameList contains it already		* c: number of cities added so far
		 * Complexity = O(m * c) = O(n^2)
		 */
		// Getting all the states
		List<String> cityNameList = new ArrayList<>();
		for (Business business : businessList) {
			String cityName =  business.getCity();

			if (!cityNameList.contains(cityName)) {
				cityNameList.add(cityName);
				cityList.add(new City(cityName));
			}
		}

		/* Step 4: Get all the businesses in each state then transfer back to city object list
		 *
		 * Old implementation:
		 * 	Created a mapping where city is the key and list of business in the specific city is the value
		 * 	Loop through the business list : n number of business entries
		 * 		Find business's city name  : O(1)
		 *		Loop through city name list  : m number of city names
		 * 				Check whether the found business's city name is inside: O(1)
		 * 				Assign to a local variable city : O(1)
		 *
		 * 		Check if the mapping contains that city key, if not create a new key pair value : O(1) operation
		 * 													 if there is, update the value for the key : O(1)
		 *
		 * Loop through the key set in the map : m number of city names
		 * 		Retrieve the business list of each key : O(1)
		 * 		Loop through the city list : m number of city objects
		 * 								Find the matching city name : O(1)
		 * 								Update that city's latest business list : O(1)
		 *
		 * Time complexity = O (n*m + m*m) = O(nm + m^2)
		 */
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

		/* Step 5: Getting all the categories in each cityState
		 * Old implementation:
		 * 	Loop through city list : n number of city objects
		 * 		Create a map where key is the category and value is the number of checkins
		 *		Retrieve list of businesses in the city: O(1)
		 * 		Create a new string list to find all category names in a city
		 * 		Create a new list to find all categories in a city
		 *
		 * 		Loop through the business list : m number of businesses
		 * 			Retrieve the business' total checkins : O(1)
		 * 			Retrieve the business' string category : O(1)
		 * 			Create a new category list
		 *
		 * 			Loop through the string category: l number of categories
		 * 				Update the business' category list: O(1)
		 *
		 * 			Loop through the business' category: l number of categories
		 * 				Check if the city category list contains the category: O(1)
		 * 					If not create a new key pair value: O(1)
		 *					If there is, update the new checkin for each category: O(1)
		 *
		 * 	Update city's category list: O(1) operation
		 *  Update city's category frequency mapping: O(1) operation
		 *
		 * Time Complexity = O (n * m * (2l)) = O(nml)
		 */
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

				for (String category : businessCategoryList) {
					if (!categoriesInCityState.contains(category)) {
						categoriesInCityState.add(category);
					}

					if (!frequencyMap.containsKey(category)) {
						frequencyMap.put(category, totalCheckin);
					} else {
						int value = frequencyMap.get(category);
						frequencyMap.put(category, value + totalCheckin);
					}
				}
			}
		}

        // Implementing K means
		// Time complexity : O(nml) ~ based on research
        List<Record> records = createRecords(cityList, kMeansCategories);
        Map<Centroid, List<Record>> clusters = KMeans.fit(records, 5, new EuclideanDistance(), 10000);

        HashMap<Integer, String> colorCode = new HashMap<>();
        colorCode.put(1,"blue");
        colorCode.put(2,"pink");
        colorCode.put(3,"purple");
        colorCode.put(4,"red");
        colorCode.put(5,"yellow");

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

		/* Time Complexity: O(n)
		 * Loop through the city coordinate list: O(n)
		 * 		Create a map with key as name of city and value as the coordinates
		 * */
        List<CityCoords> cityCoordsList = cityCoordsService.list();
        Map<String, Double[]> cityCoordsMap = new HashMap<>();
        for (CityCoords cityCoords : cityCoordsList) {
            cityCoordsMap.put(cityCoords.getCity().toLowerCase(), new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
        }

		/* Time Complexity: O(n^2)
		 * Loop through the found cluster list: O(n)
		 * 			Loop through the record list in each cluster: O(n)
		 * 				Create a new object with city name and respective coordinates and color code
		 * */
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

		double timeInSeconds = (System.nanoTime() - time) / 1_000_000_000;
		String implementationTimes = "Total time elapsed for v0: " + timeInSeconds + "s";
		System.out.println("Time elapsed for v0: " + implementationTimes);

		long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
		String memoryUsed = "Memory increased: " + (usedMemoryAfter-usedMemoryBefore)/ 1000000 + "MB";
		System.out.println(memoryUsed);

		model.addAttribute("timeTaken", implementationTimes);
		model.addAttribute("memoryUsed", memoryUsed);
		System.gc();

		return "index1";
    }

	/* Time complexity: O(n)
	 * 	Loop through the key set of the map : O(n)
	 * 		Check if the key equals to the  cityName: O(1)
	 * */
	public Double[] findCoordinates(String cityName, Map<String, Double[]> cityCoordsMap) {
        for (String key : cityCoordsMap.keySet()) {
            if (key.equals(cityName)) {
                return new Double[] {cityCoordsMap.get(key)[0], cityCoordsMap.get(key)[1]};
            }
        }
        return null;
    }

	/* Time complexity: O(n^2)
	 * 	Loop through the city set : O(n)
	 * 		Loop through the key set of the categoryFrequencyMap: O(n)
	 * 			Update the tag map: O(1)
	 * */
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

	/* Time complexity: O(n)
	 * 	Loop through the string arr : O(n)
	 * 		Update each cell in the resultArr: O(1)
	 * */
    public String[] parse(String str) {
        String[] tempArr = str.split(", ");
        String[] result = new String[tempArr.length];
        for (int idx = 0; idx < tempArr.length; idx++) {
            result[idx] = tempArr[idx];
        }
        return result;
    }
}