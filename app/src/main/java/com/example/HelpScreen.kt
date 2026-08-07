package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & FAQ", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Help FAQ",
                modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                "Welcome to TransitSafe Liability Shield! 🛡️⚡",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                "Your Ultimate Wingman for E-Scooters & E-Bikes!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
            
            FaqItem(
                question = "What is TransitSafe Liability Shield?",
                answer = "Riding a rental scooter or e-bike is a blast, but what happens if you accidentally clip a pedestrian or scratch a parked car? The rental company's insurance often has HUGE deductibles leaving YOU holding the bag! That's where we come in. TransitSafe gives you ON-DEMAND Personal Liability Insurance just for micromobility! It's your ultimate safety net so you can ride totally worry-free!"
            )
            
            FaqItem(
                question = "How does it actually work?",
                answer = "It’s brilliantly simple! Before you unlock that scooter, open our app and hit activate to grab a short-term coverage policy. If anything goes wrong, our app shifts into 'On-Scene Mode.' It’s a super calm, step-by-step wizard you can literally hand over to the other person. They scan their IDs and info, our smart AI checks it instantly, and everything is securely locked away!"
            )
            
            FaqItem(
                question = "Wait, AI checks the documents?",
                answer = "YES! Our cutting-edge Edge AI instantly scans driver's licenses and insurance cards in real-time. If it's blurry, glaring, or cropped, it automatically tells you to snap it again! No more waking up the next day realizing you got a blurry, useless photo. We guarantee you get clean, legally valid evidence right on the spot!"
            )
            
            FaqItem(
                question = "Is my data safe and secure?",
                answer = "Absolutely! We don't mess around with your privacy. Every single piece of info is stored locally on your device in a super-secure encrypted vault. Plus, we automatically back up a complete, tamper-proof package straight to your private cloud storage so you're ready for any insurance claim!"
            )
            
            FaqItem(
                question = "What if it’s a real emergency?",
                answer = "We've got your back! Our Premium Roadside Support gives you a one-tap panic button for emergency services, instantly sharing your exact GPS location. Plus, you get direct access to our live Claims Concierge—real experts who know the crazy laws around e-scooters and will fight for YOU!"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Stop riding completely unprotected. Get TransitSafe, lock in your coverage, and enjoy the ride! 🛴💨",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
