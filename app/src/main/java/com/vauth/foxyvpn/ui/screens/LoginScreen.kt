package com.vauth.foxyvpn.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vauth.foxyvpn.data.FxaAuthRepository
import com.vauth.foxyvpn.data.FoxyBackup
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.TokenStore
import com.vauth.foxyvpn.ui.theme.FoxyLanguage
import com.vauth.foxyvpn.ui.theme.NeumorphicButton
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.StyleAtmospherePosterOverlay
import com.vauth.foxyvpn.ui.theme.currentFoxyFonts
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.shakeOnTrigger
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch
import android.widget.Toast

private const val MOZILLA_SIGNUP_WEB_URL = "https://accounts.firefox.com/signup"

@Composable
fun LoginScreen(
    authRepository: FxaAuthRepository,
    onSignedIn: () -> Unit,
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    var savedAccounts by remember { mutableStateOf(tokenStore.loadAccounts()) }
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var showInAppMozillaWeb by rememberSaveable { mutableStateOf(false) }
    var restoringBackup by rememberSaveable { mutableStateOf(false) }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var awaitingTwoFactor by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val neu = currentNeumorphicColors
    val s = currentFoxyStrings
    val fonts = currentFoxyFonts

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        restoringBackup = true
        runCatching {
            val json = context.contentResolver.openInputStream(uri)?.use { input ->
                input.reader(Charsets.UTF_8).readText()
            } ?: error("Cannot read backup")
            FoxyBackup.restore(json, SettingsStore(context), tokenStore)
        }.onSuccess {
            savedAccounts = tokenStore.loadAccounts()
            Toast.makeText(
                context,
                when (s.language) {
                    FoxyLanguage.FA -> "اطلاعات بازیابی شد؛ اکنون با حساب بازیابی‌شده وارد شوید"
                    FoxyLanguage.AR -> "تمت استعادة المعلومات؛ سجّل الدخول بالحساب المستعاد الآن"
                    FoxyLanguage.EN -> "Information restored. Sign in with the restored account now."
                },
                Toast.LENGTH_LONG,
            ).show()
        }.onFailure {
            Toast.makeText(
                context,
                when (s.language) {
                    FoxyLanguage.FA -> "فایل پشتیبان نامعتبر یا ناسازگار است"
                    FoxyLanguage.AR -> "النسخة الاحتياطية غير صالحة أو غير متوافقة"
                    FoxyLanguage.EN -> "Invalid or incompatible backup file"
                },
                Toast.LENGTH_LONG,
            ).show()
        }
        restoringBackup = false
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        runCatching { authRepository.prewarmFastlyClearance() }
    }

    DisposableEffect(Unit) {
        if (!awaitingTwoFactor) {
            authRepository.clearPendingSession()
        }
        onDispose { }
    }

    BackHandler(enabled = showInAppMozillaWeb || awaitingTwoFactor) {
        if (showInAppMozillaWeb) {
            showInAppMozillaWeb = false
        } else {
            authRepository.clearPendingSession()
            awaitingTwoFactor = false
            errorMessage = null
            code = ""
        }
    }

    fun doSubmitAuth() {
        val cleanEmail = FxaAuthRepository.normalizeEmail(email)
        if (cleanEmail.isBlank() || password.isBlank() || isLoading) return
        errorMessage = null
        isLoading = true
        scope.launch {
            val result = if (isSignUpMode) {
                authRepository.startSignUp(cleanEmail, password)
            } else {
                authRepository.startLogin(cleanEmail, password)
            }
            isLoading = false
            result.onSuccess { needsVerification ->
                if (needsVerification) awaitingTwoFactor = true else onSignedIn()
            }.onFailure { err ->
                val rawMsg = err.message.orEmpty()
                if (rawMsg.contains("406") || rawMsg.contains("anti-bot", ignoreCase = true)) {
                    // Automatically open official account portal as requested
                    errorMessage = when (s.language) {
                        FoxyLanguage.FA -> "سرویس حساب ورود مستقیم را مسدود کرد؛ درگاه رسمی حساب به صورت خودکار باز شد."
                        FoxyLanguage.AR -> "تم حظر الدخول المباشر مؤقتاً؛ تم فتح البوابة الرسمية تلقائياً."
                        FoxyLanguage.EN -> "Direct login blocked by account service; opening official portal automatically."
                    }
                    showInAppMozillaWeb = true
                } else {
                    errorMessage = when {
                        rawMsg.contains("102") || rawMsg.contains("Unknown account", ignoreCase = true) ->
                            when (s.language) {
                                FoxyLanguage.FA -> "حسابی با این ایمیل یافت نشد. لطفاً ساخت حساب را انتخاب کنید."
                                FoxyLanguage.AR -> "لم يتم العثور على حساب بهذا البريد. يُرجى إنشاء حساب جديد."
                                FoxyLanguage.EN -> "Account not found with this email. Please create an account."
                            }
                        rawMsg.contains("103") || rawMsg.contains("Incorrect password", ignoreCase = true) ->
                            when (s.language) {
                                FoxyLanguage.FA -> "رمز عبور وارد شده نادرست است."
                                FoxyLanguage.AR -> "كلمة المرور غير صحيحة."
                                FoxyLanguage.EN -> "Incorrect password entered."
                            }
                        rawMsg.contains("Unable to resolve host", ignoreCase = true) || rawMsg.contains("connect", ignoreCase = true) ->
                            when (s.language) {
                                FoxyLanguage.FA -> "اتصال اینترنت برقرار نیست یا سرور در دسترس نیست."
                                FoxyLanguage.AR -> "لا يوجد اتصال بالإنترنت أو الخادم غير متاح."
                                FoxyLanguage.EN -> "Internet connection unavailable or server unreachable."
                            }
                        else -> rawMsg.ifBlank { "ورود ناموفق بود؛ لطفاً دوباره تلاش کنید." }
                    }
                }
            }
        }
    }

    fun doSubmitTwoFactor() {
        val cleanCode = FxaAuthRepository.sanitizeCode(code)
        if (cleanCode.isBlank() || isLoading) return
        errorMessage = null
        isLoading = true
        scope.launch {
            val result = authRepository.submitTwoFactorCode(cleanCode)
            isLoading = false
            result.onSuccess { onSignedIn() }
                .onFailure { err ->
                    val raw = err.message.orEmpty()
                    errorMessage = when {
                        raw.contains("105") || raw.contains("Invalid verification code", ignoreCase = true) || raw.contains("code", ignoreCase = true) ->
                            when (s.language) {
                                FoxyLanguage.FA -> "کد تأیید وارد شده نادرست یا منقضی شده است."
                                FoxyLanguage.AR -> "رمز التحقق غير صحيح أو انتهت صلاحيته."
                                FoxyLanguage.EN -> "Verification code is incorrect or expired."
                            }
                        else -> raw.ifBlank { "تأیید دو مرحله‌ای ناموفق بود؛ لطفاً دوباره تلاش کنید." }
                    }
                    if (authRepository.pendingSessionToken == null) {
                        awaitingTwoFactor = false
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neu.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        StyleAtmospherePosterOverlay(modifier = Modifier.fillMaxSize())

        if (showInAppMozillaWeb) {
            MozillaInAppWebPortal(
                initialUrl = MOZILLA_SIGNUP_WEB_URL,
                onCapturedEmail = { captured ->
                    if (captured.isNotBlank() && captured.contains("@")) {
                        email = captured.trim().lowercase()
                    }
                },
                onCloseAndSwitchToLogin = {
                    showInAppMozillaWeb = false
                    isSignUpMode = false
                    savedAccounts = tokenStore.loadAccounts()
                    if (authRepository.isSignedIn()) {
                        onSignedIn()
                    }
                },
                onClose = {
                    showInAppMozillaWeb = false
                    savedAccounts = tokenStore.loadAccounts()
                    if (authRepository.isSignedIn()) {
                        onSignedIn()
                    }
                },
            )
        } else {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Logo Badge (Perfect Circle Official Fox Icon)
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .staggeredEntrance(0)
                        .clip(CircleShape)
                        .background(Color(0xFF0A0D14))
                        .border(1.5.dp, neu.accent.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.vauth.foxyvpn.R.drawable.foxy_official_icon),
                        contentDescription = "FoxyVPN Official Logo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = s.loginTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = fonts.headingFont,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = neu.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = when (s.language) {
                        FoxyLanguage.FA -> "ورود و ساخت حساب از طریق درگاه رسمی موزیلا؛ سهمیه و مجوز اتصال به‌صورت خودکار و کاملاً امن در برنامه تنظیم می‌شود."
                        FoxyLanguage.AR -> "تسجيل الدخول وإنشاء الحساب عبر بوابة موزيلا الرسمية مع ضبط الحصص تلقائياً وبأمان."
                        FoxyLanguage.EN -> "Direct official sign-in via Mozilla Accounts; quotas and tunnel credentials sync automatically."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = fonts.bodyFont),
                    color = neu.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )

                Spacer(Modifier.height(20.dp))

                NeumorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(1)
                        .shakeOnTrigger(errorMessage),
                    cornerRadius = 24.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                    ) {
                        if (!awaitingTwoFactor) {
                            // Primary Official Mozilla Web Portal Entry
                            NeumorphicButton(
                                onClick = { showInAppMozillaWeb = true },
                                accentColor = neu.accent,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Language,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = when (s.language) {
                                            FoxyLanguage.FA -> "ورود از درگاه رسمی حساب موزیلا"
                                            FoxyLanguage.AR -> "الدخول عبر بوابة موزيلا الرسمية"
                                            FoxyLanguage.EN -> "Sign In via Official Mozilla Portal"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = fonts.headingFont,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Color.White,
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = when (s.language) {
                                    FoxyLanguage.FA -> "پشتیبانی کامل از ورود، ساخت حساب، رمز عبور و کد دومرحله‌ای بدون نیاز به کپی دستی توکن"
                                    FoxyLanguage.AR -> "دعم كامل لتسجيل الدخول، إنشاء الحساب، والتحقق المزدوج تلقائياً"
                                    FoxyLanguage.EN -> "Full support for sign-in, account creation, password and 2FA with automatic return"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.bodyFont),
                                color = neu.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            )

                            Spacer(Modifier.height(18.dp))
                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.15f))
                            Spacer(Modifier.height(18.dp))

                            // Backup & Restore Action
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(neu.surfaceElevated)
                                    .border(1.dp, neu.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .clickable(enabled = !restoringBackup) {
                                        restoreBackupLauncher.launch(arrayOf("application/json", "text/plain"))
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    if (restoringBackup) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = neu.accent,
                                        )
                                    } else {
                                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = neu.accent, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(9.dp))
                                    Text(
                                        text = when (s.language) {
                                            FoxyLanguage.FA -> "بازیابی اطلاعات و حساب‌ها (Backup)"
                                            FoxyLanguage.AR -> "استعادة المعلومات والنسخ الاحتياطي"
                                            FoxyLanguage.EN -> "Restore Backup & Accounts"
                                        },
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = fonts.headingFont,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = neu.accent,
                                    )
                                }
                            }
                        } else {
                            Text(
                                s.loginTwoFactorPrompt,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = neu.textPrimary,
                            )
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = code,
                                onValueChange = {
                                    code = it.replace(Regex("[\\p{Z}\\u200B-\\u200D\\uFEFF\\u00A0\\s]+"), "")
                                },
                                label = { Text(s.loginTwoFactorCodeLabel) },
                                leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = null, tint = neu.textSecondary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        doSubmitTwoFactor()
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(Modifier.height(24.dp))

                            NeumorphicButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    doSubmitTwoFactor()
                                },
                                accentColor = neu.accent,
                                enabled = !isLoading && code.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White,
                                    )
                                } else {
                                    Text(
                                        s.loginVerifyButton,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            authRepository.clearPendingSession()
                                            awaitingTwoFactor = false
                                            errorMessage = null
                                            code = ""
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = when (s.language) {
                                            FoxyLanguage.FA -> "بازگشت به ورود عادی"
                                            FoxyLanguage.AR -> "العودة لتسجيل الدخول"
                                            FoxyLanguage.EN -> "Back to Sign In"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = neu.textSecondary,
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            scope.launch {
                                                authRepository.resendVerificationCodeIfNeeded()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = when (s.language) {
                                            FoxyLanguage.FA -> "ارسال مجدد کد"
                                            FoxyLanguage.AR -> "إعادة إرسال الرمز"
                                            FoxyLanguage.EN -> "Resend Code"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = neu.accent,
                                    )
                                }
                            }
                        }

                        errorMessage?.let {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // Saved Multi-Accounts Quick Switcher (if accounts already exist on device)
                if (savedAccounts.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(2),
                        cornerRadius = 22.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                        ) {
                            Text(
                                text = when (s.language) {
                                    FoxyLanguage.FA -> "حساب‌های ذخیره‌شده (ورود سریع با یک لمس)"
                                    FoxyLanguage.AR -> "الحسابات المحفوظة (تبديل سريع)"
                                    FoxyLanguage.EN -> "SAVED ACCOUNTS (1-TAP SWITCH)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = fonts.headingFont,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = neu.textSecondary,
                            )
                            Spacer(Modifier.height(10.dp))
                            savedAccounts.forEachIndexed { idx, acct ->
                                if (idx > 0) {
                                    HorizontalDivider(
                                        color = neu.textSecondary.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(vertical = 8.dp),
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (tokenStore.switchToAccount(acct.email)) {
                                                savedAccounts = tokenStore.loadAccounts()
                                                onSignedIn()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AccountCircle,
                                            contentDescription = null,
                                            tint = neu.accent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = acct.email,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = fonts.monoFont,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = neu.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(neu.accent.copy(alpha = 0.14f))
                                            .border(1.dp, neu.accent.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = when (s.language) {
                                                FoxyLanguage.FA -> "انتخاب"
                                                FoxyLanguage.AR -> "اختيار"
                                                FoxyLanguage.EN -> "Switch"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = fonts.headingFont,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = neu.accent,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val FXA_CHANNEL_ID = "account_updates"
private const val CMD_FXA_STATUS = "fxaccounts:fxa_status"
private const val CMD_CAN_LINK_ACCOUNT = "fxaccounts:can_link_account"
private const val CMD_LOGIN = "fxaccounts:login"
private val SESSION_TOKEN_HEX_REGEX = Regex("^[0-9a-f]{64}$")
private val FIREFOX_ANDROID_WEB_UA: String
    get() = com.vauth.foxyvpn.data.FirefoxAccountHeaders.userAgent

private fun quoteJsString(value: String): String = org.json.JSONObject.quote(value)

private val WEBCHANNEL_HOOK_JS = """
(function() {
  if (window.__foxyChannelHooked) return;
  window.__foxyChannelHooked = true;
  var forward = function (detail) {
    try {
      var host = window.location.hostname;
      if (host !== 'accounts.firefox.com' && !host.endsWith('.firefox.com')) return;
      var payload = typeof detail === 'string' ? detail : JSON.stringify(detail);
      foxyWebChannel.onMessage(payload, window.location.href);
    } catch (e) {}
  };
  window.addEventListener('WebChannelMessageToChrome', function (e) { forward(e.detail); });
  document.addEventListener('WebChannelMessageToChrome', function (e) { forward(e.detail); });
  window.__foxyReply = function (channelId, messageId, command, dataJson) {
    try {
      var parsedData = typeof dataJson === 'string' ? JSON.parse(dataJson) : dataJson;
      var envelope = {
        id: channelId,
        message: { messageId: messageId, command: command, data: parsedData }
      };
      var eventObj = new CustomEvent('WebChannelMessageToContent', { detail: envelope });
      window.dispatchEvent(eventObj);
      document.dispatchEvent(eventObj);
      var eventStr = new CustomEvent('WebChannelMessageToContent', { detail: JSON.stringify(envelope) });
      window.dispatchEvent(eventStr);
      document.dispatchEvent(eventStr);
    } catch (e) {}
  };
  var getUrlString = function(arg) {
    if (!arg) return '';
    if (typeof arg === 'string') return arg;
    if (typeof arg.url === 'string') return arg.url;
    if (typeof arg.href === 'string') return arg.href;
    try { return arg.toString(); } catch (e) { return ''; }
  };
  var checkVerifyUrl = function(url) {
    var u = getUrlString(url).toLowerCase();
    return u.indexOf('/verify') !== -1 || u.indexOf('/totp') !== -1 || u.indexOf('/session/verify') !== -1 ||
           u.indexOf('enter_code') !== -1 || u.indexOf('confirm_signin') !== -1 || u.indexOf('confirm_email') !== -1;
  };
  var checkSuccessUrl = function(url) {
    var u = getUrlString(url).toLowerCase();
    return u.indexOf('confirmed') !== -1 || u.indexOf('connect_another_device') !== -1 ||
           u.indexOf('/pair') !== -1 || u.indexOf('/settings') !== -1 || u.indexOf('/complete') !== -1 ||
           u.indexOf('/verified') !== -1 || u.indexOf('success') !== -1 || u.indexOf('/profile') !== -1 ||
           u.indexOf('/account') !== -1 || u.indexOf('context=fx_desktop_v3') !== -1 && (u.indexOf('flow=') !== -1 || u.indexOf('uid=') !== -1);
  };
  if (window.fetch) {
    var origFetch = window.fetch;
    window.fetch = function() {
      var reqUrl = (arguments[0] && arguments[0].url) || arguments[0] || '';
      return origFetch.apply(this, arguments).then(function(res) {
        try {
          if (res && res.ok && checkVerifyUrl(reqUrl)) {
            foxyWebChannel.onSessionVerified();
          }
        } catch (e) {}
        return res;
      });
    };
  }
  if (window.XMLHttpRequest) {
    var origOpen = XMLHttpRequest.prototype.open;
    var origSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.open = function(method, url) {
      this.__foxyReqUrl = url;
      return origOpen.apply(this, arguments);
    };
    XMLHttpRequest.prototype.send = function() {
      this.addEventListener('load', function() {
        try {
          if (this.status >= 200 && this.status < 300 && checkVerifyUrl(this.__foxyReqUrl)) {
            foxyWebChannel.onSessionVerified();
          }
        } catch (e) {}
      });
      return origSend.apply(this, arguments);
    };
  }
  var notifyNav = function() {
    try {
      if (checkSuccessUrl(window.location.href)) {
        foxyWebChannel.onSessionVerified();
      }
    } catch (e) {}
  };
  var origPushState = history.pushState;
  if (origPushState) {
    history.pushState = function() {
      var ret = origPushState.apply(this, arguments);
      notifyNav();
      return ret;
    };
  }
  var origReplaceState = history.replaceState;
  if (origReplaceState) {
    history.replaceState = function() {
      var ret = origReplaceState.apply(this, arguments);
      notifyNav();
      return ret;
    };
  }
  window.addEventListener('popstate', notifyNav);
  window.addEventListener('hashchange', notifyNav);
})();
""".trimIndent()

internal class PortalWebChannelBridge(
    webView: WebView,
    private val authRepository: FxaAuthRepository?,
    private val onTokens: (String, String?, Long, String?) -> Unit,
    private val onSessionToken: (String) -> Unit,
    private val onOAuthCode: (String, String?) -> Unit = { _, _ -> },
) {
    private val webViewRef = java.lang.ref.WeakReference(webView)

    @Volatile
    var pendingSessionToken: String? = null
        private set

    @Volatile
    private var exchangeInProgress = false

    @android.webkit.JavascriptInterface
    fun onMessage(payload: String) {
        onMessage(payload, "https://accounts.firefox.com")
    }

    @android.webkit.JavascriptInterface
    fun onSessionVerified() {
        val token = pendingSessionToken ?: return
        exchangeInProgress = false
        notifySessionToken(token)
    }

    @android.webkit.JavascriptInterface
    fun onOAuthResult(status: Int, responseText: String) {
        val token = pendingSessionToken
        if (status in 200..299) {
            val parsed = runCatching {
                val json = org.json.JSONObject(responseText)
                val accessToken = json.getString("access_token")
                val refreshToken = json.optString("refresh_token").ifBlank { null }
                val expiresIn = json.optLong("expires_in", 3600L)
                Triple(accessToken, refreshToken, expiresIn)
            }.getOrNull()
            if (parsed != null) {
                val view = webViewRef.get()
                if (view != null) {
                    view.post { onTokens(parsed.first, parsed.second, parsed.third, token) }
                } else {
                    onTokens(parsed.first, parsed.second, parsed.third, token)
                }
                return
            }
        }
        val isUnconfirmed = status == 400 &&
            (responseText.contains("104") ||
                responseText.contains("Unconfirmed session", ignoreCase = true) ||
                responseText.contains("unverified", ignoreCase = true))
        if (isUnconfirmed) {
            exchangeInProgress = false
            return
        }
        if (token != null) {
            fallbackSession(token)
        }
    }

    @android.webkit.JavascriptInterface
    fun onOAuthError(@Suppress("UNUSED_PARAMETER") error: String) {
        val token = pendingSessionToken
        if (token != null) {
            fallbackSession(token)
        }
    }

    private fun fallbackSession(token: String) {
        exchangeInProgress = false
        val view = webViewRef.get()
        if (view != null) {
            view.post { onSessionToken(token) }
        } else {
            onSessionToken(token)
        }
    }

    @android.webkit.JavascriptInterface
    fun onMessage(payload: String, originUrl: String) {
        val host = runCatching { android.net.Uri.parse(originUrl).host?.lowercase() }.getOrNull()
        if (host != "accounts.firefox.com" && host?.endsWith(".firefox.com") != true) {
            return
        }
        val message = runCatching {
            org.json.JSONObject(payload).optJSONObject("message") ?: org.json.JSONObject()
        }.getOrNull() ?: return
        val command = message.optString("command")
        val data = message.optJSONObject("data") ?: org.json.JSONObject()
        val messageId = message.optString("messageId")
        when (command) {
            CMD_FXA_STATUS -> reply(
                messageId,
                CMD_FXA_STATUS,
                """{"capabilities":{"choose_what_to_sync":true,"engines":[]},"signedInUser":null}""",
            )
            CMD_CAN_LINK_ACCOUNT -> reply(
                messageId,
                CMD_CAN_LINK_ACCOUNT,
                """{"ok":true}""",
            )
            CMD_LOGIN, "fxaccounts:oauth_login" -> {
                val oauthCode = data.optString("code", "").trim()
                val token = data.optString("sessionToken", "").trim()
                    .ifBlank { data.optString("token", "").trim() }
                if (SESSION_TOKEN_HEX_REGEX.matches(token)) {
                    pendingSessionToken = token
                }
                if (oauthCode.isNotBlank()) {
                    val view = webViewRef.get()
                    if (view != null) {
                        view.post { onOAuthCode(oauthCode, pendingSessionToken) }
                    } else {
                        onOAuthCode(oauthCode, pendingSessionToken)
                    }
                } else if (SESSION_TOKEN_HEX_REGEX.matches(token)) {
                    val hasVerificationReason = data.has("verificationReason") &&
                        data.optString("verificationReason").isNotBlank()
                    val isVerified = if (data.has("verified")) {
                        data.optBoolean("verified", true)
                    } else {
                        !hasVerificationReason
                    }
                    if (isVerified && !hasVerificationReason) {
                        notifySessionToken(token)
                    }
                }
                if (messageId.isNotBlank()) {
                    reply(messageId, command, """{"ok":true}""")
                }
            }
            else -> {
                if (command.contains("verified", ignoreCase = true)) {
                    val token = data.optString("sessionToken", "").trim()
                        .ifBlank { data.optString("token", "").trim() }
                        .ifBlank { pendingSessionToken ?: "" }
                    if (SESSION_TOKEN_HEX_REGEX.matches(token)) {
                        pendingSessionToken = token
                        exchangeInProgress = false
                        notifySessionToken(token)
                    }
                }
                if (messageId.isNotBlank()) {
                    reply(messageId, command, """{"ok":true}""")
                }
            }
        }
    }

    fun notifySessionToken(token: String) {
        if (!SESSION_TOKEN_HEX_REGEX.matches(token)) return
        if (exchangeInProgress) return
        exchangeInProgress = true
        pendingSessionToken = token
        runCatching {
            CookieManager.getInstance().flush()
            com.vauth.foxyvpn.data.ControlPlaneHttp.cookieJar.syncFromWebView()
        }
        val hawkInfo = runCatching { authRepository?.buildOAuthHawkHeader(token) }.getOrNull()
        val view = webViewRef.get()
        if (hawkInfo == null || view == null) {
            fallbackSession(token)
            return
        }
        val (hawkHeader, bodyJson) = hawkInfo
        val script = """
            (function() {
              fetch('https://api.accounts.firefox.com/v1/oauth/token', {
                method: 'POST',
                headers: {
                  'Authorization': ${quoteJsString(hawkHeader)},
                  'Content-Type': 'application/json'
                },
                body: ${quoteJsString(bodyJson)}
              }).then(function(res) {
                return res.text().then(function(text) {
                  foxyWebChannel.onOAuthResult(res.status, text);
                });
              }).catch(function(err) {
                foxyWebChannel.onOAuthError(err ? (err.message || ('' + err)) : 'fetch_failed');
              });
            })();
        """.trimIndent()
        view.post {
            runCatching { view.evaluateJavascript(script, null) }
                .onFailure { fallbackSession(token) }
        }
    }

    private fun reply(messageId: String, command: String, dataJson: String) {
        val view = webViewRef.get() ?: return
        val script = "window.__foxyReply && window.__foxyReply(" +
            "${quoteJsString(FXA_CHANNEL_ID)}, ${quoteJsString(messageId)}, ${quoteJsString(command)}, ${quoteJsString(dataJson)});"
        view.post {
            runCatching { view.evaluateJavascript(script, null) }
        }
    }
}

/**
 * Embedded In-App Mozilla Account Portal connected directly to https://accounts.firefox.com
 * inside the app without opening an external browser, with full WebChannel automatic OAuth login.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MozillaInAppWebPortal(
    initialUrl: String = MOZILLA_SIGNUP_WEB_URL,
    onCapturedEmail: (String) -> Unit,
    onCloseAndSwitchToLogin: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as? com.vauth.foxyvpn.FoxyVpnApp }
    val authRepository = app?.authRepository
    val scope = rememberCoroutineScope()
    val neu = currentNeumorphicColors
    val s = currentFoxyStrings
    val fonts = currentFoxyFonts
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    // Clean official Mozilla entry URL (without rejected or deprecated parameters)
    val startUrl = remember(initialUrl) {
        if (initialUrl == MOZILLA_SIGNUP_WEB_URL) {
            "https://accounts.firefox.com/"
        } else {
            initialUrl
        }
    }
    var currentUrl by remember { mutableStateOf(startUrl) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var isPageLoading by remember { mutableStateOf(true) }
    var finalizingSession by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
        }
    }

    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .background(neu.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        // Top In-App Mozilla Browser Header
        NeumorphicCard(
            cornerRadius = 18.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        NeumorphicIconButton(
                            size = 36.dp,
                            onClick = {
                                val wv = webViewRef
                                if (wv != null && wv.canGoBack()) {
                                    wv.goBack()
                                } else {
                                    onClose()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = neu.textPrimary,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (s.language) {
                                    FoxyLanguage.FA -> "درگاه رسمی حساب موزیلا (داخل برنامه)"
                                    FoxyLanguage.AR -> "بوابة حساب موزيلا الرسمية (داخل التطبيق)"
                                    FoxyLanguage.EN -> "Official Mozilla Account Portal"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = fonts.headingFont,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = neu.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = neu.accent,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = currentUrl,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.monoFont),
                                    color = neu.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeumorphicIconButton(
                            size = 36.dp,
                            onClick = {
                                runCatching {
                                    CookieManager.getInstance().removeAllCookies(null)
                                    CookieManager.getInstance().flush()
                                    webViewRef?.clearCache(true)
                                    webViewRef?.clearHistory()
                                }
                                webViewRef?.loadUrl("https://accounts.firefox.com/")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = neu.accent,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                        NeumorphicIconButton(
                            size = 36.dp,
                            onClick = onClose,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = neu.textPrimary,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = finalizingSession || (isPageLoading && loadProgress in 1..99),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    if (finalizingSession) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = neu.accent,
                            trackColor = neu.surfaceElevated,
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { loadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = neu.accent,
                            trackColor = neu.surfaceElevated,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Embedded WebView Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, neu.textSecondary.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        val radiusPx = 20f * resources.displayMetrics.density
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            setColor(android.graphics.Color.WHITE)
                            cornerRadius = radiusPx
                        }
                        clipToOutline = true
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadsImagesAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            userAgentString = FIREFOX_ANDROID_WEB_UA
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            javaScriptCanOpenWindowsAutomatically = false
                            setSupportMultipleWindows(false)
                        }

                        val bridge = PortalWebChannelBridge(
                            webView = this,
                            authRepository = authRepository,
                            onTokens = { accessToken, refreshToken, expiresIn, sessionToken ->
                                if (finalizingSession || authRepository == null) return@PortalWebChannelBridge
                                finalizingSession = true
                                scope.launch {
                                    val res = authRepository.completeBrowserLoginWithTokens(
                                        accessToken = accessToken,
                                        refreshToken = refreshToken,
                                        expiresIn = expiresIn,
                                        sessionTokenHex = sessionToken,
                                    )
                                    finalizingSession = false
                                    if (res.isSuccess) {
                                        onCloseAndSwitchToLogin()
                                    }
                                }
                            },
                            onSessionToken = { sessionToken ->
                                if (finalizingSession || authRepository == null) return@PortalWebChannelBridge
                                finalizingSession = true
                                scope.launch {
                                    val res = authRepository.completeBrowserLogin(sessionToken)
                                    finalizingSession = false
                                    if (res.isSuccess) {
                                        onCloseAndSwitchToLogin()
                                    }
                                }
                            },
                            onOAuthCode = { code, sessionToken ->
                                if (finalizingSession || authRepository == null) return@PortalWebChannelBridge
                                finalizingSession = true
                                scope.launch {
                                    val res = authRepository.completeOAuthCodeLogin(code, sessionToken)
                                    finalizingSession = false
                                    if (res.isSuccess) {
                                        onCloseAndSwitchToLogin()
                                    }
                                }
                            },
                        )
                        addJavascriptInterface(bridge, "foxyWebChannel")

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = newProgress
                                isPageLoading = newProgress < 100
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                if (!url.isNullOrBlank()) currentUrl = url
                                isPageLoading = true
                                runCatching { view?.evaluateJavascript(WEBCHANNEL_HOOK_JS, null) }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (!url.isNullOrBlank()) currentUrl = url
                                isPageLoading = false
                                runCatching {
                                    CookieManager.getInstance().flush()
                                    com.vauth.foxyvpn.data.ControlPlaneHttp.cookieJar.syncFromWebView()
                                }
                                runCatching { view?.evaluateJavascript(WEBCHANNEL_HOOK_JS, null) }

                                val lowerUrl = url?.lowercase().orEmpty()
                                val isProfileOrSettings = lowerUrl.contains("/profile") ||
                                    lowerUrl.contains("/settings") ||
                                    lowerUrl.contains("/account") ||
                                    lowerUrl.contains("connect_another_device") ||
                                    lowerUrl.contains("confirmed")

                                if (isProfileOrSettings && !finalizingSession) {
                                    // If user reached profile page, auth succeeded on Mozilla official site!
                                    // Check if we have signed-in state or can finalize automatically.
                                    if (authRepository?.isSignedIn() == true) {
                                        onCloseAndSwitchToLogin()
                                        return
                                    }
                                }

                                // Capture email entered inside Mozilla signup/signin form so it auto-fills in Foxy VPN
                                view?.evaluateJavascript(
                                    """
                                    (function() {
                                        var el = document.querySelector('input[type="email"], input[name="email"]');
                                        if (el && el.value) return el.value;
                                        return '';
                                    })();
                                    """.trimIndent()
                                ) { rawResult ->
                                    val cleaned = rawResult?.trim()?.trim('"').orEmpty()
                                    if (cleaned.contains("@")) {
                                        onCapturedEmail(cleaned)
                                    }
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                val uri = request?.url ?: return true
                                val scheme = uri.scheme?.lowercase()
                                if (scheme != "https" && scheme != "http") return true
                                val nextUrl = uri.toString()
                                currentUrl = nextUrl
                                return false
                            }
                        }
                        loadUrl(startUrl)
                    }
                },
            )
        }

        Spacer(Modifier.height(10.dp))

        // Bottom Helper Bar: Done Creating/Signing In -> Return to App
        NeumorphicButton(
            onClick = {
                runCatching {
                    CookieManager.getInstance().flush()
                    com.vauth.foxyvpn.data.ControlPlaneHttp.cookieJar.syncFromWebView()
                }
                webViewRef?.evaluateJavascript(
                    """
                    (function() {
                        var el = document.querySelector('input[type="email"], input[name="email"]');
                        if (el && el.value) return el.value;
                        return '';
                    })();
                    """.trimIndent()
                ) { rawResult ->
                    val cleaned = rawResult?.trim()?.trim('"').orEmpty()
                    if (cleaned.contains("@")) {
                        onCapturedEmail(cleaned)
                    }
                    onCloseAndSwitchToLogin()
                }
            },
            accentColor = neu.accent,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = when (s.language) {
                    FoxyLanguage.FA -> "تایید و بازگشت به برنامه"
                    FoxyLanguage.AR -> "تأكيد والعودة للتطبيق"
                    FoxyLanguage.EN -> "Confirm & Return to App"
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = fonts.headingFont,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
        }
    }
}
