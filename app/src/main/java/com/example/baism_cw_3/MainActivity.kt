package com.example.baism_cw_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// --- Modele ---
data class CharacterResponse(val results: List<Character>)

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val image: String
)

// --- API ---
interface RickApi {
    @GET("character")
    suspend fun getCharacters(): CharacterResponse

    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): Character
}

val api: RickApi = Retrofit.Builder()
    .baseUrl("https://rickandmortyapi.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(RickApi::class.java)

// --- UI ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App() } }
    }
}

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    var characters by remember { mutableStateOf<List<Character>>(emptyList()) }
    var selected by remember { mutableStateOf<Character?>(null) }
    var idText by remember { mutableStateOf("1") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        // Przycisk 1: lista postaci
        Button(onClick = {
            scope.launch {
                try {
                    characters = api.getCharacters().results
                    error = null
                } catch (e: Exception) {
                    error = e.message
                }
            }
        }) { Text("Pobierz wszystkie postacie") }

        // Przycisk 2: postać po ID
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = idText,
                onValueChange = { idText = it },
                label = { Text("ID") },
                modifier = Modifier.width(100.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                scope.launch {
                    try {
                        selected = api.getCharacter(idText.toIntOrNull() ?: 1)
                        error = null
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }) { Text("Pobierz po ID") }
        }

        error?.let { Text("Błąd: $it", color = Color.Red) }

        // Dane konkretnej postaci
        selected?.let { c ->
            Text(
                "ID: ${c.id}\nImię: ${c.name}\nStatus: ${c.status}\nGatunek: ${c.species}\nPłeć: ${c.gender}",
                Modifier.padding(vertical = 8.dp)
            )
        }

        // Lista postaci
        LazyColumn {
            items(characters) { c ->
                Text("${c.id}. ${c.name} – ${c.species}", Modifier.padding(8.dp))
            }
        }
    }
}