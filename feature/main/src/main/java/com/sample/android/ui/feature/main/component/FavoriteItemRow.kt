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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.sample.android.data.UserMetaData
import com.sample.android.feature.main.R
import com.sample.android.ui.extension.setTimeText
import com.sample.android.ui.model.UserUiData
import com.sample.android.ui.theme.ColorBlack88
import com.sample.android.ui.theme.ColorBlackE6


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FavoriteItemRow(
    item: UserUiData,
    onFavoriteToggle: (UserUiData) -> Unit,
    onClick: (UserUiData) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(item) })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(
                    border = BorderStroke(width = 1.dp, color = ColorBlackE6),
                    shape = RoundedCornerShape(14.0.dp)
                )
                .clip(RoundedCornerShape(14.0.dp))
        ) {
            GlideImage(
                model = item.data.thumbnail,
                contentDescription = "thumbnail",
                loading = placeholder(ColorDrawable(ColorBlackE6.toArgb())),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onFavoriteToggle(item) },
                    ),
                tint = Color.Unspecified
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.data.title ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.data.timestamp.setTimeText(context, R.string.pattern_datetime_short),
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = ColorBlack88,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteItemRowPreview() {
    // Mock data for preview
    val mockUserUiData = UserUiData(
        data = UserMetaData(
            title = "Sample Title",
            thumbnail = "https://via.placeholder.com/300x300",
            url = "sample@example.com",
            datetime = "2023-01-01T12:00:00.000Z"
        ),
        isFavorite = true
    )

    FavoriteItemRow(
        item = mockUserUiData,
        onFavoriteToggle = { },
        onClick = { }
    )
}