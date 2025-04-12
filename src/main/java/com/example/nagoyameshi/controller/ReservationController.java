package com.example.nagoyameshi.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@PreAuthorize("isAuthenticated()")
public class ReservationController {
	private final ReservationService reservationService;
	private final RestaurantService restaurantService;
	
	public ReservationController(ReservationService reservationService, RestaurantService restaurantService) {
		this.reservationService = reservationService;
		this.restaurantService = restaurantService;
	}

	// 予約一覧ページ
	@GetMapping("/reservations")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
					    @PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
					    RedirectAttributes redirectAttributes,
					    Model model)
	{
		User user = userDetailsImpl.getUser();
		Role role = user.getRole();
		String userRoleName = role.getName();
		
		Page<Reservation> reservationPage;
		// 有料会員かどうか
		if (userRoleName.equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		} else {
			reservationPage = reservationService.findReservationsByUserOrderByReservedDatetimeDesc(user, pageable);
		}
		
		// 現在の日時
		LocalDateTime currentDateTime = LocalDateTime.now();
		
		model.addAttribute("reservationPage", reservationPage);
		model.addAttribute("currentDateTime", currentDateTime);
		
		return "reservations/index";
	}
	
	// 予約ページ
	@GetMapping("/restaurants/{restaurantId}/reservations/register")
	public String register(@PathVariable(name = "restaurantId") Integer restaurantId,
						   @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						   RedirectAttributes redirectAttributes,
						   Model model)
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants";
		}
		Restaurant restaurant = optionalRestaurant.get();
		
		User user = userDetailsImpl.getUser();
		Role role = user.getRole();
		String userRoleName = role.getName();
		
		if (userRoleName.equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		// 店舗の定休日のday_indexフィールドの値のリスト
		List<Integer> restaurantRegularHolidays = restaurantService.findDayIndexesByRestaurantId(restaurantId);
		
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm();
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantRegularHolidays", restaurantRegularHolidays);
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		
		return "reservations/register";
	}
	
	// フォームから送信された予約情報をデータベースに登録する。
	@PostMapping("/restaurants/{restaurantId}/reservations/create")
	public String create(@PathVariable(name = "restaurantId") Integer restaurantId,
			   			 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			   			 @ModelAttribute @Validated ReservationRegisterForm reservationRegisterForm,
			   			 BindingResult bindingResult,
			   			 RedirectAttributes redirectAttributes,
			   			 Model model)
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants";
		}
		Restaurant restaurant = optionalRestaurant.get();
		
		User user = userDetailsImpl.getUser();
		Role role = user.getRole();
		String userRoleName = role.getName();
		
		if (userRoleName.equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		// 予約時間が２時間後かどうか
		LocalDateTime reservedDateTime = LocalDateTime.of(reservationRegisterForm.getReservationDate(), reservationRegisterForm.getReservationTime());
		
		if (!reservationService.isAtLeastTwoHoursInFuture(reservedDateTime)) {
			FieldError hourError = new FieldError(bindingResult.getObjectName(), "hourError", "予約時間を現在から２時間以上後にしてください。");
			bindingResult.addError(hourError);
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("reservationRegisterForm", reservationRegisterForm);
			return "redirect:/restaurants/{restaurantId}/reservations/register";
		}
		
		reservationService.createReservation(reservationRegisterForm, restaurant, user);
		redirectAttributes.addFlashAttribute("successMessage", "予約が完了しました。");
		return "redirect:/reservations";
	}
	
	
	// 予約をデータベースから削除する。
	@PostMapping("/reservations/{reservationId}/delete")
	public String delete(@PathVariable(name = "reservationId") Integer reservationId,
  			 			 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
  			 			 RedirectAttributes redirectAttributes)
	
	{
		User user = userDetailsImpl.getUser();
		Role role = user.getRole();
		String userRoleName = role.getName();
		
		if (userRoleName.equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		Optional<Reservation> optionalReservation = reservationService.findReservationById(reservationId);
		if(optionalReservation.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "予約が存在しません。");
			return "redirect:/reservations";
		}
		Reservation reservation = optionalReservation.get();
		
		if (!reservation.getRestaurant().getId().equals(reservationId)) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			return "redirect:/reservations";
		}
		
		reservationService.deleteReservation(reservation);
		redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");
		return "redirect:/reservations";
	}
}
