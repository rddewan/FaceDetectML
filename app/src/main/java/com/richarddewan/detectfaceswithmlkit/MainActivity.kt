package com.richarddewan.detectfaceswithmlkit

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val RC_IMAGE_CAPTURE = 1
        const val RC_PICK_FILE = 2
    }

    private lateinit var highAccuracyOpts: FirebaseVisionFaceDetectorOptions
    private lateinit var realTimeOpts: FirebaseVisionFaceDetectorOptions
    private lateinit var firebaseVisionImage: FirebaseVisionImage
    private lateinit var firebaseVisionFaceDetector: FirebaseVisionFaceDetector
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var paintText: Paint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // High-accuracy landmark detection and face classification
        highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

        // Real-time contour detection of multiple faces
        realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()

        firebaseVisionFaceDetector = FirebaseVision.getInstance()
            .getVisionFaceDetector(highAccuracyOpts)


        btnCamera.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(packageManager)?.also {
                    startActivityForResult(intent, RC_IMAGE_CAPTURE)
                }
            }
        }

        btnBrowseImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).run {
                type = "image/*"
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(this, RC_PICK_FILE)
            }
        }
    }

    private fun detectFaceFromBitmap(bitmap: Bitmap? = null, uri: Uri? = null) {
        imageView.setImageDrawable(null)
        progressBar.visibility = View.VISIBLE

        val mBitmap: Bitmap
        paint = Paint(Paint.FILTER_BITMAP_FLAG)
        paintText = Paint(Paint.LINEAR_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE

        if (bitmap != null) {
            //create a scale bitmap
            val scaleBitMap = Bitmap.createScaledBitmap(bitmap,480,480,true)
            firebaseVisionImage = FirebaseVisionImage.fromBitmap(scaleBitMap)

            mBitmap = scaleBitMap.copy(scaleBitMap.config, true)
            //create canvas from bitmap
            canvas = Canvas(mBitmap)
            //set paint property
            paint.color = Color.RED
            paintText.textSize = 10f
            paintText.style = Paint.Style.FILL
            paintText.color = Color.BLUE

        } else {
            firebaseVisionImage = FirebaseVisionImage.fromFilePath(this, uri!!)
            val tempBitmap = getBitmapFromUri(uri)
            //create a mutable bitmap for drawing
            mBitmap = tempBitmap.copy(Bitmap.Config.ARGB_8888, true)
            //create canvas from bitmap
            canvas = Canvas(mBitmap)
            //set paint property
            paint.color = Color.RED
            paint.strokeWidth = 12f
            paintText.textSize = 30f
            paintText.style = Paint.Style.FILL
            paintText.color = Color.BLUE

        }

        //imageView.setImageBitmap(mBitmap)
        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val bounds = face.boundingBox
                    //draw rect
                    canvas.drawRect(bounds, paint)

                    val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                    val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees



                    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                    // nose available):
                    val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
                    leftEar?.let {
                        val leftEarPosition = leftEar.position
                        val rect = Rect(
                            leftEarPosition.x.toInt() - 20,
                            leftEarPosition.y.toInt() - 30,
                            leftEarPosition.x.toInt() + 2,
                            leftEarPosition.y.toInt() + 30
                        )
                        canvas.drawRect(rect, paint)
                    }
                    val rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR)
                    rightEar?.let {
                        val rightEarPosition = rightEar.position
                        val rect = Rect(
                            rightEarPosition.x.toInt() + 17,
                            rightEarPosition.y.toInt() - 25,
                            rightEarPosition.x.toInt() ,
                            rightEarPosition.y.toInt() + 30
                        )
                        canvas.drawRect(rect, paint)
                    }

                    //eye
                    val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
                    leftEye?.let {
                        val leftEyePosition = leftEye.position
                        val rect = Rect(leftEyePosition.x.toInt() - 30,
                            leftEyePosition.y.toInt() - 10,
                            leftEyePosition.x.toInt() + 20,
                            leftEyePosition.y.toInt() + 10
                        )
                        canvas.drawRect(rect, paint)
                    }
                    val rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
                    rightEye?.let {
                        val rightEyePosition = rightEye.position
                        val rect = Rect(rightEyePosition.x.toInt() - 30,
                            rightEyePosition.y.toInt() - 10,
                            rightEyePosition.x.toInt() + 20,
                            rightEyePosition.y.toInt() + 10
                        )
                        canvas.drawRect(rect, paint)
                    }

                    val noseBase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)
                    noseBase?.let {
                        val rect = Rect(noseBase.position.x.toInt() - 30,
                            noseBase.position.y.toInt() - 10,
                            noseBase.position.x.toInt() + 20,
                            noseBase.position.y.toInt() + 10
                        )
                        canvas.drawRect(rect,paint)


                    }

                    val mouthLeft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT)
                    val mouthRight = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT)
                    val mouthBottom = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM)
                    mouthLeft?.let {
                        val mouthLeftPosition = mouthLeft.position
                        mouthRight?.let {
                            val mouthRightPosition = mouthRight.position
                            mouthBottom?.let {
                                val mouthBottomPosition = mouthBottom.position
                                val path = Path()
                                path.moveTo(mouthLeftPosition.x -3, mouthLeftPosition.y -5)
                                path.lineTo(mouthRightPosition.x +3,mouthRightPosition.y -5)
                                path.lineTo(mouthBottomPosition.x ,mouthBottomPosition.y + 5)
                                path.close()
                                canvas.drawPath(path, paint)

                            }

                        }

                    }

                    // If contour detection was enabled:
                    val leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
                    val upperLipBottomContour =
                        face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points

                    // If classification was enabled:
                    if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        val smileProb = face.smilingProbability

                        if (smileProb >= 0.5){
                            mouthBottom?.let {
                                canvas.drawText("Smiling:\n ${smileProb * 100} %",
                                    mouthBottom.position.x - 20 ,mouthBottom.position.y,paintText)
                            }
                        }
                        else {
                            mouthBottom?.let {
                                canvas.drawText("Serious",
                                    mouthBottom.position.x - 20 ,mouthBottom.position.y + 10,paintText)
                            }

                        }
                    }
                    if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        val rightEyeOpenProb = face.rightEyeOpenProbability
                        rightEye?.let {
                            canvas.drawText("Open:\n ${rightEyeOpenProb * 100} %",
                                rightEye.position.x - 30 ,rightEye.position.y - 30,paintText)
                        }

                    }
                    if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        val leftEyeOpenProb = face.leftEyeOpenProbability
                        leftEye?.let {
                            canvas.drawText("Open:\n ${leftEyeOpenProb * 100} %",
                                leftEye.position.x - 30 ,leftEye.position.y - 30,paintText)
                        }
                    }

                    // If face tracking was enabled:
                    if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
                        val id = face.trackingId
                    }
                }

                imageView.setImageBitmap(mBitmap)
                //hide progress bar
                progressBar.visibility = View.GONE

            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Log.e(TAG, it.toString())

            }

    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {

        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                val bitmap = data?.extras?.get("data") as Bitmap
                imageView.setImageBitmap(bitmap)

                detectFaceFromBitmap(bitmap)
            }
        }
        if (requestCode == RC_PICK_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.data

                detectFaceFromBitmap(null, uri)
                //imageView.setImageBitmap(bitmap)


            }
        }
    }
}
