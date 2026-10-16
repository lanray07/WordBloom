package com.wordbloom.garden

import android.content.Context
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.wordbloom.engine.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

interface Billing { val state: StateFlow<BillingState>;fun refresh();fun purchase();fun restore() }
data class BillingState(val premium: Boolean=false,val price: String?=null,val message: String="Connecting to Amazon Appstore…")
class AmazonBilling(context: Context): Billing, PurchasingListener {
 private val prefs=context.getSharedPreferences("amazon_entitlement",Context.MODE_PRIVATE)
 private var user=prefs.getString("user",null)
 private val mutable=MutableStateFlow(BillingState(premium=user?.let { prefs.getBoolean("premium:$it",false) }?:false))
 override val state=mutable.asStateFlow()
 private var restoring=false
 private var restored=false
 private val updateReceipts=mutableListOf<Receipt>()
 private var sdkReady=false
 init {
  try { PurchasingService.registerListener(context.applicationContext,this);sdkReady=true }
  catch(e: Exception) { mutable.value=state.value.copy(message="Amazon purchasing is unavailable on this device") }
  catch(e: LinkageError) { mutable.value=state.value.copy(message="Amazon purchasing is incompatible with this device") }
 }
 override fun refresh() { safe { PurchasingService.getUserData();PurchasingService.getProductData(setOf(Entitlements.SKU)) } }
 private fun safe(action: ()->Unit) { if(!sdkReady){ mutable.value=state.value.copy(message="Amazon purchasing is unavailable on this device");return };try { action() } catch(e: Exception) { mutable.value=mutable.value.copy(message="Amazon purchasing is unavailable on this device") } }
 override fun purchase() { if(state.value.price==null) { mutable.value=state.value.copy(message="Store product unavailable. Check Amazon Appstore or try Restore.");return };safe { PurchasingService.purchase(Entitlements.SKU) } }
 override fun restore() { restoring=true;restored=false;updateReceipts.clear();safe { PurchasingService.getUserData() } }
 private fun persist(value: Boolean): Boolean {
  val id=user?:return false
  val saved=prefs.edit().putString("user",id).putBoolean("premium:$id",value).commit()
  if(saved)mutable.value=state.value.copy(premium=value)
  return saved
 }
 override fun onUserDataResponse(r: UserDataResponse) {
  if(r.requestStatus!=UserDataResponse.RequestStatus.SUCCESSFUL) { mutable.value=state.value.copy(message="Amazon account unavailable. Saved offline access retained.");return }
  val id=r.userData.userId
  if(user!=id) { updateReceipts.clear();user=id;mutable.value=state.value.copy(premium=prefs.getBoolean("premium:$id",false));prefs.edit().putString("user",id).apply() }
  safe { PurchasingService.getPurchaseUpdates(restoring) }
 }
 override fun onProductDataResponse(r: ProductDataResponse) {
  val price=if(r.requestStatus==ProductDataResponse.RequestStatus.SUCCESSFUL)r.productData[Entitlements.SKU]?.price else null
  mutable.value=state.value.copy(price=price,message=if(price==null)"Amazon product unavailable on this installation" else "One purchase. A garden of possibilities.")
 }
 override fun onPurchaseResponse(r: PurchaseResponse) {
  if(r.requestStatus==PurchaseResponse.RequestStatus.SUCCESSFUL && r.receipt!=null) {
   if(user!=null && user!=r.userData.userId) { mutable.value=state.value.copy(message="Amazon account changed. Restore from the current account.");refresh();return }
   if(user==null)user=r.userData.userId
   accept(r.receipt)
  } else mutable.value=state.value.copy(message="Purchase ${r.requestStatus.name.lowercase().replace('_',' ')}. No access granted.")
 }
 private fun accept(receipt: Receipt) {
  val relevant=receipt.sku==Entitlements.SKU && receipt.productType==ProductType.ENTITLED
  if(!relevant)return
  if(!persist(Entitlements.apply(state.value.premium,EntitlementReceipt(receipt.sku,receipt.isCanceled,true)))) {
   mutable.value=state.value.copy(message="Purchase could not be saved on this device. Please try Restore.")
   return
  }
  mutable.value=state.value.copy(message=if(receipt.isCanceled)"Premium purchase cancelled" else "Premium restored. Welcome to the whole garden.")
  if(!receipt.isCanceled) safe { PurchasingService.notifyFulfillment(receipt.receiptId,FulfillmentResult.FULFILLED) }
 }
 override fun onPurchaseUpdatesResponse(r: PurchaseUpdatesResponse) {
  if(r.requestStatus!=PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) { restoring=false;mutable.value=state.value.copy(message="Restore unavailable. Please try again with Amazon Appstore.");return }
  if(r.userData.userId!=user)return
  updateReceipts.addAll(r.receipts)
  if(r.hasMore()) safe { PurchasingService.getPurchaseUpdates(false) }
  else {
   // Updates can be unordered and paginated. Use the newest entitlement receipt.
   val latest=updateReceipts.filter { it.sku==Entitlements.SKU && it.productType==ProductType.ENTITLED }.maxWithOrNull(compareBy<Receipt> { it.purchaseDate }.thenBy { it.isCanceled })
   updateReceipts.clear()
   if(latest!=null)accept(latest)
   if(restoring) { restored=latest!=null && !latest.isCanceled;persist(restored);restoring=false;mutable.value=state.value.copy(message=if(restored)"Premium restored" else "No premium purchase found for this Amazon account") }
  }
 }
}
