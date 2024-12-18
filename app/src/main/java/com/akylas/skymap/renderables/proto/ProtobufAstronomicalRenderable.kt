// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.akylas.skymap.renderables.proto

import android.content.res.Resources
import android.util.Log
import com.akylas.skymap.R
import com.akylas.skymap.math.Vector3
import com.akylas.skymap.math.getGeocentricCoords
import com.akylas.skymap.renderables.AbstractAstronomicalRenderable
import com.akylas.skymap.renderables.LinePrimitive
import com.akylas.skymap.renderables.PointPrimitive
import com.akylas.skymap.renderables.TextPrimitive
import com.akylas.skymap.source.*
import com.akylas.skymap.source.proto.SourceProto
import com.akylas.skymap.util.MiscUtil
import java.util.*

/**
 * Implementation of the
 * [AstronomicalRenderable] interface
 * from objects serialized as protocol buffers.
 *
 * @author Brent Bryan
 */
class ProtobufAstronomicalRenderable(
    originalProto: SourceProto.AstronomicalSourceProto,
    private val resources: Resources
) : AbstractAstronomicalRenderable() {
    companion object {
        private val TAG = MiscUtil.getTag(ProtobufAstronomicalRenderable::class.java)
        private val shapeMap: MutableMap<SourceProto.Shape, PointPrimitive.Shape> = HashMap()

        // Ideally we'd get this from Context.getPackageName but for some reason passing it in as a
        // string via the contructor results in it always being null when I need it. Buggered if
        // I know why - it's certainly a concern. Hopefully this class won't be around for much longer.
        const val PACKAGE = "com.akylas.skymap"
        private fun getCoords(proto: SourceProto.GeocentricCoordinatesProto): Vector3 {
            return getGeocentricCoords(proto.rightAscension, proto.declination)
        }

        init {
            shapeMap[SourceProto.Shape.CIRCLE] = PointPrimitive.Shape.CIRCLE
            shapeMap[SourceProto.Shape.STAR] = PointPrimitive.Shape.CIRCLE
            shapeMap[SourceProto.Shape.ELLIPTICAL_GALAXY] =
                PointPrimitive.Shape.ELLIPTICAL_GALAXY
            shapeMap[SourceProto.Shape.SPIRAL_GALAXY] = PointPrimitive.Shape.SPIRAL_GALAXY
            shapeMap[SourceProto.Shape.IRREGULAR_GALAXY] = PointPrimitive.Shape.IRREGULAR_GALAXY
            shapeMap[SourceProto.Shape.LENTICULAR_GALAXY] =
                PointPrimitive.Shape.LENTICULAR_GALAXY
            shapeMap[SourceProto.Shape.GLOBULAR_CLUSTER] = PointPrimitive.Shape.GLOBULAR_CLUSTER
            shapeMap[SourceProto.Shape.OPEN_CLUSTER] =
                PointPrimitive.Shape.OPEN_CLUSTER
            shapeMap[SourceProto.Shape.NEBULA] =
                PointPrimitive.Shape.NEBULA
            shapeMap[SourceProto.Shape.HUBBLE_DEEP_FIELD] = PointPrimitive.Shape.HUBBLE_DEEP_FIELD
        }
    }

    private val proto: SourceProto.AstronomicalSourceProto

    /**
     * The data files contain only the text version of the string Ids. Looking them up
     * by this id will be expensive so precalculate any integer ids. See the datageneration
     * design doc for an explanation.
     */
    private fun processStringIds(proto: SourceProto.AstronomicalSourceProto): SourceProto.AstronomicalSourceProto {
        val processed = proto.toBuilder()
        for (strId in proto.nameStrIdsList) {
            processed.addNameIntIds(toInt(strId))
        }
        // <rant>
        // Work around Google's clumsy protocol buffer API. For some inexplicable reason the current
        // version lacks the getFooBuilderList described here:
        // https://developers.google.com/protocol-buffers/docs/reference/java-generated#fields
        // </rant>
        val newLabels: MutableList<SourceProto.LabelElementProto> = ArrayList(processed.labelCount)
        for (label in processed.labelList) {
            val labelBuilder = label.toBuilder()
            labelBuilder.stringsIntId = toInt(label.stringsStrId)
            newLabels.add(labelBuilder.build())
        }
        processed.clearLabel()
        processed.addAllLabel(newLabels)
        return processed.build()
    }

    private fun toInt(stringId: String): Int {
        val resourceId = resources.getIdentifier(stringId, "string", PACKAGE)
        return if (resourceId == 0) R.string.missing_label else resourceId
    }

    override val names: MutableList<String> = ArrayList()
/*       get() {
            if (names.isEmpty()) {
                //names.
                //names = ArrayList(proto.nameIntIdsCount)
                for (id in proto.nameIntIdsList) {
                    names!!.add(resources.getString(id))
                }
            }
            return names!!
        }

    @Synchronized
    override fun getNames(): ArrayList<String> {
        if (names == null) {
            names = ArrayList(proto.nameIntIdsCount)
            for (id in proto.nameIntIdsList) {
                names!!.add(resources.getString(id))
            }
        }
        return names!!
    }
*/
    override val searchLocation: Vector3
        get() = getCoords(proto.searchLocation)

    override val points: List<PointPrimitive>
        get() {
            if (proto.pointCount == 0) {
                return emptyList<PointPrimitive>()
            }
            val points = ArrayList<PointPrimitive>(proto.pointCount)
            for (element in proto.pointList) {
                points.add(
                    PointPrimitive(
                        getCoords(element.location),
                        element.color, element.size, shapeMap[element.shape]
                    )
                )
            }
            return points
        }
    override val labels: List<TextPrimitive>
        get() {
            if (proto.labelCount == 0) {
                return emptyList<TextPrimitive>()
            }
            val points = ArrayList<TextPrimitive>(proto.labelCount)
            for (element in proto.labelList) {
//                Log.d(TAG, "Label " + element.stringsIntId + " : " + element.stringsStrId)
                points.add(
                    TextPrimitive(
                        getCoords(element.location),
                        resources.getString(element.stringsIntId),
                        element.color, element.offset, element.fontSize
                    )
                )
            }
            return points
        }
    override val lines: List<LinePrimitive>
        get() {
            if (proto.lineCount == 0) {
                return emptyList<LinePrimitive>()
            }
            val points = ArrayList<LinePrimitive>(proto.lineCount)
            for (element in proto.lineList) {
                val vertices = ArrayList<Vector3>(element.vertexCount)
                for (elementVertex in element.vertexList) {
                    vertices.add(getCoords(elementVertex))
                }
                points.add(LinePrimitive(element.color, vertices, element.lineWidth))
            }
            return points
        }

    init {
        // Not ideal to be doing this in the constructor. TODO(john): investigate which threads
        // this is all happening on.
        proto = processStringIds(originalProto)
        for (id in proto.nameIntIdsList) {
            names.add(resources.getString(id))
        }
    }
}