package com.cs.yelp_project.citycoords;

import com.cs.yelp_project.checkin.CheckIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityCoordsService {

    private CityCoordsRepository repository;

    @Autowired
    public CityCoordsService(CityCoordsRepository repository) {
        this.repository = repository;
    }

    public List<CityCoords> list() {
        return repository.findAll();
    }

    public List<CityCoords> save(List<CityCoords> list) {
        return repository.saveAll(list);
    }

}
