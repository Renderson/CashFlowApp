package com.renderson.cashflowapp.util.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renderson.cashflowapp.extensions.parseBrazilianCurrencyToDouble
import java.text.NumberFormat
import java.util.Locale

private const val CURRENCY_MAX_DIGITS = 11

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowTextField(
    modifier: Modifier,
    value: String = "",
    hint: String = "",
    inputType: TypeInputEnum = TypeInputEnum.NONE,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Done,
    enabled: Boolean = true,
    maxLength: Int = 120,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    onInputChange: (input: String) -> Unit = {}
) {
    val isCurrency = inputType == TypeInputEnum.CURRENCY
    var inputTf by remember {
        mutableStateOf(initialTextFieldValue(value, inputType.type))
    }
    var inputStr by remember { mutableStateOf(if (isCurrency) "" else value) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(value, inputType.type) {
        if (isCurrency) {
            val synced = initialTextFieldValue(value, inputType.type)
            if (synced.text != inputTf.text) {
                inputTf = synced
            }
        } else {
            if (value != inputStr) {
                inputStr = value
            }
        }
    }

    if (isCurrency) {
        val currencyInteractionSource = remember { MutableInteractionSource() }
        BasicTextField(
            value = inputTf,
            modifier = modifier,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            onValueChange = { newValue ->
                var formattedValue = newValue.text
                val digitsOnly = formattedValue.extractNumbers?.toString()?.take(CURRENCY_MAX_DIGITS) ?: ""
                if (digitsOnly.length <= CURRENCY_MAX_DIGITS) {
                    formattedValue = digitsOnly.asCurrencyPtBR()
                    val cursorPosition = formattedValue.length.coerceAtMost(formattedValue.length)
                    inputTf = TextFieldValue(text = formattedValue, selection = TextRange(cursorPosition))
                    onInputChange(formattedValue)
                }
            },
            enabled = enabled,
            interactionSource = currencyInteractionSource,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction,
                capitalization = capitalization
            ),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = inputTf.text,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = { Text(hint) },
                    label = { Text(hint) },
                    singleLine = true,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = currencyInteractionSource,
                    colors = colors
                )
            }
        )
    } else {
        OutlinedTextField(
            modifier = modifier,
            value = inputStr,
            label = { Text(text = hint, color = MaterialTheme.colorScheme.primary) },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
                capitalization = capitalization
            ),
            enabled = enabled,
            visualTransformation = { visualTransformationGetType(inputType.type, it) },
            isError = isError,
            colors = colors,
            onValueChange = { newValue ->
                inputStr = newValue.take(maxLength)
                onInputChange(inputStr)
            }
        )
    }
}

fun String.asCurrencyPtBR(): String {
    if (isEmpty()) return "R$ 0,00"
    val intValue = extractNumbers ?: 0L
    val currencyValue = intValue * 0.01
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(currencyValue)
}

private val String.extractNumbers: Long?
    get() = filter { it in '0'..'9' }.take(CURRENCY_MAX_DIGITS).toLongOrNull()

private fun initialTextFieldValue(value: String, type: String): TextFieldValue {
    val text = when (type) {
        TypeInputEnum.CURRENCY.type -> {
            if (value.isBlank()) "R$ 0,00"
            else (value.parseBrazilianCurrencyToDouble() * 100).toLong().toString().asCurrencyPtBR()
        }
        else -> value
    }
    return TextFieldValue(text = text, selection = TextRange(text.length))
}

private fun visualTransformationGetType(type: String, input: AnnotatedString): TransformedText {
    return when (type) {
        TypeInputEnum.CURRENCY.type -> BrazilianCurrencyTransformation().filter(AnnotatedString(input.text))
        else -> visualTransformationToNormal(input)
    }
}

fun visualTransformationToNormal(input: AnnotatedString): TransformedText {

    val annotatedString = AnnotatedString(input.text)

    val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = offset

        override fun transformedToOriginal(offset: Int): Int = offset
    }
    return TransformedText(annotatedString, offsetTranslator)
}

/**
 * Valor interno = apenas dígitos = valor em centavos (ex: "1050" → R$ 10,50).
 * Permite digitar centavos naturalmente (os dois últimos dígitos são sempre decimais).
 */
class BrazilianCurrencyTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it in '0'..'9' }.take(CURRENCY_MAX_DIGITS)
        if (raw.isEmpty()) {
            val display = "R$ 0,00"
            val emptyMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = 0.coerceIn(0, display.length)
                override fun transformedToOriginal(offset: Int) = 0
            }
            return TransformedText(AnnotatedString(display), emptyMapping)
        }
        val cents = raw.toLongOrNull() ?: 0L
        val intPart = (cents / 100).toString()
        val decPart = (cents % 100).toString().padStart(2, '0')
        val intDisplay = if (intPart == "0" && raw.isNotEmpty()) intPart else intPart.reversed().chunked(3).joinToString(".").reversed()
        val display = "R$ $intDisplay,$decPart"

        val origToTrans = IntArray(raw.length + 1)
        val transToOrig = IntArray(display.length + 1)
        origToTrans[0] = 0
        transToOrig[0] = 0
        var t = 3
        var o = 0
        for (i in intPart.indices) {
            if (i > 0 && (intPart.length - i) % 3 == 0) {
                t++
                if (o <= raw.length) transToOrig[t] = o
            }
            o++
            t++
            if (o <= raw.length) origToTrans[o] = t
            if (t <= display.length) transToOrig[t] = o
        }
        t++
        if (o <= raw.length) transToOrig[t] = o
        o++
        if (o <= raw.length) origToTrans[o] = t
        if (t <= display.length) transToOrig[t] = o
        for (i in decPart.indices) {
            o++
            t++
            if (o <= raw.length) origToTrans[o] = t
            if (t <= display.length) transToOrig[t] = o
        }
        for (i in o..raw.length) origToTrans[i] = t
        for (i in t..display.length) transToOrig[i] = raw.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                origToTrans.getOrElse(offset.coerceIn(0, raw.length)) { display.length }

            override fun transformedToOriginal(offset: Int): Int =
                transToOrig.getOrElse(offset.coerceIn(0, display.length)) { raw.length }
        }
        return TransformedText(AnnotatedString(display), offsetMapping)
    }
}

enum class TypeInputEnum(val type: String) {
    CURRENCY("CURRENCY"),
    NONE("")
}

@Preview(showBackground = true)
@Composable
fun CashFlowTextFieldPreview() {
    CashFlowTextField(
        modifier = Modifier,
        value = "",
        hint = "Label",
        onInputChange = {}
    )
}