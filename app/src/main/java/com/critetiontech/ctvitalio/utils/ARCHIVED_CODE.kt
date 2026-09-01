package com.critetiontech.ctvitalio.utils

/**
 * ARCHIVED_CODE.kt
 *
 * This file contains COMMENTED-OUT code that was previously scattered across
 * the codebase. It is preserved here for historical reference ONLY.
 *
 * DO NOT use any code from this file in production.
 * If functionality needs to be restored, extract and properly refactor.
 *
 * Each entry documents: original source file + reason for removal.
 */

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/DateUtils.kt — getLastWeekRange() old implementation
// REASON: Used Monday-Sunday calendar logic. Replaced by rolling-7-day range.
// ─────────────────────────────────────────────────────────────────────────────
// fun getLastWeekRangeOld(): Pair<String, String> {
//     val calendar = Calendar.getInstance()
//     calendar.firstDayOfWeek = Calendar.MONDAY
//     calendar.add(Calendar.DAY_OF_YEAR, -7)
//     calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
//     val start = calendar.time
//     calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
//     val end = calendar.time
//     val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//     return Pair(sdf.format(start), sdf.format(end))
// }

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/DateUtils.kt — getLastMonthRange() old implementation
// REASON: Calendar-month boundary logic. Replaced by rolling-31-day range.
// ─────────────────────────────────────────────────────────────────────────────
// fun getLastMonthRangeOld(): Pair<String, String> {
//     val calendar = Calendar.getInstance()
//     calendar.add(Calendar.MONTH, -1)
//     calendar.set(Calendar.DAY_OF_MONTH, 1)
//     val start = calendar.time
//     calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
//     val end = calendar.time
//     val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//     return Pair(sdf.format(start), sdf.format(end))
// }

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/DateUtils.kt — showListBottomSheet() old version
// REASON: Did not support typed Map<String,Any> selection. Replaced by a version
//         that returns the full map item to the caller.
// ─────────────────────────────────────────────────────────────────────────────
// fun showListBottomSheetOld(context: Context, title: String, list: List<Map<String,Any>>, onSelected: (String) -> Unit)

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: UI/SignupActivity.kt — getNavigationActionForStep()
// REASON: Signup navigation action IDs for a step-based NavGraph flow.
//         Removed when the signup flow was restructured.
// ─────────────────────────────────────────────────────────────────────────────
// private fun getNavigationActionForStep(step: Int): Int? {
//     return when (step) {
//         0 -> R.id.action_signupFragment_to_addNameFragment
//         1 -> R.id.action_addNameFragment_to_addGenderFragment
//         ...etc
//     }
// }

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/ImagePickerUtil.kt — takePhoto() old version
// REASON: Used fixed filename "temp_image.jpg" causing file overwrite conflicts.
//         Replaced by timestamp-based naming.
// ─────────────────────────────────────────────────────────────────────────────
// val imageFile = File(context.cacheDir, "temp_image.jpg")

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: networking/RetrofitInstance.kt — createApiService*() per-port variants
// REASON: 5 near-identical functions (5100, 7096, 7082, 7083, 5119) that differed
//         only by base URL. Replaced by a single parameterised createApiService(baseUrl).
// ─────────────────────────────────────────────────────────────────────────────
// fun createApiService5100(...), createApiService7096(...), createApiService7082(...),
// createApiService7083(...), createApiService5119(...) — all removed.

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/background_gradient.kt — full file content
// REASON: Entire file was commented out. Binding adapter `customBackgroundTint`
//         was never wired to any XML layout attribute.
// ─────────────────────────────────────────────────────────────────────────────
// @BindingAdapter("customBackgroundTint")
// fun View.setCustomBackgroundTint(isSelected: Boolean) { ... }

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: networking/ApiService.kt — generateAuthHeaderMap() top-level function
// REASON: Duplicate of private helper in RetrofitInstance. accessToken and userId
//         were hardcoded empty strings — auth headers were non-functional.
// ─────────────────────────────────────────────────────────────────────────────
// fun generateAuthHeaderMap(token: Boolean): Map<String,String> {
//     val accessToken = " "  // hardcoded empty
//     val userId = " "       // hardcoded empty
//     ...
// }

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/Constants.java — KEY_BUNDLE_SELECTED_USER
// REASON: Exact duplicate of extraKeys.KEY_SELECTED_USER = "selectedUser".
//         Both map to the same value. Consolidated in AppConstants.
// ─────────────────────────────────────────────────────────────────────────────
// String KEY_BUNDLE_SELECTED_USER = "selectedUser";

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/LoaderUtils.kt — static dialog singleton
// REASON: Single shared Dialog instance caused one screen's hideLoading() to
//         dismiss another screen's dialog in concurrent usage.
//         Now each caller owns its dialog reference.
// ─────────────────────────────────────────────────────────────────────────────
// private var dialog: Dialog? = null  // was static in LoaderUtils object

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: UI/Login.kt — top-level onBackPressed(login: Login)
// REASON: Dead code — function was defined but never called anywhere.
// ─────────────────────────────────────────────────────────────────────────────
// fun onBackPressed(login: Login) { login.onBackPressedDispatcher.onBackPressed() }

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/PrefsManager.kt — Patient / EmployeeGoal / LeaderboardItem classes
// REASON: Data models should live in model/, not in a SharedPreferences manager.
//         Moved to model/Patient.kt, model/EmployeeGoal.kt.
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// SOURCE: utils/NetworkUtils.kt — checkAndShowToast()
// REASON: Mixed UI side-effects (Toast) into a pure utility object. Violates SRP.
//         Callers now call isConnected() and show their own UI feedback.
// ─────────────────────────────────────────────────────────────────────────────
// fun checkAndShowToast(): Boolean {
//     ...
//     Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
//     ...
// }
