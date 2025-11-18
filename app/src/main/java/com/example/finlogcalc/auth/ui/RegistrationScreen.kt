package com.example.finlogcalc.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.finlogcalc.R
import com.example.finlogcalc.Screen

@Composable
fun RegistrationScreen(navController: NavController, rootNavController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameFieldFocused by remember { mutableStateOf(false) }
    var emailFieldFocused by remember { mutableStateOf(false) }
    var passwordFieldFocused by remember { mutableStateOf(false) }
    var confirmPasswordFieldFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2C005B), Color(0xFF1B003A))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fuel), // Replace with your logo
                contentDescription = "Fuel Calculator",
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Text(
                "Fuel Calculator",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Добро пожаловать в будущее расчёта топлива",
                fontSize = 16.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            // Tab-like buttons for Login/Registration
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3A006F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.navigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Вход", color = Color.White)
                    }
                    Button(
                        onClick = { /* Current screen */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF00C4FF), Color(0xFFAD00FF))), RoundedCornerShape(24.dp)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Регистрация", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Registration form
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Создать аккаунт",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Зарегистрируйтесь для начала работы",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("Имя", color = Color.LightGray.copy(alpha = 0.8f))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ваше имя", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Имя", tint = if (nameFieldFocused) Color(0xFF00C4FF) else Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { nameFieldFocused = it.isFocused }
                        .border(if (nameFieldFocused) 1.dp else 0.dp, Brush.horizontalGradient(listOf(Color(0xFF00C4FF), Color(0xFFAD00FF))), RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        cursorColor = Color(0xFF00C4FF),
                        focusedContainerColor = Color(0xFF3A006F),
                        unfocusedContainerColor = Color(0xFF3A006F),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Email", color = Color.LightGray.copy(alpha = 0.8f), modifier = Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("your@email.com", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = if (emailFieldFocused) Color(0xFF00C4FF) else Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { emailFieldFocused = it.isFocused }
                        .border(if (emailFieldFocused) 1.dp else 0.dp, Brush.horizontalGradient(listOf(Color(0xFF00C4FF), Color(0xFFAD00FF))), RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        cursorColor = Color(0xFF00C4FF),
                        focusedContainerColor = Color(0xFF3A006F),
                        unfocusedContainerColor = Color(0xFF3A006F),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Пароль", color = Color.LightGray.copy(alpha = 0.8f), modifier = Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("********", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Пароль", tint = if (passwordFieldFocused) Color(0xFF00C4FF) else Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { passwordFieldFocused = it.isFocused }
                        .border(if (passwordFieldFocused) 1.dp else 0.dp, Brush.horizontalGradient(listOf(Color(0xFF00C4FF), Color(0xFFAD00FF))), RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        cursorColor = Color(0xFF00C4FF),
                        focusedContainerColor = Color(0xFF3A006F),
                        unfocusedContainerColor = Color(0xFF3A006F),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Подтвердите пароль", color = Color.LightGray.copy(alpha = 0.8f), modifier = Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("********", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Подтвердите пароль", tint = if (confirmPasswordFieldFocused) Color(0xFF00C4FF) else Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { confirmPasswordFieldFocused = it.isFocused }
                        .border(if (confirmPasswordFieldFocused) 1.dp else 0.dp, Brush.horizontalGradient(listOf(Color(0xFF00C4FF), Color(0xFFAD00FF))), RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        cursorColor = Color(0xFF00C4FF),
                        focusedContainerColor = Color(0xFF3A006F),
                        unfocusedContainerColor = Color(0xFF3A006F),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = false, // No remember me for registration
                            onCheckedChange = { /* No action */ },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF00C4FF),
                                uncheckedColor = Color.LightGray,
                                checkmarkColor = Color.White
                            )
                        )
                        Text("Я согласен с условиями использования и политикой конфиденциальности", color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp)) // Add spacer before registration button

                Button(
                    onClick = {
                        rootNavController.navigate(Screen.MainMenu.route) {
                            popUpTo(Screen.Auth.route) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00C4FF), Color(0xFFAD00FF))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Зарегистрироваться", color = Color.White, fontSize = 16.sp)
                    }
                }
            } // End of Registration form Column

            // "Or sign in with" section remains outside the Registration form Column
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Или войдите через",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Handle Google registration */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Google", color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { /* Handle Meta registration */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Meta", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen(rememberNavController(), rememberNavController())
}