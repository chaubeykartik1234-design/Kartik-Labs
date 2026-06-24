package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CyberGray
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainPortfolioScreen()
            }
        }
    }
}

// --- Direct REST API caller for Gemini API ---
object GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
You are Kartik AI, a highly polished, futuristic AI avatar representing Kartikeya Kumar Chaubey (founder of Kartik Labs).
Kartikeya is a Web Developer, Mobile App Developer, Video Editor, and AI Enthusiast from India.
He has an entrepreneur mindset, a passion for innovation, and focuses on building high-impact digital products.

Social Media & Links:
- YouTube Channel: https://youtube.com/@balliqdefender (with over 200,000+ content views, where he creates content on tech, defense, and development).
- Instagram: https://www.instagram.com/balliqdefender

Portfolio, Services & Credentials:
1. Website Development: Custom high-end web experiences, futuristic glassmorphic portfolios, enterprise React/NextJS projects, and lightning fast web apps.
2. Mobile App Development: Native Android, Jetpack Compose, high performance responsive multi-screen architectures, and beautiful interactive micro-animations.
3. Video Editing: Elite high-retention startup promotional videos, creative cinematic edits, custom audio synchronization, and visual effects for creators.
4. AI Solutions: Intelligent automation systems, Gemini LLM integrations, conversational smart assistants, and automated workflow pipelines.
5. UI/UX Design: Advanced Apple-level visual polish, dark-mode cyber themes, 3D spatial graphics, glassmorphic dashboard designs, and Material 3 layouts.
6. Digital Branding: Complete startup visual identities, high-tech logos, customized typography systems, and pitch-deck design.

Achievements:
- Over 200,000+ Content Views across platforms.
- Multiple high-impact digital products designed and launched.
- Creator of "Kartik Labs" platform.

Guidelines for Kartik AI:
- Speak in a highly intelligent, futuristic, elite tech startup tone.
- Keep responses relatively concise, helpful, and organized using bullet points or clean spacing.
- Proudly represent Kartikeya Kumar Chaubey (Kartik) as a world-class creator.
- When asked how to hire or contact, pitch the Contact Hub, WhatsApp link, or email: chaubeykartik1234@gmail.com.
"""

    suspend fun chatWithKartik(userMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Hello! I am Kartik AI. It seems my API credentials are currently in sandbox mode. Here is a quick overview of Kartikeya's skills: He is an expert Web & App Developer, Video Editor, and AI Enthusiast. You can reach him directly at chaubeykartik1234@gmail.com or via WhatsApp!"
        }

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", userMessage)
                    }))
                }))
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", SYSTEM_PROMPT)
                    }))
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Apologies, my uplink is experiencing interference (Error: ${response.code}). Feel free to drop Kartikeya an email at chaubeykartik1234@gmail.com!"
                }
                val bodyStr = response.body?.string() ?: ""
                val resObj = JSONObject(bodyStr)
                val candidates = resObj.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                text ?: "Connection established, but the neural output was empty. Please retry your transmission!"
            }
        } catch (e: Exception) {
            "Network uplink offline. Kartik AI was unable to establish connection. Details: ${e.localizedMessage ?: "Unknown error"}"
        }
    }
}

// --- Data Models for Services & Portfolio ---
data class ServiceItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val details: String
)

data class ProjectItem(
    val title: String,
    val category: String,
    val description: String,
    val techStack: List<String>,
    val highlights: List<String>
)

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: String = "Uplink Secure"
)

// --- Helper Functions to Launch Intents ---
fun launchWebLink(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Neural link failed to open", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsApp(context: Context, number: String = "+919336151706", message: String = "Hello Kartikeya! I saw your stunning Kartik Labs portfolio app and want to hire you!") {
    try {
        val url = "https://api.whatsapp.com/send?phone=$number&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp connection offline", Toast.LENGTH_SHORT).show()
    }
}

fun launchEmail(context: Context, email: String = "chaubeykartik1234@gmail.com") {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Kartik Labs - Business Collaboration Request")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No local mail transmitters found", Toast.LENGTH_SHORT).show()
    }
}

// --- Main Screens Layout ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainPortfolioScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Loading State
    var isInitializing by remember { mutableStateOf(true) }
    var bootProgress by remember { mutableStateOf(0.0f) }

    // Active Hub Selection: 0 = Portal (Home/About), 1 = Labs (Services/Portfolio), 2 = Kartik AI, 3 = Uplink (Contact)
    var activeHub by remember { mutableStateOf(0) }

    // Navigation and States
    val services = remember {
        listOf(
            ServiceItem(
                "Website Development",
                "Futuristic, pixel-perfect, lightning fast web portals and SaaS platforms.",
                Icons.Default.Web,
                "Leveraging state-of-the-art architectures like Next.js, React, and server-side optimizations to forge digital homes with fluid animations and responsive glassmorphism styles."
            ),
            ServiceItem(
                "Mobile App Development",
                "Exquisite native Android apps with adaptive, gorgeous layouts.",
                Icons.Default.PhoneAndroid,
                "Forging immersive experiences with Jetpack Compose, Kotlin, custom layouts, background processing, and bulletproof offline architectures."
            ),
            ServiceItem(
                "Video Editing",
                "High-retention video assets, visual effects, and startup promotions.",
                Icons.Default.VideoCameraFront,
                "Mastering audio syncing, dynamic zoom overlays, micro-transitions, color-grading, and narrative structure to maximize retention and engagement."
            ),
            ServiceItem(
                "AI Solutions",
                "Integrating advanced Large Language Models and automated AI systems.",
                Icons.Default.Memory,
                "Pioneering deep integration with Google Gemini, multi-modal pipelines, cognitive context handling, and automatic task execution flows."
            ),
            ServiceItem(
                "UI/UX Design",
                "Advanced luxury-dark aesthetic paired with clean interactive motion.",
                Icons.Default.Palette,
                "Architecting system design tokens, highly intuitive typography, fluid component states, dynamic ripples, and interactive 3D spatial mockups."
            ),
            ServiceItem(
                "Digital Branding",
                "Complete premium startup aesthetics and distinctive visuals.",
                Icons.Default.AutoAwesome,
                "Forging high-fidelity vectors, stunning pitch decks, custom adaptive icons, corporate color palettes, and memorable brand books."
            )
        )
    }

    val projects = remember {
        listOf(
            ProjectItem(
                "Aetheris AI Portal",
                "AI Solutions & Next.js Web App",
                "A next-generation artificial intelligence dashboard featuring live model comparison, vector databases, and real-time inference monitoring.",
                listOf("Next.js", "Gemini API", "TailwindCSS", "NodeJS"),
                listOf("Real-time live model analysis", "Advanced glassmorphic dashboards", "Streaming LLM responses in under 200ms", "Fully responsive adaptive layouts")
            ),
            ProjectItem(
                "NovaShell Custom Launcher",
                "Mobile Development & Jetpack Compose",
                "A ultra-minimalist, high-performance Android custom launcher designed for peak cognitive focus and seamless app workflows.",
                listOf("Kotlin", "Jetpack Compose", "Coroutines", "Room DB"),
                listOf("Zero-latency search indexes", "Custom dynamic gesture engine", "Intelligent notification category grouping", "Adaptive icon theme converter")
            ),
            ProjectItem(
                "SoraRender Cloud System",
                "Video Automation Engine",
                "An automated video render farm orchestrating cloud nodes to automatically composite high-fidelity promo clips from API triggers.",
                listOf("Python", "Docker", "FFmpeg", "AWS Cloud"),
                listOf("Simultaneous multi-node clip rendering", "Dynamic audio waveform composite", "Automated YouTube & social uploading pipelines", "90% rendering speed improvement")
            )
        )
    }

    // Trigger Initializing progress
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.0f,
            targetValue = 1.0f,
            animationSpec = tween(1800, easing = LinearEasing)
        ) { value, _ ->
            bootProgress = value
        }
        delay(100)
        isInitializing = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Futuristic Cyber Grid & Moving Particles Background
        CyberParticleBackground()

        if (isInitializing) {
            // Splash / Initializing Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Custom 3D Logo Spinner with rotating circle
                    Logo3DSpinner()

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "KARTIK LABS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonBlue,
                            letterSpacing = 6.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ESTABLISHING QUANTUM TRANSLATION...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonPurple,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bootProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(NeonBlue, NeonPurple)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(bootProgress * 100).toInt()}% SECURE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        } else {
            // Main App Shell
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                bottomBar = {
                    CyberBottomNavigation(
                        activeHub = activeHub,
                        onHubSelected = { activeHub = it }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = activeHub,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut())
                            }.using(SizeTransform(clip = false))
                        },
                        label = "HubTransition"
                    ) { targetHub ->
                        when (targetHub) {
                            0 -> PortalScreen(
                                onExploreClicked = { activeHub = 1 },
                                onHireClicked = { activeHub = 3 }
                            )
                            1 -> LabsScreen(services = services, projects = projects)
                            2 -> KartikAiScreen()
                            3 -> UplinkScreen()
                        }
                    }
                }
            }
        }
    }
}

// --- Glowing Rotating 3D Logo Spinner ---
@Composable
fun Logo3DSpinner() {
    val infiniteTransition = rememberInfiniteTransition(label = "LogoRotator")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LogoRotateY"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoPulse"
    )

    Box(
        modifier = Modifier
            .size(130.dp)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
                rotationY = rotationAngle
                cameraDistance = 8 * density
            },
        contentAlignment = Alignment.Center
    ) {
        // Holographic Outer Canvas Circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = NeonBlue,
                radius = size.minDimension / 2.2f,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                )
            )
            drawCircle(
                color = NeonPurple.copy(alpha = 0.5f),
                radius = size.minDimension / 2.5f,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), rotationAngle)
                )
            )
        }

        // Standard App icon drawable fallback centered in safe bounds
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Kartik Labs Hologram Logo",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .border(2.dp, Brush.radialGradient(listOf(AccentCyan, Color.Transparent)), CircleShape),
            contentScale = ContentScale.Fit
        )
    }
}

// --- Cosmic Cyber Background with Grid Lines and Floating Space Particles ---
@Composable
fun CyberParticleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ParticleSpace")

    // Infinite animation parameter to move background objects dynamically
    val timerFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BackgroundTimer"
    )

    // Simulating background graphics
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Radial deep purple cyber ambient glow in top right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.2f),
                        radius = size.width * 0.7f
                    )
                )

                // Radial cyan glow in bottom left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonBlue.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    )
                )

                // Standard Futuristic Cyber Grid Lines
                val gridSpacing = 45.dp.toPx()
                val lineAlpha = 0.05f

                // Vertical Grid lines
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = NeonBlue.copy(alpha = lineAlpha),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    x += gridSpacing
                }

                // Horizontal Grid lines
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = NeonBlue.copy(alpha = lineAlpha),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    y += gridSpacing
                }
            }
    ) {
        // Draw 30 floating glowing space micro-particles moving slowly
        val numParticles = 25
        for (i in 0 until numParticles) {
            val seedX = (i * 739) % 1000 / 1000f
            val seedY = (i * 913) % 1000 / 1000f
            val particleSize = ((i * 17) % 4 + 2).dp.toPx()
            val particleAlpha = ((i * 11) % 5 + 3) / 10f * 0.6f

            // Animate coordinate trajectories using sine movements
            val currentOffsetMultiplier = timerFlow / 100f
            val animatedX = (seedX * size.width + sin(currentOffsetMultiplier * 2 * Math.PI + i) * 35.dp.toPx()).toFloat()
            val animatedY = (seedY * size.height + cos(currentOffsetMultiplier * 2 * Math.PI + i) * 35.dp.toPx()).toFloat()

            val pColor = if (i % 3 == 0) AccentCyan else if (i % 3 == 1) NeonPurple else Color.White

            // Draw glowing core
            drawCircle(
                color = pColor.copy(alpha = particleAlpha),
                radius = particleSize,
                center = Offset(animatedX, animatedY)
            )

            // Draw outer neon reflection aura
            drawCircle(
                color = pColor.copy(alpha = particleAlpha * 0.3f),
                radius = particleSize * 2.2f,
                center = Offset(animatedX, animatedY)
            )
        }
    }
}

// --- Premium Glassmorphic Overlay Card ---
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    backgroundColor: Color = GlassBg,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(borderColor, borderColor.copy(alpha = 0.05f))),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(18.dp)
    ) {
        content()
    }
}

// --- DOCK HUB 1: PORTAL (Home Section & About) ---
@Composable
fun PortalScreen(
    onExploreClicked: () -> Unit,
    onHireClicked: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // typing taglines list
    val taglines = remember {
        listOf(
            "Web Developer",
            "App Developer",
            "Video Editor",
            "AI Enthusiast",
            "Digital Creator"
        )
    }

    var activeTaglineIdx by remember { mutableStateOf(0) }
    var displayedTaglineText by remember { mutableStateOf("") }
    var charIndex by remember { mutableStateOf(0) }
    var isDeleting by remember { mutableStateOf(false) }

    // typing sequence coroutine
    LaunchedEffect(activeTaglineIdx, isDeleting, charIndex) {
        val activeString = taglines[activeTaglineIdx]
        if (!isDeleting) {
            if (charIndex < activeString.length) {
                delay(120)
                displayedTaglineText = activeString.substring(0, charIndex + 1)
                charIndex++
            } else {
                // Pause at end
                delay(1500)
                isDeleting = true
            }
        } else {
            if (charIndex > 0) {
                delay(60)
                displayedTaglineText = activeString.substring(0, charIndex - 1)
                charIndex--
            } else {
                isDeleting = false
                activeTaglineIdx = (activeTaglineIdx + 1) % taglines.size
            }
        }
    }

    // Interactive tilting values of the profile card (based on touch)
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Holographic Framed Profile Avatar inside tilting modifier
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .graphicsLayer {
                    rotationX = tiltX
                    rotationY = tiltY
                    cameraDistance = 12 * density
                }
                .hoverable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    enabled = true
                )
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    // Tap causes a playful sudden 3D shift that settles back
                    coroutineScope.launch {
                        tiltX = 15f
                        tiltY = -15f
                        delay(120)
                        tiltX = 0f
                        tiltY = 0f
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Rotating Holographic Laser rings around avatar
            val infiniteTransition = rememberInfiniteTransition(label = "LaserRing")
            val orbitRotation1 by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
                label = "OrbitRot1"
            )
            val orbitRotation2 by infiniteTransition.animateFloat(
                initialValue = 360f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
                label = "OrbitRot2"
            )

            // Holographic lasers drawn
            Canvas(modifier = Modifier.size(220.dp)) {
                // Outer Cyan Dashed Ring
                drawCircle(
                    color = AccentCyan,
                    radius = 98.dp.toPx(),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), orbitRotation1)
                    )
                )

                // Middle Purple Dot/Dashed Ring
                drawCircle(
                    color = NeonPurple,
                    radius = 88.dp.toPx(),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 15f), orbitRotation2)
                    )
                )
            }

            // Real Avatar Image
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(3.dp, Brush.horizontalGradient(listOf(NeonBlue, NeonPurple)), CircleShape)
            ) {
                // We use our cool generated avatar brain image, fallback to launcher background if absent
                AsyncImage(
                    model = R.drawable.img_ai_brain_1782270502762,
                    contentDescription = "Kartikeya Kumar Chaubey - Hologram Portrait",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(id = R.drawable.ic_launcher_background)
                )
            }

            // Tech floating badges around Avatar
            Box(
                modifier = Modifier
                    .size(240.dp)
            ) {
                // Floating Code Tag top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 20.dp)
                        .background(CyberCard.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, AccentCyan, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("<CODE>", color = AccentCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                // Floating AI badge bottom left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 10.dp, y = (-20).dp)
                        .background(CyberCard.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, NeonPurple, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("AI_AGENT: ON", color = NeonPurple, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Name
        Text(
            text = "Kartikeya Kumar Chaubey",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AccentCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KARTIK LABS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonBlue,
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Typing Tagline Animation Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayedTaglineText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = AccentCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Spacer(modifier = Modifier.width(2.dp))
                // Animated Blinking Cursor
                val cursorBlink = rememberInfiniteTransition(label = "Blink")
                val cursorAlpha by cursorBlink.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                    label = "Blinker"
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(18.dp)
                        .background(NeonBlue.copy(alpha = cursorAlpha))
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Action Buttons Row with Neon Glow details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onExploreClicked() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("explore_labs_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonBlue, NeonPurple))),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonBlue.copy(alpha = 0.12f), NeonPurple.copy(alpha = 0.12f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Explore Work", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Button(
                onClick = { onHireClicked() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("hire_me_btn")
                    .drawBehind {
                        // Ambient blue glow behind button
                        drawRoundRect(
                            color = NeonBlue.copy(alpha = 0.35f),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = CyberBackground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hire Me", color = CyberBackground, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // ABOUT SECTION Glass Card
        Text(
            text = "SYSTEM OVERVIEW",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(10.dp))

        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("about_card"),
            borderColor = GlassBorder.copy(alpha = 0.5f)
        ) {
            Text(
                text = "I'm Kartikeya Kumar Chaubey, an Indian Web & App Developer, Video Editor, and AI Enthusiast who helps creators and businesses transform ideas into powerful digital experiences.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Sub-values checklist (Entrepreneur mindset, Passion, Product Focus)
            Divider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(14.dp))

            val traits = listOf(
                Pair(Icons.Default.TrendingUp, "Entrepreneur Mindset"),
                Pair(Icons.Default.Lightbulb, "Passion for Innovation"),
                Pair(Icons.Default.Psychology, "Love for AI & Emerging Tech"),
                Pair(Icons.Default.Layers, "Building Impactful Products")
            )

            traits.forEach { trait ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(trait.first, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = trait.second,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.LightGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- DOCK HUB 2: LABS (Services & Projects Showcase) ---
@Composable
fun LabsScreen(services: List<ServiceItem>, projects: List<ProjectItem>) {
    val scrollState = rememberScrollState()
    var selectedServiceIdx by remember { mutableStateOf(-1) }
    var selectedProjectIdx by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ACHIEVEMENTS / STATS SECTION
        Text(
            text = "PERFORMANCE TELEMETRY",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Multi stats showcase
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val stats = listOf(
                Triple("200K+", "Content Views", NeonBlue),
                Triple("Multiple", "Digital Projects", NeonPurple),
                Triple("Active", "Creator / Dev", AccentCyan)
            )

            stats.forEach { stat ->
                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    borderColor = stat.third.copy(alpha = 0.3f),
                    backgroundColor = CyberCard.copy(alpha = 0.8f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Animated pulse visual representation ring
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(stat.third.copy(alpha = 0.1f))
                                .border(1.dp, stat.third.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (stat.second) {
                                    "Content Views" -> Icons.Default.Visibility
                                    "Digital Projects" -> Icons.Default.DoneAll
                                    else -> Icons.Default.PlayCircleFilled
                                },
                                contentDescription = null,
                                tint = stat.third,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stat.first,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stat.second,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Gray,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // SERVICES SECTION
        Text(
            text = "KARTIK LABS SERVICES",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of services (Using Column as grid fallback for compatibility)
        for (i in services.indices step 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (j in 0..1) {
                    val idx = i + j
                    if (idx < services.size) {
                        val item = services[idx]
                        val isSelected = selectedServiceIdx == idx

                        // Animated card variables
                        val cardScale by animateFloatAsState(if (isSelected) 1.02f else 1.0f, label = "ScaleAnim")
                        val glowBorderAlpha by animateFloatAsState(if (isSelected) 0.8f else 0.15f, label = "GlowAnim")

                        GlassmorphicCard(
                            modifier = Modifier
                                .weight(1f)
                                .scale(cardScale)
                                .clickable {
                                    selectedServiceIdx = if (isSelected) -1 else idx
                                },
                            borderColor = if (isSelected) NeonBlue.copy(alpha = glowBorderAlpha) else GlassBorder.copy(alpha = 0.2f),
                            backgroundColor = if (isSelected) CyberCard.copy(alpha = 0.95f) else CyberCard.copy(alpha = 0.7f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) AccentCyan else NeonPurple,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    ),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                AnimatedVisibility(visible = isSelected) {
                                    Column {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Divider(color = Color.White.copy(alpha = 0.1f))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = item.details,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = AccentCyan,
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // PORTFOLIO / GALLERY SHOWCASE
        Text(
            text = "PROJECT SHOWCASE",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Project Cards list
        projects.forEachIndexed { idx, project ->
            val isSelected = selectedProjectIdx == idx
            val cardGlowBorder by animateColorAsState(
                targetValue = if (isSelected) NeonPurple else GlassBorder,
                label = "ProjBorderColor"
            )

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        selectedProjectIdx = if (isSelected) -1 else idx
                    },
                borderColor = cardGlowBorder.copy(alpha = if (isSelected) 0.8f else 0.3f),
                backgroundColor = if (isSelected) CyberCard.copy(alpha = 0.95f) else CyberCard.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Icon(
                            imageVector = if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand Detail",
                            tint = NeonBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.LightGray,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tech Stack badges
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(project.techStack) { tech ->
                            Box(
                                modifier = Modifier
                                    .background(NeonPurple.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tech,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isSelected) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "PROJECT HIGHLIGHTS:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            project.highlights.forEach { highlight ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(AccentCyan)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = highlight,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- DOCK HUB 3: KARTIK AI CHAT SCREEN ---
@Composable
fun KartikAiScreen() {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Chat log state
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                content = "Uplink Secure. Greetings, visitor! I am Kartik AI. Ask me anything about Kartikeya's expertise in Web & App Development, Video Editing, or AI Automation solutions. How can Kartik Labs serve you today?",
                isUser = false
            )
        )
    }

    var messageInput by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    // Quick suggestions prompt helper
    val suggestions = remember {
        listOf(
            "What services do you offer?",
            "Show me your tech projects.",
            "How can I hire Kartik Labs?",
            "Tell me about your YouTube channel."
        )
    }

    fun submitMessage(msg: String) {
        if (msg.trim().isEmpty() || isThinking) return
        messages.add(ChatMessage(msg, true))
        messageInput = ""
        isThinking = true
        keyboardController?.hide()

        // Auto Scroll to bottom
        coroutineScope.launch {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }

        // Call Gemini
        coroutineScope.launch {
            val response = GeminiApiClient.chatWithKartik(msg)
            messages.add(ChatMessage(response, false))
            isThinking = false

            // Scroll to bottom again after response
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        // Holographic AI Brain Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, NeonBlue, CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                // Spinning neural node visual indicator
                val infiniteTransition = rememberInfiniteTransition(label = "NodeRot")
                val rotAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
                    label = "Rot"
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer { rotationZ = rotAngle },
                    colorFilter = ColorFilter.tint(AccentCyan)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "KARTIK AI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF39FF14)) // Green flashing core
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COGNITIVE TERMINAL V2.5 • ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF39FF14),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chats lists
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CyberCard.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .border(0.5.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(
                                    color = if (msg.isUser) NeonPurple.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isUser) 16.dp else 2.dp,
                                        bottomEnd = if (msg.isUser) 2.dp else 16.dp
                                    )
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (msg.isUser) NeonPurple.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isUser) 16.dp else 2.dp,
                                        bottomEnd = if (msg.isUser) 2.dp else 16.dp
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (msg.isUser) "VISITOR UPLINK" else "KARTIK AI INTERFACE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (msg.isUser) NeonPurple else NeonBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        }
                    }
                }

                if (isThinking) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "Think")
                                    val bubbleScale1 by infiniteTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
                                        label = "Bubble1"
                                    )

                                    Text(
                                        text = "SYNAPSE COMPUTING",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AccentCyan,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .scale(bubbleScale1)
                                            .clip(CircleShape)
                                            .background(AccentCyan)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Suggestions Horizontal Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions) { sugg ->
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .clickable { submitMessage(sugg) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = sugg,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                placeholder = { Text("Query AI terminal...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CyberCard.copy(alpha = 0.8f),
                    unfocusedContainerColor = CyberCard.copy(alpha = 0.8f),
                    disabledContainerColor = CyberCard.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submitMessage(messageInput) })
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { submitMessage(messageInput) },
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.radialGradient(listOf(NeonBlue, NeonPurple)),
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("send_query_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = CyberBackground
                )
            }
        }
    }
}

// --- DOCK HUB 4: UPLINK (Contact Section) ---
@Composable
fun UplinkScreen() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Contact Form input states
    var clientName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var clientMessage by remember { mutableStateOf("") }
    var isSendingUplink by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SECURE TRANS-LINK",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Kartik Labs Uplink",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // WhatsApp and Email CTAs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // WhatsApp Button
            GlassmorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { launchWhatsApp(context) },
                borderColor = Color(0xFF25D366).copy(alpha = 0.4f),
                backgroundColor = Color(0xFF25D366).copy(alpha = 0.05f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Instant Uplink", color = Color.Gray, fontSize = 9.sp)
                }
            }

            // Email Button
            GlassmorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { launchEmail(context) },
                borderColor = NeonBlue.copy(alpha = 0.4f),
                backgroundColor = NeonBlue.copy(alpha = 0.05f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Direct Email", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Secure Response", color = Color.Gray, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Custom Glassmorphic form
        Text(
            text = "TRANSMIT TRANSMISSION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = GlassBorder.copy(alpha = 0.3f)
        ) {
            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_name_field"),
                label = { Text("Your Signature / Name", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = clientEmail,
                onValueChange = { clientEmail = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_email_field"),
                label = { Text("Your Decryption / Email Address", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = clientMessage,
                onValueChange = { clientMessage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("contact_message_field"),
                label = { Text("Secure Payload / Message", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SEND UPLINK BUTTON with custom progress & burst animation
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    if (clientName.trim().isEmpty() || clientEmail.trim().isEmpty() || clientMessage.trim().isEmpty()) {
                        Toast.makeText(context, "All telemetry fields must be populated", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSendingUplink = true
                    scope.launch {
                        delay(1500)
                        isSendingUplink = false
                        Toast.makeText(context, "Secure Uplink Transmitted Successfully!", Toast.LENGTH_LONG).show()
                        clientName = ""
                        clientEmail = ""
                        clientMessage = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_contact_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonBlue, NeonPurple))),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(NeonBlue.copy(alpha = 0.1f), NeonPurple.copy(alpha = 0.1f)))),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSendingUplink) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TRANSMIT PAYLOAD", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SOCIAL NETWORK CHANNELS (As requested: YouTube, Instagram)
        Text(
            text = "SOCIAL BROADCAST NETWORKS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // YouTube Channel
            GlassmorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { launchWebLink(context, "https://youtube.com/@balliqdefender") },
                borderColor = Color(0xFFFF0000).copy(alpha = 0.4f),
                backgroundColor = Color(0xFFFF0000).copy(alpha = 0.05f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFFFF0000), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("YOUTUBE", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }

            // Instagram Channel
            GlassmorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { launchWebLink(context, "https://www.instagram.com/balliqdefender") },
                borderColor = Color(0xFFE1306C).copy(alpha = 0.4f),
                backgroundColor = Color(0xFFE1306C).copy(alpha = 0.05f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFFE1306C), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("INSTAGRAM", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- FUTURISTIC NAVIGATION DECK BAR ---
@Composable
fun CyberBottomNavigation(
    activeHub: Int,
    onHubSelected: (Int) -> Unit
) {
    val items = listOf(
        Pair("Portal", Icons.Default.AccountCircle),
        Pair("Labs", Icons.Default.GridOn),
        Pair("AI Agent", Icons.Default.Psychology),
        Pair("Uplink", Icons.Default.ContactMail)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Essential Safe Area edge check for custom navigations!
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(CyberCard.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
            .border(0.8.dp, GlassBorder, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = activeHub == index

                // High fidelity scaling and shifting animation
                val navScale by animateFloatAsState(if (isSelected) 1.15f else 1.0f, label = "NavScale")
                val textColor by animateColorAsState(if (isSelected) AccentCyan else CyberGray, label = "NavColor")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(navScale)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            onHubSelected(index)
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        tint = if (isSelected) AccentCyan else CyberGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.first,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPortfolioScreenPreview() {
    MyApplicationTheme {
        MainPortfolioScreen()
    }
}

