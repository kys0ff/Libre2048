package off.kys.libre2048.ui.about

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import off.kys.libre2048.R
import off.kys.libre2048.ui.libraries.LibrariesScreen
import off.kys.libre2048.ui.theme.Libre2048Theme

/**
 * A smooth Squircle (superellipse) shape configuration for a modern app icon appearance.
 */
val SquircleShape = GenericShape { size: Size, _ ->
    val width = size.width
    val height = size.height
    val path = Path().apply {
        moveTo(0f, height / 2f)
        cubicTo(0f, height * 0.05f, width * 0.05f, 0f, width / 2f, 0f)
        cubicTo(width * 0.95f, 0f, width, height * 0.05f, width, height / 2f)
        cubicTo(width, height * 0.95f, width * 0.95f, height, width / 2f, height)
        cubicTo(width * 0.05f, height, 0f, height * 0.95f, 0f, height / 2f)
        close()
    }
    addPath(path)
}

class AboutScreen : Screen {

    @SuppressLint("LocalContextGetResourceValueCall")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName ?: "1.0"

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = stringResource(R.string.about_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(
                                painter = painterResource(R.drawable.round_arrow_back_24),
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                AboutHeader(versionName)

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                AboutItem(
                    title = stringResource(R.string.about_developer),
                    description = stringResource(R.string.about_dev_name),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.round_person_24),
                            contentDescription = null
                        )
                    }
                )

                AboutItem(
                    title = stringResource(R.string.about_source_code),
                    description = stringResource(R.string.about_source_code_desc),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.round_code_24),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            context.getString(R.string.about_github_url).toUri()
                        )
                        context.startActivity(intent)
                    }
                )

                AboutItem(
                    title = stringResource(R.string.about_license),
                    description = stringResource(R.string.about_license_desc),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.round_license_24px),
                            contentDescription = null
                        )
                    }
                )

                AboutItem(
                    title = stringResource(R.string.about_libraries),
                    description = stringResource(R.string.about_libraries_desc),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.round_list_24px),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        navigator?.push(LibrariesScreen())
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    @Composable
    private fun AboutHeader(versionName: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(112.dp)
                    .clip(SquircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    private fun AboutItem(
        title: String,
        description: String,
        icon: @Composable () -> Unit,
        onClick: (() -> Unit)? = null
    ) {
        val clickableModifier = if (onClick != null) {
            Modifier.clickable { onClick() }
        } else {
            Modifier
        }

        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        icon()
                    }
                }
            },
            trailingContent = if (onClick != null) {
                {
                    Icon(
                        painter = painterResource(R.drawable.round_chevron_right_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .then(clickableModifier)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    Libre2048Theme {
        AboutScreen().Content()
    }
}