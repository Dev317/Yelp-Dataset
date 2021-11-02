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

	public static void main(String[] args) throws IOException {
		ApplicationContext ctx = SpringApplication.run(YelpProjectApplication.class, args);
		System.out.println("All data stored!");
	}


	@Bean
	CommandLineRunner runnerBusiness(BusinessService businessService) {
		return args -> {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			TypeReference<List<Business>> typeReference = new TypeReference<List<Business>>(){};
//			InputStream inputStream = TypeReference.class.getResourceAsStream("/business/business_dataset.json");
			InputStream inputStream = TypeReference.class.getResourceAsStream("/business/business-mexican-turkish-only.json");

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
//			InputStream inputStream = TypeReference.class.getResourceAsStream("/checkin/checkin_dataset.json");
			InputStream inputStream = TypeReference.class.getResourceAsStream("/checkin/checkin-mexican-turkish-only.json");
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
