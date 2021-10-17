package com.cs.yelp_project;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessService;
import com.cs.yelp_project.checkin.CheckIn;
import com.cs.yelp_project.checkin.CheckInService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class YelpProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(YelpProjectApplication.class, args);
		System.out.println("started");
	}

//	@Bean
//	CommandLineRunner runnerBusiness(BusinessService businessService) {
//		return args -> {
//
//			ObjectMapper mapper = new ObjectMapper();
//
//			// In case any unknown values are not registered
////			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
//
//			TypeReference<List<Business>> typeReference = new TypeReference<List<Business>>(){};
//
//			InputStream inputStream = TypeReference.class.getResourceAsStream("/business/business_dataset.json");
//
//			try {
//				List<Business> businesses = mapper.readValue(inputStream,typeReference);
//				businessService.save(businesses);
//				System.out.println("Businesses Saved!");
//			} catch (IOException e) {
//				System.out.println(e.getMessage());
//			}
//		};
//	}

	@Bean
	CommandLineRunner runnerCheckIn(CheckInService checkinService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();
			TypeReference<List<CheckIn>> typeReference = new TypeReference<List<CheckIn>>(){};

			InputStream inputStream = TypeReference.class.getResourceAsStream("/checkin/checkin_dataset.json");

			try {
				List<CheckIn> checkIns = mapper.readValue(inputStream,typeReference);
				checkinService.save(checkIns);
				System.out.println("Check-Ins Saved!");
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		};
	}

}
