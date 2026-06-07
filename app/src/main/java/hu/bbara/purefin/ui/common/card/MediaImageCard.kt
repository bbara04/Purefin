package hu.bbara.purefin.ui.common.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.ui.model.MediaAction

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaImageCard(
    imageUrl: String?,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    popupActions: List<MediaAction> = emptyList(),
    imageModifier: Modifier = Modifier,
    shapeSize: Dp = 12.dp,
    imageAspectRatio: Float = 16f / 10f,
    titleStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodySmall,
    textPadding: PaddingValues = PaddingValues(8.dp),
    imageOverlay: @Composable BoxScope.() -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(shapeSize)

    var showBottomSheet by remember { mutableStateOf(false) }

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (popupActions.isNotEmpty()) {
                    { showBottomSheet = true }
                } else null
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = imageModifier
                    .fillMaxWidth()
                    .aspectRatio(imageAspectRatio)
                    .clip(RoundedCornerShape(topStart = shapeSize, topEnd = shapeSize))
                    .background(scheme.surface)
            ) {
                PurefinAsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                imageOverlay()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(textPadding)
                ) {
                    Text(
                        text = title,
                        style = titleStyle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    subtitle
                        ?.takeIf { it.isNotBlank() }
                        ?.let { text ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = text,
                                style = subtitleStyle,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                }
                if (popupActions.isNotEmpty()) {
                    IconButton(
                        onClick = { showBottomSheet = true },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More actions"
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                popupActions.forEach { action ->
                    ListItem(
                        headlineContent = { Text(text = action.name) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            action.onClick()
                            showBottomSheet = false
                        }
                    )
                }
            }
        }
    }
}
