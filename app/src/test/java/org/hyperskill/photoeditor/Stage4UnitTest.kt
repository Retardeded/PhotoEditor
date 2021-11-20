package org.hyperskill.photoeditor

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.slider.Slider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity
import androidx.test.platform.app.InstrumentationRegistry


@RunWith(RobolectricTestRunner::class)
class Stage4UnitTest {

    private val activityController = Robolectric.buildActivity(MainActivity::class.java)
    val activity = activityController.setup().get()

    @Test
    fun testShouldCheckSliderExist() {
        val slContrast = activity.findViewById<Slider>(R.id.slBrightness)

        val message = "does view with id \"slContrast\" placed in activity?"
        assertNotNull(message, slContrast)

        val message2 = "\"slider\" should have proper stepSize attribute"
        assertEquals(message2, slContrast.stepSize, 10f)

        val message3 = "\"slider\" should have proper valueFrom attribute"
        assertEquals(message3, slContrast.valueFrom, -250f)

        val message4 = "\"slider\" should have proper valueTo attribute"
        assertEquals(message4, slContrast.valueTo, 250f)

        val message5 = "\"slider\" should have proper initial value"
        assertEquals(message5, slContrast.value, 0f)
    }

    @Test
    fun testShouldCheckSliderNotCrashingByDefault() {
        val slContrast = activity.findViewById<Slider>(R.id.slContrast)
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        slContrast.value += slContrast.stepSize
        val bitmap = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        val message2 = "is \"ivPhoto\" not empty and no crash occurs while swiping slider?"
        assertNotNull(message2, bitmap)
        slContrast.value -= slContrast.stepSize
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        val slBrightness = activity.findViewById<Slider>(R.id.slBrightness)
        val slContrast = activity.findViewById<Slider>(R.id.slContrast)
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        var img0 = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        var RGB0 = img0?.let { singleColor(it,80, 90) }
        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize*9
        slContrast.value += slContrast.stepSize
        val img2 = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        val RGB2 = singleColor(img2, 80, 90)
        val message2 = "val0 ${RGB0} val2 ${RGB2}"
        if (RGB0 != null) {
            assertEquals(message2,141, RGB2.first)
            assertEquals(message2,201, RGB2.second)
            assertEquals(message2,255, RGB2.third)
        }
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit2() {
        val slBrightness = activity.findViewById<Slider>(R.id.slBrightness)
        val slContrast = activity.findViewById<Slider>(R.id.slContrast)
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        var img0 = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        var RGB0 = img0?.let { singleColor(it, 80, 90) }
        slBrightness.value += slBrightness.stepSize
        slBrightness.value += slBrightness.stepSize
        slContrast.value -= slContrast.stepSize
        val img2 = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        val RGB2 = singleColor(img2, 80, 90)
        val message2 = "val0 ${RGB0} val2 ${RGB2}"
        if (RGB0 != null) {
            assertEquals(message2,149, RGB2.first)
            assertEquals(message2,149, RGB2.second)
            assertEquals(message2,149, RGB2.third)
        }
    }

    fun singleColor(source: Bitmap, x0:Int, y0:Int): Triple<Int, Int, Int> {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        var index: Int
        var R: Int
        var G: Int
        var B: Int
        var y = x0
        var x = y0

        index = y * width + x
        // get color
        R = Color.red(pixels[index])
        G = Color.green(pixels[index])
        B = Color.blue(pixels[index])

        return  Triple(R,G,B)
    }

    fun createBitmap(): Bitmap {
        val width = 200
        val height = 100
        val pixels = IntArray(width * height)
        // get pixel array from source

        var R: Int
        var G: Int
        var B: Int
        var index: Int

        for (y in 0 until height) {
            for (x in 0 until width) {
                // get current index in 2D-matrix
                index = y * width + x
                // get color
                R = x % 100 + 40
                G = y % 100 + 80
                B = (x+y) % 100 + 120

                pixels[index] = Color.rgb(R,G,B)

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }

    fun changeBrightness(colorValue:Int, filterValue:Double):Int {
        return Math.max(Math.min(colorValue + filterValue, 255.0),0.0).toInt()
    }

    private fun createGalleryPickActivityResultStub2(activity: MainActivity): Intent {
        val resources: Resources = InstrumentationRegistry.getInstrumentation().context.resources
        val imageUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.myexample))
            .appendPath(resources.getResourceTypeName(R.drawable.myexample))
            .appendPath(resources.getResourceEntryName(R.drawable.myexample))
            .build()
        val resultIntent = Intent()
        val uri = getUriToDrawable(activity,R.drawable.myexample)
        resultIntent.setData(uri)
        return resultIntent
    }

    fun getUriToDrawable(
        context: Context,
        drawableId: Int
    ): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getResources().getResourcePackageName(drawableId)
                    + '/' + context.getResources().getResourceTypeName(drawableId)
                    + '/' + context.getResources().getResourceEntryName(drawableId)
        )
    }
}