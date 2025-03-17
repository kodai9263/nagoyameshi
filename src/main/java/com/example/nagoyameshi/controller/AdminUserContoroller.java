package com.example.nagoyameshi.controller;

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

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserContoroller {
	private final UserService userService;
	
	public AdminUserContoroller(UserService userService) {
		this.userService = userService;
	}

	// 管理者用の会員一覧ページ
	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
						@RequestParam(name = "keyword", required = false) String keyword,
						Model model) 
	{
		Page<User> userPage;
		
		if(keyword != null && !keyword.isEmpty()) {
			userPage = userService.findUserByNameLikeOrFuriganaLike(keyword, keyword, pageable);
		} else {
			userPage = userService.findAllUser(pageable);
		}
		
		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);
		
		return "admin/users/index";
	}
	
	// 管理者用の会員詳細ページ
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes,
					  Model model)
	{
		Optional<User> optionalUser = userService.findUserId(id);
		
		if (optionalUser.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが存在しません。");
			
			return "redirect:/admin/users";
		}
		
		User user = optionalUser.get();
		model.addAttribute("user", user);
		
		return "admin/users/show";
	}
}
