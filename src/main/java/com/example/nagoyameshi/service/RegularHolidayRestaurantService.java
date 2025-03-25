package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;

@Service
public class RegularHolidayRestaurantService {
	private final RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;
	private final RegularHolidayService regularHolidayService;
	
	public RegularHolidayRestaurantService(RegularHolidayRestaurantRepository regularHolidayRestaurantRepository, RegularHolidayService regularHolidayService) {
		this.regularHolidayRestaurantRepository = regularHolidayRestaurantRepository;
		this.regularHolidayService = regularHolidayService;
	}
	
	// 指定した店舗の定休日のid（RegularHolidayエンティティのid）をリスト形式で取得する。
	public List<Integer> findRegularHolidayIdsByRestaurant(Restaurant restaurant) {
		return regularHolidayRestaurantRepository.findRegularHolidayIdsByRestaurant(restaurant);
	}

	// フォームから送信された定休日のidリストをもとに、regular_holiday_restaurantテーブルにデータを登録する。
	@Transactional
	public void createRegularHolidaysRestaurants(List<Integer> restaurantHolidayIds, Restaurant restaurant) {
		for (Integer restaurantHolidayId : restaurantHolidayIds) {
			if (restaurantHolidayId != null) {
				Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService.findRegularHolidayById(restaurantHolidayId);
				
				if (optionalRegularHoliday.isPresent()) {
					RegularHoliday regularHoliday =  optionalRegularHoliday.get();
					Optional<RegularHolidayRestaurant> exstingRegularHolidayRestaurant = regularHolidayRestaurantRepository.findByRegularHolidayAndRestaurant(regularHoliday, restaurant);
					
					if (exstingRegularHolidayRestaurant.isEmpty()) {
						RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
						regularHolidayRestaurant.setRegularHoliday(regularHoliday);
						regularHolidayRestaurant.setRestaurant(restaurant);
						regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
					}
				}
			}
		}
	}
	
	// フォームから送信された定休日のidリストをもとに、regular_holiday_restaurantテーブルのデータを同期する。
	@Transactional
	public void syncRegularHolidaysRestaurants(List<Integer> restaurantHolidayIds, Restaurant restaurant) {
		
		List<RegularHolidayRestaurant> exstingRegularHolidatRestaurant = regularHolidayRestaurantRepository.findByRestaurant(restaurant);
	
		if (restaurantHolidayIds == null) {
			for (RegularHolidayRestaurant regularHolidayRestaurant : exstingRegularHolidatRestaurant) {
				regularHolidayRestaurantRepository.delete(regularHolidayRestaurant);
			}
		} else {
			for (RegularHolidayRestaurant regularHolidayRestaurant : exstingRegularHolidatRestaurant) {
				if (!restaurantHolidayIds.contains(regularHolidayRestaurant.getRegularHoliday().getId())) {
					regularHolidayRestaurantRepository.delete(regularHolidayRestaurant);
				}
			}
		}
		for (Integer restaurantHolidayId : restaurantHolidayIds) {
			if (restaurantHolidayId != null) {
				Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService.findRegularHolidayById(restaurantHolidayId);
				
				if (optionalRegularHoliday.isPresent()) {
					RegularHoliday regularHoliday = optionalRegularHoliday.get();
					Optional<RegularHolidayRestaurant> exstingRegularHolidayRestaurant = regularHolidayRestaurantRepository.findByRegularHolidayAndRestaurant(regularHoliday, restaurant);
					
					if (exstingRegularHolidayRestaurant.isEmpty()) {
						RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
						regularHolidayRestaurant.setRegularHoliday(regularHoliday);
						regularHolidayRestaurant.setRestaurant(restaurant);
						regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
					}
				}
			}
		}
	}
}
