package com.wordbloom.garden
import android.content.Context
/** Release has no reference to the development adapter, regardless of project flags. */
object BillingFactory { fun create(context: Context): Billing = AmazonBilling(context) }
