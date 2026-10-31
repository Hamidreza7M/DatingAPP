package ir.fastlove.app.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.fastlove.app.app.navigator
import ir.fastlove.app.app.repository
import ir.fastlove.app.core.designsystem.FastLoveColors
import ir.fastlove.app.core.designsystem.FastLoveTopBar
import ir.fastlove.app.core.designsystem.Spacing
import ir.fastlove.app.core.domain.DemoReply
import ir.fastlove.app.core.domain.FastLoveClock
import ir.fastlove.app.core.model.ChatMessage
import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.model.MsgType
import ir.fastlove.app.feature.dateplanner.DateProposalDialog
import ir.fastlove.app.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(personId: Int) {
    val repo = repository()
    val nav = navigator()
    val person = remember(personId) { repo.people.firstOrNull { it.id == personId } }
    if (person == null) { FastLoveTopBar("گفتگو", onBack = { nav.pop() }); return }

    val messages by repo.messages(personId).collectAsState()
    var input by remember { mutableStateOf("") }
    var showDate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        repo.sendMessage(personId, ChatMessage(text.trim(), isMe = true, timeMillis = FastLoveClock.nowMillis(), type = MsgType.TEXT, read = true))
        input = ""
        scope.launch {
            delay(900)
            val reply = DemoReply.reply(text, person, repo.messages(personId).value)
            repo.sendMessage(personId, ChatMessage(reply, isMe = false, timeMillis = FastLoveClock.nowMillis(), type = MsgType.TEXT))
        }
    }

    Column(Modifier.fillMaxSize()) {
        FastLoveTopBar(person.name, onBack = { nav.pop() }, trailing = {
            Icon(
                Icons.Filled.CalendarMonth, "پیشنهاد قرار",
                tint = FastLoveColors.Coral,
                modifier = Modifier.clickable { showDate = true }.padding(Spacing.sm)
            )
        })

        // starter topics
        if (messages.count { it.isMe } == 0) {
            Column(Modifier.padding(horizontal = Spacing.gutter)) {
                Text("پیشنهاد موضوع گفتگو", color = FastLoveColors.TextMuted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.padding(Spacing.xxs))
                FastLoveData.conversationStarters.take(3).forEach { topic ->
                    Surface(color = FastLoveColors.SurfaceElevated, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { input = topic }) {
                        Text(topic, color = FastLoveColors.TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(Spacing.md))
                    }
                }
            }
        }

        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = Spacing.gutter), state = listState, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            itemsIndexed(messages) { index, msg ->
                MessageBubble(msg) { emoji -> repo.reactTo(personId, index, if (msg.reaction == emoji) null else emoji) }
            }
        }

        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Image, "ارسال تصویر (دمو)",
                tint = FastLoveColors.TextMuted,
                modifier = Modifier.clickable {
                    repo.sendMessage(personId, ChatMessage("[تصویر — دمو]", isMe = true, timeMillis = FastLoveClock.nowMillis(), type = MsgType.IMAGE, read = true))
                }.padding(Spacing.sm)
            )
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("پیام…") },
                modifier = Modifier.weight(1f),
                maxLines = 4
            )
            Spacer(Modifier.padding(Spacing.xxs))
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(FastLoveColors.Coral).clickable { sendText(input) }.padding(Spacing.md),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Send, "ارسال", tint = FastLoveColors.TextPrimary) }
        }
    }

    if (showDate) DateProposalDialog(person, onDismiss = { showDate = false })
}

@Composable
private fun MessageBubble(msg: ChatMessage, onReact: (String) -> Unit) {
    val align = if (msg.isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bg = if (msg.isMe) FastLoveColors.Coral.copy(alpha = 0.9f) else FastLoveColors.SurfaceElevated
    Box(Modifier.fillMaxWidth(), contentAlignment = align) {
        Column(horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start) {
            Surface(color = bg, shape = RoundedCornerShape(18.dp), modifier = Modifier.widthIn(max = 280.dp).clickable { onReact("❤️") }) {
                Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
                    val content = if (msg.type == MsgType.IMAGE) "🖼️ تصویر (دمو)" else if (msg.type == MsgType.VOICE) "🎤 پیام صوتی (دمو)" else msg.text
                    Text(content, color = FastLoveColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text(FastLoveClock.timeLabel(msg.timeMillis), color = FastLoveColors.TextPrimary.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                }
            }
            msg.reaction?.let { Text(it, Modifier.padding(horizontal = Spacing.sm)) }
        }
    }
}
