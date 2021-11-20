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
import android.widget.ImageView
import com.google.android.material.slider.Slider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import android.os.Looper.getMainLooper
import android.widget.Button
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.*
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity


@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest {

    private val activityController = Robolectric.buildActivity(MainActivity::class.java)
    val activity = activityController.setup().get()

    //@ExperimentalCoroutinesApi
    //@get:Rule
    //var mainCoroutineRule = MainCoroutineRule()


    @Test
    fun testShouldCheckSliderExist() {
        val slBrightness = activity.findViewById<Slider>(R.id.slBrightness)

        val message = "does view with id \"slBrightness\" placed in activity?"
        assertNotNull(message, slBrightness)

        val message2 = "\"slider\" should have proper stepSize attribute"
        assertEquals(message2, slBrightness.stepSize, 10f)

        val message3 = "\"slider\" should have proper valueFrom attribute"
        assertEquals(message3, slBrightness.valueFrom, -250f)

        val message4 = "\"slider\" should have proper valueTo attribute"
        assertEquals(message4, slBrightness.valueTo, 250f)

        val message5 = "\"slider\" should have proper initial value"
        assertEquals(message5, slBrightness.value, 0f)
    }

    @Test
    fun testShouldCheckSliderNotCrashingByDefault() {
        val slBrightness = activity.findViewById<Slider>(R.id.slBrightness)
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        slBrightness.value += slBrightness.stepSize
        val bitmap = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        val message2 = "is \"ivPhoto\" not empty and no crash occurs while swiping slider?"
        assertNotNull(message2, bitmap)
        slBrightness.value -= slBrightness.stepSize
    }

    @Test
    fun testShouldCheckImageIsSetToDefaultBitmap() {
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        val message = "is defaultBitmap set correctly?"
        val bitmap = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        val bitmap2 = createBitmap()
        assertEquals(message, singleColor(bitmap), singleColor(bitmap2))
        assertEquals(message, bitmap.width, bitmap2.width)
    }


    @Test
    fun testShouldCheckSaveFunIsCalled() {
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        val bitmap = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        val slBrightness = activity.findViewById<Slider>(R.id.slBrightness)
        val model = mockk<IBrightnessFilter>(relaxed = true)
        every {
            runBlocking {
                model.setBrightness(any(),any())
            }
        } returns Unit


        val sliderExecutor = SliderExecutor(model, slBrightness, bitmap)
        sliderExecutor.slBrightness.value +=  sliderExecutor.slBrightness.stepSize
        sliderExecutor.slBrightness.value -=  sliderExecutor.slBrightness.stepSize

            verify {
                runBlocking {
                    model.setBrightness(any(),any())
                }
                shadowOf(getMainLooper()).idle()
            }
    }


    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        val slBrightness = activity.findViewById<Slider>(R.id.slBrightness)
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        val bitmap = createBitmap()
        var img0 = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        var RGB0 = img0?.let { singleColor(it) }
        slBrightness.value += slBrightness.stepSize*2
        slBrightness.value += slBrightness.stepSize
        shadowOf(getMainLooper()).idle()
        val img2 = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
        val RGB2 = singleColor(img2)
        val message2 = "val0 ${RGB0} val2 ${RGB2}"
        if (RGB0 != null) {
            assertNotEquals(message2,singleColor(bitmap).first+30, RGB2.first)
            assertNotEquals(message2,singleColor(bitmap).second+30, RGB2.second)
            assertNotEquals(message2,singleColor(bitmap).third+30, RGB2.third)
        }
        slBrightness.value -= slBrightness.stepSize*3
    }


    @Test
    fun testShouldCheckDefaultBitmapEditCoroutine() {
        val defaultBitmap = createBitmap()
        var RGB0 = singleColor(defaultBitmap)
        val slBrightness = activity.findViewById<Slider>(R.id.slBrightness)
        val ivPhoto = activity.findViewById<ImageView>(R.id.ivPhoto)
        for (i in 1..2) {
            val model = runBlocking {
                slBrightness.value -= slBrightness.stepSize
                val model = mockk<BrightnessFilter>(relaxed = true)
                //val model = BrightnessFilter(slBrightness, ivPhoto, coroutineContext)
                model.setBrightness(defaultBitmap, slBrightness.value.toInt())
                model
            }
            shadowOf(getMainLooper()).idle()
            val bitmap = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
            val img2 = (ivPhoto.getDrawable() as BitmapDrawable).bitmap
            val RGB2 = singleColor(img2)
            val message2 = "val0 ${RGB0} val2 ${RGB2}"

            assertEquals(message2,changeBrightness(RGB0.first,-slBrightness.stepSize*i.toDouble()), RGB2.first)
            assertEquals(message2,changeBrightness(RGB0.second,-slBrightness.stepSize*i.toDouble()), RGB2.second)
            assertEquals(message2,changeBrightness(RGB0.third,-slBrightness.stepSize*i.toDouble()), RGB2.third)
        }
    }


    fun singleColor(source: Bitmap): Triple<Int, Int, Int> {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        var index: Int
        var R: Int
        var G: Int
        var B: Int
        var y = 80
        var x = 90

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
                R = x % 100
                G = y % 100
                B = (x+y) % 100

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