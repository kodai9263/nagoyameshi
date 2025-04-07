package com.example.nagoyameshi.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/restaurants")
public class RestaurantController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	
	public RestaurantController(RestaurantService restaurantService, CategoryService categoryService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
	}

	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
						@RequestParam(name = "keyword", required = false) String keyword,
						@RequestParam(name ="categoryId", required = false) Integer categoryId,
						@RequestParam(name ="price", required = false) Integer price,
						@RequestParam(name ="order", required = false) String order,
						Model model)
	{
		Page<Restaurant> restaurantPage = null;
		List<Category> categories = categoryService.findAllCategories();
		
		/** TODO Repositoryで引数をcategoryで指定したため、引数の指定がうまくいきませんでした。
		 * 工夫としては、一旦categoryを定義するべきだと思い、restaurantServiceのメソッドを呼び出す前にcategoryを定義したらうまく動きました。
		 * 模範解答とは違った内容でしたが、この書き方でも大丈夫でしょうか？
		 */
		
		if (keyword != null && !keyword.isEmpty()) {
			if ( order != null && order.equals("lowestPriceAsc")) {
				Category category = categoryService.findFirstCtegoryByName(keyword);
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(keyword, keyword, category, pageable);
			} else {
				Category category = categoryService.findFirstCtegoryByName(keyword);
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(keyword, keyword, category, pageable);
			}
		} else if (categoryId != null) {
			Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);
			if (optionalCategory.isPresent()) {
				Category category = optionalCategory.get();
				if ( order != null && order.equals("lowestPriceAsc")) {
					restaurantPage = restaurantService.findRestaurantsByCategoryIdOrderByLowestPriceAsc(category, pageable);
				} else {
					restaurantPage = restaurantService.findRestaurantsByCategoryIdOrderByCreatedAtDesc(category, pageable);
				}
			}
		} else if (price != null) {
			if ( order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(price, pageable);
			} else {
				restaurantPage = restaurantService.findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}
		} else {
			if ( order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findAllRestaurantsByOrderByLowestPriceAsc(pageable);
			} else {
				restaurantPage = restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable);
			}
		}
		
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("categories", categories);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("price", price);
		model.addAttribute("order", order);
		
		return "restaurants/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,
					   RedirectAttributes redirectAttributes,
					   Model model)
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		model.addAttribute("restaurant", restaurant);
		
		return "restaurants/show";
	}
}