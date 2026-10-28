package com.example.gamedomino.data.repository

import com.example.gamedomino.data.model.User
import com.example.gamedomino.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Repositorio encargado de gestionar las operaciones de autenticación con Firebase.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://gamedomino-f34b5-default-rtdb.firebaseio.com/")
) {

    /**
     * Retorna el usuario actualmente autenticado o null si no hay sesión.
     */
    fun getCurrentUser() = auth.currentUser

    /**
     * Registra un nuevo usuario con correo y contraseña, y guarda sus datos en la base de datos.
     */
    suspend fun registerUser(email: String, pass: String, username: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = result.user?.uid ?: throw Exception("ID de usuario no encontrado")

            val newUser = User(id = userId, email = email, username = username)

            // Guardar la información del usuario en el nodo 'users'
            database.getReference("users").child(userId).setValue(newUser).await()

            Resource.Success(newUser)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error desconocido al registrar usuario")
        }
    }

    /**
     * Inicia sesión con correo y contraseña.
     */
    suspend fun loginUser(email: String, pass: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val userId = result.user?.uid ?: throw Exception("Usuario no encontrado")

            // Obtener el nombre de usuario guardado en Realtime Database
            val snapshot = database.getReference("users").child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
                ?: User(id = userId, email = email, username = email.substringBefore("@"))

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al iniciar sesión")
        }
    }


    fun logout() {
        auth.signOut()
    }
}