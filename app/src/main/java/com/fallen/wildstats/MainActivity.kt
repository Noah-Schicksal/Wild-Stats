package com.fallen.wildstats

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.fallen.wildstats.databinding.ActivityMainBinding
import com.fallen.wildstats.ui.utils.CampeoesLoader
import com.fallen.wildstats.ui.utils.CampeoesManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerSelector: DrawerSelector
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        FirebaseFirestore.setLoggingEnabled(true)
        auth = FirebaseAuth.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_inicio,
                R.id.nav_campeoes,
                R.id.nav_tier_list,
                R.id.nav_runas,
                R.id.nav_login,
                R.id.nav_perfil
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        setupLoadingLayout()
        setupForceUpdateButton()

        // Pré-carrega campeões e imagens locais usando CampeoesManager
        CampeoesLoader.loadCampeoes(
            context = this,
            onComplete = {
                loadingLayout.visibility = View.GONE
                CampeoesManager.preparar(this) // pré-processa bitmaps e listas
            },
            onStartDownload = { loadingLayout.visibility = View.VISIBLE }
        )

        setupDrawer(drawerLayout)
    }

    private fun setupLoadingLayout() {
        loadingLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#AA000000"))
            setPadding(32, 16, 32, 16)
            isClickable = false
            isFocusable = false
            visibility = View.GONE
        }

        val progressBar = ProgressBar(this).apply { isIndeterminate = true }
        val loadingText = TextView(this).apply {
            text = "Atualizando informações..."
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(16, 0, 0, 0)
        }

        loadingLayout.addView(progressBar)
        loadingLayout.addView(loadingText)

        val mainFrame = binding.root.findViewById<FrameLayout>(R.id.main_content_frame)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        mainFrame.addView(loadingLayout, params)
    }

    private fun setupForceUpdateButton() {
        val mainFrame = binding.root.findViewById<FrameLayout>(R.id.main_content_frame)
        val forceUpdateButton = ImageView(this).apply {
            setImageResource(R.drawable.ic_suporte)
            setBackgroundColor(Color.parseColor("#AA000000"))
            setPadding(20, 20, 20, 20)
            elevation = 10f
            setOnClickListener {
                loadingLayout.visibility = View.VISIBLE
                CampeoesLoader.loadCampeoes(
                    context = this@MainActivity,
                    onComplete = {
                        loadingLayout.visibility = View.GONE
                        CampeoesManager.preparar(this@MainActivity)
                    },
                    onStartDownload = { loadingLayout.visibility = View.VISIBLE }
                )
            }
        }

        val btnParams = FrameLayout.LayoutParams(
            120, 120, Gravity.END or Gravity.BOTTOM
        ).apply {
            marginEnd = 32
            bottomMargin = 32
        }

        mainFrame.addView(forceUpdateButton, btnParams)
    }

    private fun setupDrawer(drawerLayout: DrawerLayout) {
        val navCustomDrawer = findViewById<View>(R.id.nav_custom_drawer)

        val itemInicio = navCustomDrawer.findViewById<LinearLayout>(R.id.item_inicio)
        val itemCampeoes = navCustomDrawer.findViewById<LinearLayout>(R.id.item_campeoes)
        val itemTierList = navCustomDrawer.findViewById<LinearLayout>(R.id.item_tier_list)
        val itemRunas = navCustomDrawer.findViewById<LinearLayout>(R.id.item_runas)
        val itemLogin = navCustomDrawer.findViewById<LinearLayout>(R.id.item_login)

        val barraInicio = navCustomDrawer.findViewById<View>(R.id.barra_lateral_inicio)
        val barraCampeoes = navCustomDrawer.findViewById<View>(R.id.barra_lateral_campeoes)
        val barraTierList = navCustomDrawer.findViewById<View>(R.id.barra_lateral_tier_list)
        val barraRunas = navCustomDrawer.findViewById<View>(R.id.barra_lateral_runas)
        val barraLogin = navCustomDrawer.findViewById<View>(R.id.barra_lateral_login)

        val iconInicio = navCustomDrawer.findViewById<ImageView>(R.id.icon_inicio)
        val iconCampeoes = navCustomDrawer.findViewById<ImageView>(R.id.icon_campeoes)
        val iconTierList = navCustomDrawer.findViewById<ImageView>(R.id.icon_tier_list)
        val iconRunas = navCustomDrawer.findViewById<ImageView>(R.id.icon_runas)
        val iconLogin = navCustomDrawer.findViewById<ImageView>(R.id.icon_login)

        val tvInicio = navCustomDrawer.findViewById<TextView>(R.id.tv_inicio)
        val tvCampeoes = navCustomDrawer.findViewById<TextView>(R.id.tv_campeoes)
        val tvTierList = navCustomDrawer.findViewById<TextView>(R.id.tv_tier_list)
        val tvRunas = navCustomDrawer.findViewById<TextView>(R.id.tv_runas)
        val tvLogin = navCustomDrawer.findViewById<TextView>(R.id.tv_login)

        val barras = mapOf(
            itemInicio to barraInicio,
            itemCampeoes to barraCampeoes,
            itemTierList to barraTierList,
            itemRunas to barraRunas,
            itemLogin to barraLogin
        )

        val icones = mapOf(
            itemInicio to iconInicio,
            itemCampeoes to iconCampeoes,
            itemTierList to iconTierList,
            itemRunas to iconRunas,
            itemLogin to iconLogin
        )

        val textos = mapOf(
            itemInicio to tvInicio,
            itemCampeoes to tvCampeoes,
            itemTierList to tvTierList,
            itemRunas to tvRunas,
            itemLogin to tvLogin
        )

        drawerSelector = DrawerSelector()

        if (auth.currentUser != null) {
            tvLogin.text = "Perfil"
            iconLogin.setImageResource(R.drawable.perfil_24)
        } else {
            tvLogin.text = "Login"
            iconLogin.setImageResource(R.drawable.login_24)
        }

        val closeDrawer = {
            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
            }, 200)
        }

        val items = mapOf(
            itemInicio to R.id.nav_inicio,
            itemCampeoes to R.id.nav_campeoes,
            itemTierList to R.id.nav_tier_list,
            itemRunas to R.id.nav_runas,
            itemLogin to if (auth.currentUser != null) R.id.nav_perfil else R.id.nav_login
        )

        items.forEach { (view, destinationId) ->
            view.setOnClickListener {
                drawerSelector.atualizarSelecao(view, barras, icones, textos)
                navController.navigate(destinationId)
                closeDrawer()
            }
        }

        drawerSelector.atualizarSelecao(itemInicio, barras, icones, textos)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
