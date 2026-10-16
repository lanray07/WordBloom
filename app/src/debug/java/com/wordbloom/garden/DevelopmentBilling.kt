package com.wordbloom.garden
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
/** Explicit, isolated test adapter. No Amazon request or real payment can occur here. */
class DevelopmentBilling(
 private val readEntitlement: ()->Boolean = { false },
 private val writeEntitlement: (Boolean)->Unit = {}
): Billing {
 private val mutable=MutableStateFlow(BillingState(premium=readEntitlement(),price="TEST ONLY · no charge",message="DEVELOPMENT MODE · no real purchases"))
 override val state=mutable.asStateFlow()
 var failNext=false
 override fun refresh() { mutable.value=state.value.copy(premium=readEntitlement(),message="DEVELOPMENT MODE · no real purchases") }
 override fun purchase() {
  if(failNext) { failNext=false;mutable.value=state.value.copy(message="TEST ONLY · simulated purchase failure");return }
  writeEntitlement(true)
  mutable.value=state.value.copy(premium=true,message="TEST ONLY · development entitlement, no real payment")
 }
 override fun restore() { mutable.value=state.value.copy(premium=readEntitlement(),message="TEST ONLY · development restore complete") }
 fun revoke() { writeEntitlement(false);mutable.value=state.value.copy(premium=false,message="TEST ONLY · development entitlement revoked") }
}
