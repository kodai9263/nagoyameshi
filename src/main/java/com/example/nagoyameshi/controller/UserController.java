package com.example.nagoyameshi.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	/**
	 * @param userDetailsImpl 認証されたユーザーの情報
	 * @param model　ビューに渡すモデル
	 * @return 会員情報ページ
	 */
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser();
		
		model.addAttribute("user", user);
		return "user/index";
	}

	/**
	 * @param userDetailsImpl 認証されたユーザーの情報
	 * @param model　ビューに渡すモデル
	 * @return 会員情報編集ページ
	 */
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser();
		
		UserEditForm userEditForm = new UserEditForm(user.getName(), 
													 user.getFurigana(),
													 user.getPostalCode(),
													 user.getAddress(),
													 user.getPhoneNumber(),
													 user.getBirthday() != null ? user.getBirthday().format(DateTimeFormatter.ofPattern("yyyyMMdd")) : null,
													 user.getOccupation(),
													 user.getEmail());
		
		model.addAttribute("userEditForm", userEditForm);
		return "user/edit";
	}
	
	/**
	 * 
	 * @param userEditForm 編集フォームのデーや
	 * @param bindingResult　バリデーションの結果
	 * @param userDetailsImpl　認証されたユーザーの詳細情報
	 * @param redirectAttributes　リダイレクト時に使用する属性
	 * @param model　ビューに渡すモデル
	 * @return　更新後の会員情報ページ
	 */
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated UserEditForm userEditForm,
						 BindingResult bindingResult,
						 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 RedirectAttributes redirectAttributes,
						 Model model) 
	{
		User user = userDetailsImpl.getUser();
		
		if (userService.isEmailChanged(userEditForm, user) && userService.isEmailRegistered(userEditForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("userEditForm", userEditForm);
			
			return "user/edit";
		}
		
		userService.updateUser(user, userEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
		return "redirect:/user";
	}
}
