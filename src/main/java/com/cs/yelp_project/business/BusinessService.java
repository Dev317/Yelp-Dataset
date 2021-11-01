package com.cs.yelp_project.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {
    private BusinessRepository repository;
    private CategoryService categoryService ;

    @Autowired
    public BusinessService(BusinessRepository repository,CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }

    public List<Business> list() {
        return repository.findAll();
    }

    public List<BusinessCheckInDTO> listByCategory(String category) {
        return repository.findByCategoryType(category);
    }

    public List<Business> save(List<Business> list) {
        return repository.saveAll(list);
    }

    public Business save(Business checkIn) {
        return repository.save(checkIn);
    }

    public List<Business> filterByCity(String city) {
        return repository.findByCity(city);
    }

    public List<Business> filterByCityAndCategories(String city, String categories) {
        return repository.findByCity(city);
    }

//    public String[] parse(String str) {
//        String[] tempArr = str.split(",");
//        String[] result = new String[tempArr.length];
//        for (int idx = 0; idx < tempArr.length; idx++) {
//            String tempStr = tempArr[idx];
//            String newTempStr = "";
//            for (int i = 0; i < tempStr.length(); i++) {
//                Character character = tempStr.charAt(i);
//                if (!character.equals(" ")) {
//                    newTempStr += character;
//                }
//            }
//
//            result[idx] = newTempStr;
//        }
//        return result;
//    }

}
