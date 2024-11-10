package bit.strona.chatroomapp

sealed class Screen(val route:String){
    data object LoginScreen: Screen("login-screen")
    data object SignupScreen: Screen("signup-screen")
    data object ChatRoomsScreen: Screen("chatroom-screen")
    data object ChatScreen: Screen("chat-screen")
}