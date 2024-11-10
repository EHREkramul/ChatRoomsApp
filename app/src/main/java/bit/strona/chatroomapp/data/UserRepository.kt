package bit.strona.chatroomapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val _auth: FirebaseAuth,
    private val _firestore: FirebaseFirestore
) {

    suspend fun signUp(email: String, password: String, firstName: String, lastName: String): Result<Boolean> {
        return try {
            _auth.createUserWithEmailAndPassword(email, password).await()

            val user = User(firstName, lastName, email)
            saveUserToFirestore(user)

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            _auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        _firestore.collection("users").document(user.email).set(user).await()
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val uid = _auth.currentUser?.email
            if (uid != null) {
                val userDocument = _firestore.collection("users").document(uid).get().await()
                val user = userDocument.toObject(User::class.java)
                if (user != null) {
                    Log.d("user2","$uid")
                    Result.Success(user)
                } else {
                    Result.Error(Exception("User data not found"))
                }
            } else {
                Result.Error(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}