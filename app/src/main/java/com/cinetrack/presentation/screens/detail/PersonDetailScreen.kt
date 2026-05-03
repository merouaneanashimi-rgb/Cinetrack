package com.cinetrack.presentation.screens.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cinetrack.BuildConfig
import com.cinetrack.domain.model.Person
import com.cinetrack.domain.model.Credit
import com.cinetrack.domain.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val personRepository: PersonRepository
) : androidx.lifecycle.ViewModel() {
    private val personId: Long = savedStateHandle.get<String>("personId")?.toLongOrNull() ?: 0L

    val person: StateFlow<Person?> = personRepository.getPersonById(personId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _credits = MutableStateFlow<List<Credit>>(emptyList())
    val credits: StateFlow<List<Credit>> = _credits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        loadPerson()
    }

    private fun loadPerson() {
        viewModelScope.launch {
            _isLoading.value = true
            val p = personRepository.getPersonById(personId).first()
            p?.let {
                val result = personRepository.getPersonCredits(it.tmdbId)
                if (result is com.cinetrack.util.Result.Success) {
                    _credits.value = result.data
                }
            }
            _isLoading.value = false
        }
    }

    fun selectTab(index: Int) { _selectedTab.value = index }

    fun toggleFollow() {
        viewModelScope.launch {
            val current = person.value ?: return@launch
            personRepository.toggleFollow(personId, !current.isFollowed)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Long,
    onBackClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onShowClick: (Long) -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    val person by viewModel.person.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(person?.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    person?.let { p ->
                        IconButton(onClick = { viewModel.toggleFollow() }) {
                            Icon(
                                imageVector = if (p.isFollowed) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                contentDescription = if (p.isFollowed) "Unfollow" else "Follow",
                                tint = if (p.isFollowed) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            person?.let { personData ->
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        PersonHeader(person = personData)
                    }
                    if (!personData.biography.isNullOrEmpty()) {
                        item {
                            BiographySection(biography = personData.biography)
                        }
                    }
                    item {
                        TabRow(selectedTabIndex = selectedTab) {
                            listOf("Movies", "TV Shows").forEachIndexed { index, title ->
                                Tab(selected = selectedTab == index, onClick = { viewModel.selectTab(index) }, text = { Text(title) })
                            }
                        }
                    }
                    val filteredCredits = if (selectedTab == 0) {
                        credits.filter { it.mediaType == "movie" }
                    } else {
                        credits.filter { it.mediaType == "tv" }
                    }
                    items(filteredCredits, key = { it.id }) { credit ->
                        CreditRow(
                            credit = credit,
                            onClick = {
                                if (credit.mediaType == "movie") onMovieClick(credit.id.toLong())
                                else onShowClick(credit.id.toLong())
                            }
                        )
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Person not found")
            }
        }
    }
}

@Composable
fun PersonHeader(person: Person, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w342${person.profilePath}")
                .crossfade(true)
                .build(),
            contentDescription = person.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = person.name, style = MaterialTheme.typography.headlineSmall)
            person.knownForDepartment?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            person.birthday?.let {
                Text(text = "Born: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            person.placeOfBirth?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun BiographySection(biography: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Biography", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = biography,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
        if (!expanded) {
            Text(
                text = "Read more",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { expanded = true }
            )
        }
    }
}

@Composable
fun CreditRow(credit: Credit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w185${credit.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = credit.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = credit.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                credit.character?.let {
                    Text(text = "as $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                credit.job?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                credit.releaseDate?.let {
                    Text(text = it.take(4), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
