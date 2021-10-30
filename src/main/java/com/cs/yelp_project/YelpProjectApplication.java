package com.cs.yelp_project;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessService;
import com.cs.yelp_project.business.Category;
import com.cs.yelp_project.business.CategoryService;
import com.cs.yelp_project.checkin.CheckIn;
import com.cs.yelp_project.checkin.CheckInService;

import com.cs.yelp_project.citystate.CityState;
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
import java.util.stream.Collectors;

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

		List<CityState> cityStateList = new ArrayList<>();
		List<Business> businessList = businessService.list();


		/*
		* Switch to choose between old implementation and new implementation
		 */
		boolean useOldImplementation = true;

		/*
		* Timers
		 */
		String oldImplementationTimes = "Old Implementation:";
		String newImplementationTimes = "New Implementation:";
		String sideBySideComparison = "";
		long totalOldTime = 0;
		long totalNewTime = 0;

		/*
		* Step 1: assign the total check in count of a business by business_id from checkin dataset to business dataset
		*
		* old implementation:
		* 	1) loop through all check in values							n: number of checkin entries
		* 		2) loop through all businesses until a match is found 	x m: number of business entries
	 	* complexity = O(n * m) = O(n^2)
		*
		* new implementation:
		* 	1) loop through and map all business_id : checkintotals from checkinlist to a treemap 	n: number of checkin entries
		* 	2) loop through all businesses and retrieve the checkintotals from the tree map			m: number of business entries, O(1) treemap retrieval
		* complexity = O(n + m) = O(n)
		 */
		// STEP 1 OLD IMPLEMENTATION START//
		// long oldStep1Time = System.nanoTime();
		// int counter = 0;
		// List<CheckIn> checkInList = checkInService.list();
		// for (CheckIn checkIn : checkInList) {

		// 	String businessId = checkIn.getBusiness_id();

		// 	for (Business business : businessList) {
		// 		if (counter > checkInList.size()) {
		// 			System.out.println("Repeat ALERT!!!");
		// 			break;
		// 		}

		// 		if (counter < checkInList.size() && businessId.contains(business.getBusiness_id())) {
		// 			business.setTotalCheckin(checkIn.getTotal_checkin());
		// 			counter++;
		// 			break;
		// 		}
		// 	}
		// }
		// // STEP 1 OLD IMPLEMENTATION END//
		// oldStep1Time = System.nanoTime() - oldStep1Time;
		// oldImplementationTimes += "\nStep 1 time elapsed: " + oldStep1Time;
		// totalOldTime += oldStep1Time;

		// STEP 1 NEW IMPLEMENTATION START//
		long newStep1Time = System.nanoTime();
		List<CheckIn> checkInList = checkInService.list();
        Map<String, CheckIn> checkInMap = new HashMap<>();
        for(CheckIn checkIn : checkInList){
            CheckIn current = checkInMap.get(checkIn.getBusiness_id());
           if(current == null){
                checkInMap.put(checkIn.getBusiness_id(), checkIn);
				
           }else{
                current.setTotal_checkin(checkIn.getTotal_checkin() + current.getTotal_checkin());
           }
        }

        for (Business business: businessList) {
			if (checkInMap.containsKey(business.getBusiness_id())) {
				CheckIn checkIn = checkInMap.get(business.getBusiness_id());
				business.setTotalCheckin(checkIn.getTotal_checkin());
			} else {
				business.setTotalCheckin(0);
			}
		}
		// STEP 1 NEW IMPLEMENTATION END//
		newStep1Time = System.nanoTime() - newStep1Time;
		newImplementationTimes += "\nStep 1 time elapsed: " + newStep1Time;
		totalNewTime += newStep1Time;
		sideBySideComparison += "\nStep 1:";
		// sideBySideComparison += "\nOld: " + oldStep1Time;
		sideBySideComparison += "\nNew: " + newStep1Time;


		/*
		 * Step 2: Parse through all categories
		 *
		 * old implementation:
		 * 	loop through all businesses to their categories 		m: number of businesses
		 *		loop through the category string 					* a: number of categories in a business
		 * 			check if categoriesWord contains it already		* b: number of categories added so far
		 * 	save categories 										+ b
		 * complexity = O(m * a * b + b) = O(n^3)
		 * estimate: O(m * 10 * b + b) = O(n^2) since number of categories usually < 10
		 *
		 * new implementation:
		 *
		 * complexity =
		 */
		// STEP 2 OLD IMPLEMENTATION START//
		// long oldStep2Time = System.nanoTime();

		// // Getting all the categories and store into database
		// List<String> categoriesWord = new ArrayList<>();
		// for (Business business : businessList) {
		// 	String[] categories = parse(business.getCategories());

		// 	for (String cat:categories) {
		// 		if (!categoriesWord.contains(cat)) {
		// 			categoriesWord.add(cat);
		// 		}
		// 	}
		// }

		// for (String cat: categoriesWord) {
		// 	categoryService.save(new Category(cat));
		// }
		// STEP 2 OLD IMPLEMENTATION END//
		// oldStep2Time = System.nanoTime() - oldStep2Time;
		// oldImplementationTimes += "\nStep 2 time elapsed: " + oldStep2Time;
		// totalOldTime += oldStep2Time;

		// STEP 2 NEW IMPLEMENTATION START//
		long newStep2Time = System.nanoTime();
		categoryService.save(new Category("Mexican"));
		categoryService.save(new Category("New Mexican Cuisine"));
		categoryService.save(new Category("Nightlife"));

		// STEP 2 NEW IMPLEMENTATION END//
		newStep2Time = System.nanoTime() - newStep2Time;
		newImplementationTimes += "\nStep 2 time elapsed: " + newStep2Time;
		totalNewTime += newStep2Time;
		sideBySideComparison += "\nStep 2:";
		// sideBySideComparison += "\nOld: " + oldStep2Time;
		sideBySideComparison += "\nNew: " + newStep2Time;



		/*
		 * Step 3: get all the states
		 *
		 * old implementation:
		 * 	loop through all businesses to get their city 			m: number of businesses
		 * 			check if cityNameList contains it already		* c: number of cities added so far
		 * complexity = O(m * c) = O(n^2)
		 *
		 * complexity =
		 *
		 * new implementation:
		 *
		 * complexity =
		 */
		// STEP 3 OLD IMPLEMENTATION START//
		// long oldStep3Time = System.nanoTime();
		// // Getting all the states
		// List<String> cityNameList = new ArrayList<>();
		// for (Business business : businessList) {
		// 	String cityName =  business.getCity();

		// 	if (!cityNameList.contains(cityName)) {
		// 		cityNameList.add(cityName);
		// 		cityStateList.add( new CityState(cityName));
		// 	}
		// }
		// STEP 3 OLD IMPLEMENTATION END//
		// oldStep3Time = System.nanoTime() - oldStep3Time;
		// oldImplementationTimes += "\nStep 3 time elapsed: " + oldStep3Time;
		// totalOldTime += oldStep3Time;

		// STEP 3 NEW IMPLEMENTATION START//
		long newStep3Time = System.nanoTime();
		List<String> cityNameList = businessList.stream().map( Business::getCity ).distinct().collect( Collectors.toList() );
		for (String cityName: cityNameList) {
			cityStateList.add(new CityState(cityName));
		}

		// STEP 3 NEW IMPLEMENTATION END//
		newStep3Time = System.nanoTime() - newStep3Time;
		newImplementationTimes += "\nStep 3 time elapsed: " + newStep3Time;
		totalNewTime += newStep3Time;
		sideBySideComparison += "\nStep 3:";
		// sideBySideComparison += "\nOld: " + oldStep3Time;
		sideBySideComparison += "\nNew: " + newStep3Time;



		/*
		 * Step 4: Get all the businesses in each state then transfer back to cityState object list
		 *
		 * old implementation:
		 *
		 * complexity =
		 *
		 * new implementation:
		 *
		 * complexity =
		 */
		// STEP 4 OLD IMPLEMENTATION START//
// 		long oldStep4Time = System.nanoTime();

// 		// Getting all the businesses in each state
// 		Map<String, List<Business>> map = new HashMap<>();

// 		for (Business business : businessList) {
// 			String cityName = business.getCity();

// 			String cityState = null;

// 			for (String tempCityState : cityNameList) {
// 				if (tempCityState.equals(cityName)) {
// 					cityState = tempCityState;
// 					break;
// 				}
// 			}

// 			if (!map.containsKey(cityState)) {
// 				List<Business> newList = new ArrayList<>();
// 				newList.add(business);
// 				map.put(cityState,newList);
// 			} else {
// 				List<Business> currList = map.get(cityState);
// 				currList.add(business);
// 				map.put(cityState,currList);
// 			}
// 		}

// 		// transfer back to cityStateList object list
// 		Set<String> keySet = map.keySet();
// 		for (String key : keySet) {
// //			System.out.println(key);
// 			List<Business> businesses = map.get(key);
// 			for (CityState city : cityStateList) {
// 				if (city.getName().equals(key)) {
// 					city.setBusinessList(businesses);
// 					break;
// 				}
// 			}
// 		}

// 		// test
// //		for (CityState city : cityStateList) {
// //			List<Business> cityBusinessList = city.getBusinessList();
// //			String result = "" + city.getName() + ":";
// //			for (Business business: cityBusinessList) {
// //				result += business.getName() + " ";
// //			}
// //			System.out.println(result);
// //		}
// 		// STEP 4 OLD IMPLEMENTATION END//
// 		oldStep4Time = System.nanoTime() - oldStep4Time;
// 		oldImplementationTimes += "\nStep 4 time elapsed: " + oldStep4Time;
// 		totalOldTime += oldStep4Time;

		// STEP 4 NEW IMPLEMENTATION START//
		long newStep4Time = System.nanoTime();
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

		Set<String> keySet = map.keySet();
		for (String key : keySet) {
			List<Business> businesses = map.get(key);
			for (CityState city : cityStateList) {
				if (city.getName().equals(key)) {
					city.setBusinessList(businesses);
					break;
				}
			}
		}

		// STEP 4 NEW IMPLEMENTATION END//
		newStep4Time = System.nanoTime() - newStep4Time;
		newImplementationTimes += "\nStep 4 time elapsed: " + newStep4Time;
		totalNewTime += newStep4Time;
		sideBySideComparison += "\nStep 4:";
		// sideBySideComparison += "\nOld: " + oldStep4Time;
		sideBySideComparison += "\nNew: " + newStep4Time;



		/*
		 * Step 5: Getting all the categories in each cityState
		 *
		 * old implementation:
		 *
		 * complexity =
		 *
		 * new implementation:
		 *
		 * complexity =
		 */
		// STEP 5 OLD IMPLEMENTATION START//
// 		long oldStep5Time = System.nanoTime();

// //		 Getting all the categories in each cityState
// 		for (CityState cityState : cityStateList) {
// 			Map<String,Integer> frequencyMap = new TreeMap<String,Integer >();
// 			List<Business> businessesInCityState = cityState.getBusinessList();
// 			List<String> categoriesInCityState = new ArrayList<>();
// 			List<Category> categoriesList = new ArrayList<>();

// 			for (Business business : businessesInCityState) {
// 				int totalCheckin = business.getTotalCheckin();

// 				List<String> businessCategoryList = new ArrayList<>();

// 				String[] categories = parse(business.getCategories());

// 				for (String category : categories) {
// 					businessCategoryList.add(category);
// 				}

// 				for (String category: businessCategoryList) {
// 					if (!categoriesInCityState.contains(category)) {
// 						categoriesInCityState.add(category);
// 					}

// 					if (!frequencyMap.containsKey(category)) {
// 						frequencyMap.put(category,totalCheckin);
// 					} else {
// 						int value = frequencyMap.get(category);
// 						frequencyMap.put(category, value + totalCheckin);
// 					}

// 				}
// 			}

// 			for (String category : categoriesInCityState) {
// 				categoriesList.add(new Category(category));
// 			}

// //			frequencyMap = valueSort(frequencyMap);

// 			cityState.setCategoryList(categoriesList);
// 			cityState.setCategoryFrequency(frequencyMap);
// 		}
// 		// STEP 5 OLD IMPLEMENTATION END//
// 		oldStep5Time = System.nanoTime() - oldStep5Time;
// 		oldImplementationTimes += "\nStep 5 time elapsed: " + oldStep5Time;
// 		totalOldTime += oldStep5Time;

		// STEP 5 NEW IMPLEMENTATION START//
		long newStep5Time = System.nanoTime();
		List<String> categoriesFilter = Arrays.asList("Mexican", "Kebab", "Latin American", "Tex-Mex", "New Mexican Cuisine", "Wraps");

		for (CityState cityState : cityStateList) {
			Map<String,Integer> frequencyMap = new TreeMap<String,Integer >();
			List<Business> businessesInCityState = cityState.getBusinessList();

			for (Business business : businessesInCityState) {
				int totalCheckin = business.getTotalCheckin();

				String[] categories = parse(business.getCategories());

				for (String category : categories) {
					if (categoriesFilter.contains(category)) {
						if (!frequencyMap.containsKey(category)) {
							frequencyMap.put(category,totalCheckin);
						} else {
							int value = frequencyMap.get(category);
							frequencyMap.put(category, value + totalCheckin);
						}
					}
				}
			}

			cityState.setCategoryFrequency(frequencyMap);
		}

		// STEP 5 NEW IMPLEMENTATION END//
		newStep5Time = System.nanoTime() - newStep5Time;
		newImplementationTimes += "\nStep 5 time elapsed: " + newStep5Time;
		totalNewTime += newStep5Time;
		sideBySideComparison += "\nStep 5:";
		// sideBySideComparison += "\nOld: " + oldStep5Time;
		sideBySideComparison += "\nNew: " + newStep5Time;

		/*
		* Report the times
		 */
		System.out.println("===========");
		System.out.println(oldImplementationTimes);
		System.out.println(newImplementationTimes);
		System.out.println("===========");
		System.out.println("Old implementation total time taken: " + totalOldTime);
		System.out.println("New implementation total time taken: " + totalNewTime);
		System.out.println("===========\nSide by side comparisons:\n" + sideBySideComparison + "\n===========");

		// Testing
//		for (CityState cityState : cityStateList) {
//			List<Category> testCategory = cityState.getCategoryList();
//			String testResult = "" + cityState.getName() + ":";
//			for (Category category : testCategory ) {
//				testResult += category.getName();
//			}
//			System.out.println(testResult);
//		}

		// Testing
//		System.out.println("Sorted by categories:");
//		for (int idx = 0; idx < 5; idx++) {
//			CityState cityState = cityStateList.get(idx);
//			List frequencyList = sortMap(cityState);
//			System.out.println(cityState.getName());
//			System.out.println(frequencyList);
//		}

		// Implementing K means
		final String[] kMeansCategories = new String[] {"Mexican", "Kebab", "Latin American", "Tex-Mex","New Mexican Cuisine", "Wraps"};


		List<Record> records = createRecords(cityStateList, kMeansCategories);

		Map<Centroid, List<Record>> clusters = KMeans.fit(records, 7, new EuclideanDistance(), 1000);

		// Printing the cluster configuration
		clusters.forEach((key, value) -> {
			System.out.println("-------------------------- CLUSTER ----------------------------");

			// Sorting the coordinates to see the most significant tags first.
//			System.out.println(sortedCentroid(key));
//			String members = String.join(", ", value.stream().map(Record::getDescription).collect(toSet()));
//			System.out.print(members);
			System.out.println(key.toString());

			for (Record record : value) {
				System.out.print(record.toString());
			}

			System.out.println();
			System.out.println();
		});

	}

	public static List<Map.Entry<String, Integer>> sortMap(CityState cityState) {
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

	public static List<Record> createRecords(List<CityState> cityStateList, String[] tags) throws IOException {
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

}
