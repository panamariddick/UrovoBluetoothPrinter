# BluetoothPrinterLib

**BluetoothPrinterLib** es una biblioteca para impresión Bluetooth en Android. Permite conectar a impresoras térmicas, enviar texto, imágenes y códigos QR de forma sencilla y eficiente.

## 🚀 Ejemplo de Uso

```kotlin
val bluetoothPrinter = BluetoothPrinterLib(applicationContext)

// Conectar a la impresora
val isConnected = bluetoothPrinter.connectToPrinter("00:11:22:33:44:55")

if (isConnected) {
    // Enviar texto
    bluetoothPrinter.printText("¡Hola, impresora!")

    // Enviar un StringBuilder
    bluetoothPrinter.printData(strData) // strData: Texto con saltos de línea amplios

    // Cerrar conexión
    bluetoothPrinter.closeConnection()
} else {
    Timber.e("No se pudo conectar a la impresora.")
}
## 🧾 Ejemplo de Construcción de Documento ZPL

A continuación se muestra un ejemplo de cómo generar un documento ZPL utilizando un `StringBuilder` personalizado:

```kotlin
val zplBuilder = StringBuilder()
val config = PrintConfig(useLogo = true, logoName = "logo")

// Cargar logo preconvertido desde archivo .txt en assets
val zplLogo = readZplLogoFromAssets(context, "logo.txt")
zplBuilder.addLogoFromZplFile(config, zplLogo, logoHeight = 220)

// Agregar título centrado
zplBuilder.addTextLine("Factura Electrónica", config, centered = true)

// Línea divisoria
zplBuilder.addDivider(config)

// Agregar datos del cliente
zplBuilder.addTextLine("Cliente: Juan Pérez", config)
zplBuilder.addTextLine("Producto: Camiseta Azul", config)

// Agregar líneas vacías (espacio vertical)
zplBuilder.addEmptyLines(2, config)

// Agregar código QR con link a factura
zplBuilder.addQRCode("https://ejemplo.com/factura123", config)

// Enviar al stream de impresión
printerOutputStream.write(zplBuilder.toString().toByteArray())
