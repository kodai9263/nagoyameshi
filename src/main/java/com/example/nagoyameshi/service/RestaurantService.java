package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;

@Service
public class RestaurantService {
	private final RestaurantRepository restaurantRepository;
	private final CategoryRestaurantService categoryRestaurantService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;
	
	public RestaurantService(RestaurantRepository restaurantRepository, CategoryRestaurantService categoryRestaurantService,RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.restaurantRepository = restaurantRepository;
		this.categoryRestaurantService = categoryRestaurantService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService;
	}
	
	// すべての店舗をページングされた状態で取得する。
	public Page<Restaurant> findAllRestaurants(Pageable pageable) {
		return restaurantRepository.findAll(pageable);
	}
	
	// すべての店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する。①
	public Page<Restaurant> findAllRestaurantsByOrderByCreatedAtDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByCreatedAtDesc(pageable);
	}
	
	// すべての店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する ②
	public Page<Restaurant> findAllRestaurantsByOrderByLowestPriceAsc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
	}

	// 指定されたキーワードを店舗名に含む店舗を、ページングされた状態で取得する。
	public Page<Restaurant> findRestaurantsByNameLike(String nameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLike("%" + nameKeyword + "%", pageable);
	}
	
	// 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する ③
	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, Category category, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc("%" + nameKeyword + "%", "%" + addressKeyword + "%", category, pageable);
	}
	
	// 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する。④
	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(String nameKeyword, String addressKeyword, Category category, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc("%" + nameKeyword + "%", "%" + addressKeyword + "%", category, pageable);
	}
	
	// 指定されたidのカテゴリが設定された店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する。⑤
	public Page<Restaurant> findRestaurantsByCategoryIdOrderByCreatedAtDesc(Category category, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByCreatedAtDesc(category, pageable);
	}
	
	// 指定されたidのカテゴリが設定された店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する。 ⑥
	public Page<Restaurant> findRestaurantsByCategoryIdOrderByLowestPriceAsc(Category category, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByLowestPriceAsc(category, pageable);
	}
	
	// 指定された最低価格以下の店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する。 ⑦
	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer lowestPrice, Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByCreatedAtDesc(lowestPrice, pageable);
	}
	
	// 指定された最低価格以下の店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する。 ⑧
	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer lowestPrice, Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(lowestPrice, pageable);
	}
	
	// 指定したidを持つ店舗を取得する。
	public Optional<Restaurant> findRestaurantById(Integer id) {
		return restaurantRepository.findById(id);
	}
	
	// 店舗のレコード数を取得する。
	public long countRestaurants() {
		return restaurantRepository.count();
	}
	
	// idが最も大きい店舗を取得する。
	public Restaurant findFirstRestaurantByIdDesc() {
		return restaurantRepository.findFirstByOrderByIdDesc();
	}
	
	// フォームから送信された店舗情報をデータベースに登録する。
	@Transactional
	public void createRestaurant(RestaurantRegisterForm restaurantRegisterForm) {
		Restaurant restaurant = new Restaurant();
		MultipartFile imageFile = restaurantRegisterForm.getImageFile();
		List<Integer> categoryIds = restaurantRegisterForm.getCategoryIds();
		List<Integer> restaurantIds = restaurantRegisterForm.getRegularHolidayIds();
		
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImageName);
		}
		
		restaurant.setName(restaurantRegisterForm.getName());
		restaurant.setDescription(restaurantRegisterForm.getDescription());
		restaurant.setLowestPrice(restaurantRegisterForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantRegisterForm.getHighestPrice());
		restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());
		restaurant.setAddress(restaurantRegisterForm.getAddress());
		restaurant.setOpeningTime(restaurantRegisterForm.getOpeningTime());
		restaurant.setClosingTime(restaurantRegisterForm.getClosingTime());
		restaurant.setSeatingCapacity(restaurantRegisterForm.getSeatingCapacity());
		
		restaurantRepository.save(restaurant);
		
		if (categoryIds != null) {
			categoryRestaurantService.createCategoriesRestaurants(categoryIds, restaurant);
		}
		
		if (restaurantIds != null) {
			regularHolidayRestaurantService.createRegularHolidaysRestaurants(restaurantIds, restaurant);
		}
	}
	
	// フォームから送信された店舗情報でデータベースを更新する。
	@Transactional
	public void updateRestaurant(RestaurantEditForm restaurantEditForm, Restaurant restaurant) {
		MultipartFile imageFile = restaurantEditForm.getImageFile();
		List<Integer> categoryIds = restaurantEditForm.getCategoryIds();
		List<Integer> restaurantIds = restaurantEditForm.getRegularHolidayIds();
		
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImageName);
		}
		
		restaurant.setName(restaurantEditForm.getName());
		restaurant.setDescription(restaurantEditForm.getDescription());
		restaurant.setLowestPrice(restaurantEditForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantEditForm.getHighestPrice());
		restaurant.setPostalCode(restaurantEditForm.getPostalCode());
		restaurant.setAddress(restaurantEditForm.getAddress());
		restaurant.setOpeningTime(restaurantEditForm.getOpeningTime());
		restaurant.setClosingTime(restaurantEditForm.getClosingTime());
		restaurant.setSeatingCapacity(restaurantEditForm.getSeatingCapacity());
		
		restaurantRepository.save(restaurant);
		categoryRestaurantService.syncCategoriesWithRestaurant(categoryIds, restaurant);
		regularHolidayRestaurantService.syncRegularHolidaysRestaurants(restaurantIds, restaurant);
	}
	
	// 指定した店舗をデータベースから削除する。
	@Transactional
	public void deleteRestaurant(Restaurant restaurant) {
		restaurantRepository.delete(restaurant);
	}
	
	// 	UUIDを使って生成したファイル名を返す。
	public String generateNewFileName(String fileName) {
		String [] fileNames = fileName.split("\\.");
		
		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}
		
		String hashedImageName = String.join(".", fileNames);
		return hashedImageName;
	}
	
	// 画像ファイルを指定したファイルにコピーする。
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 価格が正しく設定されているかどうか（最高価格が最低価格以上かどうか）をチェックする。
	public boolean isValidPrice(Integer lowestPrice, Integer highestPrice) {
		return highestPrice >= lowestPrice;
	}
	
	// 営業時間が正しく設定されているかどうか（閉店時間が開店時間よりも後かどうか）をチェックする。
	public boolean isValidBusinessHours(LocalTime openingTime, LocalTime closingTime) {
		return closingTime.isAfter(openingTime);
	}
}
