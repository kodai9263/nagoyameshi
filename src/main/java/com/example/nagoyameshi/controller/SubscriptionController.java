package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	
	private static final String PRICE_ID = "price_1R8xpKCIAT0CWN6PeREg71Tv";
	
	private final StripeService stripeService;
	private final UserService userService;
	
	public SubscriptionController(StripeService stripeService, UserService userService) {
		this.stripeService = stripeService;
		this.userService = userService;
	}

	@GetMapping("/register")
	public String register() {
		return "subscription/register";
	}
	
	@PostMapping("/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 @RequestParam(name = "paymentMethodId", required = false) String paymentMethodId,
						 RedirectAttributes redirectAttributes)
	{
		try {
			User user = userDetailsImpl.getUser();
			String customerId = user.getStripeCustomerId();
			
			if (customerId == null) {
				// 顧客（Stripeの Customerオブジェクト）を作成する
				Customer customer = stripeService.createCustomer(user);
				customerId = customer.getId();
				// stripeCustomerIdフィールドに顧客IDを保存する
				userService.saveStripeCustomerId(user, customerId);
			}
			
			// フォームから送信された支払い方法（StripeのPaymentMethodオブジェクト）を顧客に紐づける
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, customerId);
			
			// フォームから送信された支払い方法を顧客のデフォルトの支払い方法に設定する
			stripeService.setDefaultPaymentMethod(paymentMethodId, customerId);
			
			// サブスクリプション（Stripeの Subscriptionオブジェクト）を作成する
			stripeService.createSubscription(customerId, PRICE_ID);
			
			// ユーザーのロールを更新する
			userService.updateRole(user, "ROLE_PAID_MEMBER");
			userService.refreshAuthenticationByRole("ROLE_PAID_MEMBER");
			
			redirectAttributes.addFlashAttribute("successMessage", "有料プランへの登録が完了しました。");
		} catch (StripeException e){
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
		}
		return "redirect:/";
	}
	
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
					   RedirectAttributes redirectAttributes,
					   Model model)
	{
		try {
			User user = userDetailsImpl.getUser();
			String customerId = user.getStripeCustomerId();
			// 顧客のデフォルトの支払い方法（Stripeの PaymentMethodオブジェクト）を取得する
			PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethod(customerId);
			PaymentMethod.Card card = paymentMethod.getCard();
			PaymentMethod.BillingDetails cardHolderName = paymentMethod.getBillingDetails();
			
			model.addAttribute("card", card);
			model.addAttribute("cardHolderName", cardHolderName);
			
			return "subscription/edit";
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法を取得できませんでした。再度お試しください。");
		}
		return "redirect:/";
	}
	
	@PostMapping("/update")
	public String update(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 @RequestParam(name = "paymentMethodId", required = false) String paymentMethodId,
						 RedirectAttributes redirectAttributes)
	{
		try {
			User user = userDetailsImpl.getUser();
			String customerId = user.getStripeCustomerId();
			// 現在のデフォルトの支払い方法（Stripeの PaymentMethodオブジェクト）のIDを取得する
			String defaltPaymentMethodId = stripeService.getDefaultPaymentMethodId(customerId);
			
			// フォームから送信された支払い方法を顧客（StripeのCustomerオブジェクト）に紐づける
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, customerId);
			
			// フォームから送信された支払い方法を顧客のデフォルトの支払い方法に設定する
			stripeService.setDefaultPaymentMethod(paymentMethodId, customerId);
			
			// defaltPaymentMethodIdと顧客の紐づけを解除する
			stripeService.detachPaymentMethodFromCustomer(defaltPaymentMethodId);
			
			redirectAttributes.addFlashAttribute("successMessage", "お支払い方法を変更しました。");
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法の変更に失敗しました。再度お試しください。");
		}
		return "redirect:/";
	}
	
	@GetMapping("/cancel")
	public String cancel() {
		return "subscription/cancel";
	}
	
	@PostMapping("/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes)
	{
		try {
			User user = userDetailsImpl.getUser();
			String customerId = user.getStripeCustomerId();
			// 顧客が契約中のサブスクリプション（Stripeの Subscriptionオブジェクト）を取得する
			List<Subscription> subscriptionList = stripeService.getSubscriptions(customerId);
			// 顧客が契約中のサブスクリプションをキャンセルする
			stripeService.cancelSubscriptions(subscriptionList);
			
			// デフォルトの支払い方法（Stripeの PaymentMethodオブジェクト）のIDを取得する
			String defaltPaymentMethod = stripeService.getDefaultPaymentMethodId(customerId);
			
			// paymentMethodと顧客の紐づけを解除する
			stripeService.detachPaymentMethodFromCustomer(defaltPaymentMethod);
			
			// ユーザーのロールを更新する
			userService.updateRole(user, "ROLE_FREE_MEMBER");
			userService.refreshAuthenticationByRole("ROLE_FREE_MEMBER");
			
			redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランの解約に失敗しました。再度お試しください。");
		}
		return "redirect:/";
	}
}
