package com.cs.yelp_project;

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
import com.cs.yelp_project.kmeans.Record;
import com.cs.yelp_project.kmeans.KMeans;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SpringBootApplication
public class YelpProjectApplication {

    public static String[] parse(String str) {
        String[] tempArr = str.split(", ");
        String[] result = new String[tempArr.length];
        for (int idx = 0; idx < tempArr.length; idx++) {
            result[idx] = tempArr[idx];
        }
        return result;
    }

	public static void main(String[] args) throws IOException {
		ApplicationContext ctx = SpringApplication.run(YelpProjectApplication.class, args);
		System.out.println("started");


		CheckInService checkInService = ctx.getBean(CheckInService.class);
		BusinessService businessService = ctx.getBean(BusinessService.class);
		CategoryService categoryService = ctx.getBean(CategoryService.class);
		CityCoordsService cityCoordsService = ctx.getBean(CityCoordsService.class);

		List<City> cityList = new ArrayList<>();
		List<Category> categoryList = new ArrayList<>();
		List<Business> businessList = businessService.list();
		List<CheckIn> checkInList = checkInService.list();

//		/*
//		* Switch to choose between old implementation and new implementation
//		 */
//		boolean useOldImplementation = true;
//
//		/*
//		* Timers
//		 */
//		String oldImplementationTimes = "Old Implementation:";
//		String newImplementationTimes = "New Implementation:";
//		String sideBySideComparison = "";
//		long totalOldTime = 0;
//		long totalNewTime = 0;
//
//		/*
//		* Step 1: assign the total check in count of a business by business_id from checkin dataset to business dataset
//		*
//		* old implementation:
//		* 	1) loop through all check in values							n: number of checkin entries
//		* 		2) loop through all businesses until a match is found 	x m: number of business entries
//	 	* complexity = O(n * m) = O(n^2)
//		*
//		* new implementation:
//		* 	1) loop through and map all business_id : checkintotals from checkinlist to a treemap 	n: number of checkin entries
//		* 	2) loop through all businesses and retrieve the checkintotals from the tree map			m: number of business entries, O(1) treemap retrieval
//		* complexity = O(n + m) = O(n)
//		 */
//		// STEP 1 OLD IMPLEMENTATION START//
//		long oldStep1Time = System.nanoTime();
////		for (CheckIn checkIn : checkInList) {
////
////			// Get the business_id of a checkin row record
////			String businessId = checkIn.getBusiness_id();
////
////			for (Business business : businessList) {
////				if (businessId.contains(business.getBusiness_id())) {
////					business.setTotalCheckin(checkIn.getTotal_checkin());
////					break;
////				}
////			}
////		}
//		// STEP 1 OLD IMPLEMENTATION END//
//		oldStep1Time = System.nanoTime() - oldStep1Time;
//		oldImplementationTimes += "\nStep 1 time elapsed: " + oldStep1Time;
//		totalOldTime += oldStep1Time;
//
//		// STEP 1 NEW IMPLEMENTATION START//
//		long newStep1Time = System.nanoTime();
//		Map<String, Integer> checkInMap = new TreeMap<>();
//		for (CheckIn checkIn : checkInList) {
//			checkInMap.put(checkIn.getBusiness_id(), checkIn.getTotal_checkin());
//		}
//		for (Business business : businessList) {
//			try {
//				business.setTotalCheckin(checkInMap.get(business.getBusiness_id()));
//			} catch (NullPointerException e) {
//				System.out.println("Business " + business.getBusiness_id() + " does not have enough checkins.");
//			}
//		}
//		// STEP 1 NEW IMPLEMENTATION END//
//		newStep1Time = System.nanoTime() - newStep1Time;
//		newImplementationTimes += "\nStep 1 time elapsed: " + newStep1Time;
//		totalNewTime += newStep1Time;
//		sideBySideComparison += "\nStep 1:";
//		sideBySideComparison += "\nOld: " + oldStep1Time;
//		sideBySideComparison += "\nNew: " + newStep1Time;
//
//
//		/*
//		 * Step 2: Parse through categories of all businesses and find a list of distinct categories
//		 *
//		 * old implementation:
//		 * 	loop through all businesses to their categories 		m: number of businesses
//		 *		loop through the category string characters         a: number of characters in a string
//		 * 		loop through the category string 					* (a + b): number of categories in a business
//		 * 			check if categoriesWord contains it already		* c: number of categories added so far
//		 * 	save categories 										+ c
//		 * complexity = O(m * (a + b) * c + c) = O(n^3)
//		 * estimate: O(m * 10 * b + b) = O(n^2) since number of categories usually < 10
//		 *
//		 * new implementation:
//		 *
//		 * complexity =
//		 */
//		// STEP 2 OLD IMPLEMENTATION START//
//		long oldStep2Time = System.nanoTime();
//
//		// Getting all the categories and store into database
//		List<String> categoriesWord = new ArrayList<>();
//		for (Business business : businessList) {
//			String[] categories = parse(business.getCategories());
//
//			for (String cat:categories) {
//				if (!categoriesWord.contains(cat)) {
//					categoriesWord.add(cat);
//				}
//			}
//		}
//
//		for (String cat: categoriesWord) {
//			categoryService.save(new Category(cat));
//		}
//		// STEP 2 OLD IMPLEMENTATION END//
//		oldStep2Time = System.nanoTime() - oldStep2Time;
//		oldImplementationTimes += "\nStep 2 time elapsed: " + oldStep2Time;
//		totalOldTime += oldStep2Time;
//
//		// STEP 2 NEW IMPLEMENTATION START//
//		long newStep2Time = System.nanoTime();
//		//TODO step 2 new implementation code goes here
//
//		// STEP 2 NEW IMPLEMENTATION END//
//		newStep2Time = System.nanoTime() - newStep2Time;
//		newImplementationTimes += "\nStep 2 time elapsed: " + newStep2Time;
//		totalNewTime += newStep2Time;
//		sideBySideComparison += "\nStep 2:";
//		sideBySideComparison += "\nOld: " + oldStep2Time;
//		sideBySideComparison += "\nNew: " + newStep2Time;
//
//
//		/*
//		 * Step 3: get all the states
//		 *
//		 * old implementation:
//		 * 	loop through all businesses to get their city 			m: number of businesses
//		 * 			check if cityNameList contains it already		* c: number of cities added so far
//		 * complexity = O(m * c) = O(n^2)
//		 *
//		 * complexity =
//		 *
//		 * new implementation:
//		 *
//		 * complexity =
//		 */
//		// STEP 3 OLD IMPLEMENTATION START//
//		long oldStep3Time = System.nanoTime();
//		// Getting all the states
//		List<String> cityNameList = new ArrayList<>();
//		for (Business business : businessList) {
//			String cityName =  business.getCity();
//
//			if (!cityNameList.contains(cityName)) {
//				cityNameList.add(cityName);
//				cityList.add( new City(cityName));
//			}
//		}
//		// STEP 3 OLD IMPLEMENTATION END//
//		oldStep3Time = System.nanoTime() - oldStep3Time;
//		oldImplementationTimes += "\nStep 3 time elapsed: " + oldStep3Time;
//		totalOldTime += oldStep3Time;
//
//		// STEP 3 NEW IMPLEMENTATION START//
//		long newStep3Time = System.nanoTime();
//		//TODO step 3 new implementation code goes here
//
//		// STEP 3 NEW IMPLEMENTATION END//
//		newStep3Time = System.nanoTime() - newStep3Time;
//		newImplementationTimes += "\nStep 3 time elapsed: " + newStep3Time;
//		totalNewTime += newStep3Time;
//		sideBySideComparison += "\nStep 3:";
//		sideBySideComparison += "\nOld: " + oldStep3Time;
//		sideBySideComparison += "\nNew: " + newStep3Time;
//
//
//
//		/*
//		 * Step 4: Get all the businesses in each state then transfer back to city object list
//		 *
//		 * old implementation:
//		 * 	created a mapping where city is the key and list of business in the specific city is the value
//		 * 	loop through the business list : n number of business entries
//		 * 		find business's city name  : O(1) operation
//		 *		loop through city name list  : m number of city names
//		 * 				check whether the found business's city name is inside: O(1) operation
//		 * 				assign to a local variable city : O(1) operation
//		 *
//		 * 		check if the mapping contains that city key, if not create a new key pair value : O(1) operation
//		 * 													 if there is, update the value for the key : O(1) operation
//		 *
//		 *
//		 * loop through the key set in the map : m number of city names
//		 * 		retrieve the business list of each key : O(1) operation
//		 * 		loop through the city list : m number of city objects
//		 * 								find the matching city name : O(1) operation
//		 * 								update that city's latest business list : O(1) operation
//		 *
//		 * complexity = O (n*m + m*m) = O(nm + m^2)
//		 *
//		 * new implementation:
//		 *
//		 * complexity =
//		 */
//		// STEP 4 OLD IMPLEMENTATION START//
//		long oldStep4Time = System.nanoTime();
//
//		// Getting all the businesses in each city
//		Map<String, List<Business>> mapCityBusiness = new HashMap<>();
//
//		for (Business business : businessList) {
//			String cityName = business.getCity();
//
//			String city = null;
//
//			for (String tempCity : cityNameList) {
//				if (tempCity.equals(cityName)) {
//					city = tempCity;
//					break;
//				}
//			}
//
//			if (!mapCityBusiness.containsKey(city)) {
//				List<Business> cityBusinessList = new ArrayList<>();
//				cityBusinessList.add(business);
//				mapCityBusiness.put(city, cityBusinessList);
//			} else {
//				List<Business> currCityBusinessList = mapCityBusiness.get(city);
//				currCityBusinessList.add(business);
//				mapCityBusiness.put(city,currCityBusinessList);
//			}
//		}
//
//		// transfer back to city object list
//		Set<String> keySet = mapCityBusiness.keySet();
//		for (String key : keySet) {
//			List<Business> cityBusinessList = mapCityBusiness.get(key);
//			for (City city : cityList) {
//				if (city.getName().equals(key)) {
//					city.setBusinessList(cityBusinessList);
//					break;
//				}
//			}
//		}
//
//		// STEP 4 OLD IMPLEMENTATION END//
//		oldStep4Time = System.nanoTime() - oldStep4Time;
//		oldImplementationTimes += "\nStep 4 time elapsed: " + oldStep4Time;
//		totalOldTime += oldStep4Time;
//
//		// STEP 4 NEW IMPLEMENTATION START//
//		long newStep4Time = System.nanoTime();
//		//TODO step 4 new implementation code goes here
//
//		// STEP 4 NEW IMPLEMENTATION END//
//		newStep4Time = System.nanoTime() - newStep4Time;
//		newImplementationTimes += "\nStep 4 time elapsed: " + newStep4Time;
//		totalNewTime += newStep4Time;
//		sideBySideComparison += "\nStep 4:";
//		sideBySideComparison += "\nOld: " + oldStep4Time;
//		sideBySideComparison += "\nNew: " + newStep4Time;
//
//
//		/*
//		 * Step 5: Getting all the categories in each cityState
//		 *
//		 * old implementation:
//		 * 	loop through city list : n number of city objects
//		 * 		create a map where key is the category and value is the number of checkins
//		 *		retrieve list of businesses in the city: O(1) operation
//		 * 		create a new string list to find all category names in a city
//		 * 		create a new list to find all categories in a city
//		 *
//		 * 		loop through the business list : m number of businesses
//		 * 			retrieve the business' total checkins : O(1) operation
//		 * 			retrieve the business' string category : O(1) operation
//		 * 			create a new category list
//		 *
//		 * 			loop through the string category: l number of categories
//		 * 				update the business' category list: O(1) operation
//		 *
//		 * 			loop through the business' category: l number of categories
//		 * 				check if the city category list contains the category: O(1) operation
//		 * 					if not create a new key pair value: O(1) operation
//		 *					if there is, update the new checkin for each category: O(1) operation
//		 *
//		 * 	update city's category list: O(1) operation
//		 *  update city's category frequency mapping: O(1) operation
//		 *
//		 * complexity = O (n * m * (2l)) = O(nml)
//		 *
//		 * new implementation:
//		 *
//		 * complexity =
//		 */
//		// STEP 5 OLD IMPLEMENTATION START//
//		long oldStep5Time = System.nanoTime();
//
////		 Getting all the categories in each city
//		for (City city : cityList) {
//			Map<String,Integer> frequencyMap = new TreeMap<String,Integer >();
//			List<Business> cityBusinessList = city.getBusinessList();
//
//			List<String> categoriesInCityState = new ArrayList<>();
//			List<Category> categoriesList = new ArrayList<>();
//
//			for (Business business : cityBusinessList) {
//				int totalCheckin = business.getTotalCheckin();
//
//				List<String> businessCategoryList = new ArrayList<>();
//
//				String[] categories = parse(business.getCategories());
//
//				for (String category : categories) {
//					businessCategoryList.add(category);
//				}
//
//				for (String category: businessCategoryList) {
//					if (!categoriesInCityState.contains(category)) {
//						categoriesInCityState.add(category);
//					}
//
//					if (!frequencyMap.containsKey(category)) {
//						frequencyMap.put(category,totalCheckin);
//					} else {
//						int value = frequencyMap.get(category);
//						frequencyMap.put(category, value + totalCheckin);
//					}
//
//				}
//			}
//
//			for (String category : categoriesInCityState) {
//				categoriesList.add(new Category(category));
//			}
//
////			frequencyMap = valueSort(frequencyMap);
//
//			city.setCategoryList(categoriesList);
//			city.setCategoryFrequency(frequencyMap);
//		}
//		// STEP 5 OLD IMPLEMENTATION END//
//		oldStep5Time = System.nanoTime() - oldStep5Time;
//		oldImplementationTimes += "\nStep 5 time elapsed: " + oldStep5Time;
//		totalOldTime += oldStep5Time;
//
//		// STEP 5 NEW IMPLEMENTATION START//
//		long newStep5Time = System.nanoTime();
//		//TODO step 5 new implementation code goes here
//
//		// STEP 5 NEW IMPLEMENTATION END//
//		newStep5Time = System.nanoTime() - newStep5Time;
//		newImplementationTimes += "\nStep 5 time elapsed: " + newStep5Time;
//		totalNewTime += newStep5Time;
//		sideBySideComparison += "\nStep 5:";
//		sideBySideComparison += "\nOld: " + oldStep5Time;
//		sideBySideComparison += "\nNew: " + newStep5Time;
//
//		/*
//		* Report the times
//		 */
//		System.out.println("===========");
//		System.out.println(oldImplementationTimes);
//		System.out.println(newImplementationTimes);
//		System.out.println("===========");
//		System.out.println("Old implementation total time taken: " + totalOldTime);
//		System.out.println("New implementation total time taken: " + totalNewTime);
//		System.out.println("===========\nSide by side comparisons:\n" + sideBySideComparison + "\n===========");
//
//
//		// Implementing K means
//		final String[] kMeansCategories = new String[] {"Mexican", "Kebab"};
//
//		List<Record> records = createRecords(cityList, kMeansCategories);
//
//		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 7, new EuclideanDistance(), 1000);
//
//		// Printing the cluster configuration
//		clusters.forEach((key, value) -> {
//			System.out.println("-------------------------- CLUSTER ----------------------------");
//
//			// Sorting the coordinates to see the most significant tags first.
////			System.out.println(sortedCentroid(key));
////			String members = String.join(", ", value.stream().map(Record::getDescription).collect(toSet()));
////			System.out.print(members);
//			System.out.println(key.toString());
//
//			for (Record record : value) {
//				System.out.print(record.getDescription());
//				System.out.print(",");
//			}
//
//			System.out.println();
//			System.out.println();
//		});
//
//		List<CityCoords> cityCoordsList = cityCoordsService.list();
//		Map<String, Double[]> cityCoordsMap = new HashMap<>();
//		for (CityCoords cityCoords : cityCoordsList) {
//			cityCoordsMap.put(cityCoords.getCity().toLowerCase(), new Double[]{cityCoords.getLatitude(), cityCoords.getLongitude()});
//		}
//
//		for (Centroid centroid : clusters.keySet()) {
//			for (Record record : clusters.get(centroid)) {
//				Double[] coordinates = findCoordinates(record.getDescription().toLowerCase(), cityCoordsMap);
//				System.out.println(record.getDescription());
//				if (coordinates != null) {
//					System.out.println(coordinates[0]);
//					System.out.println(coordinates[1]);
//				}
//
//			}
//		}

	}

//	public static Double[] findCoordinates(String cityName, Map<String, Double[]> cityCoordsMap) {
//		for (String key : cityCoordsMap.keySet()) {
//			if (key.equals(cityName)) {
//				return new Double[] {cityCoordsMap.get(key)[0], cityCoordsMap.get(key)[1]};
//			}
//		}
//		return null;
//	}
//
//	public static List<Map.Entry<String, Integer>> sortMap(City city) {
//		Map<String, Integer> testFrequencyMap = city.getCategoryFrequency();
//		List sortedList = new ArrayList(testFrequencyMap.entrySet());
//		Collections.sort(sortedList, Collections.reverseOrder(new Comparator<Map.Entry<String, Integer>>() {
//			@Override
//			public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
//				return e1.getValue().compareTo(e2.getValue());
//			}
//		}));
//		return sortedList;
//	}
//
//	public static List<Record> createRecords(List<City> cityStateList, String[] tags) throws IOException {
//		List<Record> records = new ArrayList<>();
//
//		for (City cityState : cityStateList) {
//			String cityName = cityState.getName();
//			Map<String, Integer> currentFrequencyMap = cityState.getCategoryFrequency();
//			Map<String, Double> tagMap = new HashMap<>();
//
//			for (String key : tags) {
//				if (currentFrequencyMap.get(key) != null) {
//					tagMap.put(key, currentFrequencyMap.get(key).doubleValue());
//				} else {
//					tagMap.put(key, .0);
//				}
//			}
//
//			// Only keep popular tags.
//			records.add(new Record(cityName, tagMap));
//		}
//
//		return records;
//	}


	@Bean
	CommandLineRunner runnerBusiness(BusinessService businessService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();

			// In case any unknown values are not registered
//			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			TypeReference<List<Business>> typeReference = new TypeReference<List<Business>>(){};

//			InputStream inputStream = TypeReference.class.getResourceAsStream("/business/business-mexican-turkish-only.json");
			InputStream inputStream = TypeReference.class.getResourceAsStream("/business/business_dataset.json");

			try {
				List<Business> businesses = mapper.readValue(inputStream,typeReference);
				businessService.save(businesses);
				System.out.println("All businesses saved!");

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

		};
	}

	@Bean
	CommandLineRunner runnerCheckIn(CheckInService checkinService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();
			TypeReference<List<CheckIn>> typeReference = new TypeReference<List<CheckIn>>(){};

//			InputStream inputStream = TypeReference.class.getResourceAsStream("/checkin/checkin-mexican-turkish-only.json");
			InputStream inputStream = TypeReference.class.getResourceAsStream("/checkin/checkin_dataset.json");

			try {
				List<CheckIn> checkIns = mapper.readValue(inputStream,typeReference);
				checkinService.save(checkIns);
				System.out.println("All check-ins saved!");
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

		};
	}

	@Bean
	CommandLineRunner runnerCityCoords(CityCoordsService cityCoordsService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();

			// In case any unknown values are not registered
//			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			TypeReference<List<CityCoords>> typeReference = new TypeReference<List<CityCoords>>(){};

			InputStream inputStream = TypeReference.class.getResourceAsStream("/coordinates/uscities.json");

			try {
				List<CityCoords> cityCoordsList = mapper.readValue(inputStream,typeReference);
				cityCoordsService.save(cityCoordsList);
				System.out.println("All city coordinates saved!");

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

		};
	}

}
