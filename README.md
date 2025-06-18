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
