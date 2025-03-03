package com.jakewharton.mosaic

import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.testing.SnapshotStrategy

internal object DumpSnapshots : SnapshotStrategy<String> {
	override fun create(mosaic: Mosaic): String {
		return mosaic.dump()
	}
}

internal object NodeSnapshots : SnapshotStrategy<MosaicNode> {
	override fun create(mosaic: Mosaic): MosaicNode {
		return (mosaic as MosaicComposition).rootNode
	}
}

internal class RenderingSnapshots(
	private val vtDisplay: VtDisplay,
) : SnapshotStrategy<String> {
	override fun create(mosaic: Mosaic): String {
		return vtDisplay.render(mosaic).toString()
	}
}
