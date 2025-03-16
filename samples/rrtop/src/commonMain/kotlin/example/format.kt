package example

private const val BytesInKiB = 2L shl 10
private const val BytesInMiB = 2L shl 20
private const val BytesInGiB = 2L shl 30
private const val BytesInTiB = 2L shl 40

private const val OneThousand = 1_000L
private const val TenThousand = 10_000L
private const val OneHundredThousand = 100_000L
private const val OneMillion = 1_000_000L
private const val TenMillion = 10_000_000L
private const val OneHundredMillion = 100_000_000L
private const val OneBillion = 1_000_000_000L
private const val TenBillion = 10_000_000_000L

fun Long.formatBytes(): String {
	return when {
		this < BytesInKiB -> "$this B"
		this < BytesInMiB -> "${String.format("%.2f", this / BytesInKiB.toDouble())} KiB"
		this < BytesInGiB -> "${String.format("%.2f", this / BytesInMiB.toDouble())} MiB"
		this < BytesInTiB -> "${String.format("%.2f", this / BytesInGiB.toDouble())} GiB"
		else -> "${String.format("%.2f", this / BytesInTiB.toDouble())} TiB"
	}
}

fun Long.formatToShortForm(): String {
	return when {
		this < TenThousand -> this.toString()
		this < OneHundredThousand -> "${String.format("%.2f", this / OneThousand.toDouble())}K"
		this < OneMillion -> "${String.format("%.1f", this / OneThousand.toDouble())}K"
		this < TenMillion -> "${this / OneThousand}K"
		this < OneHundredMillion -> "${String.format("%.2f", this / OneMillion.toDouble())}M"
		this < OneBillion -> "${String.format("%.1f", this / OneMillion.toDouble())}M"
		this < TenBillion -> "${this / OneMillion}M"
		else -> "${String.format("%.2f", this / OneBillion.toDouble())}B"
	}
}

fun Float.formatPercent(): String {
	return String.format("%.2f%%", this)
}
