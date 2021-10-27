package com.cs.yelp_project.checkin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@Data
@Getter
public class CheckIn {

    @Id
    private @JsonProperty("business_id") String business_id;

    @Length(max=5000000)
    private @JsonProperty("date") String date;
    private @JsonProperty("total_checkin") int total_checkin;

    public CheckIn() {}

}
