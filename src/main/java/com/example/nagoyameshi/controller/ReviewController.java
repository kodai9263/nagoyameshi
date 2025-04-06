package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.ReviewService;

@Controller
@RequestMapping("/restaurants/{restaurantId}/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final RestaurantService restaurantService;
	
	public ReviewController(ReviewService reviewService, RestaurantService restaurantService) {
		this.restaurantService = restaurantService;
		this.reviewService = reviewService;
	}
	
	@GetMapping
	public String index(@PathVariable("restaurantId") Integer id,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@PageableDefault(page = 0, size = 5, sort = "id", direction = Direction.ASC) Pageable pageable,
						RedirectAttributes redirectAttributes,
						Model model)
	{
		// 店舗の情報を取得する
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants/index";
		}
		Restaurant restaurant = optionalRestaurant.get();
		
		// 現在ログイン中のロール名を取得する
		User user = userDetailsImpl.getUser();
		Role role = user.getRole();
		String userRoleName = role.getName();
		
		// 
		Page<Review> reviewPage = reviewService.findReviewsByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
		if (userRoleName != "ROLE_PAID_MEMBER") {
			//TODO 有料会員でなければ最初の三件のみ取得したい
		}
		Boolean hasUserAlreadyReviewd = reviewService.hasUserAlreadyReviewed(restaurant, user);
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("userRoleName", userRoleName);
		model.addAttribute("reviewPage", reviewPage);
		model.addAttribute("hasUserAlreadyReviewd", hasUserAlreadyReviewd);
		
		return "reviews/index";
	}

}
