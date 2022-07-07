package com.robotoss.filament_flutter_android.model

import com.google.android.filament.Entity
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.Material
import com.google.android.filament.VertexBuffer

class Mesh {

    @Entity
    val rendererable = EntityManager.get().create()
    var vertexBuffer: VertexBuffer? = null
    var indexBuffer: IndexBuffer? = null
    var material: Material? = null

    init {
    }

}