package pa.com.purpleponydev.bluetoothprinterlib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.OutputStream

data class PrintConfig(
    var xStart: Int = 30,
    var yStart: Int = 400,
    var labelHeightLimit: Int = 3200,
    var lineHeight: Int = 30,
    var fontHeight: Int = 24,
    var fontWidth: Int = 12,
    var labelWidth: Int = 600,// ajustar a 400 o menos para bixolon
    var labelLength: Int = 800,
    var printSpeed: String = "2",
    var mediaMode: String = "T",
    var useLogo: Boolean = true, // Nueva propiedad
    var logoName: String = "logo", // Nombre de imagen en memoria de la impresora
    val labelHeightMin: Int = 300
)

fun StringBuilder.addTextLine(
    text: String,
    config: PrintConfig,
    centered: Boolean = false
) {
    val maxCharsPerLine = config.labelWidth / config.fontWidth
    val lines = text.chunked(maxCharsPerLine)

    for (line in lines) {
        val x = if (centered) {
            (config.labelWidth - line.length * config.fontWidth) / 2
        } else {
            config.xStart
        }

        append("^FO$x,${config.yStart}^FD$line^FS")
        config.yStart += config.lineHeight
    }
}


fun StringBuilder.addDivider(config: PrintConfig) {
    append("^FO${config.xStart},${config.yStart}^GB500,4,1^FS")
    config.yStart += config.lineHeight
}

fun StringBuilder.addEmptyLines(count: Int, config: PrintConfig) { //Pasale la cantidad de lineas
    config.yStart += config.lineHeight * count
}

fun StringBuilder.addLogo(config: PrintConfig) {
    if (!config.useLogo) return

    val logoX = (config.labelWidth - 200) / 2 // Ajusta según tamaño del logo
    val logoY = config.yStart
    append("^FO$logoX,$logoY^XG${config.logoName},1,1^FS")
    config.yStart += 220 // Espacio ocupado por el logo
}

fun StringBuilder.addQRCode(
    data: String,
    config: PrintConfig,
    moduleSize: Int = 8,
    centered: Boolean = true
) {
    val qrWidth = estimateQRWidth(moduleSize)
    val qrX = if (centered) {
        (config.labelWidth - qrWidth) / 2
    } else {
        config.xStart
    }
    val qrY = config.yStart

    append("^FO140,$qrY")
    append("^BQN,2,$moduleSize")
    append("^FDLA,$data^FS")

    config.yStart += qrWidth + 40 // Alto aproximado igual al ancho
}

fun estimateQRWidth(moduleSize: Int, modules: Int = 23): Int {
    return moduleSize * modules
}

fun sendDrawableAsZplLogo(
    context: Context,
    drawableResId: Int,
    printerOutputStream: OutputStream,
    imageName: String = "logo" // esto se usará como logo.GRF
) {
    val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
    val zplData = convertBitmapToZpl(bitmap, imageName)
    printerOutputStream.write(zplData.toByteArray(Charsets.UTF_8))
}

fun convertBitmapToZpl(bitmap: Bitmap, imageName: String): String {
    val width = ((bitmap.width + 7) / 8) * 8
    val height = bitmap.height
    val bytesPerRow = width / 8
    val totalBytes = bytesPerRow * height

    val sb = StringBuilder()
    sb.append("~DG${imageName}.GRF,$totalBytes,$bytesPerRow,")

    for (y in 0 until height) {
        var byte = 0
        var bitCount = 0
        for (x in 0 until width) {
            val pixel = if (x < bitmap.width) bitmap.getPixel(x, y) else Color.WHITE
            val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
            val bit = if (gray < 128) 1 else 0
            byte = (byte shl 1) or bit
            bitCount++

            if (bitCount == 8) {
                sb.append(String.format("%02X", byte))
                byte = 0
                bitCount = 0
            }
        }
    }

    return sb.toString()
}

fun StringBuilder.addLogoVer2(
    config: PrintConfig,
    logoWidth: Int,
    logoHeight: Int,
    logoZplContent: String // contenido leído de logo.txt
) {
    if (!config.useLogo) return

    // Insertar el contenido ZPL del logo
    append(logoZplContent).append("\n")

    // Calcular coordenadas para centrar el logo
    val logoX = (config.labelWidth - logoWidth) / 2
    val logoY = config.yStart

    // Insertar la instrucción para imprimir el logo ya cargado
    append("^FO$logoX,$logoY^XG${config.logoName}.GRF,1,1^FS")

    // Avanzar cursor Y
    config.yStart += logoHeight + 30
}

fun readZplLogoFromAssets(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}

//Usa esta funcion pasando sus parametros donde armas la factura: zplBuilder.addLogoFromZplFile(config, zplLogo, logoHeight = 220)
//la imagen debe estar en dimensiones no mayor a 200x200 para mejor control de la impresion.
//el zplLogo lo obtienes de un logo.txt que debes generar a partir de una imagen pasandola por labelary recuerda no agregarle etiquetas de visualizacion XA XZ ya que estamos haciendo un append a la etiqueta ya existente.
// val zplLogo = readZplLogoFromAssets(requireContext(), "logo.txt")
fun StringBuilder.addLogoFromZplFile(config: PrintConfig, logoZplRaw: String, logoHeight: Int) {
    if (!config.useLogo) return

    // Reemplaza solo la parte del FO con el nuevo Y
    val updatedLogo = logoZplRaw.replace(Regex("""\^FO(\d+),(\d+)""")) {
        val x = it.groupValues[1]
        "^FO$x,${config.yStart}"
    }

    append(updatedLogo).append("\n")

    config.yStart += logoHeight + 30
}

fun StringBuilder.addTextAt(
    x: Int,
    text: String,
    config: PrintConfig
) {
    append("^FO$x,${config.yStart}^FD$text^FS\n")
}

fun StringBuilder.addLineWithColumns(
    texts: List<Pair<Int, String>>, // lista de (x, texto)
    config: PrintConfig
) {
    texts.forEach { (x, text) ->
        addTextAt(x, text, config)
    }
    config.yStart += config.lineHeight // Avanza la línea vertical
}

fun StringBuilder.addLabelValueLine(
    label: String,
    value: Double,
    config: PrintConfig,
    xValue: Int = 400,
    widthFill: Int = 6
) {
    append("^FO${config.xStart},${config.yStart}^FD$label^FS\n")
    append("^FO${xValue},${config.yStart}^FD${fill(String.format("%.2f", value), widthFill, 0)}^FS\n")
    config.yStart += config.lineHeight
}

fun StringBuilder.addSimplePaymentLine(
    label: String,
    amount: Double,
    config: PrintConfig
) {
    append("^FO${config.xStart},${config.yStart}^FD$label:^FS\n")
    append("^FO400,${config.yStart}^FD${"%.2f".format(amount)}^FS\n")
    config.yStart += config.lineHeight
}
fun fill(text: String, length: Int, align: Int): String {
    return when (align) {
        0 -> text.padEnd(length, ' ') // Alineado a la izquierda
        1 -> text.padStart(length, '0') // Alineado a la derecha con ceros
        else -> text
    }
}



