package com.example.paint; // <- El "paquete" agrupa archivos Java. Debe coincidir con tu app.

/*
 * Importamos clases de Android que vamos a usar.
 * Piensa en "import" como traer herramientas a tu archivo.
 */
import android.graphics.Color;              // Colores básicos (BLACK, RED, etc).
import android.os.Bundle;                  // Objeto con datos del estado al crear la Activity.
import android.util.TypedValue;            // Para convertir dp -> px de forma correcta.
import android.view.MenuItem;              // Representa un elemento pulsado en un menú.
import android.view.MotionEvent;           // Eventos de toque (down/move/up) para arrastrar.
import android.view.View;                  // Clase base para todos los elementos de UI.
import android.widget.AdapterView;         // Callback para selección en Spinner.
import android.widget.ArrayAdapter;        // Adaptador sencillo para listas/Spinner.
import android.widget.Button;              // Botón normal.
import android.widget.FrameLayout;         // Contenedor que apila vistas (como “capas”).
import android.widget.ImageView;           // Para mostrar imágenes en pantalla.
import android.widget.PopupMenu;           // Menú contextual emergente anclado a un botón.
import android.widget.Spinner;             // Desplegable para elegir una opción (colores).
import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;       // Permite que la UI ocupe toda la pantalla.
import androidx.appcompat.app.AppCompatActivity; // Clase base de una pantalla (Activity).
import androidx.core.graphics.Insets;      // Tamaños de las barras del sistema (status/nav).
import androidx.core.view.ViewCompat;      // Utilidades para trabajar con vistas de forma moderna.
import androidx.core.view.WindowInsetsCompat; // Acceso unificado a los "insets" del sistema.
import android.widget.Toast;

/*
 * Una Activity es una PANTALLA de tu app.
 * MainActivity es la pantalla principal que se lanza al abrir la app.
 */
public class MainActivity extends AppCompatActivity {
    private int money = 10000;   // saldo inicial
    private int experience = 0;    // puntos de experiencia
    private int buildingsCount = 0;
    private int happiness = 50;
    private int ronda = 1;
    private int tarraco = 0;
    private int poblation = 3;
    private int casas = 0;
    private int ayuntamiento = 1;
    private int farolas = 0;
    private int seguridad = 0;
    private int estacionpolicial = 0;
    private int policia = 0;
    private int impuestos = 0;
    private int tienda = 0;
    private int porcentajeimpuestos = 15;
    private int dinero1 = 1000;
    private int dinero2 = 34000;
    private int dinerototal = 0;

    // Variables para recordar el desplazamiento entre el dedo y la vista al arrastrar.
    // dX y dY nos ayudan a que el objeto no "salte" cuando lo cogemos.
    private float dX, dY;

    /*
     * onCreate es el punto de entrada cuando Android crea esta pantalla.
     * Aquí "inflamos" el layout, buscamos vistas por id, y conectamos la lógica.
     */
    private void showStatsDialog() {
        String msg = "Dinero: " + money + " Հ"
                + "\nExperiencia: " + experience
                + "\nEdificios: " + buildingsCount
                + "\nFelicidad: " + happiness + " %"
                + "\nRonda: " + ronda
                + "\nPoblación: " + poblation
                + "\nImpuestos: " + porcentajeimpuestos + " %";
        new AlertDialog.Builder(this)
                .setTitle("Estadísticas")
                .setMessage(msg)
                .setPositiveButton("SALIR", null)
                .show();
    }

    private void showMission() {
        if (ronda == 1){
            String msg = "Hay 3 habitantes (incluyéndote a tí). ¿Qué te parece si empezamos construyendo 3 casas?";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        }
        if (ronda == 2 && ayuntamiento == 0) {
            String msg = "¿Pero cómo vas a gobernar? ¿Qué te parece si construimos un ayuntamiento?";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        } else if ( ronda == 2 && ayuntamiento == 1) {
            String msg = "Haz tu primer paso como gobernador. Construye una tienda";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        }
        if (ronda == 3 && farolas == 0 && seguridad == 0) {
            String msg = "La gente se está quejando de que no ve por la noche. Construye 4 farolas alrededor la la ciudad";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        } else if (ronda == 3 && farolas == 4 && seguridad == 0){
            String msg = "¡Muy buena inversión! La gente todavía se siente insegura por la noche. Pon 5 cámeras de seguridad. 4 de ellas en las farolas y la quinta en el ayuntamiento.";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        }
        if (ronda == 4 && estacionpolicial == 0) {
            String msg = "¿Pero quién va a controlar las cámeras? Pon una estación de policía";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        } else if (ronda == 4 && estacionpolicial == 1 && policia == 0) {
            String msg = "¡Pero tienes que contratar a un policía!";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        } else if (ronda == 4 && estacionpolicial == 1 && policia == 1 && casas == 3) {
            String msg = "¡Pero qué son estos modales! Construye una casa para el policía, ya que él es un habitante más.";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        }
        if (ronda == 5 && impuestos == 0) {
            String msg = "¡Estás progresando muuuucho! ¡Sube los impuestos para ganar más dinero!";
            new AlertDialog.Builder(this)
                    .setTitle("Misiones")
                    .setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null)
                    .show();
        }


    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     // Llama al comportamiento base de AppCompatActivity.

        // EdgeToEdge permite que el contenido vaya "bajo" las barras de estado/navegación.
        EdgeToEdge.enable(this);

        // Vinculamos esta Activity con el layout XML (res/layout/activity_main.xml).
        // Ese XML define la jerarquía de vistas: lienzo, botones, etc.
        setContentView(R.layout.activity_main);

        // Ajuste automático de "padding" para que el contenido no quede tapado por las barras.
        // findViewById(R.id.main) obtiene la vista raíz del layout (con ese id en el XML).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Obtenemos cuánto ocupan las barras del sistema (superior/inferior/laterales).
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Aplicamos ese espacio como "relleno" (padding).
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Devolvemos los mismos insets indicando que ya los hemos tenido en cuenta.
            return insets;
        });

        // ----------- 1) OBTENER REFERENCIAS A LAS VISTAS DEL LAYOUT -----------

        // "DrawingView" es una vista personalizada (tu lienzo para dibujar).
        // Está definida en el XML con <com.example.paint.DrawingView .../>
        DrawingView drawingView = findViewById(R.id.drawingView);

        // Botones de la fila inferior (Stats / Rehacer / Limpiar)
        Button btnStats      = findViewById(R.id.btnStats);
        Button btnRonda      = findViewById(R.id.btnRonda);
        Button btnMission     = findViewById(R.id.btnMission);

        // Botón "Construir" que abre el menú para crear elementos (casa, etc.)
        Button btnConstruir = findViewById(R.id.btnConstruir);

        // Spinner (desplegable) para elegir color del trazo del pincel
        Spinner spinner     = findViewById(R.id.spinnerColors);

        // "root" es el contenedor principal (FrameLayout) donde añadiremos casas nuevas.
        View root           = findViewById(R.id.main);

        // panelBotonera es el contenedor inferior con los botones.
        // Lo usamos para no permitir que las casas/edificios “pisen” esa zona al arrastrar.
        View panelBotonera  = findViewById(R.id.panelBotonera);

        // ----------- 2) CONECTAR BOTONES DEL LIENZO A SUS ACCIONES -----------

        // setOnClickListener “escucha” pulsaciones. Con lambda (Java 8) queda más corto.
        btnStats.setOnClickListener(v -> showStatsDialog());

        btnRonda.setOnClickListener(v -> {
            if (ronda == 1 && casas == 3) {
                ronda++;
                dinerototal = dinero1 + dinero2;
                money = money + dinerototal * porcentajeimpuestos / 100;
                happiness --;
                Toast.makeText(this, "¡Ronda " + ronda + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "¡Debes completar las misiones antes de cambiar de ronda!", Toast.LENGTH_SHORT).show();
            }

            if (ronda == 2 && ayuntamiento == 1 && tienda == 1) {
                ronda++;
                dinerototal = dinero1 + dinero2;
                money = money + dinerototal * porcentajeimpuestos / 100;
                happiness --;
                Toast.makeText(this, "¡Ronda " + ronda + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "¡Debes completar las misiones antes de cambiar de ronda!", Toast.LENGTH_SHORT).show();
            }

            if (ronda == 3 && farolas == 4 && seguridad == 4){
                ronda ++;
                dinerototal = dinero1 + dinero2;
                money = money + dinerototal * porcentajeimpuestos / 100;
                happiness --;
                Toast.makeText(this, "¡Ronda " + ronda + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "¡Debes completar las misiones antes de cambiar de ronda!", Toast.LENGTH_SHORT).show();
            }

            if (ronda == 4 && estacionpolicial == 1 && policia == 1 && casas == 4) {
                ronda ++;
                dinerototal = dinero1 + dinero2;
                money = money + dinerototal * porcentajeimpuestos / 100;
                happiness --;
                Toast.makeText(this, "¡Ronda " + ronda + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "¡Debes completar las misiones antes de cambiar de ronda!", Toast.LENGTH_SHORT).show();
            }


        });
        btnMission.setOnClickListener(v -> showMission());  // Misión por nivel

        // ----------- 3) CONFIGURAR EL SPINNER DE COLORES -----------

        // Un ArrayAdapter es un "puente" que convierte una lista (R.array.color_names)
        // en elementos visuales dentro del Spinner.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,                                // Contexto (esta Activity)
                R.array.color_names,                 // Lista de nombres de colores (strings.xml)
                android.R.layout.simple_spinner_item // Layout simple para cada ítem
        );
        // Layout para cuando el usuario despliega la lista del Spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Conectamos el adaptador al Spinner
        spinner.setAdapter(adapter);

        // Cuando el usuario elige un color, cambiamos el color del trazo del DrawingView.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // Se llama cada vez que se selecciona un ítem de la lista.
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Obtenemos el texto del color (ej. "Rojo")
                String name = parent.getItemAtPosition(pos).toString();

                // Convertimos el nombre elegido en un int de color real.
                int c;
                switch (name) {
                    case "Rojo":     c = Color.RED; break;
                    case "Azul":     c = Color.BLUE; break;
                    case "Verde":    c = Color.GREEN; break;
                    case "Amarillo": c = Color.YELLOW; break;
                    case "Magenta":  c = Color.MAGENTA; break;
                    default:         c = Color.BLACK; break; // Si no coincide, negro por defecto.
                }

                // Llamamos a tu método del lienzo para cambiar el color del pincel.
                drawingView.setStrokeColor(c);
            }

            // Si no hay nada seleccionado (raro en un Spinner), no hacemos nada.
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });


        // ----------- 4) MENÚ "CONSTRUIR" QUE CREA NUEVOS EDIFICIOS -----------

        // Al pulsar el botón Construir, mostramos un PopupMenu anclado al botón.
        btnConstruir.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v); // "this" es el contexto; "v" es el botón.

            // Inflamos (cargamos) el XML del menú: res/menu/menu_construir.xml
            // Ahí tienes items como: <item android:id="@+id/action_casa" android:title="Casa"/>
            menu.getMenuInflater().inflate(R.menu.menu_construir, menu.getMenu());

            // ¿Qué pasa cuando el usuario toca una opción del menú?
            menu.setOnMenuItemClickListener((MenuItem item) -> {
                int itemId = item.getItemId();

                // Si elige "Casa"
                if (itemId == R.id.action_casa) {
                    if (money >= 1000) {
                        money = money - 1000;
                        experience = experience + 10;
                        happiness ++;
                        casas ++;
                        crearEdificio(root, panelBotonera, R.drawable.casa, 96); // 96dp
                    } else {
                        Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                        happiness --; // Quito 1 de felicidad porque la construcción se ha cancelado
                    }
                    return true;
                }
                // Si elige "Tarraco"
                else if (itemId == R.id.action_tarraco) {
                    if (money >= 50000 && tarraco == 0) {
                        money = money - 50000;
                        experience = experience + 500;
                        happiness = happiness + 10;
                        Toast.makeText(this, "¡Has construido tu primera distribuidora de productos!", Toast.LENGTH_SHORT).show();
                        crearEdificio(root, panelBotonera, R.drawable.tarraco, 120); // 120dp
                        tarraco++;
                    } else if (tarraco == 1) {
                        Toast.makeText(this, "¡Ya tienes un edificio de Tarraco! ¿Qué te parece si construimos otra cosa?", Toast.LENGTH_SHORT).show();
                    } else if (money <= 5000) {
                        Toast.makeText(this, "¡No tienes suficiente dinero!", Toast.LENGTH_SHORT).show();
                        happiness = happiness - 5;
                    }
                    return true;
                }
                // Si elige "Ayuntamiento"
                else if (itemId == R.id.action_ayuntamiento) {
                    if (money >= 20000) {
                        money = money - 20000;
                        experience = experience + 100;
                        happiness = happiness + 10;
                        crearEdificio(root, panelBotonera, R.drawable.ayuntamiento, 120);
                    } else {
                        Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                        happiness = happiness - 10;
                    }
                    return true;
                }
                // Si elige "PrimaPrix"
                else if (itemId == R.id.action_primaprix) {
                    if (money >= 5000) {
                        money = money - 5000;
                        experience = experience + 30;
                        happiness ++;
                        crearEdificio(root, panelBotonera, R.drawable.primaprix, 120);
                    } else {
                        Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                        happiness --;
                    }
                    return true;
                }
                // Si elige "Esclat"
                else if (itemId == R.id.action_esclat) {
                    if (money >= 6000){
                        money = money - 6000;
                        experience = experience + 30;
                        happiness ++;
                        crearEdificio(root, panelBotonera, R.drawable.esclat, 120);
                    } else {
                        Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                        happiness --;
                    }
                    return true;
                }
                // Si elige "Marvimundo"
                else if (itemId == R.id.action_marvimundo) {
                    if (money >= 7000){
                        money = money - 7000;
                        experience = experience + 30;
                        crearEdificio(root, panelBotonera, R.drawable.esclat, 120);
                        happiness ++;
                    } else {
                        Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                        happiness --;
                    }
                    return true;
                }
                // Si elige "Estación de policía"
                else if (itemId == R.id.action_estacion) {
                    if (money >= 8000){
                        money = money - 8000;
                        experience = experience + 35;
                        crearEdificio(root, panelBotonera, R.drawable.estacionpolicial, 120);
                        happiness ++;
                    } else {
                        Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                        happiness --;
                    }
                    return true;
                }
                // Si elige "Cámara de seguridad"
                else if (itemId == R.id.action_camara) {
                    if (money >= 50){
                        money = money - 50;
                        experience = experience + 5;
                        crearEdificio(root, panelBotonera, R.drawable.camaradeseguridad, 50);
                        happiness ++;
                    } else {
                        Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                        happiness --;
                    }
                    return true;
                }
                return false; // No gestionamos otras opciones
            });

            // Finalmente, mostramos el menú en pantalla.
            menu.show();
        });
    }

    /*
     * Función auxiliar para crear un edificio dinámicamente en el mapa.
     * - Lo crea como ImageView con el drawable que le pases.
     * - Le da un tamaño en dp (convertido a px).
     * - Lo añade al FrameLayout raíz.
     * - Lo coloca en el centro de la zona útil.
     * - Lo hace arrastrable sin invadir la botonera ni salirse de la pantalla.
     */
    private void crearEdificio(View root, View panelBotonera, int drawableId, int sizeDp) {
        // ----- A) Crear la imagen -----
        ImageView edificio = new ImageView(this);
        edificio.setImageResource(drawableId);

        // ----- B) Asignar tamaño (dp → px) -----
        int sizePx = dp(sizeDp);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        edificio.setLayoutParams(params);

        // ----- C) Añadir al contenedor principal -----
        ((FrameLayout) root).addView(edificio);
        buildingsCount++;

        // ----- D) Posición inicial (centrado en zona útil) -----
        root.post(() -> {
            float cx = (root.getWidth() - edificio.getWidth()) / 2f;
            float maxY = root.getHeight()
                    - panelBotonera.getHeight()
                    - edificio.getHeight();
            float cy = Math.max(0, maxY / 2f);

            edificio.setX(Math.max(0, cx));
            edificio.setY(cy);
        });

        // ----- E) Hacer el edificio arrastrable -----
        edificio.setOnTouchListener((viewTouch, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // Guardamos offset dedo-vista
                    dX = viewTouch.getX() - event.getRawX();
                    dY = viewTouch.getY() - event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // Nueva posición sumando offset
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    // Límites
                    float maxX = root.getWidth() - viewTouch.getWidth();
                    float maxY2 = root.getHeight()
                            - panelBotonera.getHeight()
                            - viewTouch.getHeight();

                    // Recortamos dentro de [0, max]
                    viewTouch.setX(Math.max(0, Math.min(newX, maxX)));
                    viewTouch.setY(Math.max(0, Math.min(newY, maxY2)));
                    return true;
            }
            return false;
        });
    }

    /*
     * Función de utilidad: convierte "dp" a "px".
     * Por qué: las pantallas tienen densidades diferentes. 96dp "se ve" similar en distintas
     * densidades. Para asignar tamaño en código (que necesita px), hacemos esta conversión.
     */
    private int dp(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,            // Unidades de entrada (dp)
                dp,                                      // Valor a convertir
                getResources().getDisplayMetrics()       // Densidad real de la pantalla
        ));
    }
}


