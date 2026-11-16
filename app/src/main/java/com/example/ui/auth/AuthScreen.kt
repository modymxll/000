package com.example.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.components.BubblesAtmosphericBackdrop
import com.example.ui.components.BubblesLogoMark
import com.example.ui.components.GlassSurface
import com.example.ui.components.PulsingLiveDot
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.BubblesCyan
import com.example.ui.theme.BubblesCyanBorder
import com.example.ui.theme.BubblesCyanBright
import com.example.ui.theme.BubblesCyanDark
import com.example.ui.theme.BubblesGray
import com.example.ui.theme.BubblesSlateDark
import com.example.ui.theme.GlassCardTranslucent
import com.example.ui.theme.GlassPillFloating
import com.example.ui.theme.GlassSpecHighlight
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusRed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AuthScreen(
    onToggleLanguage: () -> Unit,
    onLoginWithCode: suspend (code: String) -> Result<Unit>
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var accessCode by remember { mutableStateOf("") }
    var showPinDigits by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            for (delta in listOf(-18f, 18f, -12f, 12f, -6f, 0f)) {
                shakeOffset.animateTo(delta, spring(stiffness = 1400f))
            }
        }
    }

    suspend fun performCodeLogin(codeToTry: String) {
        if (codeToTry.length < 4 || isSubmitting) return
        isSubmitting = true
        errorMessage = null
        val res = onLoginWithCode(codeToTry)
        isSubmitting = false
        if (res.isFailure) {
            errorMessage = res.exceptionOrNull()?.message ?: if (s.isArabic) "كود الدخول غير صحيح" else "Invalid access code"
            triggerShake()
        }
    }

    fun submitCode(codeToTry: String) {
        if (codeToTry.length < 4 || isSubmitting) return
        scope.launch { performCodeLogin(codeToTry) }
    }

    // Auto-submit on 6th digit entered cleanly
    LaunchedEffect(accessCode) {
        if (accessCode.length == 6) {
            performCodeLogin(accessCode)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BubblesAtmosphericBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Brand Pill + Language Switcher
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GlassPillFloating,
                    border = BorderStroke(1.dp, GlassSpecHighlight),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingLiveDot(StatusEmerald)
                        Text(
                            text = "Bubbles Laundry",
                            style = MaterialTheme.typography.labelLarge,
                            color = BubblesSlateDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    onClick = onToggleLanguage,
                    shape = RoundedCornerShape(20.dp),
                    color = GlassPillFloating,
                    border = BorderStroke(1.dp, BubblesCyanBorder),
                    shadowElevation = 4.dp,
                    modifier = Modifier.testTag("auth_language_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.Language, contentDescription = null, tint = BubblesCyanDark, modifier = Modifier.size(17.dp))
                        Text(
                            text = if (s.isArabic) "EN" else "AR",
                            color = BubblesCyanDark,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            GlassSurface(
                cornerRadius = 30.dp,
                containerColor = GlassCardTranslucent,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(BubblesCyan.copy(alpha = 0.28f), Color.Transparent)
                                        )
                                    )
                            )
                            BubblesLogoMark(modifier = Modifier.size(50.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (s.isArabic) "الدخول بكود التحقق" else "Access Code Login",
                                style = MaterialTheme.typography.titleLarge,
                                color = BubblesSlateDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (s.isArabic) {
                                    "أدخل كود الإدارة لصلاحيات المدير أو كود المندوب"
                                } else {
                                    "Enter Admin code or Driver code to unlock your workspace"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = BubblesGray
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = BubblesCyanDark, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (s.isArabic) "كود الدخول (4 - 6 أرقام)" else "Access Code (4 - 6 digits)",
                                style = MaterialTheme.typography.labelMedium,
                                color = BubblesCyanDark
                            )
                        }
                        IconButton(
                            onClick = { showPinDigits = !showPinDigits },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (showPinDigits) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Toggle code visibility",
                                tint = BubblesGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 6 animated glass PIN slots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        for (idx in 0 until 6) {
                            val ch = accessCode.getOrNull(idx)
                            val isActiveSlot = idx == accessCode.length
                            val filled = ch != null
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (filled) BubblesCyan.copy(alpha = 0.14f)
                                        else Color.White.copy(alpha = 0.65f)
                                    )
                                    .border(
                                        width = if (isActiveSlot || filled) 1.6.dp else 1.dp,
                                        color = when {
                                            isActiveSlot -> BubblesCyan
                                            filled -> BubblesCyanDark.copy(alpha = 0.5f)
                                            else -> Color.White.copy(alpha = 0.85f)
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (filled) {
                                    if (showPinDigits) {
                                        Text(
                                            text = ch.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = BubblesSlateDark
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)
                                                .clip(CircleShape)
                                                .background(BubblesCyanDark)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Direct keyboard input option
                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(6)
                            accessCode = digits
                            errorMessage = null
                        },
                        placeholder = { Text(if (s.isArabic) "أو اكتب الكود هنا..." else "Or type access code...") },
                        leadingIcon = { Icon(Icons.Rounded.Key, contentDescription = null, tint = BubblesCyanDark) },
                        singleLine = true,
                        visualTransformation = if (showPinDigits) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitCode(accessCode) }),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BubblesCyan,
                            unfocusedBorderColor = BubblesCyanBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.55f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.42f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("access_code_input")
                    )

                    // Glassmorphic Numeric Keypad
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9")
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        rows.forEach { rowDigits ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowDigits.forEach { d ->
                                    GlassKeypadButton(
                                        label = d,
                                        onClick = {
                                            if (accessCode.length < 6) {
                                                errorMessage = null
                                                accessCode += d
                                            }
                                        },
                                        modifier = Modifier.weight(1f).testTag("pin_key_$d")
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassKeypadActionButton(
                                icon = Icons.Rounded.ClearAll,
                                contentDescription = "Clear",
                                tint = StatusRed,
                                onClick = {
                                    accessCode = ""
                                    errorMessage = null
                                },
                                modifier = Modifier.weight(1f).testTag("pin_key_clear")
                            )
                            GlassKeypadButton(
                                label = "0",
                                onClick = {
                                    if (accessCode.length < 6) {
                                        errorMessage = null
                                        accessCode += "0"
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("pin_key_0")
                            )
                            GlassKeypadActionButton(
                                icon = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Backspace",
                                tint = BubblesCyanDark,
                                onClick = {
                                    if (accessCode.isNotEmpty()) {
                                        accessCode = accessCode.dropLast(1)
                                        errorMessage = null
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("pin_key_back")
                            )
                        }
                    }

                    Button(
                        onClick = { submitCode(accessCode) },
                        enabled = accessCode.length >= 4 && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(listOf(BubblesCyanBright, BubblesCyan, BubblesCyanDark))
                            )
                            .testTag("code_sign_in_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                        } else {
                            Icon(Icons.Rounded.LockOpen, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (s.isArabic) "دخول" else "Unlock",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = StatusRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = StatusRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassKeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.66f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
        shadowElevation = 2.dp,
        modifier = modifier.height(50.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BubblesSlateDark
            )
        }
    }
}

@Composable
private fun GlassKeypadActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = tint.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = modifier.height(50.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(21.dp))
        }
    }
}
