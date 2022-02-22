package io.supercharge.the3dmodelsampleapp.nodes

import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.RenderableDefinition
import com.google.ar.sceneform.rendering.RenderableDefinition.Submesh
import com.google.ar.sceneform.rendering.Vertex
import com.google.ar.sceneform.rendering.Vertex.UvCoordinate
import com.google.ar.sceneform.utilities.AndroidPreconditions
import java.lang.AssertionError
import java.util.ArrayList
import java.util.concurrent.ExecutionException

class BoxFactory {

    companion object {
        private const val COORDS_PER_TRIANGLE = 3

        fun makeCube(
            size: Vector3,
            center: Vector3?,
            materials: List<Material>,
        ): ModelRenderable {
            AndroidPreconditions.checkMinAndroidApiLevel()
            val extents = size.scaled(0.5f)

            val p0 = Vector3.add(center, Vector3(-extents.x, -extents.y, extents.z))
            val p1 = Vector3.add(center, Vector3(extents.x, -extents.y, extents.z))
            val p2 = Vector3.add(center, Vector3(extents.x, -extents.y, -extents.z))
            val p3 = Vector3.add(center, Vector3(-extents.x, -extents.y, -extents.z))
            val p4 = Vector3.add(center, Vector3(-extents.x, extents.y, extents.z))
            val p5 = Vector3.add(center, Vector3(extents.x, extents.y, extents.z))
            val p6 = Vector3.add(center, Vector3(extents.x, extents.y, -extents.z))
            val p7 = Vector3.add(center, Vector3(-extents.x, extents.y, -extents.z))

            val up = Vector3.up()
            val down = Vector3.down()
            val front = Vector3.forward()
            val back = Vector3.back()
            val left = Vector3.left()
            val right = Vector3.right()

            val uv00 = UvCoordinate(0.0f, 0.0f)
            val uv10 = UvCoordinate(1.0f, 0.0f)
            val uv01 = UvCoordinate(0.0f, 1.0f)
            val uv11 = UvCoordinate(1.0f, 1.0f)

            val vertices = ArrayList(
                listOf(
                    // Bottom
                    Vertex.builder().setPosition(p0).setNormal(down).setUvCoordinate(uv01).build(),
                    Vertex.builder().setPosition(p1).setNormal(down).setUvCoordinate(uv11).build(),
                    Vertex.builder().setPosition(p2).setNormal(down).setUvCoordinate(uv10).build(),
                    Vertex.builder().setPosition(p3).setNormal(down).setUvCoordinate(uv00).build(),

                    // Left
                    Vertex.builder().setPosition(p7).setNormal(left).setUvCoordinate(uv01).build(),
                    Vertex.builder().setPosition(p4).setNormal(left).setUvCoordinate(uv11).build(),
                    Vertex.builder().setPosition(p0).setNormal(left).setUvCoordinate(uv10).build(),
                    Vertex.builder().setPosition(p3).setNormal(left).setUvCoordinate(uv00).build(),

                    // Back
                    Vertex.builder().setPosition(p4).setNormal(back).setUvCoordinate(uv01).build(),
                    Vertex.builder().setPosition(p5).setNormal(back).setUvCoordinate(uv11).build(),
                    Vertex.builder().setPosition(p1).setNormal(back).setUvCoordinate(uv10).build(),
                    Vertex.builder().setPosition(p0).setNormal(back).setUvCoordinate(uv00).build(),

                    // Front
                    Vertex.builder().setPosition(p6).setNormal(front).setUvCoordinate(uv01).build(),
                    Vertex.builder().setPosition(p7).setNormal(front).setUvCoordinate(uv11).build(),
                    Vertex.builder().setPosition(p3).setNormal(front).setUvCoordinate(uv10).build(),
                    Vertex.builder().setPosition(p2).setNormal(front).setUvCoordinate(uv00).build(),

                    // Right
                    Vertex.builder().setPosition(p5).setNormal(right).setUvCoordinate(uv01).build(),
                    Vertex.builder().setPosition(p6).setNormal(right).setUvCoordinate(uv11).build(),
                    Vertex.builder().setPosition(p2).setNormal(right).setUvCoordinate(uv10).build(),
                    Vertex.builder().setPosition(p1).setNormal(right).setUvCoordinate(uv00).build(),

                    // Top
                    Vertex.builder().setPosition(p7).setNormal(up).setUvCoordinate(uv01).build(),
                    Vertex.builder().setPosition(p6).setNormal(up).setUvCoordinate(uv11).build(),
                    Vertex.builder().setPosition(p5).setNormal(up).setUvCoordinate(uv10).build(),
                    Vertex.builder().setPosition(p4).setNormal(up).setUvCoordinate(uv00).build()
                )
            )

            val numSides = 6
            val verticesPerSide = 4
            val trianglesPerSide = 2

            val submeshes = mutableListOf<Submesh>()

            for (i in 0 until numSides) {
                val triangleIndices = ArrayList<Int>(trianglesPerSide * COORDS_PER_TRIANGLE)

                // First triangle for this side.
                triangleIndices.add(3 + verticesPerSide * i)
                triangleIndices.add(1 + verticesPerSide * i)
                triangleIndices.add(0 + verticesPerSide * i)

                // Second triangle for this side.
                triangleIndices.add(3 + verticesPerSide * i)
                triangleIndices.add(2 + verticesPerSide * i)
                triangleIndices.add(1 + verticesPerSide * i)

                submeshes.add(
                    Submesh.builder()
                        .setTriangleIndices(triangleIndices)
                        .setMaterial(
                            materials[i]
                        )
                        .build()
                )
            }

            val renderableDefinition = RenderableDefinition.builder()
                .setVertices(vertices)
                .setSubmeshes(submeshes)
                .build()

            val future = ModelRenderable.builder().setSource(renderableDefinition).build()

            val result: ModelRenderable = try {
                future.get()
            } catch (ex: ExecutionException) {
                throw AssertionError("Error creating renderable.", ex)
            } catch (ex: InterruptedException) {
                throw AssertionError("Error creating renderable.", ex)
            } ?: throw AssertionError("Error creating renderable.")

            return result
        }
    }
}
