package com.websmithing.gpstracker2.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.location.Location
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.websmithing.gpstracker2.R
import com.websmithing.gpstracker2.ui.theme.error
import com.websmithing.gpstracker2.ui.theme.primary
import com.websmithing.gpstracker2.ui.theme.secondary
import com.websmithing.gpstracker2.util.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.api.IGeoPoint
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import java.io.File
import kotlin.math.atan2
import javax.inject.Inject

fun Color.toHexInt(): Int = (0xFF shl 24) or
        ((red * 255).toInt() shl 16) or
        ((green * 255).toInt() shl 8) or
        ((blue * 255).toInt())

enum class MarkerColor(val colorInt: Int) {
    GREY(secondary.toHexInt()),
    BLUE(primary.toHexInt()),
    RED(error.toHexInt())
}

fun drawableToBitmap(drawable: android.graphics.drawable.Drawable, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return bitmap
}

fun createUserIconBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val cx = width / 2f
    val cy = height / 3f
    val r = width / 4f
    canvas.drawCircle(cx, cy, r, paint)

    val path = android.graphics.Path().apply {
        moveTo(cx - r, cy + r)
        lineTo(cx + r, cy + r)
        lineTo(cx, cy + r * 3)
        close()
    }
    canvas.drawPath(path, paint)
    return bitmap
}

fun createColoredMarkerBitmap(
    context: Context,
    color: Int,
    markerSize: Int,
    iconSize: Int
): Pair<Bitmap, Bitmap> {
    val markerDrawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_background)
    val bgBitmap = markerDrawable?.let { drawableToBitmap(it, markerSize, markerSize) }
        ?: Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            val paint = Paint().apply { this.color = color; style = Paint.Style.FILL; isAntiAlias = true }
            val path = android.graphics.Path()
            val cr = (markerSize * 0.1f).coerceAtLeast(8f)
            path.moveTo(cr, 0f); path.lineTo(markerSize.toFloat(), 0f)
            path.lineTo(markerSize.toFloat(), markerSize - cr)
            path.quadTo(markerSize.toFloat(), markerSize.toFloat(), markerSize - cr, markerSize.toFloat())
            path.lineTo(cr, markerSize.toFloat())
            path.quadTo(0f, markerSize.toFloat(), 0f, markerSize - cr)
            path.lineTo(0f, cr)
            path.quadTo(0f, 0f, cr, 0f)
            path.close()
            canvas.drawPath(path, paint)
        }

    val iconBitmap = try {
        ContextCompat.getDrawable(context, R.drawable.ic_user)?.let { drawableToBitmap(it, iconSize, iconSize) }
            ?: createUserIconBitmap(iconSize, iconSize)
    } catch (e: Exception) {
        createUserIconBitmap(iconSize, iconSize)
    }

    if (markerDrawable != null) {
        val newBitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        val paint = Paint().apply { colorFilter = android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN) }
        canvas.drawBitmap(bgBitmap, 0f, 0f, paint)
        bgBitmap.recycle()
        return Pair(newBitmap, iconBitmap)
    }

    return Pair(bgBitmap, iconBitmap)
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker
) : androidx.lifecycle.ViewModel() {

    private val _hasLocationPermission = MutableStateFlow(permissionChecker.hasLocationPermissions())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    fun refreshPermissions() {
        _hasLocationPermission.value = permissionChecker.hasLocationPermissions()
    }
}

@Composable
fun OsmMapContainer(
    modifier: Modifier = Modifier,
    markerColor: MarkerColor = MarkerColor.GREY,
    onPointerClick: () -> Unit
) {
    val ctx = LocalContext.current
    var lastFixLocation by remember { mutableStateOf<Location?>(null) }
    val moscowPoint = remember { GeoPoint(55.7558, 37.6173) }
    var directionAngle by remember { mutableStateOf(0f) }

    val colorState = rememberUpdatedState(markerColor)
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val viewModel: MapViewModel = hiltViewModel()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()

    fun updateDirection(newLoc: Location) {
        lastFixLocation?.let { prev ->
            val dLat = newLoc.latitude - prev.latitude
            val dLon = newLoc.longitude - prev.longitude
            if (dLat != 0.0 || dLon != 0.0) {
                directionAngle = Math.toDegrees(atan2(dLon, dLat)).toFloat()
            }
        }
        lastFixLocation = newLoc
    }

    var initialPositionSet by remember { mutableStateOf(false) }
    var followLocationEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            initialPositionSet = false
            followLocationEnabled = false
        }
    }

    AndroidView(factory = { ctx ->
        val cacheDir = File(ctx.cacheDir, "osmdroid_tiles").apply { mkdirs() }
        Configuration.getInstance().apply {
            userAgentValue = "WaliotTracker/1.0"
            osmdroidBasePath = ctx.cacheDir
            osmdroidTileCache = cacheDir
        }

        MapView(ctx).apply {
            mapViewRef = this
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(18.0)
            isHorizontalMapRepetitionEnabled = false
            setBuiltInZoomControls(false)
            setTilesScaledToDpi(true)
            minZoomLevel = 4.0
            maxZoomLevel = 19.0
            isClickable = true

            val density = ctx.resources.displayMetrics.density
            val markerSize = (48 * density).toInt()
            val iconSize = (32 * density).toInt()
            val preGeneratedBitmaps = MarkerColor.values().associateWith { color ->
                createColoredMarkerBitmap(ctx, color.colorInt, markerSize, iconSize)
            }

            val overlay = object : MyLocationNewOverlay(GpsMyLocationProvider(ctx), this) {
                private var currentBackgroundBitmap: Bitmap? = null
                private var currentIconBitmap: Bitmap? = null
                private var lastColor: MarkerColor? = null
                private var firstFixReceived = false

                private fun updateBitmaps(color: MarkerColor) {
                    if (lastColor != color) {
                        val (bg, icon) = preGeneratedBitmaps[color]!!
                        currentBackgroundBitmap = bg
                        currentIconBitmap = icon
                        lastColor = color
                    }
                }

                override fun drawMyLocation(canvas: Canvas?, pj: Projection?, lastFix: Location?) {
                    if (canvas == null || pj == null) return
                    val loc = lastFix ?: lastFixLocation ?: return
                    updateBitmaps(colorState.value)
                    updateDirection(loc)

                    val point = pj.toPixels(GeoPoint(loc.latitude, loc.longitude), null)

                    currentBackgroundBitmap?.let {
                        val bgMatrix = Matrix().apply {
                            postTranslate(-it.width / 2f, -it.height / 2f)
                            postRotate(directionAngle - 45)
                            postTranslate(point.x.toFloat(), point.y.toFloat())
                        }
                        canvas.drawBitmap(it, bgMatrix, null)
                    }

                    currentIconBitmap?.let {
                        val iconMatrix = Matrix().apply {
                            postTranslate(-it.width / 2f, -it.height / 2f)
                            postTranslate(point.x.toFloat(), point.y.toFloat())
                        }
                        canvas.drawBitmap(it, iconMatrix, null)
                    }
                }

                override fun onSingleTapConfirmed(e: android.view.MotionEvent?, map: MapView?): Boolean {
                    if (e == null || map == null) return false
                    val loc = myLocation ?: lastFixLocation ?: return false
                    val point = map.projection.toPixels(loc as IGeoPoint?, null)
                    val radius = currentBackgroundBitmap?.width?.div(2) ?: 0
                    if (e.x.toInt() in (point.x - radius)..(point.x + radius) &&
                        e.y.toInt() in (point.y - radius)..(point.y + radius)
                    ) {
                        onPointerClick()
                        map.invalidate()
                        return true
                    }
                    return false
                }

                override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
                    super.onLocationChanged(location, source)

                    location?.let { loc ->
                        val previousLoc = lastFixLocation
                        lastFixLocation = loc

                        previousLoc?.let { prev ->
                            val dLat = loc.latitude - prev.latitude
                            val dLon = loc.longitude - prev.longitude
                            if (dLat != 0.0 || dLon != 0.0) {
                                directionAngle = Math.toDegrees(atan2(dLon, dLat)).toFloat()
                            }
                        }

                        if (hasLocationPermission && !firstFixReceived && !initialPositionSet) {
                            firstFixReceived = true
                            post {
                                controller.setCenter(GeoPoint(loc.latitude, loc.longitude))
                                initialPositionSet = true

                                if (!followLocationEnabled) {
                                    enableFollowLocation()
                                    followLocationEnabled = true
                                }
                            }
                        }
                    }
                }

                override fun onDetach(mapView: MapView?) {
                    preGeneratedBitmaps.values.forEach { (bg, icon) ->
                        bg.recycle()
                        icon.recycle()
                    }
                    super.onDetach(mapView)
                }
            }

            if (hasLocationPermission) {
                overlay.enableMyLocation()
                overlay.isDrawAccuracyEnabled = false

                overlay.disableFollowLocation()
                followLocationEnabled = false

                try {
                    val locationProvider = GpsMyLocationProvider(ctx)
                    locationProvider.getLastKnownLocation()?.let { lastKnownLocation ->
                        post {
                            controller.setCenter(GeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude))
                            initialPositionSet = true

                            lastFixLocation = lastKnownLocation

                            overlay.enableFollowLocation()
                            followLocationEnabled = true
                        }
                    }
                } catch (e: Exception) {
                }

                overlay.runOnFirstFix {
                    post {
                        val geoPoint = overlay.myLocation
                        if (geoPoint != null && !initialPositionSet) {
                            controller.setCenter(geoPoint)
                            initialPositionSet = true

                            val location = Location("gps").apply {
                                latitude = geoPoint.latitude
                                longitude = geoPoint.longitude
                            }
                            lastFixLocation = location

                            if (!followLocationEnabled) {
                                overlay.enableFollowLocation()
                                followLocationEnabled = true
                            }
                        }
                    }
                }
            } else {
                overlay.disableMyLocation()
                overlay.disableFollowLocation()
                followLocationEnabled = false
                post {
                    controller.setCenter(moscowPoint)
                    initialPositionSet = true
                }
            }

            overlays.add(overlay)
        }
    }, update = { mapView ->
        mapView.post {
            viewModel.refreshPermissions()

            val overlay = mapView.overlays.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
            overlay?.apply {
                if (hasLocationPermission) {
                    enableMyLocation()
                    isDrawAccuracyEnabled = false

                    if (!initialPositionSet) {
                        myLocation?.let { geoPoint ->
                            // Мгновенная установка центра
                            mapView.controller.setCenter(geoPoint)
                            initialPositionSet = true

                            val location = Location("gps").apply {
                                latitude = geoPoint.latitude
                                longitude = geoPoint.longitude
                            }
                            lastFixLocation = location

                            if (!followLocationEnabled) {
                                enableFollowLocation()
                                followLocationEnabled = true
                            }
                        } ?: run {
                            disableFollowLocation()
                            followLocationEnabled = false
                        }
                    } else {
                        if (followLocationEnabled) {
                            enableFollowLocation()
                        } else {
                            disableFollowLocation()
                        }
                    }
                } else {
                    disableMyLocation()
                    disableFollowLocation()
                    followLocationEnabled = false
                    mapView.controller.setCenter(moscowPoint)
                    initialPositionSet = true
                }
            }
            mapView.invalidate()
        }
    }, modifier = modifier)
}