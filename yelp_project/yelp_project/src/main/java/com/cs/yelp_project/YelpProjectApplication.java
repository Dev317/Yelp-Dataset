package com.cs.yelp_project;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessService;
import com.cs.yelp_project.checkin.CheckIn;
import com.cs.yelp_project.checkin.CheckinService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.util.List;

@SpringBootApplication
public class YelpProjectApplication {


	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(YelpProjectApplication.class, args);

	}

	@Bean
	CommandLineRunner runnerCheckIn(CheckinService checkinService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();
			TypeReference<List<CheckIn>> typeReference = new TypeReference<List<CheckIn>>(){};

			InputStream inputStream = TypeReference.class.getResourceAsStream("/checkin/checkin.json");

			try {
				List<CheckIn> checkIns = mapper.readValue(inputStream,typeReference);
				checkinService.save(checkIns);
				System.out.println("Check-Ins Saved!");
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		};
	}

	@Bean
	CommandLineRunner runnerBusiness(BusinessService businessService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();

			// In case any unknown values are not registered
//			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			TypeReference<List<Business>> typeReference = new TypeReference<List<Business>>(){};

			InputStream inputStream = TypeReference.class.getResourceAsStream("/business/business.json");

			try {
				List<Business> businesses = mapper.readValue(inputStream,typeReference);
				businessService.save(businesses);
				System.out.println("Businesses Saved!");
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		};
	}

}
