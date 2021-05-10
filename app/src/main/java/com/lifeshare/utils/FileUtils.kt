package com.lifeshare.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val tag = "FileUtils"

suspend fun Fragment.compressImageFile(
        path: String,
        shouldOverride: Boolean = true,
        uri: Uri
): String {
    return withContext(Dispatchers.IO) {
        var scaledBitmap: Bitmap? = null

        try {
            val (hgt, wdt) = requireContext().getImageHgtWdt(uri)
            try {
                val bm = requireContext().getBitmapFromUri(uri)
                Log.d(tag, "original bitmap height${bm?.height} width${bm?.width}")
                Log.d(tag, "Dynamic height$hgt width$wdt")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Part 1: Decode image
            val unscaledBitmap = decodeFile(requireContext(), uri, wdt, hgt, ScalingLogic.FIT)
            if (unscaledBitmap != null) {
                if (!(unscaledBitmap.width <= 800 && unscaledBitmap.height <= 800)) {
                    // Part 2: Scale image
                    scaledBitmap = createScaledBitmap(unscaledBitmap, wdt, hgt, ScalingLogic.FIT)
                } else {
                    scaledBitmap = unscaledBitmap
                }
            }

            // Store to tmp file
            val mFolder = File("${requireContext().filesDir}/Images")
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }

            val tmpFile = File(mFolder.absolutePath, "IMG_${getTimestampString()}.png")

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tmpFile)
                scaledBitmap?.compress(
                        Bitmap.CompressFormat.PNG,
                        getImageQualityPercent(tmpFile),
                        fos
                )
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            var compressedPath = ""
            if (tmpFile.exists() && tmpFile.length() > 0) {
                compressedPath = tmpFile.absolutePath
                if (shouldOverride) {
                    val srcFile = File(path)
                    val result = tmpFile.copyTo(srcFile, true)
                    Log.d(tag, "copied file ${result.absolutePath}")
                    Log.d(tag, "Delete temp file ${tmpFile.delete()}")
                }
            }

            scaledBitmap?.recycle()

            return@withContext if (shouldOverride) path else compressedPath
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return@withContext ""
    }

}

@Throws(IOException::class)
fun Context.getBitmapFromUri(uri: Uri, options: BitmapFactory.Options? = null): Bitmap? {
    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
    val image: Bitmap? = if (options != null)
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
    else
        BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor?.close()
    return image
}

fun getTimestampString(): String {
    val date = Calendar.getInstance()
    return SimpleDateFormat("yyyy MM dd hh mm ss", Locale.US).format(date.time).replace(" ", "")
}

fun decodeResource(
        res: Resources,
        resId: Int,
        dstWidth: Int,
        dstHeight: Int,
        scalingLogic: ScalingLogic
): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(res, resId, options)
    options.inJustDecodeBounds = false
    options.inSampleSize = calculateSampleSize(
            options.outWidth, options.outHeight, dstWidth,
            dstHeight, scalingLogic
    )

    return BitmapFactory.decodeResource(res, resId, options)
}

fun decodeFile(
        context: Context,
        uri: Uri,
        dstWidth: Int,
        dstHeight: Int,
        scalingLogic: ScalingLogic
): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    context.getBitmapFromUri(uri, options)
    options.inJustDecodeBounds = false

    options.inSampleSize = calculateSampleSize(
            options.outWidth,
            options.outHeight,
            dstWidth,
            dstHeight,
            scalingLogic
    )

    return context.getBitmapFromUri(uri, options)
}

/**
 * Utility function for creating a scaled version of an existing bitmap
 *
 * @param unscaledBitmap Bitmap to scale
 * @param dstWidth Wanted width of destination bitmap
 * @param dstHeight Wanted height of destination bitmap
 * @param scalingLogic Logic to use to avoid image stretching
 * @return New scaled bitmap object
 */
fun createScaledBitmap(
        unscaledBitmap: Bitmap, dstWidth: Int, dstHeight: Int,
        scalingLogic: ScalingLogic
): Bitmap {
    val srcRect = calculateSrcRect(
            unscaledBitmap.width, unscaledBitmap.height,
            dstWidth, dstHeight, scalingLogic
    )
    val dstRect = calculateDstRect(
            unscaledBitmap.width,
            unscaledBitmap.height,
            dstWidth,
            dstHeight,
            scalingLogic
    )
    val scaledBitmap =
            Bitmap.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(scaledBitmap)
    canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))

    return scaledBitmap
}

/**
 * ScalingLogic defines how scaling should be carried out if source and
 * destination image has different aspect ratio.
 *
 * CROP: Scales the image the minimum amount while making sure that at least
 * one of the two dimensions fit inside the requested destination area.
 * Parts of the source image will be cropped to realize this.
 *
 * FIT: Scales the image the minimum amount while making sure both
 * dimensions fit inside the requested destination area. The resulting
 * destination dimensions might be adjusted to a smaller size than
 * requested.
 */
enum class ScalingLogic {
    CROP, FIT
}

/**
 * Calculate optimal down-sampling factor given the dimensions of a source
 * image, the dimensions of a destination area and a scaling logic.
 *
 * @param srcWidth Width of source image
 * @param srcHeight Height of source image
 * @param dstWidth Width of destination area
 * @param dstHeight Height of destination area
 * @param scalingLogic Logic to use to avoid image stretching
 * @return Optimal down scaling sample size for decoding
 */
fun calculateSampleSize(
        srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int,
        scalingLogic: ScalingLogic
): Int {
    if (scalingLogic == ScalingLogic.FIT) {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        return if (srcAspect > dstAspect) {
            srcWidth / dstWidth
        } else {
            srcHeight / dstHeight
        }
    } else {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        return if (srcAspect > dstAspect) {
            srcHeight / dstHeight
        } else {
            srcWidth / dstWidth
        }
    }
}

/**
 * Calculates source rectangle for scaling bitmap
 *
 * @param srcWidth Width of source image
 * @param srcHeight Height of source image
 * @param dstWidth Width of destination area
 * @param dstHeight Height of destination area
 * @param scalingLogic Logic to use to avoid image stretching
 * @return Optimal source rectangle
 */
fun calculateSrcRect(
        srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int,
        scalingLogic: ScalingLogic
): Rect {
    if (scalingLogic == ScalingLogic.CROP) {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        return if (srcAspect > dstAspect) {
            val srcRectWidth = (srcHeight * dstAspect).toInt()
            val srcRectLeft = (srcWidth - srcRectWidth) / 2
            Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight)
        } else {
            val srcRectHeight = (srcWidth / dstAspect).toInt()
            val scrRectTop = (srcHeight - srcRectHeight) / 2
            Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight)
        }
    } else {
        return Rect(0, 0, srcWidth, srcHeight)
    }
}

/**
 * Calculates destination rectangle for scaling bitmap
 *
 * @param srcWidth Width of source image
 * @param srcHeight Height of source image
 * @param dstWidth Width of destination area
 * @param dstHeight Height of destination area
 * @param scalingLogic Logic to use to avoid image stretching
 * @return Optimal destination rectangle
 */
fun calculateDstRect(
        srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int,
        scalingLogic: ScalingLogic
): Rect {
    return if (scalingLogic == ScalingLogic.FIT) {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        if (srcAspect > dstAspect) {
            Rect(0, 0, dstWidth, (dstWidth / srcAspect).toInt())
        } else {
            Rect(0, 0, (dstHeight * srcAspect).toInt(), dstHeight)
        }
    } else {
        Rect(0, 0, dstWidth, dstHeight)
    }
}

fun getImageQualityPercent(file: File): Int {
    val sizeInBytes = file.length()
    val sizeInKB = sizeInBytes / 1024
    val sizeInMB = sizeInKB / 1024

    return when {
        sizeInMB <= 1 -> 80
        sizeInMB <= 2 -> 60
        else -> 40
    }
}

fun Context.getImageHgtWdt(uri: Uri): Pair<Int, Int> {
    val opt = BitmapFactory.Options()

    /* by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded.
    If you try the use the bitmap here, you will get null.*/
    opt.inJustDecodeBounds = true
    val bm = getBitmapFromUri(uri, opt)

    var actualHgt = (opt.outHeight).toFloat()
    var actualWdt = (opt.outWidth).toFloat()

    /*val maxHeight = 816.0f
    val maxWidth = 612.0f*/
    val maxHeight = 720f
    val maxWidth = 1280f
    var imgRatio = actualWdt / actualHgt
    val maxRatio = maxWidth / maxHeight

//    width and height values are set maintaining the aspect ratio of the image
    if (actualHgt > maxHeight || actualWdt > maxWidth) {
        when {
            imgRatio < maxRatio -> {
                imgRatio = maxHeight / actualHgt
                actualWdt = (imgRatio * actualWdt)
                actualHgt = maxHeight
            }
            imgRatio > maxRatio -> {
                imgRatio = maxWidth / actualWdt
                actualHgt = (imgRatio * actualHgt)
                actualWdt = maxWidth
            }
            else -> {
                actualHgt = maxHeight
                actualWdt = maxWidth
            }
        }
    }

    return Pair(actualHgt.toInt(), actualWdt.toInt())
}
