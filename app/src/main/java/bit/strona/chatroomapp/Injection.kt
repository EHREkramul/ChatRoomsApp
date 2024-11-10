package bit.strona.chatroomapp

import com.google.firebase.firestore.FirebaseFirestore

object Injection {
    private val _instance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    fun instance(): FirebaseFirestore {
        return _instance
    }
}