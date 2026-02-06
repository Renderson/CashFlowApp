package com.renderson.cashflowapp.util.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview

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
    colors: TextFieldColors = TextFieldDefaults.colors(),
    onInputChange: (input: String) -> Unit = {}
) {

    var input by remember { mutableStateOf(value) }
    LaunchedEffect(value) { input = value }

    var isError by remember { mutableStateOf(false) }

    TextField(
        modifier = modifier,
        value = input,
        label = {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.primary
            )
        },
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
            val filtered = when (inputType.type) {
                TypeInputEnum.CURRENCY.type -> filterCurrencyInput(newValue)
                else -> newValue.take(maxLength)
            }
            if (filtered.length <= maxLength) {
                input = filtered
                if (input.isEmpty()) {
                    isError = false
                    onInputChange(input)
                } else {
                    val (isValid, formattedInput) = validateInput(inputType.type, input)
                    if (isValid) {
                        onInputChange(formattedInput)
                        isError = false
                    } else {
                        onInputChange("")
                        isError = true
                    }
                }
            }
        }
    )
}

private fun validateInput(inputType: String, input: String): Pair<Boolean, String> {
    return when (inputType) {
        TypeInputEnum.CURRENCY.type -> Pair(true, input)
        TypeInputEnum.PESO.type -> Pair(true, input)
        TypeInputEnum.ALTURA.type -> Pair(true, input)
        else -> Pair(true, input)
    }
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

private fun filterCurrencyInput(input: String): String {
    var hasComma = false
    var digitsAfterComma = 0
    return buildString {
        for (c in input) {
            when {
                c in '0'..'9' -> {
                    if (hasComma) {
                        if (digitsAfterComma < 2) {
                            append(c)
                            digitsAfterComma++
                        }
                    } else {
                        append(c)
                    }
                }
                c == ',' && !hasComma -> {
                    append(c)
                    hasComma = true
                }
            }
        }
    }
}

class BrazilianCurrencyTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val commaIndex = raw.indexOf(',')
        val intStr = if (commaIndex < 0) raw.filter { it in '0'..'9' } else raw.take(commaIndex).filter { it in '0'..'9' }
        val decStr = if (commaIndex < 0) "00" else raw.drop(commaIndex + 1).filter { it in '0'..'9' }.take(2).padEnd(2, '0')
        val intDisplay = if (intStr.isEmpty()) "0" else intStr.reversed().chunked(3).joinToString(".").reversed()
        val display = "R$ $intDisplay,$decStr"

        val origToTrans = IntArray(raw.length + 1)
        val transToOrig = IntArray(display.length + 1)
        origToTrans[0] = 0
        transToOrig[0] = 0
        var t = 3
        for (i in 1..display.length) transToOrig[i] = 0
        var o = 0
        for (i in intStr.indices) {
            if (i > 0 && (intStr.length - i) % 3 == 0) {
                t++
                transToOrig[t] = o
            }
            o++
            t++
            if (o <= raw.length) origToTrans[o] = t
            if (t <= display.length) transToOrig[t] = o
        }
        if (commaIndex >= 0) {
            o++
            t++
            if (o <= raw.length) origToTrans[o] = t
            if (t <= display.length) transToOrig[t] = o
            for (i in decStr.indices) {
                o++
                t++
                if (o <= raw.length) origToTrans[o] = t
                if (t <= display.length) transToOrig[t] = o
            }
        }
        for (i in o..raw.length) origToTrans[i] = t

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
    EMAIL("EMAIL"),
    CPF("CPF"),
    NAME("NAME"),
    PESO("PESO"),
    ALTURA("ALTURA"),
    DATA("DATA"),
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