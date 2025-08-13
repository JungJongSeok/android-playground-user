package com.sample.android.ui.feature.main.component

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.sample.android.data.model.UserMetaData
import com.sample.android.feature.main.R
import com.sample.android.ui.extension.setTimeText
import com.sample.android.ui.mapper.toUiData
import com.sample.android.ui.model.UserUiData
import com.sample.android.ui.theme.ColorBlack44
import com.sample.android.ui.theme.ColorBlack88
import com.sample.android.ui.theme.ColorBlackE6

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SearchItemRow(
    item: UserUiData,
    onFavoriteToggle: (UserUiData) -> Unit,
    onClick: (UserUiData) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(item) })
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterVertically)
                .border(
                    border = BorderStroke(width = 1.dp, color = ColorBlackE6),
                    shape = RoundedCornerShape(14.0.dp)
                )
                .clip(RoundedCornerShape(14.0.dp))
        ) {
            GlideImage(
                model = item.thumbnail,
                contentDescription = "thumbnail",
                loading = placeholder(ColorDrawable(ColorBlackE6.toArgb())),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onFavoriteToggle(item) },
                    )
            ) {
                Icon(
                    painter = painterResource(
                        id = if (item.isFavorite) {
                            R.drawable.icon_like_on
                        } else {
                            R.drawable.icon_like_off
                        }
                    ),
                    contentDescription = "favorite like button",
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .size(22.dp),
                    tint = Color.Unspecified
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = item.title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.url ?: "",
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = ColorBlack44,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.timestamp.setTimeText(context, R.string.pattern_datetime_full),
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = ColorBlack88,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SearchItemRowPreview() {
    // Mock data for preview
    val mockUserUiData = UserMetaData(
        title = "Sample User Name",
        thumbnail = "https://via.placeholder.com/300x300",
        url = "sample@example.com",
        datetime = "2023-01-01T12:00:00.000Z"
    ).toUiData(isFavorite = true)

    SearchItemRow(
        item = mockUserUiData,
        onFavoriteToggle = { },
        onClick = { }
    )
}