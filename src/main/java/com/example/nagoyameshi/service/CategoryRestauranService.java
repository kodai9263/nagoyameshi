package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@Service
public class CategoryRestauranService {
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	
	public CategoryRestauranService(CategoryRestaurantRepository categoryRestaurantRepository) {
		this.categoryRestaurantRepository = categoryRestaurantRepository;
	}

	public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant) {
		return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}
}

public void createCategoriesRestaurants(List<Integer> categoryIds, Restaurant restaurant) {
	// カテゴリIDのリストをループして処理を行う
	for (Integer categoryId : categoryIds) {
		// カテゴリIDがnullでない場合の処理
		if (categoryIds != null) {
			// カテゴリIDに対応するCategoryエンティティをデータベースから取得
			Optional<Category> category = 
		}
	}
}