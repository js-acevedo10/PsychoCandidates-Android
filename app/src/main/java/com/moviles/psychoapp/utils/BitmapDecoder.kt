package com.moviles.psychoapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri


/**
 * Created by juanacevedo on 10/6/17.
 */

class BitmapDecoder {
    companion object {
        fun decodeBitmapUri(ctx: Context, uri: Uri): Bitmap {
            val targetW = 600
            val targetH = 600
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri), null, bmOptions)
            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight

            val scaleFactor = Math.min(photoW / targetW, photoH / targetH)
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri), null, bmOptions)
        }
    }
}