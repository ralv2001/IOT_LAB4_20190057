# IOT_LAB4_20190057 - **RICARDO ALVARADO RUIZ**
Este es mi laboratorio 4 :)

## **Importante: LEER TODO ANTES DE REVISAR EL PROYECTO**
Desafíos en la Implementación y Soluciones
Problema 1: Intermitencia del Servicio (Error 502)
Durante el desarrollo se identificó que WeatherAPI utiliza BunnyCDN como red de distribución de contenido (CDN). Ocasionalmente, el CDN presentó errores 502 (Bad Gateway) cuando no puede establecer conexión con el servidor de origen. Esto resultó en una intermitencia, donde la aplicación funciona correctamente la mayoría de las veces, pero falla esporádicamente con errores del servidor (#Es posible que la aplicación falle o se cierre repentinamente debido a esto, por favor, simplemente volver a correr el proyecto).
Solución Implementada:
Se implementó un sistema de reintentos automáticos que detecta específicamente los errores 502 y realiza hasta 3 intentos adicionales con intervalos de 2 segundos entre cada uno. Esto permite que la aplicación supere los problemas temporales del CDN, ya que normalmente el cache expira o el servidor de origen vuelve a estar disponible después de algunos intentos.
javaif (response.code() == 502) {
    if (retryCount < MAX_RETRIES) {
        retryCount++;
        new Handler().postDelayed(() -> {
            getForecast(originalLocationId, days);
        }, 2000);
    }
}
Problema 2: Bug en la API - ID de Ubicación NULL
Se descubrió que cuando se consulta el pronóstico del clima usando el formato id:número, la API devuelve correctamente todos los datos excepto el campo location.id, que viene como null. Esto es un bug en el diseño de la API, ya que cuando se busca por nombre sí devuelve el ID correctamente.
Solución Implementada:
La aplicación almacena el ID original antes de realizar la consulta y lo utiliza cuando la API devuelve null. Esto asegura que siempre se pueda mostrar el ID correcto al usuario, independientemente de la respuesta de la API.
java// Guardar el ID original antes de la llamada
originalLocationId = locationId;

// Usar el ID original cuando la API devuelve null
if (locationId == null || locationId.trim().isEmpty()) {
    locationId = originalLocationId.substring(3); // Remueve "id:"
}

## **Uso de Inteligencia artificial**
En este proyecto se usó la inteligencia artificial "Claude", principalemente para el parcheo de bugs, esto implica principalmente los 3 archivos contenidos en cada una de las carpetas "adapters" y "fragments", especialemente en la sección de Navegation en el archivo de LocationsFragment:
    private void setupRecyclerView(List<LocationModel> locations) {
        adapter = new LocationAdapter(locations, location -> {
            ....
Esto también incluyo el archivo nav_graph.xml, en la carpeta res/navigation/nav_graph.xml

## **Diseño de la aplicación**
Me parece importante rendir créditos al usuario **Aqsa**, del cual me basé en su diseño en la aplicación *Figma* para este laboratorio.  
Su modelo del usuario se encuentra en [este enlace de Figma](https://www.figma.com/design/Ax4JWzDUvrlky87CeFgLuk/Weather-app--Community-?node-id=0-1&p=f&t=OfGIq2Rfk3uq7LD2-0).

## **Información del Proyecto:** 
La versión de Android utilizada es:  
**API 31 ("S", Android 12.0)**

## **Entorno de Desarrollo**
**Android Studio**  
Emulador configurado:  
**Pixel 4 API 31 (Android 12.0 "S") x86_64**

## **Configuración del Proyecto**
**Nombre:** IOT_LAB4_20190057
**Package name:** com.example.IOT_LAB4_20190057 
**Lenguaje:** Java  
**Build configuration:** Groovy DSL (build.gradle)
