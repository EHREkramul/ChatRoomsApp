package bit.strona.chatroomapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bit.strona.chatroomapp.data.Message
import bit.strona.chatroomapp.data.Result
import bit.strona.chatroomapp.screen.ChatRoomListScreen
import bit.strona.chatroomapp.screen.ChatScreen
import bit.strona.chatroomapp.screen.SignUpScreen
import bit.strona.chatroomapp.screen.LoginScreen
import bit.strona.chatroomapp.ui.theme.ChatRoomAppTheme
import bit.strona.chatroomapp.viewmodel.AuthViewModel
import bit.strona.chatroomapp.viewmodel.RoomViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatRoomAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationGraph()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = Screen.SignupScreen.route) {
        composable(Screen.SignupScreen.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route) }
            )
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route) },
                onSignInSuccess = { navController.navigate(Screen.ChatRoomsScreen.route) }
            )
        }
        composable(Screen.ChatRoomsScreen.route) {
            ChatRoomListScreen (
                roomViewModel = roomViewModel,
                onJoinClicked = {
                    room ->
                    navController.navigate("${Screen.ChatScreen.route}/${room.id}")
                }
            )
        }
        composable("${Screen.ChatScreen.route}/{roomId}"){
            val roomId: String = it.arguments?.getString("roomId")?:""
            ChatScreen(roomId = roomId)
        }
    }
}

class MessageRepository(private val firestore: FirebaseFirestore) {

    suspend fun sendMessage(roomId: String, message: Message): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomId).collection("messages").add(message).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getChatMessages(roomId: String): Flow<List<Message>> {
        return callbackFlow {
            val subscription = firestore.collection("rooms").document(roomId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, _ ->
                    querySnapshot?.let {
                        trySend(
                            it.documents.map { doc ->
                                doc.toObject(Message::class.java)!!.copy()
                            }
                        ).isSuccess
                    }
                }
            awaitClose { subscription.remove() }
        }
    }
}