package com.example.nagoyameshi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.service.ReviewService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReviewControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ReviewService reviewService;

	@Test
	public void 未ログインの場合はレビュー一覧ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はレビュー一覧ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
		       .andExpect(status().isOk())
		       .andExpect(view().name("reviews/index"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合はレビュー一覧ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
		       .andExpect(status().isOk())
		       .andExpect(view().name("reviews/index"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー一覧ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
			   .andExpect(status().isForbidden());
	}
	
	@Test
	public void 未ログインの場合はレビュー投稿ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はレビュー投稿ページから有料プラン登録ページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
			   .andExpect(status().is3xxRedirection())
		       .andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合はレビュー投稿ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
		       .andExpect(status().isOk())
		       .andExpect(view().name("reviews/register"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー投稿ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
			   .andExpect(status().isForbidden());
	}
}
