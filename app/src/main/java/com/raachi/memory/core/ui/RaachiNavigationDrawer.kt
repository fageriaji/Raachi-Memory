package com.raachi.memory.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raachi.memory.BuildConfig
import com.raachi.memory.R

enum class DrawerDestination(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val iconColor: Color,
) {
    HOME(R.string.nav_home, Icons.Outlined.Home, Color(0xFF3155D9)),
    REMINDERS(R.string.nav_reminders, Icons.Outlined.Notifications, Color(0xFFE62E73)),
    LEDGER(R.string.nav_ledger, Icons.Outlined.AccountBalanceWallet, Color(0xFF8B3FD6)),
    EXPENSES(R.string.nav_expenses, Icons.Outlined.Payments, Color(0xFFF57C23)),
    ACTIVITY(R.string.nav_activity, Icons.Outlined.History, Color(0xFF159A9C)),
    PROFILE(R.string.nav_profile, Icons.Outlined.Person, Color(0xFF4F5BD5)),
    BACKUP_RESTORE(R.string.nav_backup_restore, Icons.Outlined.Backup, Color(0xFF249557)),
    SETTINGS(R.string.nav_settings, Icons.Outlined.Settings, Color(0xFF687386)),
    ABOUT(R.string.nav_about, Icons.Outlined.Info, Color(0xFF1479DB)),
}

@Composable
fun RaachiNavigationDrawer(
    selected: DrawerDestination?,
    profileName: String,
    profilePhotoUri: String?,
    onSelected: (DrawerDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerWidth = (LocalConfiguration.current.screenWidthDp.dp * 0.72f).coerceAtMost(300.dp)
    ModalDrawerSheet(
        modifier = modifier
            .width(drawerWidth)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        DrawerProfileHeader(name = profileName, photoUri = profilePhotoUri)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item { Spacer(Modifier.height(10.dp)) }
            items(5) { index ->
                val destination = DrawerDestination.entries[index]
                DrawerItem(destination, selected == destination, onSelected)
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp))
            }
            items(4) { index ->
                val destination = DrawerDestination.entries[index + 5]
                DrawerItem(destination, selected == destination, onSelected)
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
        DrawerFooter()
    }
}

@Composable
private fun DrawerProfileHeader(name: String, photoUri: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ProfileAvatar(
            name = name,
            photoUri = photoUri,
            size = 100.dp,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DrawerItem(
    destination: DrawerDestination,
    selected: Boolean,
    onSelected: (DrawerDestination) -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = stringResource(destination.labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        },
        selected = selected,
        onClick = { onSelected(destination) },
        icon = {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = destination.iconColor,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedContainerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun DrawerFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.drawer_version, BuildConfig.VERSION_NAME),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.drawer_copyright, stringResource(R.string.app_name)),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
