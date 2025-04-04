package com.example.nagoyameshi.service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;

@Service
public class StripeService {
	
	// 依存性の注入後に一度だけ実行するメソッド
	@PostConstruct
	private void init() { 
		// Stripeのシークレットキーを設定する
		Stripe.apiKey = "sk_test_51QvWXqCIAT0CWN6P3Q5VP6Exolge3dMhTph21gV05CZC0GfUKm66ZqHqD8GEiYQkjp6MAqaUeZCWOIi9JhELGkMM00HhmksbZF";
	}

	// 顧客（StripeのCustomerオブジェクト）を作成する。
	public Customer createCustomer(User user) throws StripeException {
		// CustomerCreateParamsオブジェクトの作成
		CustomerCreateParams params = CustomerCreateParams.builder()
										.setName(user.getName())
										.setEmail(user.getEmail())
										.build();
		
		// Customerオブジェクトを作成し戻り値として返す。
		return Customer.create(params);
	}
	
	/** 支払い方法（StripeのPaymentMethodオブジェクト）を顧客（StripeのCustomerオブジェクト）に紐づける。
	 * @param paymentMethodId 支払い方法ID
	 * @param customerId　顧客ID
	 */
	public void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
		// 顧客IDを設定した PaymentMethodAttachParamsオブジェクトを作成する
		PaymentMethodAttachParams params = PaymentMethodAttachParams.builder().setCustomer(customerId).build();
		// 支払い方法のIDをもとに PaymentMethodオブジェクトを取得する
		PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
		// PaymentMethodオブジェクトを顧客に紐づける
		paymentMethod.attach(params);
	}
	
	/** 顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）を設定する
	 * @param paymentMethodId 支払い方法ID
	 * @param customerId　顧客ID
	 */
	public void setDefaultPaymentMethod(String paymentMethodId, String customerId) throws StripeException {
		// 支払い方法のIDをデフォルトの支払い方法として設定した CustomerUpdateParamsオブジェクトを作成する
		CustomerUpdateParams params = CustomerUpdateParams.builder()
										.setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
												.setDefaultPaymentMethod(paymentMethodId).build()).build();
		// 顧客IDをもとに Customerオブジェクトを取得する
		Customer customer = Customer.retrieve(customerId);
		// Customerオブジェクトを更新する
		customer.update(params);
	}
	
	/** サブスクリプション（StripeのSubscriptionオブジェクト）を作成する。
	 * @param customerId 顧客ID
	 * @param priceId 価格ID
	 */
	public Subscription createSubscription(String customerId, String priceId) throws StripeException {
		// 顧客IDと価格IDを設定した SubscriptionCreateParamsオブジェクトを作成する
		SubscriptionCreateParams params = SubscriptionCreateParams.builder()
											.setCustomer(customerId)
											.addItem(
												SubscriptionCreateParams.Item.builder()
													.setPrice(priceId)
													.build()).build();
		// Subscriptionオブジェクトを作成し、戻り値として返す
		return Subscription.create(params);
	}
	
	/** 顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）を取得する。
	 * @param customerId 顧客ID
	 */
	public PaymentMethod getDefaultPaymentMethod(String customerId) throws StripeException {
		// 顧客IDをもとに Customerオブジェクトを取得する
		Customer customer = Customer.retrieve(customerId);
		// Customerオブジェクトからデフォルトの支払い方法のIDを取得する
		String defaultPayment = customer.getInvoiceSettings().getDefaultPaymentMethod();
		// デフォルトの支払い方法のIDをもとに PaymentMethodオブジェクトを取得し、戻り値として返す
		return PaymentMethod.retrieve(defaultPayment);
	}
	
	/** 顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）のIDを取得する。
	 *  @param customerId 顧客ID
	 */
	public String getDefaultPaymentMethodId(String customerId) throws StripeException {
		// 顧客IDをもとに Customerオブジェクトを取得する
		Customer customer = Customer.retrieve(customerId);
		// Customerオブジェクトからデフォルトの支払い方法のIDを取得し、戻り値として返す
		return customer.getInvoiceSettings().getDefaultPaymentMethod();
	}
	
	/** 支払い方法（StripeのPaymentMethodオブジェクト）と顧客（StripeのCustomerオブジェクト）の紐づけを解除する。
	 * @param paymentMethodId 支払い方法ID
	 */
	public void detachPaymentMethodFromCustomer(String paymentMethodId) throws StripeException {
		// 支払い方法のIDをもとに PaymentMethodオブジェクトを取得する
		PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
		// PaymentMethodオブジェクトの紐づけを解除する
		paymentMethod.detach();
	}
	
	/** サブスクリプション（StripeのSubscriptionオブジェクト）を取得する。
	 * @param customerId 顧客ID
	 */
	public List<Subscription> getSubscriptions(String customerId) throws StripeException {
		// 顧客IDを設定した SubscriptionListParamsオブジェクトを作成する
		SubscriptionListParams params = SubscriptionListParams.builder()
											.setCustomer(customerId)
											.build();
		// Subscriptionオブジェクトのリストを取得し、戻り値として返す
		SubscriptionCollection subscriptionCollection = Subscription.list(params);
		List<Subscription> subscriptionList = subscriptionCollection.getData();
		return subscriptionList;
	}
	
	/** サブスクリプション（StripeのSubscriptionオブジェクト）をキャンセルする。
	 * @param subscriptionList Subscriptionオブジェクトのリスト
	 */
	public void cancelSubscriptions(List<Subscription> subscriptionList) throws StripeException {
		for (Subscription subscriptions : subscriptionList) {
			subscriptions.cancel();
		}
	}
}
