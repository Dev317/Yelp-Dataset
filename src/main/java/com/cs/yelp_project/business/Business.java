package com.cs.yelp_project.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@AllArgsConstructor
public class Business {

    @Id
    private @JsonProperty("business_id") String business_id;

    private @JsonProperty("name") String name;
    private @JsonProperty("address") String address;
    private @JsonProperty("city") String city;
    private @JsonProperty("state") String state;
    private @JsonProperty("postal_code") String postal_code;
    private @JsonProperty("latitude") Double latitude;
    private @JsonProperty("longitude") Double longitude;
    private @JsonProperty("stars") Double stars;
    private @JsonProperty("review_count") Integer review_count;
    private @JsonProperty("is_open") Integer is_open;

    @Length(max=50000000)
    private @JsonProperty("categories") String categories;

    @Embedded
    private @JsonProperty("attributes") Attributes attributes;

    @Embedded
    private @JsonProperty("hours") Hours hours;

    public Business() {}

}

