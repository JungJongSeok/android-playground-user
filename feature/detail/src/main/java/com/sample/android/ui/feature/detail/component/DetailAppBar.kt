package com.sample.android.ui.feature.detail.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sample.android.feature.detail.R
import com.sample.android.ui.model.UserUiData


@Composable
fun DetailAppBar(
    currentData: UserUiData?,
    onBackClick: () -> Unit,
    onFavoriteClick: (UserUiData) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onClick = onBackClick)

        Text(
            text = currentData?.title ?: "",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 2.dp)
        )

        FavoriteButton(
            isFavorite = currentData?.isFavorite == true,
            onClick = { currentData?.let { onFavoriteClick(it) } }
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.icon_back),
        contentDescription = "Back",
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    )
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(
            id = if (isFavorite) {
                R.drawable.icon_like_on
            } else {
                R.drawable.icon_like_off
            }
        ),
        contentDescription = "Favorite",
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    )
}
