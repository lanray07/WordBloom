package com.wordbloom.garden
import android.content.Context
object BillingFactory {
 fun create(context: Context): Billing {
  if(!BuildConfig.DEVELOPMENT_BILLING)return AmazonBilling(context)
  val preferences=context.getSharedPreferences("development_only_entitlement",Context.MODE_PRIVATE)
  return DevelopmentBilling({preferences.getBoolean("owned",false)},{value -> check(preferences.edit().putBoolean("owned",value).commit())})
 }
}
