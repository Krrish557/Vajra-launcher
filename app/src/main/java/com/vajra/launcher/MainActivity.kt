package com.vajra.launcher

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.vajra.launcher.data.apps.AppRepository
import com.vajra.launcher.data.config.PreferencesManager
import com.vajra.launcher.data.search.SearchProvider
import com.vajra.launcher.databinding.ActivityMainBinding
import com.vajra.launcher.environment.SystemMonitor
import com.vajra.launcher.data.environment.EnvironmentRouter
import com.vajra.launcher.data.executor.ToolExecutor
import com.vajra.launcher.data.tools.ToolStatusResolver
import com.vajra.launcher.ui.controllers.AppsDrawerViewController
import com.vajra.launcher.ui.controllers.CustomizationViewController
import com.vajra.launcher.ui.controllers.CyberCategoriesViewController
import com.vajra.launcher.ui.controllers.HomeViewController
import com.vajra.launcher.ui.controllers.SearchViewController
import com.vajra.launcher.ui.controllers.SplashViewController
import com.vajra.launcher.ui.controllers.SystemInfoViewController
import com.vajra.launcher.ui.controllers.ToolConfigViewController
import com.vajra.launcher.ui.controllers.ToolDetailsViewController
import com.vajra.launcher.ui.controllers.ToolListViewController
import com.vajra.launcher.ui.navigation.Screen
import java.util.ArrayDeque

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var systemMonitor: SystemMonitor
    private lateinit var appRepository: AppRepository
    private lateinit var searchProvider: SearchProvider
    private lateinit var environmentManager: com.vajra.launcher.data.environment.EnvironmentManager
    private lateinit var environmentRouter: EnvironmentRouter
    private lateinit var toolExecutor: ToolExecutor
    private lateinit var toolStatusResolver: ToolStatusResolver

    private val backStack = ArrayDeque<Screen>()
    private var currentScreen: Screen? = null

    private var activeHomeController: HomeViewController? = null
    private var activeSystemController: SystemInfoViewController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        prefsManager = PreferencesManager(this)

        // Apply selected theme before super.onCreate
        when (prefsManager.theme) {
            PreferencesManager.THEME_AMOLED -> setTheme(R.style.Theme_Vajra_Amoled)
            PreferencesManager.THEME_LIGHT -> setTheme(R.style.Theme_Vajra_Light)
            else -> setTheme(R.style.Theme_Vajra)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        environmentManager = com.vajra.launcher.data.environment.EnvironmentManager(this)
        environmentRouter = EnvironmentRouter(environmentManager)
        toolExecutor = ToolExecutor(environmentRouter)
        toolStatusResolver = ToolStatusResolver(environmentManager)
        systemMonitor = SystemMonitor(this)
        appRepository = AppRepository(this)
        searchProvider = SearchProvider(appRepository)

        setupWindowInsets()
        setupBottomNavigation()
        setupBackNavigation()

        // Show Boot / Splash on initial startup
        if (savedInstanceState == null) {
            binding.splashContainer.visibility = View.VISIBLE
            val splashController = SplashViewController(binding.splashContainer) {
                // Smooth transition from Splash to Home
                binding.splashContainer.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            binding.splashContainer.visibility = View.GONE
                        }
                    })
                navigateTo(Screen.Home, addToBackStack = false)
            }
            splashController.start()
        } else {
            binding.splashContainer.visibility = View.GONE
            navigateTo(Screen.Home, addToBackStack = false)
        }
    }

    override fun onStart() {
        super.onStart()
        activeHomeController?.start(lifecycleScope)
        activeSystemController?.start(lifecycleScope)
    }

    override fun onStop() {
        super.onStop()
        activeHomeController?.stop()
        activeSystemController?.stop()
    }

    private fun setupWindowInsets() {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            val navBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars())

            binding.root.setPadding(0, statusBarInsets.top, 0, 0)
            binding.bottomNavContainer.setPadding(0, 0, 0, navBarInsets.bottom)
            insets
        }
    }

    private fun setupBottomNavigation() {
        binding.navHome.setOnClickListener {
            if (currentScreen !is Screen.Home) {
                navigateTo(Screen.Home)
            }
        }

        binding.navCyber.setOnClickListener {
            if (currentScreen !is Screen.CyberCategories) {
                navigateTo(Screen.CyberCategories)
            }
        }

        binding.navApps.setOnClickListener {
            if (currentScreen !is Screen.Apps) {
                navigateTo(Screen.Apps)
            }
        }

        binding.navSystem.setOnClickListener {
            if (currentScreen !is Screen.SystemInfo) {
                navigateTo(Screen.SystemInfo)
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backStack.isNotEmpty()) {
                    val prevScreen = backStack.pop()
                    navigateTo(prevScreen, addToBackStack = false)
                } else if (currentScreen !is Screen.Home) {
                    navigateTo(Screen.Home, addToBackStack = false)
                } else {
                    // Already at Home screen root of the launcher.
                    // Strictly consume back press and stay on Home.
                    // The only way to open the default/alternate launcher is via the Apps screen card.
                }
            }
        })
    }

    fun navigateTo(screen: Screen, addToBackStack: Boolean = true) {
        hideKeyboard()

        val isPrimaryTab = screen is Screen.Home ||
                screen is Screen.CyberCategories ||
                screen is Screen.Apps ||
                screen is Screen.SystemInfo

        if (isPrimaryTab) {
            backStack.clear()
        } else if (addToBackStack && currentScreen != null) {
            backStack.push(currentScreen)
        }

        // Clean up previous active screen observers
        activeHomeController?.stop()
        activeHomeController = null
        activeSystemController?.stop()
        activeSystemController = null

        currentScreen = screen
        binding.screenContainer.removeAllViews()

        when (screen) {
            is Screen.Home -> {
                updateNavSelection(0)
                val controller = HomeViewController(
                    container = binding.screenContainer,
                    systemMonitor = systemMonitor,
                    onNavigateToSystem = { navigateTo(Screen.SystemInfo) },
                    onNavigateToSearch = { navigateTo(Screen.GlobalSearch()) }
                )
                activeHomeController = controller
                binding.screenContainer.addView(controller.binding.root)
                controller.start(lifecycleScope)
            }
            is Screen.CyberCategories -> {
                updateNavSelection(1)
                val controller = CyberCategoriesViewController(
                    container = binding.screenContainer,
                    environmentManager = environmentManager
                ) { catId ->
                    navigateTo(Screen.ToolList(catId))
                }
                binding.screenContainer.addView(controller.binding.root)
            }
            is Screen.ToolList -> {
                updateNavSelection(1)
                val controller = ToolListViewController(
                    container = binding.screenContainer,
                    categoryId = screen.categoryId,
                    toolStatusResolver = toolStatusResolver,
                    scope = lifecycleScope,
                    onToolSelected = { toolId -> navigateTo(Screen.ToolDetails(toolId)) },
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
                binding.screenContainer.addView(controller.binding.root)
            }
            is Screen.ToolDetails -> {
                updateNavSelection(1)
                val controller = ToolDetailsViewController(
                    container = binding.screenContainer,
                    toolId = screen.toolId,
                    toolExecutor = toolExecutor,
                    toolStatusResolver = toolStatusResolver,
                    scope = lifecycleScope,
                    onAdvConfigSelected = { toolId -> navigateTo(Screen.ToolConfig(toolId)) },
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
                binding.screenContainer.addView(controller.binding.root)
            }
            is Screen.ToolConfig -> {
                updateNavSelection(1)
                val controller = ToolConfigViewController(
                    container = binding.screenContainer,
                    toolId = screen.toolId,
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
                binding.screenContainer.addView(controller.binding.root)
            }
            is Screen.Apps -> {
                updateNavSelection(2)
                val controller = AppsDrawerViewController(binding.screenContainer, appRepository, lifecycleScope)
                binding.screenContainer.addView(controller.binding.root)
            }
            is Screen.GlobalSearch -> {
                updateNavSelection(-1)
                val controller = SearchViewController(
                    container = binding.screenContainer,
                    searchProvider = searchProvider,
                    appRepository = appRepository,
                    scope = lifecycleScope,
                    initialQuery = screen.query,
                    onToolSelected = { toolId -> navigateTo(Screen.ToolDetails(toolId)) },
                    onCategorySelected = { catId -> navigateTo(Screen.ToolList(catId)) },
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
                binding.screenContainer.addView(controller.binding.root)
            }
            is Screen.Customization -> {
                updateNavSelection(3)
                val controller = CustomizationViewController(
                    container = binding.screenContainer,
                    prefsManager = prefsManager,
                    onThemeChanged = {
                        recreate()
                    },
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
                binding.screenContainer.addView(controller.binding.root)
            }
            is Screen.SystemInfo -> {
                updateNavSelection(3)
                val controller = SystemInfoViewController(
                    container = binding.screenContainer,
                    systemMonitor = systemMonitor,
                    onOpenCustomization = { navigateTo(Screen.Customization) },
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
                activeSystemController = controller
                binding.screenContainer.addView(controller.binding.root)
                controller.start(lifecycleScope)
            }
        }
    }

    private fun updateNavSelection(selectedIndex: Int) {
        val activeColor = ContextCompat.getColor(this, R.color.vajra_nav_active)
        val inactiveColor = ContextCompat.getColor(this, R.color.vajra_nav_inactive)

        val navItems = listOf(
            Triple(binding.navHomeIcon, binding.navHomeLabel, binding.navHomeIndicator),
            Triple(binding.navCyberIcon, binding.navCyberLabel, binding.navCyberIndicator),
            Triple(binding.navAppsIcon, binding.navAppsLabel, binding.navAppsIndicator),
            Triple(binding.navSystemIcon, binding.navSystemLabel, binding.navSystemIndicator)
        )

        navItems.forEachIndexed { index, (icon, label, indicator) ->
            val isSelected = index == selectedIndex
            val color = if (isSelected) activeColor else inactiveColor

            icon.imageTintList = ColorStateList.valueOf(color)
            label.setTextColor(color)
            label.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            indicator.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        currentFocus?.let { view ->
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        } ?: imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
