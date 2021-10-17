package com.cs.yelp_project.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import javax.persistence.Embeddable;

@Data
@AllArgsConstructor
@Embeddable
@Getter
public class Attributes {

    private @JsonProperty("RestaurantsTableService") String RestaurantsTableService;
    private @JsonProperty("WiFi") String WiFi;
    private @JsonProperty("BikeParking") String BikeParking;
    private @JsonProperty("BusinessParking") String  BusinessParking;
    private @JsonProperty("BusinessAcceptsCreditCards") String BusinessAcceptsCreditCards;
    private @JsonProperty("RestaurantsReservations") String RestaurantsReservations;
    private @JsonProperty("WheelchairAccessible") String WheelchairAccessible;
    private @JsonProperty("Caters") String Caters;
    private @JsonProperty("OutdoorSeating") String OutdoorSeating;
    private @JsonProperty("RestaurantsGoodForGroups") String RestaurantsGoodForGroups;
    private @JsonProperty("HappyHour") String HappyHour;
    private @JsonProperty("BusinessAcceptsBitcoin") String BusinessAcceptsBitcoin;
    private @JsonProperty("RestaurantsPriceRange2") String RestaurantsPriceRange2;
    private @JsonProperty("Ambience") String Ambience;
    private @JsonProperty("HasTV") String HasTV;
    private @JsonProperty("Alcohol") String Alcohol;
    private @JsonProperty("GoodForMeal") String GoodForMeal;
    private @JsonProperty("DogsAllowed") String DogsAllowed;
    private @JsonProperty("RestaurantsTakeOut") String RestaurantsTakeOut;
    private @JsonProperty("NoiseLevel") String NoiseLevel;
    private @JsonProperty("RestaurantsAttire") String RestaurantsAttire;
    private @JsonProperty("RestaurantsDelivery") String RestaurantsDelivery;
    private @JsonProperty("GoodForKids") String GoodForKids;
    private @JsonProperty("ByAppointmentOnly") String ByAppointmentOnly;
    private @JsonProperty("Music") String Music;
    private @JsonProperty("GoodForDancing") String GoodForDancing;
    private @JsonProperty("BestNights") String BestNights;
    private @JsonProperty("BYOB") String BYOB;
    private @JsonProperty("CoatCheck") String CoatCheck;
    private @JsonProperty("Smoking") String Smoking;
    private @JsonProperty("DriveThru") String DriveThru;
    private @JsonProperty("BYOBCorkage") String BYOBCorkage;
    private @JsonProperty("Corkage") String Corkage;
    private @JsonProperty("RestaurantsCounterService") String RestaurantsCounterService;
    private @JsonProperty("AcceptsInsurance") String AcceptsInsurance;
    private @JsonProperty("DietaryRestrictions") String DietaryRestrictions;
    private @JsonProperty("AgesAllowed") String AgesAllowed;
    private @JsonProperty("Open24Hours") String Open24Hours;
    private @JsonProperty("HairSpecializesIn") String HairSpecializesIn;

    public Attributes() {}
}
