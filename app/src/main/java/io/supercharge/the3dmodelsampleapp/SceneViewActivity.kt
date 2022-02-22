package io.supercharge.the3dmodelsampleapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import io.supercharge.the3dmodelsampleapp.nodes.DragTransformableNode
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import java.lang.Exception
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.Texture
import io.supercharge.the3dmodelsampleapp.nodes.BoxFactory
import android.graphics.BitmapFactory

import android.graphics.Bitmap

import android.content.res.AssetManager
import java.io.IOException
import java.io.InputStream
import io.supercharge.the3dmodelsampleapp.nodes.BoxMaterials
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class SceneViewActivity : AppCompatActivity() {

    lateinit var sceneView: SceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_view)
        sceneView = findViewById(R.id.sceneView)
        addNodeToScene()
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    private fun addNodeToScene() {

        val canvasWidth = 5f
        val canvasHeight = 8f
        val canvasThickness = 2.0f

        val boxWidth: Float = canvasWidth / canvasHeight
        val boxHeight = 1f
        val boxDepth: Float = canvasThickness / canvasHeight

        val boxWidthHalf = boxWidth / 2
        val boxHeightHalf = boxHeight / 2
        val boxDepthHalf = boxDepth / 2

        val transformationSystem = makeTransformationSystem()
        val node = DragTransformableNode(1f, transformationSystem)

        val bitmapFromAsset = getBitmapFromAsset(this, "giraffe.jpg")
        val whiteColor = Color(resources.getColor(R.color.white, null))
        val sandBlack = Color(resources.getColor(R.color.black, null))

        val sideMaterial = MaterialFactory.makeTransparentWithColor(this, whiteColor)
        val blackMaterial = MaterialFactory.makeTransparentWithColor(this, sandBlack)

        val materialFront = Texture.builder().setSource(bitmapFromAsset).build()
            .thenCompose { texture ->
                MaterialFactory.makeTransparentWithTexture(this, texture)
            }

        val combinedDataCompletionStage: CompletionStage<BoxMaterials> = CompletableFuture.allOf(
            materialFront, sideMaterial, blackMaterial
        ).thenApply {
            BoxMaterials(
                sideMaterial.join(),
                sideMaterial.join(),
                materialFront.join(),
                blackMaterial.join(),
                sideMaterial.join(),
                sideMaterial.join(),
            )
        }

        combinedDataCompletionStage.thenApply { boxMaterials ->
            val renderable: Renderable = BoxFactory.makeCube(
                size = Vector3(boxWidth, boxHeight, boxDepth),
                center = Vector3(boxWidthHalf, boxHeightHalf, boxDepthHalf),
                materials = listOf(
                    boxMaterials.bottom,
                    boxMaterials.left,
                    boxMaterials.front,
                    boxMaterials.back,
                    boxMaterials.right,
                    boxMaterials.top,
                )
            )

            renderable.collisionShape = null
            node.renderable = renderable

            node.localPosition = Vector3(-boxWidthHalf, -boxHeightHalf, -boxDepthHalf)
            sceneView.scene.addChild(node)

            node.select()

            sceneView.setBackgroundColor(getColor(R.color.white))

            sceneView.scene
                .addOnPeekTouchListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
                    transformationSystem.onTouch(
                        hitTestResult,
                        motionEvent
                    )
                }
        }
    }

    fun getBitmapFromAsset(context: Context, filePath: String): Bitmap? {
        val assetManager: AssetManager = context.assets
        val istr: InputStream
        var bitmap: Bitmap? = null

        try {
            istr = assetManager.open(filePath)
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            // handle exception
        }

        return bitmap
    }

    private fun makeTransformationSystem(): TransformationSystem {
        val footprintSelectionVisualizer = FootprintSelectionVisualizer()
        return TransformationSystem(resources.displayMetrics, footprintSelectionVisualizer)
    }

    override fun onResume() {
        super.onResume()

        try {
            sceneView.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            sceneView.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
