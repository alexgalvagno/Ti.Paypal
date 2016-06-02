/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package ti.paypal;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import org.json.JSONException;

import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalOAuthScopes;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ShippingAddress;

import android.content.Intent;
import android.app.Activity;

import org.appcelerator.titanium.TiApplication;

@Kroll.proxy(creatableInModule = PaypalModule.class)
public class PaymentProxy extends KrollProxy {
	// Standard Debugging variables
	private static final String LCAT = "PaymentProxy";
	String currencyCode, shortDescription;
	int intentMode;
	KrollModule proxy;
	String merchantName, clientId;
	private static final int REQUEST_CODE_PAYMENT = 1;
	private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
	private static final int REQUEST_CODE_PROFILE_SHARING = 3;
	PayPalConfiguration ppConfiguration = new PayPalConfiguration();
	List<PayPalItem> paypalItems;

	// Constructor
	public PaymentProxy() {
		super();
	}

	@Override
	protected void onActivityResult(int reqCode, int resCode,
			Intent data) {
		// error: method does not override or implement a method from a
		// supertype
		if (reqCode == REQUEST_CODE_PAYMENT) {
			if (resCode == Activity.RESULT_OK) {
				PaymentConfirmation confirm = data
						.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
				if (confirm != null) {
					try {
						if (proxy.hasListeners("paymentDidComplete")) {
							KrollDict event = new KrollDict();
							event.put("success", true);
							event.put("confirm", confirm.toJSONObject()
									.toString(4));
							event.put("payment", confirm.getPayment()
									.toJSONObject());
							proxy.fireEvent("paymentDidComplete", event);
						}

					} catch (JSONException e) {
						Log.e(LCAT, "an extremely unlikely failure occurred: ",
								e);
					}
				}
			} else if (resCode == Activity.RESULT_CANCELED) {
				if (proxy.hasListeners("paymentDidCancel")) {
					KrollDict event = new KrollDict();
					event.put("success", false);
					proxy.fireEvent("paymentDidCancel", event);
				}
			} else if (resCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
			}
		} else if (reqCode == REQUEST_CODE_FUTURE_PAYMENT) {
			if (resCode == Activity.RESULT_OK) {
				PayPalAuthorization auth = data
						.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
				if (auth != null) {
					try {
						Log.i("FuturePaymentExample", auth.toJSONObject()
								.toString(4));

						String authorization_code = auth.getAuthorizationCode();
						Log.i("FuturePaymentExample", authorization_code);

						sendAuthorizationToServer(auth);
						// displayResultText("Future Payment code received from PayPal");

					} catch (JSONException e) {
						Log.e("FuturePaymentExample",
								"an extremely unlikely failure occurred: ", e);
					}
				}
			} else if (resCode == Activity.RESULT_CANCELED) {
				Log.i("FuturePaymentExample", "The user canceled.");
				if (proxy.hasListeners("paymentDidCancel")) {
					KrollDict event = new KrollDict();
					event.put("success", false);
					proxy.fireEvent("paymentDidCancel", event);
				}
			} else if (resCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
				Log.i("FuturePaymentExample",
						"Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
			}
		} else if (reqCode == REQUEST_CODE_PROFILE_SHARING) {
			if (resCode == Activity.RESULT_OK) {
				PayPalAuthorization auth = data
						.getParcelableExtra(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION);
				if (auth != null) {
					try {
						Log.i("ProfileSharingExample", auth.toJSONObject()
								.toString(4));

						String authorization_code = auth.getAuthorizationCode();
						Log.i("ProfileSharingExample", authorization_code);

						sendAuthorizationToServer(auth);
						// displayResultText("Profile Sharing code received from PayPal");

					} catch (JSONException e) {
						Log.e("ProfileSharingExample",
								"an extremely unlikely failure occurred: ", e);
					}
				}
			} else if (resCode == Activity.RESULT_CANCELED) {
				if (proxy.hasListeners("paymentDidCancel")) {
					KrollDict event = new KrollDict();
					event.put("success", false);
					proxy.fireEvent("paymentDidCancel", event);
				}
				Log.i("ProfileSharingExample", "The user canceled.");
			} else if (resCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
				Log.i("ProfileSharingExample",
						"Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
			}
		}

	}

	// this method (called by JS level) opens the billing layer:
	@Kroll.method
	public void show() {
		Context context = TiApplication.getInstance().getApplicationContext();
		PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
		Intent intent = new Intent(context, PaymentActivity.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,
				ppConfiguration);
		intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
		TiApplication.getAppRootOrCurrentActivity().startActivityForResult(
				intent, REQUEST_CODE_PAYMENT);
	}

	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
		if (options.containsKeyAndNotNull("currencyCode")) {
			currencyCode = TiConvert.toString(options.get("currencyCode"));
		}
		if (options.containsKeyAndNotNull("shortDescription")) {
			shortDescription = TiConvert.toString(options
					.get("shortDescription"));
		}
		if (options.containsKeyAndNotNull("intent")) {
			intentMode = TiConvert.toInt(options.get("intent"));
		}
		if (options.containsKeyAndNotNull("items")) {
			List<KrollDict> paymentItems = new ArrayList<KrollDict>();
			if (!(paymentItems instanceof Object)) {
				throw new IllegalArgumentException("Invalid argument type `"
						+ paymentItems.getClass().getName()
						+ "` passed to consume()");
			}
			paypalItems = new ArrayList<PayPalItem>();
			for (int i = 0; i < paymentItems.size(); i++) {
				String name = "", sku = "", currency = "EU";
				BigDecimal price = new BigDecimal(0);
				int quantify = 1;
				KrollDict paymentItem = paymentItems.get(i);
				if (paymentItem.containsKeyAndNotNull("name")) {
					name = TiConvert.toString(paymentItem.get("name"));
				}
				if (paymentItem.containsKeyAndNotNull("sku")) {
					sku = TiConvert.toString(paymentItem.get("sku"));
				}
				if (paymentItem.containsKeyAndNotNull("currency")) {
					currency = TiConvert.toString(paymentItem.get("currency"));
				}
				if (paymentItem.containsKeyAndNotNull("quantify")) {
					quantify = TiConvert.toInt(paymentItem.get("quantify"));
				}
				if (paymentItem.containsKeyAndNotNull("price")) {
					price = new BigDecimal(TiConvert.toString(paymentItem
							.get("price")));
				}
				paypalItems.add(new PayPalItem(name, quantify, price, sku,
						currency));
			}
		}
		if (options.containsKeyAndNotNull("configuration")) {
			KrollDict configurationDict = options.getKrollDict("configuration");

			if (!(configurationDict instanceof KrollDict)) {
				throw new IllegalArgumentException("Invalid argument type `"
						+ configurationDict.getClass().getName()
						+ "` passed to consume()");
			}
			if (configurationDict.containsKeyAndNotNull("merchantName")) {
				merchantName = configurationDict.getString("merchantName");
			}
			ppConfiguration.environment(PaypalModule.CONFIG_ENVIRONMENT)
					.merchantName(merchantName).clientId(PaypalModule.clientId);
			Log.d(LCAT, ppConfiguration.toString());
		}
	}

	private PayPalPayment getThingToBuy(String paymentIntent) {
		return new PayPalPayment(new BigDecimal("0.01"), "USD", "sample item",
				paymentIntent);
	}

	private void sendAuthorizationToServer(PayPalAuthorization authorization) {
	}
}