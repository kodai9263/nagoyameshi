package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@Service
public class CategoryRestaurantService {
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final CategoryService categoryService;
	
	public CategoryRestaurantService(CategoryRestaurantRepository categoryRestaurantRepository, CategoryService categoryService) {
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.categoryService = categoryService;
	}

	public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant) {
		return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}

	@Transactional
	public void createCategoriesRestaurants(List<Integer> categoryIds, Restaurant restaurant) {
		// カテゴリIDのリストをループして処理を行う
		for (Integer categoryId : categoryIds) {
			// カテゴリIDがnullでない場合の処理
			if (categoryId != null) {
				// カテゴリIDに対応するCategoryエンティティをデータベースから取得
				Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);
				// optionalCategoryが値を持っているかを確認 データベースに登録されているか
				if (optionalCategory.isPresent()) {
					// カテゴリが存在する場合、そのカテゴリを取得
					Category category = optionalCategory.get();
					// 店舗とカテゴリの組み合わせがすでに存在するか確認
					Optional<CategoryRestaurant> existingCategoryRestaurant = categoryRestaurantRepository.findByCategoryAndRestaurant(category, restaurant);
					// 存在しない場合、新しくCategoryRestaurantエンティティを作成して保存
					if (existingCategoryRestaurant.isEmpty()) {
						CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
						categoryRestaurant.setCategory(category);
						categoryRestaurant.setRestaurant(restaurant);
						categoryRestaurantRepository.save(categoryRestaurant);
					}
				}		
			}			
		}				
	}
	
	@Transactional
	public void syncCategoriesWithRestaurant(List<Integer> categoryIds, Restaurant restaurant) {
		// 現在の店舗に関連付けられているすべてのカテゴリを取得
		List<CategoryRestaurant> existingCategoryRestaurants = categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant);

		// フォームから送信されたカテゴリIDのリストがnullの場合、すべての紐づけを解除
		if (categoryIds == null) {
			for (CategoryRestaurant categoryrestaurant : existingCategoryRestaurants) {
				categoryRestaurantRepository.delete(categoryrestaurant);
			}
		} else {
			// 既存の紐づけがフォームから送信されたカテゴリIDのリストに含まれていない場合、その紐づけを解除
			for (CategoryRestaurant categoryrestaurant : existingCategoryRestaurants) {
				if (!categoryIds.contains(categoryrestaurant.getCategory().getId())) {
					categoryRestaurantRepository.delete(categoryrestaurant);
				}
			}
		}		
		// フォームから送信されたカテゴリIDが既存の紐づけに存在しない場合、新しく紐づけを行う
		for (Integer categoryId : categoryIds) {
			// カテゴリIDがnullでない場合の処理
			if (categoryId != null) {
				// カテゴリIDに対応するCategoryエンティティをデータベースから取得
				Optional <Category> optionalCategory = categoryService.findCategoryById(categoryId);
				// optionalCategoryが値を持っているかを確認 データベースに登録されているか
				if (optionalCategory.isPresent()) {
					// カテゴリが存在する場合、そのカテゴリを取得
					Category category = optionalCategory.get();
					// 店舗とカテゴリの組み合わせがすでに存在するか確認
					Optional<CategoryRestaurant> existingCategoryRestaurant = categoryRestaurantRepository.findByCategoryAndRestaurant(category, restaurant);
					// 存在しない場合、新しくCategoryRestaurantエンティティを作成して保存
					if (existingCategoryRestaurant.isEmpty()) {
						CategoryRestaurant categpryRestaurant = new CategoryRestaurant();
						categpryRestaurant.setCategory(category);
						categpryRestaurant.setRestaurant(restaurant);
						categoryRestaurantRepository.save(categpryRestaurant);
					}
				}
			}
		}		
	}
}