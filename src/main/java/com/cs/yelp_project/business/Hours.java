package com.cs.yelp_project.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Embeddable;

@Data
@AllArgsConstructor
@Embeddable
public class Hours {
    private @JsonProperty("Monday") String Monday;
    private @JsonProperty("Tuesday") String Tuesday;
    private @JsonProperty("Wednesday") String Wednesday;
    private @JsonProperty("Thursday") String Thursday;
    private @JsonProperty("Friday") String Friday;
    private @JsonProperty("Saturday") String Saturday;
    private @JsonProperty("Sunday") String Sunday;

    public Hours() {};
}
