package com.howest.mobilesecurity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.howest.mobilesecurity.PokeApiFetcher.Companion.fetchPokemonInfo
import com.howest.mobilesecurity.ui.theme.MobilesecurityTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var user: MyDatabaseHelper.User? = null
    private var pokemonList: MutableList<String> by mutableStateOf(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseDatabase.getInstance()
            .setPersistenceEnabled(true)

        FirebaseApp.initializeApp(this)

        val dbHelper = MyDatabaseHelper(this)

        setContent {
            MobilesecurityTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "start") {
                    composable("start") {
                        StartScreen(dbHelper, navController = navController)
                    }
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable("pokedex") {
                        PokeDex(navController = navController, user!!.name, dbHelper)
                    }
                    composable("pokemon_details/{pokemonName}") { backStackEntry ->
                        val pokemonName = backStackEntry.arguments?.getString("pokemonName") ?: ""
                        PokemonDetails(pokemonName, navController, dbHelper)
                    }
                }
            }
        }
    }


    @Composable
    fun StartScreen(dbHelper: MyDatabaseHelper,
                    navController: NavController
    )
    {
        var username by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Welcome",
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Login/Register",
                fontSize = 25.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = username,
                onValueChange = { username = it },
                label = { R.string.username },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { R.string.password },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() } )
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Button(onClick = {
                    if(passwordInput.isEmpty() || username.isEmpty()){
                        errorMsg = "You have to fill in both of the inputs."
                    }
                    val isPasswordMatch = dbHelper.checkPasswordMatch(username, passwordInput)
                    if (isPasswordMatch) {
                        user = dbHelper.getUserByUsername(username)
                        navController.navigate("home")
                    } else {
                        errorMsg = "Wrong Password!"
                        passwordInput = ""
                    }
                }) {
                    Text(text = "Login")
                }
                Button(onClick = {
                    val isUserInUse = dbHelper.checkForUserInUse(username)

                    if(!(username.isEmpty() || passwordInput.isEmpty())){
                        if(!isUserInUse){
                            dbHelper.insertUser(username, passwordInput)
                            dbHelper.addCurrentUserToFirebase(username)
                            user = dbHelper.getUserByUsername(username)
                            navController.navigate("home")
                        }else {
                            errorMsg = "Username already in use."
                        }
                    }else {
                        errorMsg = "Please fill in username AND password"
                    }
                    username = ""
                    passwordInput = ""
                }) {
                    Text(text = "Register")
                }
            }
            Text(text = errorMsg,
                color = Color.Red)
        }
    }

    @Composable
    fun PokeDex(
        navController: NavHostController,
        name: String,
        dbHelper: MyDatabaseHelper
    ) {
        var errorMsg by remember { mutableStateOf("")}
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$name's PokeDex",
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.padding(16.dp))

            pokemonList = dbHelper.getUserPokemon(user!!.id).toMutableList();
            if (pokemonList.isNotEmpty()) {
                Column {
                    pokemonList.forEach { pokemon ->
                        Text(
                            text = pokemon,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("pokemon_details/$pokemon")
                                }
                                .padding(bottom = 4.dp)
                        )
                    }
                }
            } else {
                errorMsg = "No pokemons found"
            }

            Row(modifier = Modifier.padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
                Button(
                    onClick = {
                        navController.navigate("home")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Home")
                }
            }

            Text(
                text = errorMsg,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    @Composable
    fun PokemonDetails(
        pokemonName: String,
        navController: NavHostController,
        dbHelper: MyDatabaseHelper
    ) {
        var errorMsg by remember { mutableStateOf("")}
        var pokemon by remember { mutableStateOf("") }
        var pokemonImg by remember { mutableStateOf("") }
        LaunchedEffect(pokemonName) { // Fetch on navigation
            fetchPokemonInfo(pokemonName) { fetchedPokemon, imageUrl ->
                if (fetchedPokemon != null) {
                    pokemon = (fetchedPokemon ?: "")
                    pokemonImg = (imageUrl ?: "")
                } else {
                    errorMsg = "Pokemon not found"
                }
            }
        }
        Column {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                model = pokemonImg,
                contentDescription = "The searched pokemon",
            )
            Text(text = pokemon,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp))

            if(!pokemonList.contains(pokemonName.lowercase())) {
                Button(onClick = { dbHelper.savePokemonForUser(user!!.id, pokemonName.lowercase())}) {
                    Text(text = "Add Pokemon to PokeDex!")
                }
            }
        }
        Row(modifier = Modifier.padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
            Button(
                onClick = {
                    navController.navigate("home")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Home")
            }
        }
        Text(text = errorMsg,
            color = Color.Red)
    }

    @Composable
    fun HomeScreen(
        navController: NavHostController
    ) {
        val errorMsg by remember { mutableStateOf("")}
        val focusManager = LocalFocusManager.current
        var pokemonName by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                OutlinedTextField(
                    value = pokemonName,
                    onValueChange = { pokemonName = it },
                    label = { Text(stringResource(R.string.pokemon)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() } )
                )

                Spacer(modifier = Modifier.padding(16.dp))
                Button(
                    onClick = {
                        navController.navigate("pokemon_details/$pokemonName")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Search For Pokemon!")
                }

            Spacer(modifier = Modifier.padding(16.dp))

            Button(onClick = { navController.navigate("pokedex") }) {
                Text(text = "Go to PokeDex!")
            }

            Button(onClick = { navController.navigate("login") }) {
                Text(text = "Logout")
            }

            Text(text = errorMsg,
                color = Color.Red)
        }
    }

}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobilesecurityTheme {
        Greeting("Android")
    }
}