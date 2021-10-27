package com.cs.yelp_project.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private CategoryRepository repository;

    @Autowired
    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> getAllCategories () {
        return repository.findAll();
    }

    public void saveAll(List<Category> list) {
        repository.saveAll(list);
    }

    public Category findCategoryByName(String categoryName) {
        Optional<Category> foundCategory = repository.findByName(categoryName);

        if (foundCategory.isPresent()) {
            return foundCategory.get();
        }

        return null;
    }

    public void save(Category category) {
        repository.save(category);
    }
}
