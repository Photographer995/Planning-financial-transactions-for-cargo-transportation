package com.example.finlogcalc.utils.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor // Import SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.finlogcalc.R
import com.example.finlogcalc.NeonColors

@Composable
fun ChatBackgroundChangerDialog(
    onDismiss: () -> Unit,
    onBackgroundSelected: (Int) -> Unit,
    currentBackground: Int
) {
    var selectedBackground by remember { mutableStateOf(currentBackground) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Выберите фон",
                color = NeonColors.Cyan400 // Neon color for the title
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.4f)) // Translucent background for the list
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(NeonColors.Cyan500.copy(alpha = 0.3f), NeonColors.Purple500.copy(alpha = 0.3f))),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp)
            ) {
                items(chatBackgrounds) { background ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBackground = background.imageRes }
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedBackground == background.imageRes) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            NeonColors.Purple500.copy(alpha = 0.6f),
                                            NeonColors.Pink500.copy(alpha = 0.6f)
                                        )
                                    )
                                } else {
                                    SolidColor(Color.Black.copy(alpha = 0.2f)) // Changed to SolidColor brush
                                }
                            )
                            .border(
                                width = 1.dp,
                                brush = if (selectedBackground == background.imageRes) {
                                    Brush.horizontalGradient(listOf(NeonColors.Purple400, NeonColors.Pink500))
                                } else {
                                    Brush.horizontalGradient(listOf(NeonColors.Cyan500.copy(alpha = 0.2f), NeonColors.Purple500.copy(alpha = 0.2f)))
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .shadow(
                                elevation = if (selectedBackground == background.imageRes) 8.dp else 2.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = if (selectedBackground == background.imageRes) NeonColors.Purple500.copy(alpha = 0.4f) else Color.Transparent,
                                spotColor = if (selectedBackground == background.imageRes) NeonColors.Purple500.copy(alpha = 0.4f) else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedBackground == background.imageRes,
                            onClick = { selectedBackground = background.imageRes },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NeonColors.Cyan400,
                                unselectedColor = NeonColors.Gray400
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = background.thumbnailRes),
                            contentDescription = background.name,
                            modifier = Modifier
                                .size(60.dp) // Slightly larger thumbnail
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, NeonColors.Cyan500.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = background.name,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onBackgroundSelected(selectedBackground)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(NeonColors.PurpleToPinkGradient))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(NeonColors.Purple400, NeonColors.Pink500)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = NeonColors.Purple500.copy(alpha = 0.4f),
                        spotColor = NeonColors.Purple500.copy(alpha = 0.4f)
                    )
            ) {
                Text("Применить", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(NeonColors.Cyan500, NeonColors.Purple500))
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonColors.Cyan400),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = NeonColors.Cyan500.copy(alpha = 0.2f),
                        spotColor = NeonColors.Cyan500.copy(alpha = 0.2f)
                    )
            ) {
                Text("Отмена")
            }
        },
        containerColor = Color.Black.copy(alpha = 0.7f), // Translucent background for the dialog itself
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(listOf(NeonColors.Cyan500.copy(alpha = 0.5f), NeonColors.Purple500.copy(alpha = 0.5f))),
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = NeonColors.Cyan500.copy(alpha = 0.2f),
                spotColor = NeonColors.Purple500.copy(alpha = 0.2f)
            )
    )
}
