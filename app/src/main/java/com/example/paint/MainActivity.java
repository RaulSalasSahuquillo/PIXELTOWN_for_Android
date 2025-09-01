package com.example.paint;
// Debe coincidir con el paquete de DrawingView y con tu estructura de carpetas.

import android.graphics.Color;                 // Para usar Color.BLACK, Color.RED, etc.
import android.os.Bundle;                      // Estado/paquete de datos de la Activity.
import androidx.activity.EdgeToEdge;           // Para usar pantalla completa con barras superpuestas.
import androidx.appcompat.app.AppCompatActivity;// Activity base con compatibilidad amplia.
import androidx.core.graphics.Insets;          // Medidas de barras de sistema.
import androidx.core.view.ViewCompat;          // Utilidades para vistas.
import androidx.core.view.WindowInsetsCompat;  // Acceso a "insets" (barras de estado/navegación).
import android.widget.Button;                  // Botones de la UI.

public class MainActivity extends AppCompatActivity {

    // onCreate es el "punto de entrada" cuando se crea la pantalla.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Llama al comportamiento base.

        // Activa el modo "Edge-to-Edge" para que el contenido pueda ocupar toda la pantalla,
        // y luego nosotros ajustamos los márgenes con los insets del sistema.
        EdgeToEdge.enable(this);

        // Asocia esta Activity con el archivo de diseño res/layout/activity_main.xml
        setContentView(R.layout.activity_main);

        // Ajusta automáticamente el padding de la vista raíz para no quedar
        // ocultos por la barra de estado o la barra de navegación.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Obtiene el tamaño de las barras del sistema (arriba, abajo, lados).
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Aplica ese espacio como padding, así el contenido no se tapa.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets; // Devuelve los insets para que el sistema sepa que ya los consumimos.
        });

        // Obtenemos la referencia al lienzo (nuestra vista personalizada) por su id en XML.
        DrawingView drawingView = findViewById(R.id.drawingView);

        // Obtenemos referencias a los botones (también por id, definidos en el XML).
        Button btnUndo  = findViewById(R.id.btnUndo);
        Button btnRedo  = findViewById(R.id.btnRedo);
        Button btnClear = findViewById(R.id.btnClear);
        Button btnBlack = findViewById(R.id.btnBlack);
        Button btnRed   = findViewById(R.id.btnRed);

        // Conectamos cada botón con una acción sobre el lienzo:
        btnUndo.setOnClickListener(v -> drawingView.undo());                 // Deshacer último trazo.
        btnRedo.setOnClickListener(v -> drawingView.redo());                 // Rehacer trazo deshecho.
        btnClear.setOnClickListener(v -> drawingView.clearCanvas());         // Borrar todo.
        btnBlack.setOnClickListener(v -> drawingView.setStrokeColor(Color.BLACK)); // Cambiar a negro.
        btnRed.setOnClickListener(v -> drawingView.setStrokeColor(Color.RED));     // Cambiar a rojo.

        // Nota: Si quieres cambiar el grosor desde aquí, puedes llamar:
        // drawingView.setStrokeWidth(20f); // por ejemplo, 20 píxeles.
    }
}
