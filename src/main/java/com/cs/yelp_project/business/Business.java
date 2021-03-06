package com.cs.yelp_project.business;

import com.cs.yelp_project.citystate.CityState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

import javax.persistence.*;

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

//    @ManyToMany(mappedBy = "businesses")
//    private List<Category> category_name;

    private int totalCheckin;


    public Business() {}

//    public void setCategory_name(List<Category> categoryList) {
//        this.category_name = categoryList;
//    }

    public void setTotalCheckin(int totalCheckin) {
        this.totalCheckin = totalCheckin;
    }

    public int getTotalCheckin() {
        return totalCheckin;
    }
}

