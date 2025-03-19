package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/admin/restaurants")

public class AdminRestaurantController {
	private final RestaurantService restaurantService;
	
	public AdminRestaurantController(RestaurantService restaurantService) {
		this.restaurantService = restaurantService;
	}
	
	// 管理者用の店舗一覧ページ
	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
						@RequestParam(name = "keyword", required = false) String keyword,
						Model model)
	{
		Page<Restaurant> restaurantPage;
		
		if (keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantService.findRestaurantsByNameLike(keyword, pageable);
		} else {
			restaurantPage = restaurantService.findAllRestaurants(pageable);
		}
		
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);
		return "admin/restaurants/index";
	}
	
	// 管理者用の店舗詳細ページ
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes,
					   Model model)
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/admin/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		model.addAttribute("restaurant", restaurant);
		
		return "admin/restaurants/show";
	}
	
	// 管理者用の店舗登録ページ
	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		return "admin/restaurants/register";
	}

	// フォームから送信された店舗情報をデータベースに登録する。
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm,
						 BindingResult bindingResult,
						 RedirectAttributes redirectAttributes,
						 Model model)
	{
		
		if(restaurantRegisterForm.getLowestPrice() != null && restaurantRegisterForm.getHighestPrice() != null && !restaurantService.isValidPrice(restaurantRegisterForm.getLowestPrice(), restaurantRegisterForm.getHighestPrice())) {
			FieldError lowestError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最高価格以下の金額を入力してください。");
			FieldError highestError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最低価格以下の金額を入力してください。");
			bindingResult.addError(lowestError);
			bindingResult.addError(highestError);
		}
		
		if(restaurantRegisterForm.getOpeningTime() != null && restaurantRegisterForm.getClosingTime() != null && !restaurantService.isValidBusinessHours(restaurantRegisterForm.getOpeningTime(), restaurantRegisterForm.getClosingTime())) {
			FieldError opningTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "閉店時間前の時間を入力してください。");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "開店時間後の時間を入力してください。");
			bindingResult.addError(opningTimeError);
			bindingResult.addError(closingTimeError);
			
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("restaurantRegisterForm", restaurantRegisterForm);
			return  "admin/restaurants/register";
		}
		
		restaurantService.createRestaurant(restaurantRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");
		return "redirect:/admin/restaurants";
	}
	
	// 管理者用の店舗編集ページ
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id,
					   RedirectAttributes redirectAttributes,
					   Model model)
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/admin/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getName(), null, restaurant.getDescription(), restaurant.getLowestPrice(), restaurant.getHighestPrice(),
																   restaurant.getPostalCode(), restaurant.getAddress(), restaurant.getOpeningTime(), restaurant.getClosingTime(), restaurant.getSeatingCapacity());
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		return "admin/restaurants/edit";
	}
	
	// フォームから送信された店舗情報でデータベースを更新する。
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated RestaurantEditForm restaurantEditForm,
						 @PathVariable(name = "id") Integer id,
						 BindingResult bindingResult,
						 RedirectAttributes redirectAttributes,
						 Model model)
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/admin/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		
		if(restaurantEditForm.getLowestPrice() != null && restaurantEditForm.getHighestPrice() != null && !restaurantService.isValidPrice(restaurantEditForm.getLowestPrice(), restaurantEditForm.getHighestPrice())) {
			FieldError lowestError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最高価格以下の金額を入力してください。");
			FieldError highestError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最低価格以下の金額を入力してください。");
			bindingResult.addError(lowestError);
			bindingResult.addError(highestError);
		}
		
		if(restaurantEditForm.getOpeningTime() != null && restaurantEditForm.getClosingTime() != null && !restaurantService.isValidBusinessHours(restaurantEditForm.getOpeningTime(), restaurantEditForm.getClosingTime())) {
			FieldError opningTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "閉店時間前の時間を入力してください。");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "開店時間後の時間を入力してください。");
			bindingResult.addError(opningTimeError);
			bindingResult.addError(closingTimeError);
			
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("restaurantEditForm", restaurantEditForm);
			return  "admin/restaurants/register";
		}
		
		restaurantService.updateRestaurant(restaurantEditForm, restaurant);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を編集しました。");
		return "redirect:/admin/restaurants";
	}
	
	// 店舗をデータベースから削除する。
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id,
						 RedirectAttributes redirectAttributes)
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/admin/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		restaurantService.deleteRestaurant(restaurant);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");
		return "redirect:/admin/restaurants";
	}
}
