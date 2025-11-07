package me.jorgepinto.unabshop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {

    val auth = Firebase.auth
    val user = auth.currentUser
    val repo = remember { ProductoRepository() }
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // Campos del formulario
    var nombre by remember { mutableStateOf(TextFieldValue("")) }
    var descripcion by remember { mutableStateOf(TextFieldValue("")) }
    var precio by remember { mutableStateOf(TextFieldValue("")) }

    // Cargar productos al iniciar
    LaunchedEffect(Unit) {
        repo.obtenerProductos { productos = it }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Unab Shop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notificaciones */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = { /* TODO: Carrito */ }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        // Navegar a login y limpiar stack
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFFF9900),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            if (user != null) {
                Text(
                    text = "Bienvenido: ${user.email}",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Campos del formulario
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Botón para agregar producto
            Button(
                onClick = {
                    val numero = productos.size + 1
                    val nombreFinal = if (nombre.text.isNotBlank())
                        "Producto $numero: ${nombre.text}"
                    else
                        "Producto $numero"

                    val nuevo = Producto(
                        nombre = nombreFinal,
                        descripcion = descripcion.text.ifBlank { "Sin descripción" },
                        precio = precio.text.toDoubleOrNull() ?: 0.0
                    )

                    repo.agregarProducto(nuevo) { ok ->
                        if (ok) {
                            repo.obtenerProductos { productos = it }
                            nombre = TextFieldValue("")
                            descripcion = TextFieldValue("")
                            precio = TextFieldValue("")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Agregar producto", color = Color.White)
            }

            Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)

            // Lista de productos
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                itemsIndexed(productos) { index, producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Descripción: ${producto.descripcion}")
                                Text("Precio: $${producto.precio}")
                            }
                            IconButton(onClick = {
                                producto.id?.let { id ->
                                    repo.eliminarProducto(id) { ok ->
                                        if (ok) repo.obtenerProductos { productos = it }
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
