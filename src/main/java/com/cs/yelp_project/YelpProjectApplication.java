package com.cs.yelp_project;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessService;
import com.cs.yelp_project.business.Category;
import com.cs.yelp_project.business.CategoryService;
import com.cs.yelp_project.checkin.CheckIn;
import com.cs.yelp_project.checkin.CheckInService;

import com.cs.yelp_project.citystate.CityState;
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

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(YelpProjectApplication.class, args);
		System.out.println("started");

		CheckInService checkInService = ctx.getBean(CheckInService.class);
		BusinessService businessService = ctx.getBean(BusinessService.class);
		CategoryService categoryService = ctx.getBean(CategoryService.class);

		List<CityState> cityStateList = new ArrayList<>();
		List<Business> businessList = businessService.list();

		//Everything Takes About 3 Minutes To Run!
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

		// Getting all the categories and store into database
		categoryService.save(new Category("Mexican"));
		categoryService.save(new Category("New Mexican Cuisine"));
		categoryService.save(new Category("Nightlife"));

		// Getting all the states
		List<String> cityNameList = businessList.stream().map( Business::getCity ).distinct().collect( Collectors.toList() );
		for (String cityName: cityNameList) {
			cityStateList.add(new CityState(cityName));
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

		// Testing
		System.out.println("Sorted by categories:");
		for (int idx = 0; idx < 5; idx++) {
			CityState cityState = cityStateList.get(idx);
			List<Map.Entry<String,Integer>> frequencyList = sortMap(cityState);
			System.out.println(cityState.getName());
			System.out.println(frequencyList);
		}

	}

	public static List<Map.Entry<String, Integer>> sortMap(CityState cityState) {
		Map<String, Integer> testFrequencyMap = cityState.getCategoryFrequency();
		List sortedList = new ArrayList(testFrequencyMap.entrySet());
		Collections.sort(sortedList, Collections.reverseOrder(new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
				return -e1.getValue().compareTo(e2.getValue());
			}
		}));
		return sortedList;
	}


	@Bean
	CommandLineRunner runnerBusiness(BusinessService businessService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();

			// In case any unknown values are not registered
//			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			TypeReference<List<Business>> typeReference = new TypeReference<List<Business>>(){};

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
