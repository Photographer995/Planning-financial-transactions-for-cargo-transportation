package com.example.finlogcalc.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.finlogcalc.calculator.ui.Orange400

@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String = { it.toString() }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.5f))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        options.forEach { option ->
            Text(
                text = optionToString(option),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOptionSelected(option) }
                    .background(if (selectedOption == option) Orange400 else Color.Transparent)
                    .padding(vertical = 12.dp),
                color = if (selectedOption == option) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
