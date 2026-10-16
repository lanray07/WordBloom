package com.wordbloom.garden
import org.junit.Assert.*
import org.junit.Test
class BillingContractTest {
 private var saved=false
 private fun adapter()=DevelopmentBilling({saved},{saved=it})
 @Test fun testModeIsAlwaysClearlyLabelled() {
  val billing=adapter();assertFalse(billing.state.value.premium);assertTrue(billing.state.value.price!!.contains("TEST ONLY"));assertTrue(billing.state.value.message.contains("DEVELOPMENT"))
 }
 @Test fun purchasePersistsBeforeGrantingAccess() {
  val billing=adapter();billing.purchase();assertTrue(saved);assertTrue(billing.state.value.premium)
 }
 @Test fun failedPurchaseDoesNotGrantAccess() {
  val billing=adapter();billing.failNext=true;billing.purchase();assertFalse(saved);assertFalse(billing.state.value.premium)
 }
 @Test fun restoreReflectsPersistedEntitlement() {
  val billing=adapter();saved=true;billing.restore();assertTrue(billing.state.value.premium);saved=false;billing.restore();assertFalse(billing.state.value.premium)
 }
 @Test fun revocationSurvivesAdapterRecreation() {
  val billing=adapter();billing.purchase();billing.revoke();assertFalse(saved);assertFalse(adapter().state.value.premium)
 }
}
