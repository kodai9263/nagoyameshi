package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class HomeController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	
	public HomeController(RestaurantService restaurantService, CategoryService categoryService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
	}
	
	@GetMapping("/")
	public String index(Model model) {
		Page<Restaurant> highlyRatedRestaurants = restaurantService.findAllRestaurantsByOrderByAverageScoreDesc(PageRequest.of(0, 6));
		Page<Restaurant> newRestaurants = restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(PageRequest.of(0, 6));
		Category washoku = categoryService.findFirstCtegoryByName("和食");
		Category udon = categoryService.findFirstCtegoryByName("うどん");
		Category don = categoryService.findFirstCtegoryByName("丼物");
		Category ramen = categoryService.findFirstCtegoryByName("ラーメン");
		Category oden = categoryService.findFirstCtegoryByName("おでん");
		Category fried = categoryService.findFirstCtegoryByName("揚げ物");
		List<Category> categories = categoryService.findAllCategories();
		
		model.addAttribute("highlyRatedRestaurants",highlyRatedRestaurants);
		model.addAttribute("newRestaurants", newRestaurants);
		model.addAttribute("washoku", washoku);
		model.addAttribute("udon", udon);
		model.addAttribute("don", don);
		model.addAttribute("ramen", ramen);
		model.addAttribute("oden", oden);
		model.addAttribute("fried", fried);
		model.addAttribute("categories", categories);
		
		return "index";
	}
}
