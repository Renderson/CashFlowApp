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
        onValueChange = {
            if (it.length <= maxLength) {
                input = it
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
    return when(inputType) {
        //TypeInputEnum.EMAIL.type -> Pair(input.isEmailValid(), input)
        //TypeInputEnum.CPF.type -> Pair(StringUtil.isCPF(input), input)
        //TypeInputEnum.NAME.type -> Pair(input.isNameValid(), input)
        TypeInputEnum.PESO.type -> Pair(true, input)
        TypeInputEnum.ALTURA.type -> Pair(true, input)
        else -> Pair(true, input)
    }
}

private fun visualTransformationGetType(type: String, input: AnnotatedString): TransformedText {
    /*return when (type) {
        TypeInputEnum.CPF.type -> {
            visualTransformationToCPF(input)
        }
        TypeInputEnum.PESO.type -> {
            visualTransformationToWeight(input)
        }
        TypeInputEnum.ALTURA.type -> {
            visualTransformationToHeight(input)
        }
        TypeInputEnum.DATA.type -> {
            visualTransformationToBrDate(input)
        }
        else -> {
            visualTransformationToNormal(input)
        }
    }*/
    return visualTransformationToNormal(input)
}

fun visualTransformationToNormal(input: AnnotatedString): TransformedText {

    val annotatedString = AnnotatedString(input.text)

    val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = offset

        override fun transformedToOriginal(offset: Int): Int = offset
    }
    return TransformedText(annotatedString, offsetTranslator)
}

enum class TypeInputEnum(val type: String) {
    EMAIL("EMAIL"),
    CPF("CPF"),
    NAME("NAME"),
    PESO("PESO"),
    ALTURA("ALTURA"),
    DATA("DATA"),
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