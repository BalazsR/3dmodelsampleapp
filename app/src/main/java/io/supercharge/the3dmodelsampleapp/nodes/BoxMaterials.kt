package io.supercharge.the3dmodelsampleapp.nodes

import com.google.ar.sceneform.rendering.Material

data class BoxMaterials(
    val bottom: Material,
    val left: Material,
    val front: Material,
    val back: Material,
    val right: Material,
    val top: Material,
)
