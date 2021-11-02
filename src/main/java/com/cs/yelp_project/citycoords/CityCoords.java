package com.cs.yelp_project.citycoords;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class CityCoords {

    @Id
    private @JsonProperty("city")  String city;
    private @JsonProperty("state") String state;
    private @JsonProperty("latitude") Double latitude;
    private @JsonProperty("longitude") Double longitude;

    public CityCoords() {}


}
