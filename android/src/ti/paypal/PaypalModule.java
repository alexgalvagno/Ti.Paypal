/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package ti.paypal;

import java.util.Locale;
import java.util.Currency;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.titanium.TiProperties;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.kroll.common.Log;

import com.paypal.android.sdk.payments.PayPalConfiguration;

import java.util.ArrayList;


@Kroll.module(name = "Paypal", id = "ti.paypal")
public class PaypalModule extends KrollModule {
	private static final String LCAT = "PaypalModule";
	public int debugLevel;
	public String clientIdSandbox;
	public String clientIdProduction;
	public static String clientId;
	public static int environment;
	public static String CONFIG_ENVIRONMENT;

	@Kroll.constant
	public static final int ENVIRONMENT_SANDBOX = 0;
	@Kroll.constant
	public static final int ENVIRONMENT_PRODUCTION = 1;
	@Kroll.constant
	public static int PAYMENT_INTENT_SALE = 0;
	@Kroll.constant
	public static int PAYMENT_INTENT_AUTHORIZE = 1;
	@Kroll.constant
	public static int PAYMENT_INTENT_ORDER = 2;

	private TiProperties appProperties;

	public PaypalModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		Log.d(LCAT, "inside onAppCreate");
	}

	@Kroll.method
	public void initialize(@Kroll.argument(optional = true) KrollDict args) {
		appProperties = TiApplication.getInstance().getAppProperties();
		String environmentString = appProperties.getString(
				"PAYPAL_ENVIRONMENT", "SANDBOX");
		if (environmentString.equals("SANDBOX")) {
			environment = ENVIRONMENT_SANDBOX;
		}
		if (environmentString.equals("PRODUCTION")) {
			environment = ENVIRONMENT_PRODUCTION;
		}
		clientIdSandbox = appProperties.getString("PAYPAL_CLIENT_ID_SANDBOX",
				"");
		clientIdProduction = appProperties.getString(
				"PAYPAL_CLIENT_ID_PRODUCTION", "");

		Log.d(LCAT, "clientIdSandbox after reading of properties="
				+ clientIdSandbox);
		if (args != null && args instanceof KrollDict) {
			if (args.containsKeyAndNotNull("clientIdSandbox")) {
				clientIdSandbox = TiConvert.toString(args
						.get("clientIdSandbox"));
			}
			if (args.containsKeyAndNotNull("clientIdProduction")) {
				clientIdProduction = TiConvert.toString(args
						.get("clientIdProduction"));
			}
			if (args.containsKeyAndNotNull("environment")) {
				environment = TiConvert.toInt(args.get("environment"));
			}
			if (environment == ENVIRONMENT_SANDBOX) {
				CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
				clientId = clientIdSandbox;
			}
			if (environment == ENVIRONMENT_PRODUCTION) {
				CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
				clientId = clientIdProduction;
			}
		}
		
		Log.d(LCAT, "clientId=" + clientId);
	}

	@Kroll.method
	public KrollDict createConfiguration(KrollDict args) {
		return args;

	}

	@Kroll.method
	public KrollDict createPaymentItem(KrollDict args) {
		return args;
	}

	@Kroll.method
	public void setDebuglevel(int level) {
		debugLevel=level;
	}

	@Kroll.method
	public ArrayList<KrollDict> getAllCurrencySigns() {
		ArrayList<KrollDict> list = new ArrayList<KrollDict>();
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale l : locales) {
			if (null == l.getCountry() || l.getCountry().isEmpty())
				continue;
			Currency c = Currency.getInstance(l);
			KrollDict item = new KrollDict();
			item.put("country", l.getCountry());
			item.put("displayCountry", l.getDisplayCountry());
			item.put("iso3country", l.getISO3Country());
			item.put("symbol", c.getSymbol());
			item.put("displayName", c.getDisplayName());
			list.add(item);
		}
		return list;
	}
}
