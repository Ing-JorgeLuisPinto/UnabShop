package me.jorgepinto.unabshop

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore

class ProductoRepository {

    private val db = Firebase.firestore

    fun agregarProducto(producto: Producto, callback: (Boolean) -> Unit) {
        db.collection("productos")
            .add(producto)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun obtenerProductos(callback: (List<Producto>) -> Unit) {
        db.collection("productos")
            .get()
            .addOnSuccessListener { result ->
                val productos = result.documents.map { doc ->
                    val producto = doc.toObject(Producto::class.java)
                    producto!!.copy(id = doc.id)
                }
                callback(productos)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun eliminarProducto(id: String, callback: (Boolean) -> Unit) {
        db.collection("productos").document(id)
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
