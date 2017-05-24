/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package ti.paypal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ProofOfPayment;

// https://github.com/luis1987/PayPalAppcelerator/blob/master/android/src/com/bea/paypal/ModulopaypalModule.java
// example :https://github.com/paypal/PayPal-Android-SDK/blob/master/SampleApp/src/main/java/com/paypal/example/paypalandroidsdkexample/SampleActivity.java

@Kroll.proxy(creatableInModule = PaypalModule.class)
public class PaymentProxy extends KrollProxy {
	private final class PaymentResultHandler implements TiActivityResultHandler {
		public void onError(Activity arg0, int arg1, Exception e) {
			Log.e(LCAT, e.getMessage());
		}

		public void onResult(Activity dummy, int requestCode, int resultCode,
				Intent data) {

			log("if you see this on console, then the paypal overlay has anwsered");
			log(" Answer from PayPal: " + requestCode + "   resultCode="
					+ resultCode);
			if (requestCode == PaymentProxy.REQUEST_CODE_PAYMENT) {
				log("REQUEST_CODE_PAYMENT");
				if (resultCode == Activity.RESULT_OK) {
					log("RESULT_OK");
					PaymentConfirmation confirm = data
							.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
					if (confirm != null) {
						log("RESULT_CONFIRMATION");

						if (hasListeners("paymentDidComplete")) {
							ProofOfPayment proofOfPayment = confirm
									.getProofOfPayment();

							KrollDict client = new KrollDict();
							KrollDict response = new KrollDict();
							KrollDict event = new KrollDict();
							KrollDict payment = new KrollDict();

							client.put("environment", confirm.getEnvironment());
							client.put("platform", "Android");
							client.put("product_name", "PayPal Android SDK");

							response.put("create_time",
									proofOfPayment.getCreateTime());
							response.put("intent", proofOfPayment.getIntent());
							response.put("order_id",
									proofOfPayment.getTransactionId());
							response.put("state", proofOfPayment.getState());
							response.put("id", proofOfPayment.getPaymentId());
							payment.put("client", client);
							payment.put("response", response);
							event.put("payment", payment);
							fireEvent("paymentDidComplete", event);
						}

					} else {
						log("no RESULT_CONFIRMATION");
					}
				} else if (resultCode == Activity.RESULT_CANCELED) {
					log("RESULT_CANCELED");
					if (hasListeners("paymentDidCancel")) {
						log("paymentDidCancel");
						KrollDict event = new KrollDict();
						event.put("success", false);
						fireEvent("paymentDidCancel", event);
					}
				} else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
					log("RESULT_EXTRAS_INVALID");
				}
			} else if (requestCode == REQUEST_CODE_FUTUREPAYMENT) {
				if (resultCode == Activity.RESULT_OK) {
					PayPalAuthorization auth = data
							.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
					if (auth != null) {
						try {
							log(auth.toJSONObject().toString(4));
							String authorization_code = auth
									.getAuthorizationCode();
							log(authorization_code);
							sendAuthorizationToServer(auth);
						} catch (JSONException e) {
							log("an extremely unlikely failure occurred: " + e);
						}
					}
				} else if (resultCode == Activity.RESULT_CANCELED) {
					log("The user canceled.");
					/*
					 * if (hasListeners("paymentDidCancel")) { KrollDict event =
					 * new KrollDict(); event.put("success", false);
					 * fireEvent("paymentDidCancel", event); }
					 */
				} else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
					log("Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
				}
			}
		}
	}

	// Standard Debugging variables
	private static final String LCAT = "PayPalProxy 💰💰";
	String currencyCode, shortDescription, clientId;
	int intentMode, debug;
	boolean futurePayment = false;
	BigDecimal amount = new BigDecimal(0.0), shipping = new BigDecimal(0.0),
			tax = new BigDecimal(0.0);
	public static final int REQUEST_CODE_PAYMENT = 1,
			REQUEST_CODE_FUTUREPAYMENT = 2;
	List<PaymentItem> paymentItems;

	public PaymentProxy() {
		super();
		debug = PaypalModule.debug;

	}

	private void log(String msg) {
		if (this.debug > 1) {
			Log.d(LCAT, msg);
		}
	}

	/*
	 * this method (called by JS level) opens the billing layer
	 */

	@Kroll.method
	public void show() {
		showPaymentOverLay();
	}

	@Kroll.method
	public void showPaymentOverLay() {
		Context context = TiApplication.getInstance().getApplicationContext();
		// Activity a = TiApplication.getAppRootOrCurrentActivity();
		final Intent intent = new Intent(context, PaymentActivity.class);
		log("start opening paypal billing layer");

		if (futurePayment == false) {
			log("standard payment (no futurePayment) with intentMode="
					+ intentMode);
			PayPalPayment thingsToBuy = null;

			if (intentMode == PaypalModule.PAYMENT_INTENT_SALE) {
				thingsToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
			} else if (intentMode == PaypalModule.PAYMENT_INTENT_AUTHORIZE) {
				thingsToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_AUTHORIZE);
			} else if (intentMode == PaypalModule.PAYMENT_INTENT_ORDER) {
				thingsToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_ORDER);
			}

			intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,
					PaypalModule.ppConfiguration);
			/* putting payload */
			intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingsToBuy);
		} else {
			intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,
					PaypalModule.ppConfiguration);
		}
		final TiActivitySupport activitySupport = (TiActivitySupport) TiApplication
				.getInstance().getCurrentActivity();

		if (TiApplication.isUIThread()) {
			activitySupport.launchActivityForResult(intent,
					REQUEST_CODE_PAYMENT, new PaymentResultHandler());
		} else {
			TiMessenger.postOnMain(new Runnable() {
				@Override
				public void run() {
					activitySupport.launchActivityForResult(intent,
							REQUEST_CODE_PAYMENT, new PaymentResultHandler());
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
		log("start importing payment details");
		if (options.containsKeyAndNotNull("intent")) {
			this.intentMode = TiConvert.toInt(options.get("intent"));
		}
		if (options.containsKeyAndNotNull("futurePayment")) {
			this.futurePayment = TiConvert.toBoolean(options
					.get("futurePayment"));
		}
		if (options.containsKeyAndNotNull("currencyCode")) {
			this.currencyCode = TiConvert.toString(options.get("currencyCode"));
		}
		if (options.containsKeyAndNotNull("shortDescription")) {
			this.shortDescription = TiConvert.toString(options
					.get("shortDescription"));
		}
		if (options.containsKeyAndNotNull("amount")) {
			this.amount = (new BigDecimal(options.getDouble("amount")))
					.setScale(2, BigDecimal.ROUND_HALF_UP);
			Log.d(LCAT, ">>>>>>>>>>>>>>>>>> Amount=" + this.amount);
		}
		if (options.containsKeyAndNotNull("tax")) {
			this.tax = (new BigDecimal(options.getDouble("tax"))).setScale(2,
					BigDecimal.ROUND_HALF_UP);
		}
		if (options.containsKeyAndNotNull("shipping")) {
			this.shipping = (new BigDecimal(options.getDouble("shipping")))
					.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		if (options.containsKeyAndNotNull("items")) {
			log("importing of items from basket");
			paymentItems = new ArrayList<PaymentItem>();
			Object items = options.get("items");
			if (!(items.getClass().isArray())) {
				throw new IllegalArgumentException("items must be an array");
			}
			Object[] itemArray = (Object[]) items;
			for (int index = 0; index < itemArray.length; index++) {
				KrollDict dict = new KrollDict(
						(Map<? extends String, ? extends Object>) itemArray[index]);
				PaymentItem paymentItem = new PaymentItem();
				if (dict.containsKeyAndNotNull("name")) {
					paymentItem.setName(dict.getString("name"));
				}
				if (dict.containsKeyAndNotNull("sku")) {
					paymentItem.setSku(dict.getString("sku"));
				}
				paymentItem.setCurrency("USD");
				if (dict.containsKeyAndNotNull("currency")) {
					paymentItem.setCurrency(dict.getString("currency"));

				}
				if (dict.containsKeyAndNotNull("quantity")) {
					paymentItem.setQuantity(dict.getInt("quantity"));
				}
				if (dict.containsKeyAndNotNull("price")) {
					double price = dict.getDouble("price");
					paymentItem.setPrice(new BigDecimal(price));
				}
				paymentItems.add(paymentItem);
			}
			log("items imported");
		}
		if (options.containsKeyAndNotNull("configuration")) {
			KrollDict configurationDict = options.getKrollDict("configuration");
			if (!(configurationDict instanceof KrollDict)) {
				throw new IllegalArgumentException("Invalid argument type `"
						+ configurationDict.getClass().getName()
						+ "` passed to consume()");
			}
			log("PaypalModule.CONFIG_ENVIRONMENT="
					+ PaypalModule.CONFIG_ENVIRONMENT);
			PaypalModule.ppConfiguration
					.environment(PaypalModule.CONFIG_ENVIRONMENT);
			if (configurationDict.containsKeyAndNotNull("merchantName")) {
				PaypalModule.ppConfiguration.merchantName(configurationDict
						.getString("merchantName"));
			}
			if (configurationDict
					.containsKeyAndNotNull("merchantPrivacyPolicyURL")) {
				try {
					PaypalModule.ppConfiguration.merchantPrivacyPolicyUri(Uri
							.parse(configurationDict
									.getString("merchantPrivacyPolicyURL")));
				} catch (NullPointerException e) {
				}
				try {
					PaypalModule.ppConfiguration.merchantUserAgreementUri(Uri
							.parse(configurationDict
									.getString("merchantUserAgreementURL")));
				} catch (NullPointerException e) {
				}
			}
			PaypalModule.ppConfiguration.clientId(PaypalModule.clientId);
			log(PaypalModule.ppConfiguration.toString());
		}
	}

	private PayPalPayment getStuffToBuy(String paymentIntent) {
		log("getStuffToBuy started");
		if (paymentItems == null) {
			log("payment only in short form, without basket");
			return new PayPalPayment(amount, currencyCode, shortDescription,
					paymentIntent);
		}
		/* iterating thru all items from KrollDict: */
		PayPalItem[] items = new PayPalItem[this.paymentItems.size()];
		for (int i = 0; i < this.paymentItems.size(); i++) {
			PaymentItem item = paymentItems.get(i);
			items[i] = new PayPalItem(item.getName(), item.getQuantity(),
					item.getPrice(), item.getCurrency(), item.getSku());
		}
		BigDecimal subtotal = PayPalItem.getItemTotal(items);
		BigDecimal shipping = this.shipping;
		BigDecimal tax = this.tax;
		PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails(
				shipping, subtotal, tax);
		BigDecimal amount = subtotal.add(shipping).add(tax);
		PayPalPayment payment = new PayPalPayment(amount, this.currencyCode,
				this.shortDescription, paymentIntent);
		payment.items(items).paymentDetails(paymentDetails);
		payment.custom("This is text that will be associated with the payment that the app can use.");
		Log.d(LCAT, "payment=" + payment.toString());
		return payment;
	}

	private void sendAuthorizationToServer(PayPalAuthorization authorization) {
		KrollDict res = new KrollDict();
		res.put("code", authorization.getAuthorizationCode());
		res.put("environment", authorization.getEnvironment());
		res.put("json", authorization.toJSONObject());
		fireEvent("autorization", res);
	}
}
